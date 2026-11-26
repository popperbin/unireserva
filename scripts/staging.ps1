<#
Opera el staging local de UniReserva. Ver docs/staging-local.md y docs/recuperacion.md.

  .\scripts\staging.ps1 desplegar -Commit <sha completo de main> [-ConAcceso]
  .\scripts\staging.ps1 respaldar
  .\scripts\staging.ps1 restaurar -Archivo <ruta .dump> -Confirmar
  .\scripts\staging.ps1 estado
  .\scripts\staging.ps1 detener
#>
param(
    [Parameter(Mandatory, Position = 0)]
    [ValidateSet('desplegar', 'respaldar', 'restaurar', 'estado', 'detener')]
    [string]$Accion,
    [string]$Commit,
    [string]$Archivo,
    [switch]$Confirmar,
    [switch]$ConAcceso,
    [ValidatePattern('^[a-z0-9-]+$')]
    [string]$Proyecto = 'unireserva-staging'
)

$ErrorActionPreference = 'Stop'
$Repositorio = 'popperbin/unireserva'
$UsuarioBd = 'unireserva_staging'

# Todo el estado vive fuera del repositorio, en el perfil del propietario.
$Base = Join-Path $env:LOCALAPPDATA "UniReserva\$Proyecto"
$ArchivoEnv = Join-Path $Base 'staging.env'
$ArchivoActual = Join-Path $Base 'actual.txt'
$Copias = Join-Path $Base 'copias'

function Assert-Exito([string]$Paso) {
    if ($LASTEXITCODE -ne 0) { throw "Fallo: $Paso (codigo $LASTEXITCODE)" }
}

# Carpeta corta: Windows limita la longitud de las rutas.
function Get-DirVersion([string]$Sha) { Join-Path $Base "v\$($Sha.Substring(0, 12))" }

function Get-Actual {
    if (Test-Path $ArchivoActual) { (Get-Content $ArchivoActual -TotalCount 1).Trim() }
}

# Ejecuta Docker Compose con los archivos de una version concreta.
function Invoke-Compose([string]$Sha, [string[]]$Argumentos) {
    $env:APP_TAG = $Sha
    $archivoCompose = Join-Path (Get-DirVersion $Sha) 'compose.staging.yaml'
    docker compose -p $Proyecto --env-file $ArchivoEnv -f $archivoCompose --profile acceso @Argumentos
}

function Get-ShaDesplegado {
    $sha = Get-Actual
    if (-not $sha) { throw "No hay ninguna version desplegada en $Proyecto." }
    $sha
}

function New-ArchivoEnv {
    if (Test-Path $ArchivoEnv) { return }
    $bytes = New-Object byte[] 24
    [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
    $clave = ($bytes | ForEach-Object { $_.ToString('x2') }) -join ''
    Set-Content $ArchivoEnv -Encoding Ascii -Value "POSTGRES_PASSWORD=$clave", 'TS_AUTHKEY='
    Write-Host "Credenciales nuevas en $ArchivoEnv (no compartir)."
}

# Solo se despliega un commit integrado en main y con la CI en verde.
function Get-VersionAprobada([string]$Sha) {
    if ($Sha -notmatch '^[0-9a-f]{40}$') { throw 'Indica -Commit con el SHA completo (40 caracteres).' }
    $destino = Get-DirVersion $Sha
    if (-not (Test-Path $destino)) {
        git clone --quiet -c core.longpaths=true "https://github.com/$Repositorio.git" $destino
        Assert-Exito 'clonar el repositorio'
    }
    git -C $destino fetch --quiet origin main
    Assert-Exito 'actualizar main'
    git -C $destino merge-base --is-ancestor $Sha origin/main
    Assert-Exito "el commit $Sha no pertenece a main"

    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    $ci = Invoke-RestMethod "https://api.github.com/repos/$Repositorio/commits/$Sha/check-runs?check_name=verificar"
    $correctas = @($ci.check_runs | Where-Object { $_.app.slug -eq 'github-actions' -and $_.conclusion -eq 'success' })
    if ($correctas.Count -eq 0) { throw "La CI 'verificar' no esta aprobada para $Sha." }

    git -C $destino checkout --quiet --force --detach $Sha
    Assert-Exito 'seleccionar el commit'
    if (-not (Test-Path (Join-Path $destino 'compose.staging.yaml'))) { throw 'Esa version no contiene la configuracion de staging.' }
}

function Backup-Datos([string]$Sha) {
    New-Item -ItemType Directory -Force $Copias | Out-Null
    $destino = Join-Path $Copias ("unireserva-{0:yyyyMMdd-HHmmss}.dump" -f (Get-Date))
    # El volcado se copia como archivo: redirigir binarios en PowerShell 5.1 los corrompe.
    Invoke-Compose $Sha 'exec', '-T', 'db', 'pg_dump', '-U', $UsuarioBd, '-d', 'unireserva', '-Fc', '-f', '/tmp/copia.dump'
    Assert-Exito 'pg_dump'
    Invoke-Compose $Sha 'exec', '-T', 'db', 'pg_restore', '--list', '/tmp/copia.dump' | Out-Null
    Assert-Exito 'comprobar la copia'
    Invoke-Compose $Sha 'cp', 'db:/tmp/copia.dump', $destino
    Assert-Exito 'extraer la copia'
    # shortcut: conserva 14 copias en el mismo disco; copiarlas a otro medio si los datos llegan a importar.
    Get-ChildItem $Copias -Filter 'unireserva-*.dump' | Sort-Object Name -Descending |
        Select-Object -Skip 14 | Remove-Item
    Write-Host "Copia creada: $destino"
}

New-Item -ItemType Directory -Force $Base | Out-Null

switch ($Accion) {
    'desplegar' {
        Get-VersionAprobada $Commit
        New-ArchivoEnv
        $anterior = Get-Actual

        Invoke-Compose $Commit 'build', 'app'
        Assert-Exito 'construir la imagen'
        if ($anterior) {
            Invoke-Compose $anterior 'up', '-d', '--wait', 'db'
            Assert-Exito 'iniciar la base para la copia previa'
            Backup-Datos $anterior
        }

        $servicios = @('db', 'app')
        if ($ConAcceso) { $servicios += 'tailscale' }
        Invoke-Compose $Commit (@('up', '-d', '--wait', '--wait-timeout', '180') + $servicios)
        if ($LASTEXITCODE -ne 0) {
            if ($anterior -and $anterior -ne $Commit) {
                Write-Warning "Fallo el despliegue. Volviendo a $anterior. Las migraciones aplicadas NO se deshacen."
                Invoke-Compose $anterior 'up', '-d', '--wait', '--wait-timeout', '180', 'app'
                Assert-Exito 'volver a la version anterior (revisar docs/recuperacion.md)'
            }
            throw "El despliegue de $Commit fallo."
        }
        Set-Content $ArchivoActual -Encoding Ascii -Value $Commit
        Write-Host "Desplegado $Commit"
    }
    'respaldar' { Backup-Datos (Get-ShaDesplegado) }
    'restaurar' {
        if (-not $Confirmar) { throw 'Restaurar reemplaza los datos actuales. Repite con -Confirmar.' }
        if (-not $Archivo -or -not (Test-Path $Archivo -PathType Leaf)) { throw 'Indica -Archivo con una copia existente.' }
        $sha = Get-ShaDesplegado
        Invoke-Compose $sha 'up', '-d', '--wait', 'db'
        Assert-Exito 'iniciar la base'
        # Primero se carga en una base aparte: si la copia esta danada, los datos actuales no se tocan.
        Invoke-Compose $sha 'cp', (Resolve-Path $Archivo).Path, 'db:/tmp/restaurar.dump'
        Assert-Exito 'subir la copia'
        Backup-Datos $sha
        Invoke-Compose $sha 'exec', '-T', 'db', 'dropdb', '-U', $UsuarioBd, '--if-exists', 'unireserva_nueva'
        Assert-Exito 'limpiar la base temporal'
        Invoke-Compose $sha 'exec', '-T', 'db', 'createdb', '-U', $UsuarioBd, 'unireserva_nueva'
        Assert-Exito 'crear la base temporal'
        Invoke-Compose $sha 'exec', '-T', 'db', 'pg_restore', '-U', $UsuarioBd, '-d', 'unireserva_nueva', '--exit-on-error', '/tmp/restaurar.dump'
        Assert-Exito 'restaurar (los datos actuales siguen intactos)'
        Invoke-Compose $sha 'stop', 'app'
        Assert-Exito 'detener la aplicacion'
        Invoke-Compose $sha 'exec', '-T', 'db', 'dropdb', '-U', $UsuarioBd, '--force', 'unireserva'
        Assert-Exito 'eliminar la base anterior'
        Invoke-Compose $sha 'exec', '-T', 'db', 'psql', '-U', $UsuarioBd, '-d', 'postgres', '-v', 'ON_ERROR_STOP=1', '-c', 'ALTER DATABASE unireserva_nueva RENAME TO unireserva'
        Assert-Exito 'activar la base restaurada'
        Invoke-Compose $sha 'up', '-d', '--wait', '--wait-timeout', '180', 'app'
        Assert-Exito 'reiniciar la aplicacion'
        Write-Host "Restaurado desde $Archivo"
    }
    'estado' {
        $sha = Get-ShaDesplegado
        Write-Host "Version: $sha"
        $filas = @(Invoke-Compose $sha 'ps', '-a', '--format', '{{.Service}} {{.State}} {{.Health}}')
        Assert-Exito 'consultar el estado'
        $filas | Write-Host
        $problemas = @()
        foreach ($servicio in 'db', 'app') {
            if (-not ($filas -match "^$servicio running healthy")) { $problemas += "$servicio no esta sano" }
        }
        $ultima = Get-ChildItem $Copias -Filter '*.dump' -ErrorAction SilentlyContinue |
            Sort-Object Name -Descending | Select-Object -First 1
        if (-not $ultima) { $problemas += 'no hay copias' }
        elseif ($ultima.LastWriteTime -lt (Get-Date).AddHours(-26)) { $problemas += 'la ultima copia tiene mas de 26 horas' }
        $libre = [math]::Round((Get-PSDrive $env:SystemDrive[0]).Free / 1GB)
        Write-Host "Disco libre: $libre GB"
        if ($libre -lt 20) { $problemas += 'menos de 20 GB libres' }
        if ($problemas) { Write-Warning ($problemas -join '; '); exit 1 }
        Write-Host 'Todo correcto.'
    }
    'detener' {
        Invoke-Compose (Get-ShaDesplegado) 'stop'
        Assert-Exito 'detener'
    }
}

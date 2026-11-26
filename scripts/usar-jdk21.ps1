# Activa JDK 21 SOLO en la terminal actual. No cambia variables del sistema ni del usuario.
# Uso, desde la raiz del repositorio:  .\scripts\usar-jdk21.ps1
$patrones = "$env:USERPROFILE\.jdks\*21*", "$env:ProgramFiles\Eclipse Adoptium\jdk-21*", "$env:ProgramFiles\Java\jdk-21*"
$jdk = Get-Item $patrones -ErrorAction SilentlyContinue |
    Where-Object { Test-Path "$($_.FullName)\bin\javac.exe" } |
    Select-Object -First 1

if (-not $jdk) {
    Write-Error "No se encontro un JDK 21. Instalalo: winget install EclipseAdoptium.Temurin.21.JDK"
    return
}

$env:JAVA_HOME = $jdk.FullName
$env:Path = "$($jdk.FullName)\bin;$env:Path"
Write-Host "JAVA_HOME=$env:JAVA_HOME (solo en esta terminal)"

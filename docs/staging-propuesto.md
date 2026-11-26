# Staging aislado (propuesta, sin desplegar)

Estado: **propuesto**. Nada de esto existe en un servidor. Requiere aprobación.

## Recomendación

Usar el entorno local de cada integrante para desarrollar y sustentar. El curso exige un prototipo funcional, no un servidor. Un staging en un servidor compartido con otros servicios solo debería crearse si aparece una necesidad concreta y su propietario lo aprueba.

## Diseño

```yaml
name: unireserva-staging

services:
  app:
    image: ghcr.io/popperbin/unireserva@${APP_DIGEST:?falta el digest}
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/unireserva
      POSTGRES_USER: unireserva_staging
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:?falta la clave}
      SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE: "5"
      SERVER_ADDRESS: 0.0.0.0   # dentro del contenedor; fuera solo se publica en loopback
      JAVA_TOOL_OPTIONS: -XX:MaxRAMPercentage=65
    ports: ["127.0.0.1:18080:8080"]
    networks: [interna, acceso]
    depends_on:
      db: { condition: service_healthy }
    mem_limit: 512m
    memswap_limit: 512m
    cpus: 0.50
    pids_limit: 200
    read_only: true
    tmpfs: [/tmp]
    cap_drop: [ALL]
    security_opt: ["no-new-privileges:true"]
    restart: on-failure:3
    logging: &logs
      driver: json-file
      options: { max-size: "10m", max-file: "3" }

  db:
    image: postgres:17-alpine
    command: ["postgres", "-c", "shared_buffers=64MB", "-c", "max_connections=20"]
    environment:
      POSTGRES_DB: unireserva
      POSTGRES_USER: unireserva_staging
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:?falta la clave}
    volumes: [datos:/var/lib/postgresql/data]
    networks: [interna]
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -h 127.0.0.1 -U unireserva_staging -d unireserva"]
      interval: 10s
      retries: 10
    mem_limit: 256m
    memswap_limit: 256m
    cpus: 0.25
    pids_limit: 100
    security_opt: ["no-new-privileges:true"]
    restart: on-failure:3
    logging: *logs

networks:
  interna:
    internal: true          # sin salida: aqui vive la base
  acceso:
    driver_opts:
      com.docker.network.bridge.enable_ip_masquerade: "false"   # corta la salida a Internet

volumes:
  datos:
```

## Decisiones

| Tema | Propuesta | Motivo |
|---|---|---|
| Recursos | Máximo 768 MiB y 0,75 CPU, sin swap adicional | No competir con otros servicios |
| Disco | Rotación de registros; presupuesto orientativo de 3 GB | Docker no limita el tamaño del volumen: hay que vigilarlo |
| Imagen | Construida en GitHub Actions, publicada en GHCR y fijada por digest | No compilar en el servidor |
| Credenciales | Propias del staging, generadas en el servidor, archivo con permisos `600`, nunca en Git ni compartidas | Aislamiento |
| Red | Redes propias; base sin puerto publicado; sin `network_mode: host`, sin redes externas ni socket de Docker | Aislamiento |
| Acceso | Solo `127.0.0.1`; túnel SSH del propietario para demostraciones | La aplicación no tiene control de acceso. El resto del equipo no recibe cuentas del servidor |
| Datos | Solo ficticios | |
| Apagado | `docker compose down` cuando no se use (conserva datos) | |

## Condiciones antes de desplegar

1. Aprobación explícita del propietario del servidor.
2. Capacidad y puerto `18080` comprobados ese día; mantenimiento y copias de seguridad al día.
3. Escribir `Dockerfile` (usuario sin privilegios) y el flujo de publicación: **no existen todavía**.
4. Probar desde el contenedor que no alcanza otros servicios del servidor. Quitar la salida a Internet no basta para bloquear el acceso a la propia máquina; puede requerir reglas de firewall.

## Sin probar

Solo se comprobó que el YAML es válido para Docker Compose. No se ha ejecutado.

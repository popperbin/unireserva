# Diagrama de despliegue: staging local (propuesto)

```mermaid
flowchart TB
    subgraph Comp["Dispositivo de cada integrante"]
        N["Navegador + Tailscale (cuenta propia)"]
    end
    subgraph GH["GitHub"]
        R["main + CI"]
    end
    subgraph PC["Portátil Windows"]
        S["scripts/staging.ps1<br/>(ejecución manual)"]
        subgraph DD["Docker Desktop / WSL2: proyecto unireserva-staging"]
            T["tailscale<br/>HTTPS 443"]
            A["app<br/>Spring Boot :8080"]
            D[("db<br/>PostgreSQL :5432")]
            V[("volumen datos")]
        end
        B["Copias .dump"]
    end
    N -->|"túnel cifrado, solo 443"| T
    T -->|"red web (interna)"| A
    A -->|"red datos (interna)"| D
    D --- V
    S -->|"clona commit aprobado"| R
    S -->|"construye y levanta"| DD
    S -->|"pg_dump / pg_restore"| B
```

Fuera del diagrama porque no participan: no hay puertos publicados en el equipo ni en el router. Solo el contenedor `tailscale` tiene salida a Internet (red `salida`).

Este diagrama describe staging, no el diagrama de despliegue final que exige el curso.

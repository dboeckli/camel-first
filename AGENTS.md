# AGENTS.md

## Projekt

`camel-first` — Apache-Camel-Lernprojekt auf Spring Boot (Java 21): Timer-/File-Routen, Transformation, Beans,
Processor, strukturiertes Logging, Actuator. Siehe README.md für Konzept und Beispiel-Routen.

## Kommandos

| Zweck | Befehl |
|---|---|
| Format prüfen (spring-javaformat + spotless inkl. shfmt) | `./mvnw validate` |
| Build (ohne Docker) | `./mvnw package -Dskip.docker.build=true` |
| Tests | `./mvnw test` |

## Sandbox

- CI pinnt Helm `v4.3.2` via `azure/setup-helm@v5.1.0` vor dem ersten Helm-Aufruf (Runner-Default
  ist Helm 3). Die Sandbox liefert Helm 4 als `helm`; `-Dhelm.executable=helm4` ist nicht mehr nötig.

- Kit: opencode-sandbox-kit (README → Sandbox). Sandbox-Quirk: vor jedem `./mvnw`
  `export npm_config_bin_links=false` (Spotless/prettier → EPERM im Mount).
- Maven-Auflösung nutzt bei Mount `C:\development\maven-repo:ro` den Host-Cache. Nur echte Maven-Builds sind
  repräsentativ (`mvn dependency:get` ignoriert settings-`<proxies>`).
- Formatting: shfmt `3.13.1` (Spotless `<shfmt>` + CI `mfinelli/setup-shfmt@v4`).

## Hinweise

- Registry-/Migrations-Entscheidungen: opencode-sandbox-kit Buchhaltung #44.
- Onboarding-Drehbuch: opencode-sandbox-kit #45 (dieses Projekt: Issue #122).

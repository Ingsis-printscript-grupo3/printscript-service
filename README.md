# printscript-service

Servicio de Snippet Searcher que **procesa código PrintScript**: lo valida, lo ejecuta,
lo formatea y lo lintea. Es **stateless**: no guarda nada, recibe todo lo que necesita en
cada request.

No lo consume la UI directamente, sino el `snippet-service`, que le pregunta antes de
guardar un snippet y cada vez que hay que formatearlo, lintearlo o correr sus tests.

Por dentro usa la librería `org.printscript:runner`, publicada en GitHub Packages desde
el repo [printscript](https://github.com/Ingsis-printscript-grupo3/printscript).

## Endpoints

Todos son `POST` con cuerpo JSON. Los campos `language`, `version` y `content` son
obligatorios en los cuatro.

| Path | Campos extra | Qué hace |
| --- | --- | --- |
| `/validations` | — | Dice si el snippet cumple las reglas del lenguaje (US1 a US4) |
| `/executions` | `inputs` | Lo ejecuta y devuelve lo que imprimió (US8, US9, US16) |
| `/lintings` | `rules` | Devuelve las reglas de estilo que no cumple (US5, US14, US15) |
| `/formattings` | `rules` | Devuelve el snippet formateado (US11, US12, US13) |

Las `rules` usan las mismas claves que los archivos de config de PrintScript. Si no vienen,
el linter corre sus reglas predeterminadas y el formatter no aplica ninguna.

### Respuestas

```jsonc
// POST /validations
{ "valid": false, "errors": [ { "message": "Expected ';'.", "line": 2, "column": 1, "endLine": 2, "endColumn": 8 } ] }

// POST /executions   ->  inputs: ["Juan"]
{ "outputs": ["Juan"], "errors": [] }

// POST /lintings
{ "warnings": [ { "message": "Identifier 'my_var' does not match format camel case", "line": 1, "column": 5 } ], "errors": [] }

// POST /formattings
{ "content": "let x: number = 1;", "errors": [] }
```

`errors` son errores del lenguaje: el snippet no lexea, no parsea, no tipa o falla en
ejecución. Traen `line` y `column`, que es lo que la UI necesita para marcar dónde está el problema.
Un `0` significa que ese error no tiene una posición en el fuente.

### Errores

**Un snippet inválido no es un error HTTP.** Si el código está mal, la respuesta es `200`
con los errores adentro. Los códigos de error quedan para problemas del pedido, así quien
llama puede distinguir *"el snippet está mal"* de *"el servicio no anda"*.

| Status | Cuándo | Body |
| --- | --- | --- |
| `200` | El pedido es válido, aunque el snippet esté mal | la respuesta del endpoint |
| `400` | La versión del lenguaje no existe | `{"error": "Unsupported PrintScript version: 9.9"}` |

Versiones soportadas: `1.0` y `1.1`.

## Correrlo

```bash
./gradlew bootRun
```

Levanta en el puerto **8080**. Para ver que está arriba: `GET http://localhost:8080/actuator/health`.

## Checks

```bash
./gradlew check
```

Corre ktlint y los tests. Es lo mismo que corre el CI en cada PR contra `main` o `dev`.

## Docker

El build necesita credenciales de GitHub, porque se baja PrintScript de GitHub Packages
y **Packages pide autenticación aunque el paquete sea público**.

```bash
export GITHUB_ACTOR=tu-usuario
export GITHUB_TOKEN=tu-token-con-read:packages

docker build --build-arg GITHUB_ACTOR --build-arg GITHUB_TOKEN -t printscript-service .
docker run -p 8080:8080 printscript-service
```

Pasar los `--build-arg` **sin valor** hace que Docker los tome del entorno, así el token no
queda en el historial de la terminal.

El `Dockerfile` es multi-stage: la etapa que compila tiene el JDK, el código y el token, y
se descarta entera. **La imagen final solo lleva el JRE y el jar**, sin el token ni el
código fuente.

## En el sistema entero

El `docker-compose.yml` vive en el repo
[snippet-service](https://github.com/Ingsis-printscript-grupo3/snippet-service) y levanta
los tres servicios juntos. Este **no necesita base de datos**, y es el único de los tres
cuyo build necesita credenciales.

Escucha en el `8080`, el puerto por defecto de Spring. El compose lo publica en el `8082`
del host para que los tres servicios no se peleen por el mismo puerto afuera: el `8080` ya
lo usa el `snippet-service` y el `8081` el `permission-service`.

## Decisiones

- Usa **`Engine` directo, no `PrintScriptRunner`**. Esa fachada existe para el TCK y lee
  las variables de entorno reales de la máquina; un servidor no debe usarla.
- **Un `Engine` nuevo por request.** El input es una cola que se vacía y el entorno es de
  esa ejecución: compartir la instancia mezclaría los inputs de dos usuarios simultáneos.
- **El entorno va siempre vacío** (`MapEnvProvider()`), y el request no acepta variables.
  Así un snippet no puede leer los secretos del contenedor con `readEnv`.
- **`errors` hoy trae un solo elemento.** El `Engine` corta en el primer error. Es una
  lista para poder devolver varios más adelante sin romper el contrato.

## 🧬 Mutant Detector API – Examen MercadoLibre

API REST desarrollada en Java 17 + Spring Boot 3 + Gradle, que determina si una secuencia de ADN pertenece a un mutante o a un humano, según la consigna técnica de MercadoLibre.

El proyecto cumple con:

- Validación estricta de ADN en matriz NxN
- Solo caracteres A, T, C, G
- Detección de 2 o más secuencias de 4 letras iguales (horizontal, vertical, diagonales)
- Persistencia en H2 con deduplicación por hash de ADN
- Endpoint `/mutant` con respuestas 200/403/400
- Endpoint `/stats` con `count_mutant_dna`, `count_human_dna`, `ratio`
- Documentación con Swagger / OpenAPI 3
- Manejo centralizado de errores con `GlobalExceptionHandler`
- Tests unitarios y de integración con muy buena cobertura (Jacoco)
- Dockerfile listo para despliegue (por ejemplo, en Render)

## 🧰 Tecnologías utilizadas

- Java 17 → lenguaje principal del proyecto
- Spring Boot 3.2.5 → framework base para creación de API REST
- Gradle → herramienta de construcción y manejo de dependencias
- Spring Web → desarrollo de controladores HTTP/REST
- Spring Data JPA → acceso a base de datos con repositorios e interfaces
- H2 Database → base de datos en memoria para pruebas y persistencia interna
- Hibernate → ORM utilizado por defecto para el mapeo de entidades
- Spring Validation (Jakarta Validation) → validación de entrada usando anotaciones (@Valid, custom validator)
- Springdoc OpenAPI 3 (2.5.0) → documentación automática con Swagger UI
- Lombok → generación automática de getters, setters, constructores y builders
- JUnit 5 → motor principal de testing
- Mockito → mocking de servicios, repositorios y dependencias en tests
- Jacoco → cobertura de código y generación de reportes
- Docker → Dockerfile para empaquetar la aplicación en contenedor
- Git & GitHub → control de versiones y repositorio remoto
- Render → plataforma de hosting en la nube

## 📁 Estructura del Proyecto

```bash
GlobalMutantes51451-main/
├── Dockerfile
├── build.gradle
├── settings.gradle
├── .gitignore
├── assets/
│   ├── capturas/
│   │   ├── 1.png   # Swagger UI
│   │   ├── 2.png   # /mutant 200 OK (mutante)
│   │   ├── 3.png   # /mutant 403 Forbidden (humano)
│   │   ├── 4.png   # /stats OK
│   │   ├── 5.png   # (captura extra)
│   │   ├── 6.png   # H2 con datos
│   │   ├── 7.png
│   │   └── 8.png
│   └── diagramas/
│       ├── DIagrama de secuencia POST(:mutant).png
│       └── Diagrama de secuencia GET(:stats).png
└── src/
    ├── main/
    │   ├── java/com/mutantes/mutant_detector/
    │   │   ├── MutantDetectorApplication.java
    │   │   ├── config/
    │   │   │   └── SwaggerConfig.java
    │   │   ├── controller/
    │   │   │   └── MutantController.java
    │   │   ├── dto/
    │   │   │   ├── DnaRequest.java
    │   │   │   ├── ErrorResponse.java
    │   │   │   └── StatsResponse.java
    │   │   ├── entity/
    │   │   │   └── DnaRecord.java
    │   │   ├── exception/
    │   │   │   ├── DnaHashCalculationException.java
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   └── InvalidDnaException.java
    │   │   ├── repository/
    │   │   │   └── DnaRecordRepository.java
    │   │   ├── service/
    │   │   │   ├── MutantDetector.java
    │   │   │   ├── MutantService.java
    │   │   │   └── StatsService.java
    │   │   └── validation/
    │   │       ├── ValidDnaSequence.java
    │   │       └── ValidDnaSequenceValidator.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/mutantes/mutant_detector/
            ├── MutantDetectorApplicationTests.java
            ├── controller/MutantControllerTest.java
            └── service/
                ├── MutantDetectorTest.java
                ├── MutantServiceTest.java
                └── StatsServiceTest.java
```

## Lógica de Negocio – Detección de Mutantes
El ADN se recibe como un arreglo de String, formando una matriz NxN:

{
  "dna": [
    "ATGCGA",
    "CAGTGC",
    "TTATGT",
    "AGAAGG",
    "CCCCTA",
    "TCACTG"
  ]
}
## Condiciones para ser mutante
La clase central es MutantDetector:

```java
@Service
public class MutantDetector {

    private static final int TAMANIO_MINIMO = 4;
    private static final int PARALELIZACION = 20;

    public boolean isMutant(String[] dna) {
        // Validaciones + lógica secuencial/paralela
    }
}
```
## Un ADN es mutante si se encuentran 2 o más secuencias de 4 letras iguales (A, T, C o G) en:

Horizontales → AAAA

Verticales → CCCC

Diagonales principales (↘) → TTTT

Diagonales secundarias (↗) → GGGG

Características del algoritmo:

Valida que:

el array no sea nulo ni vacío

todas las filas tengan el mismo largo

la matriz sea NxN

solo haya caracteres A/T/C/G

Usa una matriz de char para acceder rápido a las posiciones.

Tiene versión secuencial y versión paralela:

Para matrices pequeñas usa streams secuenciales.

Para matrices de tamaño >= PARALELIZACION (20) utiliza IntStream.range(...).parallel() para explotar múltiples núcleos.

Mide el número total de secuencias encontradas y corta en cuanto detecta que ya es mutante.

La validación de forma y caracteres también se refuerza en:

```java
public class ValidDnaSequenceValidator implements ConstraintValidator<ValidDnaSequence, String[]> {
    @Override
    public boolean isValid(String[] dna, ConstraintValidatorContext context) {
        if (dna == null || dna.length == 0) return false;

        int n = dna.length;
        Pattern pattern = Pattern.compile("^[ATCG]+$");

        for (String row : dna) {
            if (row == null || row.length() != n) return false;
            if (!pattern.matcher(row).matches()) return false;
        }
        return true;
    }
}
```
## 🏛 Arquitectura por Capas
La API sigue una arquitectura por capas clara:

Controller (MutantController)

Expone los endpoints REST /mutant y /stats.

Orquesta la llamada a los servicios.

Devuelve los códigos HTTP correctos.

Service

MutantService: coordina verificación, hash y persistencia.

MutantDetector: contiene el algoritmo de detección.

StatsService: calcula estadísticas a partir de la base de datos.

Repository

DnaRecordRepository: acceso a la tabla dna_records mediante Spring Data JPA.

Entity

DnaRecord: entidad JPA que representa el registro persistido de un ADN.

Validation

ValidDnaSequence + ValidDnaSequenceValidator: validación personalizada para ADN.

Exception / Handling

InvalidDnaException, DnaHashCalculationException

GlobalExceptionHandler: convierte excepciones en respuestas JSON estándar ErrorResponse.

Config

SwaggerConfig: definición básica de OpenAPI.

## 📊 Modelo de Datos – DnaRecord
```java
@Entity
@Table(name = "dna_records", indexes = {
        @Index(name = "idx_dna_hash", columnList = "dnaHash", unique = true),
        @Index(name = "idx_is_mutant", columnList = "isMutant")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DnaRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 128)
    private String dnaHash;

    @Column(name = "is_mutant", nullable = false)
    private boolean isMutant;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
```
Decisiones importantes:

dnaHash único → evita duplicar ADN en base de datos.

Índices en dnaHash y isMutant → mejor rendimiento en búsquedas y estadísticas.

@PrePersist asigna createdAt automáticamente.

## 💾 Persistencia y Deduplicación
La lógica de negocio está en MutantService:
```java
@Service
@RequiredArgsConstructor
public class MutantService {

    private final MutantDetector mutantDetector;
    private final DnaRecordRepository dnaRecordRepository;

    public boolean verifyAndSave(String[] dna) {
        // 1. Calcular hash
        // 2. Buscar en base de datos si ya existe
        // 3. Si existe, reutilizar resultado
        // 4. Si no existe, detectar mutante y guardar
    }

    private String calculateHash(String[] dna) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String raw = String.join(",", dna);
            byte[] encodedhash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new DnaHashCalculationException("Error calculando el hash del ADN", e);
        }
    }
}
```
Comportamiento:

Se calcula un hash SHA-256 a partir de la matriz dna.

Se consulta el repositorio:

```java
Optional<DnaRecord> findByDnaHash(String dnaHash);
```
Si el hash ya está en la BD:

Se reutiliza isMutant del registro existente (no se recalcúla el algoritmo).

Si el hash no existe:

Se ejecuta mutantDetector.isMutant(dna).

Se guarda un nuevo DnaRecord con el resultado.

## 📊 Estadísticas – /stats
Servicio:

```java
@Service
@RequiredArgsConstructor
public class StatsService {

    private final DnaRecordRepository dnaRecordRepository;

    public StatsResponse getStats() {
        long mutantCount = dnaRecordRepository.countByIsMutant(true);
        long humanCount = dnaRecordRepository.countByIsMutant(false);

        double ratio = (humanCount == 0) ? 0.0 : (double) mutantCount / humanCount;

        return new StatsResponse(mutantCount, humanCount, ratio);
    }
}
```
DTO:

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "StatsResponse", description = "Representa las estadísticas de ADN verificados.")
public class StatsResponse {

    @JsonProperty("count_mutant_dna")
    @Schema(description = "Número de ADN mutantes verificados", example = "40")
    private long countMutantDna;

    @JsonProperty("count_human_dna")
    @Schema(description = "Número de ADN humanos verificados", example = "100")
    private long countHumanDna;

    @Schema(description = "Proporción entre mutantes y humanos", example = "0.4")
    private double ratio;
}
```
🌐 Endpoints REST
Todos los endpoints se exponen desde MutantController:

```java
@RestController
@RequiredArgsConstructor
@RequestMapping
public class MutantController {

    private final MutantService mutantService;
    private final StatsService statsService;

    @PostMapping("/mutant")
    public ResponseEntity<Void> checkMutant(@Valid @RequestBody DnaRequest request) {
        boolean isMutant = mutantService.verifyAndSave(request.getDna());
        if (isMutant) {
            return ResponseEntity.ok().build();              // 200 OK
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden
        }
    }

    @GetMapping("/stats")
    public StatsResponse getStats() {
        return statsService.getStats();
    }
}
```
🔹 POST /mutant
```java
Request body:

{
  "dna": ["ATGCGA","CAGTGC","TTATGT","AGAAGG","CCCCTA","TCACTG"]
}
```
Respuestas:

200 OK → el ADN es mutante

403 Forbidden → el ADN NO es mutante

400 Bad Request → ADN mal formado, caracteres inválidos, matriz no NxN, JSON inválido, etc.

Capturas:

Swagger UI general

Mutante (200)

No mutante (403)

🔹 GET /stats
Respuesta:

```java
{
  "count_mutant_dna": 40,
  "count_human_dna": 100,
  "ratio": 0.4
}
```
Captura:


## 📘 Documentación Swagger / OpenAPI
Dependencia en build.gradle:
```java
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0'
```
Configuración en SwaggerConfig:

```java
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI mutantDetectorAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mutant Detector API")
                        .version("1.0")
                        .description("API para detectar mutantes - Examen MercadoLibre"));
    }
}
```
Y en application.properties:

```java
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true
```
Rutas finales:

Swagger UI:
http://localhost:8080/swagger-ui.html

OpenAPI JSON:
http://localhost:8080/api-docs

## 🧾 Manejo de Errores
El manejo centralizado se realiza en GlobalExceptionHandler y se normaliza en el DTO ErrorResponse:

```java
@Data
@Builder
@AllArgsConstructor
@Schema(name = "ErrorResponse", description = "Estructura estandarizada para errores de la API")
public class ErrorResponse {
    private String error;
    private String message;
    private int status;
    private String path;
    private LocalDateTime timestamp;
}
```
Ejemplos de errores manejados:

MethodArgumentNotValidException → errores de validación (@Valid en DnaRequest)

InvalidDnaException → errores de formato/validación interna de ADN

DnaHashCalculationException → problemas al calcular hash SHA-256

HttpRequestMethodNotSupportedException → método HTTP incorrecto (por ejemplo, POST a /stats)

HttpMessageNotReadableException → JSON mal formado o falta body

Respuestas JSON estandarizadas:
```java
{
  "error": "Validation Error",
  "message": "El campo dna no puede estar vacío",
  "status": 400,
  "path": "/mutant",
  "timestamp": "2025-11-24T14:55:11"
}
```

## 🗄 Base de Datos H2
Configuración en application.properties:

```java
spring.datasource.url=jdbc:h2:mem:mutantsdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```
Consola H2:

URL: http://localhost:8080/h2-console

Driver: org.h2.Driver

JDBC URL: jdbc:h2:mem:mutantsdb

User: sa

Password: (vacío)

Captura con datos (dna_records):


## 🧪 Testing y Cobertura
Tests ubicados en src/test/java/com/mutantes/mutant_detector/:

MutantDetectorTest

Más de 20 tests para:

Secuencias horizontales, verticales, diagonales ↘ y ↗

Matrices 4x4 mínimas

Matrices grandes para version paralela

Casos no mutantes

Casos inválidos: nulos, vacíos, caracteres inválidos, minúsculas, etc.

MutantServiceTest

Verifica:

Que no se duplique ADN ya existente (hash)

Que se guarde correctamente un ADN nuevo

Comportamiento cuando el detector lanza excepciones de validación

StatsServiceTest

Casos:

BD vacía → stats en 0

Solo mutantes / solo humanos / mezcla

ratio con división segura cuando humanCount = 0

MutantControllerTest

/mutant:

Mutante → 200 OK

Humano → 403 Forbidden

ADN inválido → 400 Bad Request

/stats:

Respuesta correcta

Método incorrecto (POST) → 405

Error interno del servicio → 500

Además hay:

MutantDetectorApplicationTests → prueba de carga de contexto.

Ejecución de tests

./gradlew test
Reporte Jacoco
El proyecto usa el plugin jacoco para medir cobertura.

Generar reporte:

./gradlew test jacocoTestReport
El reporte HTML se genera en:

build/reports/jacoco/test/html/index.html

## 🛠 Ejecución Local
Requisitos
Java 17

Gradle Wrapper (incluido en el proyecto)

Pasos
Clonar el repositorio:

```java
git clone https://github.com/cironaoctavio/GlobalMutantes51451.git

cd GlobalMutantes51451-main
```

Compilar y correr tests:

```java
./gradlew clean test
```

Levantar la aplicación:
```java
./gradlew bootRun
```
Probar endpoints:

POST http://localhost:8080/mutant

GET http://localhost:8080/stats

http://localhost:8080/swagger-ui.html

http://localhost:8080/h2-console

🐳 Docker y Deploy (ej. Render)
El proyecto incluye un Dockerfile multi-stage:

Etapa de construcción
```java
FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /app
COPY . .
RUN ./gradlew bootJar --no-daemon
```
Etapa de ejecución
```java
FROM eclipse-temurin:17-jre-alpine
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```
Build local de la imagen

```java
docker build -t mutant-detector .
```
Correr el contenedor

```java
docker run -p 8080:8080 mutant-detector
```
Deploy en Render (modo Docker)
Subir el repo a GitHub.

En Render → New > Web Service.

Elegir el repo.

Runtime: Docker (Render detecta el Dockerfile automáticamente).

Crear el servicio.

La app se expondrá en una URL pública y se podrán usar los mismos endpoints:

POST https://globalmutantes51451.onrender.com/mutant

GET https://globalmutantes51451.onrender.com/stats

Swagger: https://globalmutantes51451.onrender.com/swagger-ui.html

## 📊 Diagramas
En assets/diagramas/ se incluyen:

DIagrama de secuencia POST(/mutant).png

Diagrama de secuencia GET(/stats).png

Representan gráficamente:

Flujo completo de /mutant:

Usuario → Controller → Service → Detector → Repository → H2 → Response

Flujo de /stats:

Usuario → Controller → StatsService → Repository → H2 → Response

Estos diagramas son adecuados para documentar el proyecto en informes o presentaciones.

📄 Créditos
Proyecto desarrollado como entrega del examén global de la materia Desarrollo de Software de la comisión 3k09

Autor: Octavio Martínez Cirona
Legajo: 51451

Stack:  Java 17
        Spring Boot 3.2.5
        Gradle · H2
        Swagger/OpenAPI 3
        JUnit 5
        Mockito
        Jacoco
        Docker

# Configurable Mapping Engine

A dynamic and extensible Java-based mapping engine powered by Spring Boot and YAML/JSON configuration. This project enables transformation between source and target Java objects using declarative, context-aware field mapping rules. Ideal for banking middleware, integration layers, or enterprise service architectures.

---

## 🌐 Overview

This engine allows you to define mapping rules between data models without writing Java mapping code manually. Instead, you use **YAML or JSON files** to define:

- Source and target types
- Field-to-field mappings
- Conditions (e.g. `NotNullCondition`)
- Transformers (e.g. `RegexReplaceTransformer`, `BooleanToFlagTransformer`)

The system supports **custom transformers**, **SpEL expressions**, **conditional mappings**, and provides both **REST API** and **Graphical Visualization** for mapping introspection and editing.

---

## ✨ Key Features

- ✅ **Declarative configuration** of mappings (YAML/JSON)
- 🔁 **Bidirectional object transformation** support
- 🧠 **Context-aware mapping** with dynamic properties
- 🧩 **Extensible via custom transformers and conditions**
- 🔎 **REST APIs** for mapping editing and visualization
- 📊 **Graphviz & D3.js** mapping visualization

---

## 📁 Project Structure

```text
org.example.configmapping
├── api                   # Core service interfaces (MappingService, MappingContext)
├── core                 # Engine logic: loading, applying, registering mappings
│   ├── definition       # FieldMapping, MappingDefinition, Conditions
│   └── transform        # ValueTransformer interface and base implementation
├── config               # Loader for YAML/JSON configuration files
├── transformers         # Built-in transformers (regex, enum, case, etc.)
├── visualization        # REST APIs and D3/Graphviz generators for mapping inspection
├── exception            # Custom exceptions
└── jacksonConfig        # Jackson module registration for dynamic loading
```

## 🔧 Configuration Example (YAML)

```yaml
id: exampleMapping
sourceType: com.example.dto.InputDto
targetType: com.example.dto.OutputDto
priority: 1
fieldMappings:
  - sourcePath: "customerName"
    targetPath: "name"
    transformer:
      @class: org.example.configmapping.mapping.transformers.UpperCaseTransformer

  - sourcePath: "birthDate"
    targetPath: "formattedBirthDate"
    transformer:
      @class: org.example.configmapping.mapping.transformers.DateFormatTransformer
      inputFormat: "yyyy-MM-dd"
      outputFormat: "dd/MM/yyyy"

  - sourcePath: "status"
    targetPath: "statusFlag"
    transformer:
      @class: org.example.configmapping.mapping.transformers.BooleanToFlagTransformer
      trueValue: "Y"
      falseValue: "N"
```

## 🚀 How to Use

### 1. Load Mappings
Mappings are automatically loaded on application startup from:

```
classpath:/mappings/definitions/**/*.yml
```

You can customize the path via:

```
adria.mapping.definitions-path=classpath:/custom/path/
```

### 2. Perform a Mapping

```java
TransferMappingService transferService = ...;

InputDto source = new InputDto(...);
OutputDto result = transferService.map(source, OutputDto.class);
```

You can also use a mapping ID or provide a MappingContext:

```java
MappingContext context = new MappingContext();
context.setBankId("ABC");
context.putProperty("someKey", someValue);

OutputDto result = transferService.mapWithId(source, OutputDto.class, "myMappingId", "ABC");
```

## 🔍 REST API for Mapping Edition

| Endpoint | Description |
|----------|-------------|
| GET /api/mappings/visualization | List all loaded mappings |
| GET /api/mappings/visualization/{id} | Get full mapping definition |
| GET /api/mappings/visualization/{id}/graphviz | Generate DOT (Graphviz) diagram |
| GET /api/mappings/visualization/{id}/d3 | Get JSON for D3.js |
| PUT /api/mappings/editor/{id} | Update a mapping |
| POST /api/mappings/editor/{id}/fields | Add a new field |
| PUT /api/mappings/editor/{id}/fields/{index} | Update a specific field |
| DELETE /api/mappings/editor/{id}/fields/{index} | Remove a field |

## 🧩 Built-in Transformers

- UpperCaseTransformer
- LowerCaseTransformer
- RegexReplaceTransformer
- BooleanToFlagTransformer
- DateFormatTransformer
- EnumMappingTransformer
- StringTruncateTransformer
- DefaultValueTransformer
- PrefixTransformer
- StringCastTransformer

## 🛠️ Extending the Engine

You can implement custom transformers by extending ValueTransformer:

```java
public class MyCustomTransformer implements ValueTransformer {
    @Override
    public Object transform(Object value, MappingContext context) {
        // your transformation logic here
    }
}
```

Or implement a condition:

```java
public class MyCondition implements MappingCondition {
    @Override
    public boolean evaluate(Object source, Object target, MappingContext context) {
        // conditional logic
    }

    @Override
    public MappingCondition copy() {
        return new MyCondition();
    }
}
```

## 📦 Build & Run

Requirements: Java 17+, Maven 3.8+

```
mvn clean install
mvn spring-boot:run
```

Visit the visualizer:

```
http://localhost:8080/static/mapping-visualizer
```

## 📄 License

This project is licensed under the MIT License.

## 👥 Contributors

- Ilyas Dahhou – Project owner
- FATIMA ASEBBANE – Contributor

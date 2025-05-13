# ⚙️ YAML Mapping Engine User Guide

This document explains how to use the ADRIA application's dynamic mapping engine. It allows you to define transformations from source objects to target objects via YAML files without manual coding.

## ✨ Objective

This engine facilitates mapping between heterogeneous data structures while maintaining great flexibility through YAML configuration. It supports:

* simple mappings
* conditional mappings
* custom transformations (date, enums, expressions, etc.)

---

## 📁 YAML Files Location

All YAML mapping files must be placed in the following directory:

```
src/main/resources/mappings/definitions/
```

Each file must have a `.yml` extension and contain a unique identifier (`id`) for the mapping.

---

## 🔍 Minimal YAML File Example

```yaml
id: forecastMovementMapping
sourceType: ma.adria.bank.apidemo1.xyz.dtos.ExternalPrevisionnelMovement
targetType: ma.adria.bank.dto.MouvementPrevisionnelDto
priority: 1
fieldMappings:
  - sourcePath: "libelle"
    targetPath: "libelle"
  - sourcePath: "amount"
    targetPath: "montant"
    transformer:
      @class: org.example.configmapping.mapping.transformers.StringCastTransformer
  - sourcePath: "date"
    targetPath: "dateOperation"
    transformer:
      @class: org.example.configmapping.mapping.transformers.DateFormatTransformer
      inputFormat: "yyyy-MM-dd"
      outputFormat: "dd/MM/yyyy"
```

---

## 📚 YAML Files Structure

### Main Fields:

* `id`: unique mapping identifier
* `sourceType`: source Java class
* `targetType`: target Java class
* `priority`: priority (used for conflict resolution)
* `fieldMappings`: list of fields to map

### 🏛️ FieldMapping

Each field defines a transformation:

* `sourcePath`: source access path (supports SpEL)
* `targetPath`: destination field
* `transformer` (optional): transformer to apply
* `condition` (optional): condition (e.g., `NotNullCondition`)
* `constant` (optional): constant value

---

## 🚀 Usage in Java Code

### Via the TransferMappingService:

The `TransferMappingService` is a convenient wrapper used in the business layer to encapsulate the mapping engine:

```java
public class TransferMappingService {
    private final MappingService mappingService;

    public TransferMappingService(MappingService mappingService) {
        this.mappingService = mappingService;
    }

    public <S, T> T map(S source, Class<T> targetType) {
        return mappingService.transform(source, targetType);
    }

    public <S, T> T mapWithId(S source, Class<T> targetType, String mappingId, String bankId) {
        return mappingService.transformWithId(source, targetType, mappingId, null);
    }
}
```

Usage:

```java
DeviseDTO dto = transferMappingService.map(externalRate, DeviseDTO.class);
```

---

## 🧰 Ready-to-Use Transformers

Here is the complete list of transformers available in the engine, with a brief description:

* `BooleanToFlagTransformer`: Converts a boolean to a numeric indicator (true → 1, false → 0)
* `DateFormatTransformer`: Reformats a date (String or Date) from one format to another
* `DateToStringTransformer`: Converts a Date object to a string according to a given format
* `DefaultValueTransformer`: Provides a default value if the source value is null
* `EnumMappingTransformer`: Maps a text value to a Java enumeration element
* `GlobalRemainingToDroitUtilisationTransformer`: Specific business transformer related to global usage rights
* `LowerCaseTransformer`: Converts the source string to lowercase
* `PrefixTransformer`: Adds a prefix to the source value (useful for identifiers or labels)
* `RegexReplaceTransformer`: Applies a regex transformation (e.g., text cleaning or formatting)
* `StringCastTransformer`: Applies `toString()` to any object
* `StringToBigDecimalTransformer`: Converts a string to `BigDecimal`
* `StringToDateTransformer`: Converts a string to `Date` using an input format
* `StringToIntTransformer`: Converts a string to an integer (`Integer`)
* `StringTruncateTransformer`: Truncates the string to a maximum length
* `TypeConverterTransformer`: Generic converter between Java types (String → Integer, etc.)
* `UpperCaseTransformer`: Converts the source string to uppercase

You can also add your own custom transformers by implementing the `ValueTransformer` interface.

### Example of YAML Definition with Transformers:

```yaml
fieldMappings:
  - sourcePath: "updateDate"
    targetPath: "applicationdate"
    transformer:
      @class: org.example.configmapping.mapping.transformers.DateFormatTransformer
       inputFormat: "yyyy-MM-dd'T'HH:mm:ss'Z'"
       outputFormat: "yyyy-MM-dd"

  - sourcePath: "rate"
    targetPath: "formattedRate"
    transformers:
      - @class: org.example.configmapping.mapping.transformers.InverseRateTransformer
           scale: 6
      - @class: org.example.configmapping.mapping.transformers.TypeConverterTransformer
           targetType: "java.lang.String"
```

---

## ⚠️ Best Practices

* Define a clear and explicit `mappingId`
* Always use a transformer for type conversions (e.g., date, amount)
* Document each YAML file with an explanatory comment
* Avoid business logic in mapping (use simple and targeted transformers)
* Prefer composition of small reusable transformers

---

## 📄 Conclusion

This engine allows you to create robust, maintainable, and adaptable mappings to your needs without modifying Java code.
Each mapping is easily testable and replaceable via simple YAML configuration.

You are free to add your own transformers according to specific business needs.

# ⚙️ Guide d'utilisation du moteur de mapping YAML

Ce document explique comment utiliser le moteur de mapping dynamique de l'application ADRIA. Il permet de définir, via des fichiers YAML, des transformations d'objets source vers des objets cible sans codage manuel.

## ✨ Objectif

Ce moteur facilite le mapping entre des structures de données hétérogènes tout en gardant une grande flexibilité grâce à la configuration YAML. Il supporte les:

* mappings simples
* mappings conditionnels
* transformations personnalisées (date, enums, expressions, etc.)

---

## 📁 Emplacement des fichiers YAML

Tous les fichiers YAML de mapping doivent être placés dans le répertoire suivant :

```
src/main/resources/mappings/definitions/
```

Chaque fichier doit avoir une extension `.yml` et contenir un identifiant unique (`id`) pour le mapping.

---

## 🔍 Exemple minimal de fichier YAML

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

## 📚 Structure des fichiers YAML

### Champs principaux :

* `id` : identifiant unique du mapping
* `sourceType` : classe Java source
* `targetType` : classe Java cible
* `priority` : priorité (utilisé pour résolution de conflits)
* `fieldMappings` : liste des champs à mapper

### 🏛️ FieldMapping

Chaque champ définit une transformation:

* `sourcePath` : chemin d'accès source (supporte SpEL)
* `targetPath` : champ de destination
* `transformer` (optionnel) : transformateur à appliquer
* `condition` (optionnel) : condition (ex. `NotNullCondition`)
* `constant` (optionnel) : valeur constante

---

## 🚀 Utilisation dans le code Java

### Via le service TransferMappingService :

Le service `TransferMappingService` est un wrapper pratique utilisé dans la couche métier pour encapsuler le moteur de mapping :

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

Utilisation :

```java
DeviseDTO dto = transferMappingService.map(externalRate, DeviseDTO.class);
```

---

## 🧰 Transformateurs prêts à l'emploi

Voici la liste complète des transformateurs disponibles dans le moteur, avec une brève description :

* `BooleanToFlagTransformer` : Convertit un booléen en indicateur numérique (true → 1, false → 0)
* `DateFormatTransformer` : Reformate une date (String ou Date) d’un format à un autre
* `DateToStringTransformer` : Convertit un objet Date en chaîne selon un format donné
* `DefaultValueTransformer` : Fournit une valeur par défaut si la valeur source est null
* `EnumMappingTransformer` : Mappe une valeur texte vers un élément d’énumération Java
* `GlobalRemainingToDroitUtilisationTransformer` : Transformateur métier spécifique lié au droit d’utilisation global
* `LowerCaseTransformer` : Met la chaîne source en minuscules
* `PrefixTransformer` : Ajoute un préfixe à la valeur source (utile pour des identifiants ou libellés)
* `RegexReplaceTransformer` : Applique une transformation regex (ex. nettoyage ou formatage de texte)
* `StringCastTransformer` : Applique `toString()` à n’importe quel objet
* `StringToBigDecimalTransformer` : Convertit une chaîne en `BigDecimal`
* `StringToDateTransformer` : Convertit une chaîne en `Date` à l’aide d’un format d’entrée
* `StringToIntTransformer` : Convertit une chaîne en entier (`Integer`)
* `StringTruncateTransformer` : Tronque la chaîne à une longueur maximale
* `TypeConverterTransformer` : Convertisseur générique entre types Java (String → Integer, etc.)
* `UpperCaseTransformer` : Met la chaîne source en majuscules

Vous pouvez également ajouter vos propres transformateurs personnalisés en implémentant l'interface `ValueTransformer`.

### Exemple de définition YAML avec transformateurs :

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

## ⚠️ Bonnes pratiques

* Définir un `mappingId` clair et explicite
* Toujours utiliser un transformer pour les conversions de type (ex : date, montant)
* Documenter chaque fichier YAML avec un commentaire explicatif
* Éviter la logique métier dans le mapping (utiliser des transformateurs simples et ciblés)
* Préférer la composition de petits transformateurs réutilisables

---

## 📄 Conclusion

Ce moteur permet de créer des mappings robustes, maintenables, et adaptables à vos besoins sans modifier le code Java.
Chaque mapping est facilement testable et remplaçable via une simple configuration YAML.

Vous êtes libre d'ajouter vos propres transformateurs en fonction des besoins métier spécifiques.

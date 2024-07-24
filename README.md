# Azure Code Migration with OpenRewrite
This repository showcases the integration of OpenRewrite with Maven for code migration purposes.
The migration recipe is defined to transition from `azure-core` to `azure-core-v2` libraries. 

## Setup

The migration recipe is defined in the `java-rewrite-core` module as detailed below:

```yaml
### Recipe Configuration for OpenRewrite
type: specs.openrewrite.org/v1beta/recipe
name: com.azure.rewrite.java.core.MigrateAzureCoreSamplesToAzureCoreV2
displayName: Migrate azure-core samples to azure-core-v2
description: This recipe migrates the samples in azure-core to azure-core-v2
recipeList:
  - org.openrewrite.java.ChangePackage:
      oldPackageName: com.azure
      newPackageName: io.clientcore
      recursive: true
```
You can find the recipe configuration in the `rewrite.yml` file [here]().

## Usage
### Maven Plugin Configuration
The OpenRewrite Maven plugin is configured in the `java-rewrite-core` module to run the migration recipe on the sample project
as follows:
```xml
<plugin>
    <groupId>org.openrewrite.maven</groupId>
    <artifactId>rewrite-maven-plugin</artifactId>
    <version>5.7.1</version>
    <configuration>
        <activeRecipes>
            <recipe>com.azure.rewrite.java.core.MigrateAzureCoreSamplesToAzureCoreV2</recipe>
        </activeRecipes>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>rewrite-java-core</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
</plugin>
```
The plugin configuration is defined in the `pom.xml` file [here]().

## Execution
The `rewrite-sample` module is configured to use the `openrewrite-maven-plugin` to run the OpenRewrite recipe on the sample project.
The `rewrite-sample` module contains the modules `azure-ai-translation-text-v1` and `azure-ai-translation-text-v2`
to demonstrate the migration of code from `azure-core` to `azure-core-v2`.

### Dry Run
To run the OpenRewrite recipe in dry-run mode, execute the following command:
```shell
mvn rewrite:dryRun
```
This will generate a file `rewrite.patch` in `target/rewrite` directory.

### Run (apply changes)
To actually apply the changes to the sample project, execute the following command:
```shell
mvn rewrite:run
```

## Testing
Refer to [Recipe Testing](https://docs.openrewrite.org/authoring-recipes/recipe-testing) for information on testing the recipe with unit tests.

## Openrewrite Reference
- [Rewrite Recipe Starter](https://github.com/moderneinc/rewrite-recipe-starter):  Template for building your own recipe JARs
- [Best practices for writing recipes](https://docs.openrewrite.org/recipes/recipes/openrewritebestpractices)
- [Collaboration Proposal](https://github.com/openrewrite/collaboration-proposals/issues/new/choose): collaboration with OpenRewrite







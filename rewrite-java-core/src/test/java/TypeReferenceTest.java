import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * TypeReferenceTest is used to test out the recipe that changes the usage of TypeReference (azure core v1)
 * to ParameterizedType (azure core v2)
 * @author Ali Soltanian Fard Jahromi
 */
public class TypeReferenceTest implements RewriteTest {

    /**
     * This method sets which recipe should be used for testing
     * @param spec stores settings for testing environment; e.g. which recipes to use for testing
     */
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResource("/META-INF/rewrite/rewrite.yml",
                "com.azure.rewrite.java.core.MigrateAzureCoreSamplesToAzureCoreV2");
    }

    /**
     * This test method is used to make sure that TypeReference is correctly
     * changed to ParameterizedType
     */
    @Test
    void testTypeReferenceVariableDeclarationChange() {
        @Language("java") String before = "";
        before += "\nimport java.lang.reflect.ParameterizedType;";
        before += "\nimport java.lang.reflect.Type;";
        before += "\nimport java.util.List;";
        before += "\nimport com.azure.core.util.serializer.TypeReference;";
        before += "\npublic class Testing {";
        before += "\n  private static final TypeReference<List<Testing>> TESTING_TYPE = new TypeReference<List<Testing>>() {};";
        before += "\n}";


        @Language("java") String after = "";
        after += "\nimport java.lang.reflect.ParameterizedType;";
        after += "\nimport java.lang.reflect.Type;";
        after += "\nimport java.util.List;";
        after += "\n\npublic class Testing {";
        after += "\n  private static final Type TESTING_TYPE = new ParameterizedType() {";
        after += " @Override public Type getRawType() { return List.class; }";
        after += " @Override public Type[] getActualTypeArguments() { return new Type[] { Testing.class }; }";
        after += " @Override public Type getOwnerType() { return null; }";
        after += "};";
        after += "\n}";

        rewriteRun(
                java(before,after)
        );
    }
}

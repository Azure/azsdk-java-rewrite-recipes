import com.azure.recipes.v2recipes.TypeReferenceRecipe;
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
        spec.recipes(new TypeReferenceRecipe());
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
        before += "\n  private static final TypeReference<List<String>> TESTING_TYPE = new TypeReference<List<String>>() {\n  };";
        before += "\n}";


        @Language("java") String after = "import java.lang.reflect.ParameterizedType;\n" +
                "import java.lang.reflect.Type;\n" +
                "import java.util.List;\n" +
                "public class Testing {\n" +
                "  private static final java.lang.reflect.Type TESTING_TYPE = new ParameterizedType() {\n" +
                "      @Override\n" +
                "      public java.lang.reflect.Type getRawType() {\n" +
                "          return java.util.List.class;\n" +
                "      }\n\n" +
                "      @Override\n" +
                "      public java.lang.reflect.Type[] getActualTypeArguments() {\n" +
                "          return new java.lang.reflect.Type[]{String.class};\n" +
                "      }\n\n" +
                "      @Override\n" +
                "      public java.lang.reflect.Type getOwnerType() {\n" +
                "          return null;\n" +
                "      }\n" +
                "  };\n" +
                "}\n";

        rewriteRun(
                spec -> spec.cycles(2)
                        .expectedCyclesThatMakeChanges(2),
                java(before,after)
        );
    }
}

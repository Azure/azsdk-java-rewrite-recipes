import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * MiscAnnotationTest is used to test out the recipe that changes the type for the @Generated, @Immutable,
 * and @ReturnType annotations.
 * @author Ali Soltanian Fard Jahromi
 */
public class MiscAnnotationTest implements RewriteTest {
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
     * This method tests to make sure the @Generated annotation type is changed
     * */
    @Test
    void testGeneratedAnnotationImportChange() {
        @Language("java") String before = "import com.azure.core.annotation.Generated;\n" +
                "@Generated(generated = Generated.class)\n";
        before += "\npublic class Testing {";
        before += "\n}";

        @Language("java") String after = "import com.azure.core.v2.annotation.Generated;\n\n" +
                "@Generated(generated = Generated.class)\n";
        after += "\npublic class Testing {";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }

    /**
     * This method tests to make sure the @Immutable annotation type is changed
     * */
    @Test
    void testImmutableAnnotationImportChange() {
        @Language("java") String before = "import com.azure.core.annotation.Immutable;\n" +
                "@Immutable(immutable = Immutable.class)\n";
        before += "\npublic class Testing {";
        before += "\n}";

        @Language("java") String after = "import com.azure.core.v2.annotation.Immutable;\n\n" +
                "@Immutable(immutable = Immutable.class)\n";
        after += "\npublic class Testing {";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }

    /**
     * This method tests to make sure the @ReturnType annotation type is changed
     * */
    @Test
    void testReturnTypeAnnotationImportChange() {
        @Language("java") String before = "import com.azure.core.annotation.ReturnType;\n" +
                "@ReturnType(returnType = ReturnType.class)\n";
        before += "\npublic class Testing {";
        before += "\n}";

        @Language("java") String after = "import com.azure.core.v2.annotation.ReturnType;\n\n" +
                "@ReturnType(returnType = ReturnType.class)\n";
        after += "\npublic class Testing {";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }
}

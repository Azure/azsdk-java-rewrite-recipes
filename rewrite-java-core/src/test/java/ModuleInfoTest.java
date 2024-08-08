import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.text.FindAndReplace;

import static org.openrewrite.test.SourceSpecs.text;

/**
 * ModuleInfoTest is used to test out the recipe that converts dependencies in module-info.java to use com.azure.core.v2.
 * @author Ali Soltanian Fard Jahromi
 */
public class ModuleInfoTest implements RewriteTest {

    /**
     * This method sets which recipe should be used for testing
     * @param spec stores settings for testing environment; e.g. which recipes to use for testing
     */
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new FindAndReplace("requires\\s+(transitive\\s+)?com\\.azure\\.core(?!\\.v2)",
                "requires $1com.azure.core.v2",
                true,false,null,null,null,null));
    }

    /**
     * This test method is used to make sure module info is transformed to use com.azure.core.v2
     */
    @Test
    void testBasicMatch() {
        rewriteRun(
                // Define input and expected output
                text("module com.azure.ai.translation.text { requires com.azure.core; }",
                        "module com.azure.ai.translation.text { requires com.azure.core.v2; }")
        );
    }

    /**
     * This test method is used to make sure module info is transformed to use transitive com.azure.core.v2
     */
    @Test
    void testTransitiveMatch() {
        rewriteRun(
                // Define input and expected output
                text("module com.azure.ai.translation.text { requires transitive com.azure.core; }",
                        "module com.azure.ai.translation.text { requires transitive com.azure.core.v2; }")
        );
    }

    /**
     * This test method is used to make sure no changes are applied if module-info already uses com.azure.core.v2
     */
    @Test
    void testNoMatch() {
        rewriteRun(
                // Define input and expected output
                text("module com.azure.ai.translation.text { requires com.azure.core.v2; }")
        );
    }

    /**
     * This test method is used to make sure no changes are applied if module-info already uses transitive com.azure.core.v2
     */
    void testTransitiveNoMatch() {
        rewriteRun(
                // Define input and expected output
                text("module com.azure.ai.translation.text { requires transitive com.azure.core.v2; }")
        );
    }

    /**
     * This test method is used to make sure module info is transformed to use transitive com.azure.core.v2 and com.azure.core.v2
     */
    @Test
    void testMultipleMatches() {
        rewriteRun(
                // Define input and expected output
                text("module com.azure.ai.translation.text { requires com.azure.core; requires transitive com.azure.core; }",
                        "module com.azure.ai.translation.text { requires com.azure.core.v2; requires transitive com.azure.core.v2; }")
        );
    }
}

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class ClientLoggerTest implements RewriteTest {
    /**
     * ClientLoggerTest tests the recipe that changes
     * com.azure.core.util.logging.ClientLogger to io.clientcore.core.util.ClientLogger.
     * @author Ali Soltanian Fard Jahromi
     */

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResource("/META-INF/rewrite/rewrite.yml",
                "com.azure.rewrite.java.core.MigrateAzureCoreSamplesToAzureCoreV2");
    }

    /* Test to make sure ClientLogger type and import is changed */
    @Test
    public void testClientLoggerTypeAndImportChanged() {
        @Language("java") String before = "import com.azure.core.util.logging.ClientLogger;";
        before += "\npublic class Testing {";
        before += "\n  public Testing(){";
        before += "\n    com.azure.core.util.logging.ClientLogger c = new ClientLogger(Testing.class);";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import io.clientcore.core.util.ClientLogger;";
        after += "\n\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n    io.clientcore.core.util.ClientLogger c = new ClientLogger(Testing.class);";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }

}

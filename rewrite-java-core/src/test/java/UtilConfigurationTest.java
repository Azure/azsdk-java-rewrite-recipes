import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class UtilConfigurationTest implements RewriteTest {
    /**
     * UtilConfigurationTest tests util.Configuration migrations from
     * com.azure.core.util to io.clientcore.core.util.configuration.
     * @author Annabelle Mittendorf Smith
     */

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResource("/META-INF/rewrite/rewrite.yml",
                "com.azure.rewrite.java.core.MigrateAzureCoreSamplesToAzureCoreV2");
    }

    /* Testing ChangeType recipe */
    @Test
    public void testConfigurationTypeChanged() {
        @Language("java") String before = "import com.azure.core.util.Configuration;";
        before += "\npublic class Testing {";
        before += "\n  public Testing(){";
        before += "\n    com.azure.core.util.Configuration c = new Configuration();";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import io.clientcore.core.util.configuration.Configuration;";
        after += "\n\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n    io.clientcore.core.util.configuration.Configuration c = new Configuration();";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }

}

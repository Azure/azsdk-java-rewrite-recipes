import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class CoreUtilsTest implements RewriteTest {
    /**
     * Test migrations from
     * com.azure.core.util.CoreUtils to com.azure.core.v2.util.CoreUtils
     * @author Jessica Lang
     */

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResource("/META-INF/rewrite/rewrite.yml",
                "com.azure.rewrite.java.core.MigrateAzureCoreSamplesToAzureCoreV2");
    }

    /* Testing ChangeType recipe */
    @Test
    public void testConfigurationTypeChanged() {
        @Language("java") String before = "import com.azure.core.util.CoreUtils;";
        before += "\npublic class Testing {";
        before += "\n  public Testing(){";
        before += "\n    com.azure.core.util.CoreUtils cu = new CoreUtils();";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import com.azure.core.v2.util.CoreUtils;";
        after += "\n\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n    com.azure.core.v2.util.CoreUtils cu = new CoreUtils();";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }

}


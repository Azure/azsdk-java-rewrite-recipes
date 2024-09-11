import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class BinaryDataTest implements RewriteTest {
    /**
     * BinaryDataTest tests the recipe that changes
     * com.azure.core.util.BinaryData to io.clientcore.core.util.binarydata.BinaryData.
     * @author Ali Soltanian Fard Jahromi
     */

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResource("/META-INF/rewrite/rewrite.yml",
                "com.azure.rewrite.java.core.MigrateAzureCoreSamplesToAzureCoreV2");
    }

    /* Test to make sure BinaryData type and import is changed */
    @Test
    public void testClientLoggerTypeAndImportChanged() {
        @Language("java") String before = "import com.azure.core.util.BinaryData;";
        before += "\npublic class Testing {";
        before += "\n  public Testing(){";
        before += "\n    com.azure.core.util.BinaryData b = BinaryData.fromObject(null);";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import io.clientcore.core.util.binarydata.BinaryData;";
        after += "\n\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n    io.clientcore.core.util.binarydata.BinaryData b = BinaryData.fromObject(null);";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }

}

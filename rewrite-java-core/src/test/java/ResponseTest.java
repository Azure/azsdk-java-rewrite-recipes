import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.ChangeType;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * ResponseTest is used to test out the recipe that changes
 * references to com.azure.core.http.rest.response to
 * io.clientcore.core.http.models.response.
 * @author Ali Soltanian Fard Jahromi
 */
public class ResponseTest implements RewriteTest {

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
     * This test method is used to make sure that the Response type is updated to the new version
     */
    @Test
    void testUpdateResponseTypeAndImport() {
        @Language("java") String before = "import com.azure.core.http.rest.Response;\n" +
                "import com.azure.core.util.BinaryData;";
        before += "\npublic class Testing {";
        before += "\n  public Testing(){";
        before += "\n    com.azure.core.http.rest.Response<BinaryData> bdr = null;";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import io.clientcore.core.http.models.Response;\n"
                + "import io.clientcore.core.util.binarydata.BinaryData;";
        after += "\n\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n    io.clientcore.core.http.models.Response<BinaryData> bdr = null;";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }
}

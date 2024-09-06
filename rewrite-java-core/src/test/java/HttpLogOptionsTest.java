import com.azure.recipes.v2recipes.HttpLogOptionsRecipe;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class HttpLogOptionsTest implements RewriteTest {
    /**
     * HttpLogOptionsTest tests the recipe that changes
     * com.azure.core.http.policy.HttpLogDetailLevel to io.clientcore.core.http.models.HttpLogOptions.HttpLogDetailLevel
     * and com.azure.core.http.policy.HttpLogOptions to io.clientcore.core.http.models.HttpLogOptions
     *
     * @author Ali Soltanian Fard Jahromi
     */

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new HttpLogOptionsRecipe());
    }

    /* Test to make sure HttpLogOptions and HttpLogDetailLevel type is changed, while also updating imports*/
    @Test
    public void testHttpLogOptionsLogLevelChanged() {
        @Language("java") String before = "import com.azure.core.http.policy.HttpLogOptions;";
        before += "\nimport com.azure.core.http.policy.HttpLogDetailLevel;";
        before += "\npublic class Testing {";
        before += "\n  public Testing(){";
        before += "\n    com.azure.core.http.policy.HttpLogOptions h = new HttpLogOptions();h.setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS);";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import io.clientcore.core.http.models.HttpLogOptions;";
        after += "\nimport io.clientcore.core.http.models.HttpLogOptions.HttpLogDetailLevel;";
        after += "\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n     io.clientcore.core.http.models.HttpLogOptions h = new HttpLogOptions();h.setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS);";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }

}

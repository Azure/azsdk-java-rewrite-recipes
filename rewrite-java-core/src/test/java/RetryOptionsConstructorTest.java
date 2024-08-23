import com.azure.recipes.v2recipes.RetryOptionsConstructorRecipe;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * RetryOptionsTest is used to test out the recipe that removes usage of
 * FixedDelay and ExponentialDelay from the RetryOptions constructor and updates
 * it to use the new azure-core-v2 HttpRetryOptions class.
 * @author Ali Soltanian Fard Jahromi
 */
public class RetryOptionsConstructorTest implements RewriteTest {

    /**
     * This method sets which recipe should be used for testing
     * @param spec stores settings for testing environment; e.g. which recipes to use for testing
     */
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new RetryOptionsConstructorRecipe());
    }

    /**
     * This test method is used to make sure that RetryOptions is updated to the new constructor and class
     */
    @Test
    void testChangeRetryOptionsType() {
        @Language("java") String before = "import com.azure.core.http.policy.RetryOptions;import java.time.Duration;import com.azure.core.http.policy.FixedDelayOptions;";
        before += "\npublic class Testing {";
        before += "\n  public Testing(){";
        before += "\n    com.azure.core.http.policy.RetryOptions r = new RetryOptions(new FixedDelayOptions(3, Duration.ofMillis(50)));";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import io.clientcore.core.http.models.HttpRetryOptions;import java.time.Duration;import com.azure.core.http.policy.FixedDelayOptions;";
        after += "\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n     io.clientcore.core.http.models.HttpRetryOptions r = new HttpRetryOptions(3, Duration.ofMillis(50));";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }

    /**
     * This test method is used to make sure that RetryOptions is updated to the new constructor and class
     * if the FixedDelayOptions is passed as a variable and not a direct instantiation in the constructor of
     * the RetryOptions.
     */
    @Test
    void testChangeRetryOptionsTypeNoArgInit() {
        @Language("java") String before = "import com.azure.core.http.policy.RetryOptions;import java.time.Duration;import com.azure.core.http.policy.FixedDelayOptions;";
        before += "\npublic class Testing {";
        before += "\n  FixedDelayOptions f = new FixedDelayOptions(3, Duration.ofMillis(50));";
        before += "\n  String test = \"testing\";";
        before += "\n  public Testing(){";
        before += "\n    com.azure.core.http.policy.RetryOptions r = new RetryOptions(f);";
        before += "\n     test = \"test\";";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import io.clientcore.core.http.models.HttpRetryOptions;import java.time.Duration;import com.azure.core.http.policy.FixedDelayOptions;";
        after += "\npublic class Testing {";
        after += "\n  FixedDelayOptions f = new FixedDelayOptions(3, Duration.ofMillis(50));";
        after += "\n  String test = \"testing\";";
        after += "\n  public Testing(){";
        after += "\n     io.clientcore.core.http.models.HttpRetryOptions r = new HttpRetryOptions(3, Duration.ofMillis(50));";
        after += "\n     test = \"test\";";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }
}

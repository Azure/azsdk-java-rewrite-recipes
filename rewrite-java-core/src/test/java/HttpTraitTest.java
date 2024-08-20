import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.ChangeMethodName;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * HttpTraitTest tests interface migration from com.azure.core.client.traits.HttpTrait
 * to io.clientcore.core.models.traits.HttpTrait.
 * Tests:
 * Simple method renaming with declarative recipe.
 * CURRENTLY UNFINISHED. All tests for HttpTrait migration will be added here.
 * @author Annabelle Mittendorf Smith
 */

public class HttpTraitTest implements RewriteTest {
    /**
     * This method defines recipes used for testing.
     * @param spec stores settings for testing environment.
     */
    @Override
    public void defaults(RecipeSpec spec) {
        // Declarative recipes defined in rewrite.yml
        spec.recipes(new ChangeMethodName("com.azure.core.client.traits.HttpTrait retryOptions(..)",
                "httpRetryOptions",
                true, false),
                new ChangeMethodName("com.azure.core.client.traits.HttpTrait pipeline(..)",
                        "httpPipeline",
                        true, false),
                new ChangeMethodName("com.azure.core.client.traits.HttpTrait addPolicy(..)",
                        "addHttpPipelinePolicy",
                        true, false));

    }

    /**
     * Basic implementation of starting interface com.azure.core.client.traits.HttpTrait
     */
    @Language("java") String blankBefore = "import com.azure.core.client.traits.HttpTrait;\n" +
            "import com.azure.core.http.HttpClient;\n" +
            "import com.azure.core.http.HttpPipeline;\n" +
            "import com.azure.core.http.policy.HttpLogOptions;\n" +
            "import com.azure.core.http.policy.HttpPipelinePolicy;\n" +
            "import com.azure.core.http.policy.RetryOptions;\n" +
            "import com.azure.core.util.ClientOptions;\n" +
            "\n" +
            "public class BlankHttpTrait implements HttpTrait<BlankHttpTrait> {\n" +
            "    @Override\n" +
            "    public BlankHttpTrait httpClient(HttpClient httpClient) {\n" +
            "        return null;\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public BlankHttpTrait pipeline(HttpPipeline pipeline) {\n" +
            "        return null;\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public BlankHttpTrait addPolicy(HttpPipelinePolicy pipelinePolicy) {\n" +
            "        return null;\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public BlankHttpTrait retryOptions(RetryOptions retryOptions) {\n" +
            "        return null;\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public BlankHttpTrait httpLogOptions(HttpLogOptions logOptions) {\n" +
            "        return null;\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public BlankHttpTrait clientOptions(ClientOptions clientOptions) {\n" +
            "        return null;\n" +
            "    }\n" +
            "}";

    /**
     * Basic implementation of target interface io.clientcore.core.models.traits.HttpTrait
     */
    @Language("java") String blankAfter = "import io.clientcore.core.http.client.HttpClient;\n" +
            "import io.clientcore.core.http.models.HttpLogOptions;\n" +
            "import io.clientcore.core.http.models.HttpRedirectOptions;\n" +
            "import io.clientcore.core.http.models.HttpRetryOptions;\n" +
            "import io.clientcore.core.http.pipeline.HttpPipeline;\n" +
            "import io.clientcore.core.http.pipeline.HttpPipelinePolicy;\n" +
            "import io.clientcore.core.models.traits.HttpTrait;\n" +
            "\n" +
            "public class BlankHttpTrait implements HttpTrait<BlankHttpTrait> {\n" +
            "    @Override\n" +
            "    public BlankHttpTrait httpClient(HttpClient client) {\n" +
            "        return null;\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public BlankHttpTrait httpPipeline(HttpPipeline pipeline) {\n" +
            "        return null;\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public BlankHttpTrait addHttpPipelinePolicy(HttpPipelinePolicy pipelinePolicy) {\n" +
            "        return null;\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public BlankHttpTrait httpRetryOptions(HttpRetryOptions retryOptions) {\n" +
            "        return null;\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public BlankHttpTrait httpLogOptions(HttpLogOptions logOptions) {\n" +
            "        return null;\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public BlankHttpTrait httpRedirectOptions(HttpRedirectOptions redirectOptions) {\n" +
            "        return null;\n" +
            "    }\n" +
            "}";
    /**
     * Test that non-interface methods with the same name are not altered.
     */
    @Test
    void doesNotChangeNonInheritedMethods() {
        @Language("java")String noChange = "public class TestClass {\n" +
                "    public void retryOptions() {\n" +
                "    }\n" +
                "    public void pipeline() {\n" +
                "    }\n" +
                "    public void addPolicy() {\n" +
                "    }\n" +
                "}\n";
        rewriteRun(java(noChange));
    }

    /**
     * Test simple declarative rename of:
     * retryOptions() to httpRetryOptions()
     * pipeline() to httpPipeline()
     * addPolicy() to addHttpPipelinePolicy
     */
    @Test
    void declarativeRenameMethodsSuccessful() {
        @Language("java") String after = "import com.azure.core.client.traits.HttpTrait;\n" +
                "import com.azure.core.http.HttpClient;\n" +
                "import com.azure.core.http.HttpPipeline;\n" +
                "import com.azure.core.http.policy.HttpLogOptions;\n" +
                "import com.azure.core.http.policy.HttpPipelinePolicy;\n" +
                "import com.azure.core.http.policy.RetryOptions;\n" +
                "import com.azure.core.util.ClientOptions;\n" +
                "\n" +
                "public class BlankHttpTrait implements HttpTrait<BlankHttpTrait> {\n" +
                "    @Override\n" +
                "    public BlankHttpTrait httpClient(HttpClient httpClient) {\n" +
                "        return null;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public BlankHttpTrait httpPipeline(HttpPipeline pipeline) {\n" +
                "        return null;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public BlankHttpTrait addHttpPipelinePolicy(HttpPipelinePolicy pipelinePolicy) {\n" +
                "        return null;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public BlankHttpTrait httpRetryOptions(RetryOptions retryOptions) {\n" +
                "        return null;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public BlankHttpTrait httpLogOptions(HttpLogOptions logOptions) {\n" +
                "        return null;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public BlankHttpTrait clientOptions(ClientOptions clientOptions) {\n" +
                "        return null;\n" +
                "    }\n" +
                "}";

        rewriteRun(
                java(blankBefore,after)
        );
    }

}
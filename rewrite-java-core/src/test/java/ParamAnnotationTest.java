import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.ChangeType;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * ParamAnnotationTest is used to test out the recipe that changes the type for the @HostParam, @HeaderParam
 * , @BodyParam and @QueryParam annotations.
 * @author Ali Soltanian Fard Jahromi
 */
public class ParamAnnotationTest implements RewriteTest {
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
     * This method tests to make sure the @HostParam annotation type is changed
     * */
    @Test
    void testHostParam() {
        @Language("java") String before = "import com.azure.core.annotation.HostParam;\n" +
                "@HostParam(host = HostParam.class)\n";
        before += "\npublic class Testing {";
        before += "\n}";

        @Language("java") String after = "import io.clientcore.core.http.annotation.HostParam;\n\n" +
                "@HostParam(host = HostParam.class)\n";
        after += "\npublic class Testing {";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }

    /**
     * This method tests to make sure the @HeaderParam annotation type is changed
     * */
    @Test
    void testHeaderParam() {
        @Language("java") String before = "import com.azure.core.annotation.HeaderParam;\n" +
                "@HeaderParam(header = HeaderParam.class)\n";
        before += "\npublic class Testing {";
        before += "\n}";

        @Language("java") String after = "import io.clientcore.core.http.annotation.HeaderParam;\n\n" +
                "@HeaderParam(header = HeaderParam.class)\n";
        after += "\npublic class Testing {";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }

    /**
     * This method tests to make sure the @BodyParam annotation type is changed
     * */
    @Test
    void testBodyParam() {
        @Language("java") String before = "import com.azure.core.annotation.BodyParam;\n" +
                "@BodyParam(body = BodyParam.class)\n";
        before += "\npublic class Testing {";
        before += "\n}";

        @Language("java") String after = "import io.clientcore.core.http.annotation.BodyParam;\n\n" +
                        "@BodyParam(body = BodyParam.class)\n";
        after += "\npublic class Testing {";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }

    /**
     * This method tests to make sure the @QueryParam annotation type is changed
     * */
    @Test
    void testQueryParam() {
        @Language("java") String before = "import com.azure.core.annotation.QueryParam;\n" +
                "@QueryParam(query = QueryParam.class)\n";
        before += "\npublic class Testing {";
        before += "\n}";

        @Language("java") String after = "import io.clientcore.core.http.annotation.QueryParam;\n\n" +
                "@QueryParam(query = QueryParam.class)\n";
        after += "\npublic class Testing {";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }
}

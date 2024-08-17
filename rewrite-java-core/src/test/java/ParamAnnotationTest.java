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
class ParamAnnotationTest implements RewriteTest {
    /**
     * This method sets which recipe should be used for testing
     * @param spec stores settings for testing environment; e.g. which recipes to use for testing
     */
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipes(new ChangeType("com.azure.core.annotation.HostParam",
                "io.clientcore.core.http.annotation.HostParam", null),

                new ChangeType("com.azure.core.annotation.HeaderParam",
                        "io.clientcore.core.http.annotation.HeaderParam", null),

                new ChangeType("com.azure.core.annotation.QueryParam",
                        "io.clientcore.core.http.annotation.QueryParam", null),

                new ChangeType("com.azure.core.annotation.BodyParam",
                        "io.clientcore.core.http.annotation.BodyParam", null));
    }

    /**
     * This test method is used to make sure that the annotation types are changed correctly
     */
    @Test
    void testParam() {
        @Language("java") String before = "import com.azure.core.annotation.HostParam;" +
                "\nimport com.azure.core.annotation.BodyParam;" +
                "\nimport com.azure.core.util.Context;" +
                "\nimport com.azure.core.util.BinaryData;" +
                "\nimport com.azure.core.annotation.QueryParam;" +
                "\nimport com.azure.core.annotation.HeaderParam;" +
                "\nimport com.azure.core.http.rest.RequestOptions;";
        before += "\npublic class Testing {";
        before += "\n  ";
        before += "\n    Response<BinaryData> getSupportedLanguagesSync(@BodyParam(\"application/json\") BinaryData body, @HostParam(\"Endpoint\") String endpoint, @QueryParam(\"api-version\") String apiVersion, @HeaderParam(\"accept\") String accept, RequestOptions requestOptions, Context context);";
        before += "\n  ";
        before += "\n}";

        @Language("java") String after =
                "\nimport com.azure.core.util.Context;" +
                "\nimport io.clientcore.core.http.annotation.BodyParam;" +
                "\nimport io.clientcore.core.http.annotation.HeaderParam;" +
                "\nimport io.clientcore.core.http.annotation.HostParam;" +
                "\nimport io.clientcore.core.http.annotation.QueryParam;" +
                "\nimport com.azure.core.util.BinaryData;" +
                "\nimport com.azure.core.http.rest.RequestOptions;";
        after += "\n\npublic class Testing {";
        after += "\n  ";
        after += "\n    Response<BinaryData> getSupportedLanguagesSync(@BodyParam(\"application/json\") BinaryData body, @HostParam(\"Endpoint\") String endpoint, @QueryParam(\"api-version\") String apiVersion, @HeaderParam(\"accept\") String accept, RequestOptions requestOptions, Context context);";
        after += "\n  ";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }
}

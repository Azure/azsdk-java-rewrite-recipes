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

    @Test
    public void testConfigurationImportChanged() {
        @Language("java") String before = "import com.azure.core.util.Configuration;\n" +
                "public class Testing { Configuration configuration; }\n";

        @Language("java") String after = "import io.clientcore.core.util.configuration.Configuration;\n" +
                "public class Testing { Configuration configuration; }\n";

        rewriteRun(java(before,after));
    }

}

package java.com.azure.ai.translation.text;

import com.azure.ai.translation.text.TextTranslationClient;
import com.azure.ai.translation.text.TextTranslationClientBuilder;
import com.azure.ai.translation.text.models.InputTextItem;
import com.azure.ai.translation.text.models.TranslatedTextItem;
import io.clientcore.core.credential.KeyCredential;
import io.clientcore.core.http.models.HttpLogOptions;
import io.clientcore.core.http.models.HttpRetryOptions;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class TextTranslationSample {
    public static void main(String[] args) {
        TextTranslationClient textTranslationClient = new TextTranslationClientBuilder()
                .credential(new KeyCredential("<api-key>"))
                .endpoint("<endpoint>")
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BODY_AND_HEADERS))
                .httpRetryOptions(new HttpRetryOptions(3, Duration.ofMillis(50)))
                .buildClient();

        List<InputTextItem> inputTextItems = Arrays.asList(new InputTextItem("hello world"));
        List<TranslatedTextItem> result = textTranslationClient.translate(Arrays.asList("es"), inputTextItems);

        result.stream()
                .flatMap(translatedTextItem -> translatedTextItem.getTranslations().stream())
                .forEach(translation -> System.out.println("Translated text to " + translation.getTargetLanguage() + " : " + translation.getText()));
    }
}

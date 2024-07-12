package java.com.azure.ai.translation.text;

import com.azure.ai.translation.text.TextTranslationClient;
import com.azure.ai.translation.text.TextTranslationClientBuilder;
import com.azure.ai.translation.text.models.InputTextItem;
import com.azure.ai.translation.text.models.TranslatedTextItem;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryOptions;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class TextTranslationSample {

    public static void main(String[] args) {
        TextTranslationClient textTranslationClient = new TextTranslationClientBuilder()
                .credential(new KeyCredential("<api-key>"))
                .endpoint("<endpoint>")
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .retryOptions(new RetryOptions(new FixedDelayOptions(3, Duration.ofMillis(50))))
                .buildClient();

        List<InputTextItem> inputTextItems = Arrays.asList(new InputTextItem("hello world"));
        List<TranslatedTextItem> result = textTranslationClient.translate(Arrays.asList("es"), inputTextItems);

        result.stream()
                .flatMap(translatedTextItem -> translatedTextItem.getTranslations().stream())
                .forEach(translation -> System.out.println("Translated text to " + translation.getTargetLanguage() + " : " + translation.getText()));
    }
}

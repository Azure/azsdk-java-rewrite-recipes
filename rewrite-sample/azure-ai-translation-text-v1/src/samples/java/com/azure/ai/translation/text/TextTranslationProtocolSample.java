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
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.TypeReference;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrating the use of protocol client methods to translate text.
 */
public class TextTranslationProtocolSample {
    public static void main(String[] args) {
        TextTranslationClient textTranslationClient = new TextTranslationClientBuilder()
                .credential(new KeyCredential("<api-key>"))
                .endpoint("<endpoint>")
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .retryOptions(new RetryOptions(new FixedDelayOptions(3, Duration.ofMillis(50))))
                .buildClient();

        List<InputTextItem> inputTextItems = Arrays.asList(new InputTextItem("hello world"));

        List<String> targetLanguages = Arrays.asList("es");
        BinaryData requestBody = BinaryData.fromObject(inputTextItems);
        RequestOptions requestOptions = new RequestOptions().setContext(Context.NONE);

        Response<BinaryData> binaryDataResponse = textTranslationClient.translateWithResponse(targetLanguages, requestBody, requestOptions);
        List<TranslatedTextItem> result = binaryDataResponse.getValue().toObject(new TypeReference<List<TranslatedTextItem>>() { });

        result.stream()
                .flatMap(translatedTextItem -> translatedTextItem.getTranslations().stream())
                .forEach(translation -> System.out.println("Translated text to " + translation.getTargetLanguage() + " : " + translation.getText()));

    }
}

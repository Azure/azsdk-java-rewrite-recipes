package java.com.azure.ai.translation.text;

import com.azure.ai.translation.text.TextTranslationClient;
import com.azure.ai.translation.text.TextTranslationClientBuilder;
import com.azure.ai.translation.text.models.InputTextItem;
import com.azure.ai.translation.text.models.TranslatedTextItem;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.util.Context;
import io.clientcore.core.util.binarydata.BinaryData;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrating the use of protocol client methods to translate text.
 */
public class UserClass {
    void myMethod() {

        TextTranslationClient textTranslationClient = new TextTranslationClientBuilder().buildClient();

        List<InputTextItem> inputTextItems = Arrays.asList(new InputTextItem("hello world"));
        List<String> targetLanguages = Arrays.asList("es");
        BinaryData requestBody = BinaryData.fromObject(inputTextItems);
        RequestOptions requestOptions = new RequestOptions().setContext(Context.none());

        Response<BinaryData> binaryDataResponse = textTranslationClient.translateWithResponse(targetLanguages, requestBody, requestOptions);
        List<TranslatedTextItem> result = null;
        try {
            result = binaryDataResponse.getValue().toObject(new ParameterizedType() {
                @Override
                public Type getRawType() {
                    return List.class;
                }

                @Override
                public Type[] getActualTypeArguments() {
                    return new Type[]{TranslatedTextItem.class};
                }

                @Override
                public Type getOwnerType() {
                    return null;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.ai.translation.text.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * Dictionary Lookup Element.
 */
@Immutable
public final class DictionaryLookupItem implements JsonSerializable<DictionaryLookupItem> {
    /*
     * A string giving the normalized form of the source term.
     * For example, if the request is "JOHN", the normalized form will be "john".
     * The content of this field becomes the input to lookup examples.
     */
    @Generated
    private final String normalizedSource;

    /*
     * A string giving the source term in a form best suited for end-user display.
     * For example, if the input is "JOHN", the display form will reflect the usual
     * spelling of the name: "John".
     */
    @Generated
    private final String displaySource;

    /*
     * A list of translations for the source term.
     */
    @Generated
    private final List<DictionaryTranslation> translations;

    /**
     * Creates an instance of DictionaryLookupItem class.
     * 
     * @param normalizedSource the normalizedSource value to set.
     * @param displaySource the displaySource value to set.
     * @param translations the translations value to set.
     */
    @Generated
    private DictionaryLookupItem(String normalizedSource, String displaySource,
        List<DictionaryTranslation> translations) {
        this.normalizedSource = normalizedSource;
        this.displaySource = displaySource;
        this.translations = translations;
    }

    /**
     * Get the normalizedSource property: A string giving the normalized form of the source term.
     * For example, if the request is "JOHN", the normalized form will be "john".
     * The content of this field becomes the input to lookup examples.
     * 
     * @return the normalizedSource value.
     */
    @Generated
    public String getNormalizedSource() {
        return this.normalizedSource;
    }

    /**
     * Get the displaySource property: A string giving the source term in a form best suited for end-user display.
     * For example, if the input is "JOHN", the display form will reflect the usual
     * spelling of the name: "John".
     * 
     * @return the displaySource value.
     */
    @Generated
    public String getDisplaySource() {
        return this.displaySource;
    }

    /**
     * Get the translations property: A list of translations for the source term.
     * 
     * @return the translations value.
     */
    @Generated
    public List<DictionaryTranslation> getTranslations() {
        return this.translations;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("normalizedSource", this.normalizedSource);
        jsonWriter.writeStringField("displaySource", this.displaySource);
        jsonWriter.writeArrayField("translations", this.translations, (writer, element) -> writer.writeJson(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of DictionaryLookupItem from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of DictionaryLookupItem if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the DictionaryLookupItem.
     */
    @Generated
    public static DictionaryLookupItem fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String normalizedSource = null;
            String displaySource = null;
            List<DictionaryTranslation> translations = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("normalizedSource".equals(fieldName)) {
                    normalizedSource = reader.getString();
                } else if ("displaySource".equals(fieldName)) {
                    displaySource = reader.getString();
                } else if ("translations".equals(fieldName)) {
                    translations = reader.readArray(reader1 -> DictionaryTranslation.fromJson(reader1));
                } else {
                    reader.skipChildren();
                }
            }
            return new DictionaryLookupItem(normalizedSource, displaySource, translations);
        });
    }
}
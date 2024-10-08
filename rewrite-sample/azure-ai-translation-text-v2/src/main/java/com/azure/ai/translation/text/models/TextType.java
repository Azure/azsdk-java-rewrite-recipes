// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.ai.translation.text.models;

import com.azure.core.v2.annotation.Generated;
import io.clientcore.core.util.ExpandableEnum;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Translation text type.
 */
public final class TextType implements ExpandableEnum<String> {
    private static final Map<String, TextType> VALUES = new ConcurrentHashMap<>();

    /**
     * Plain text.
     */
    @Generated
    public static final TextType PLAIN = fromString("Plain");

    /**
     * HTML-encoded text.
     */
    @Generated
    public static final TextType HTML = fromString("Html");

    private final String name;

    private TextType(String name) {
        this.name = name;
    }

    /**
     * Creates or finds a TextType.
     * 
     * @param name a name to look for.
     * @return the corresponding TextType.
     */
    @Generated
    public static TextType fromString(String name) {
        if (name == null) {
            return null;
        }
        TextType value = VALUES.get(name);
        if (value != null) {
            return value;
        }
        return VALUES.computeIfAbsent(name, key -> new TextType(key));
    }

    /**
     * Gets the value of the TextType instance.
     * 
     * @return the value of the TextType instance.
     */
    @Generated
    @Override
    public String getValue() {
        return this.name;
    }

    @Generated
    @Override
    public String toString() {
        return name;
    }
}

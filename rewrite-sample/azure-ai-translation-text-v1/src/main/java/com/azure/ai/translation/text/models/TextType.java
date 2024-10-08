// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.ai.translation.text.models;

import com.azure.core.annotation.Generated;
import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * Translation text type.
 */
public final class TextType extends ExpandableStringEnum<TextType> {
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

    /**
     * Creates a new instance of TextType value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Generated
    @Deprecated
    public TextType() {
    }

    /**
     * Creates or finds a TextType from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding TextType.
     */
    @Generated
    public static TextType fromString(String name) {
        return fromString(name, TextType.class);
    }

    /**
     * Gets known TextType values.
     * 
     * @return known TextType values.
     */
    @Generated
    public static Collection<TextType> values() {
        return values(TextType.class);
    }
}

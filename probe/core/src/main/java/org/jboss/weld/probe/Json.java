/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.probe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.inject.Vetoed;

import org.jboss.weld.util.Preconditions;

/**
 * Simple JSON generator. A third-party library is not used intentionally - we don't need any other dependencies.
 *
 * @author Martin Kouba
 */
@Vetoed
final class Json {

    private static final String NAME = "name";
    private static final String OBJECT_START = "{";
    private static final String OBJECT_END = "}";
    private static final String ARRAY_START = "[";
    private static final String ARRAY_END = "]";
    private static final String NAME_VAL_SEPARATOR = ":";
    private static final String ENTRY_SEPARATOR = ",";

    private static final int CONTROL_CHAR_START = 0;
    private static final int CONTROL_CHAR_END = 0x1f;

    private static final Map<Character, String> REPLACEMENTS;

    static {
        REPLACEMENTS = new HashMap<>();
        // control characters
        for (int i = CONTROL_CHAR_START; i <= CONTROL_CHAR_END; i++) {
            REPLACEMENTS.put((char) i, String.format("\\u%04x", i));
        }
        // quotation mark
        REPLACEMENTS.put('"', "\\\"");
        // reverse solidus
        REPLACEMENTS.put('\\', "\\\\");
    }

    private static final char CHAR_QUOTATION_MARK = '"';

    private Json() {
    }

    /**
     * @return the new JSON array builder, empty builders are not ignored
     */
    static JsonArrayBuilder arrayBuilder() {
        return new JsonArrayBuilder(false);
    }

    /**
     *
     * @param ignoreEmptyBuilders
     * @return the new JSON array builder
     * @see JsonBuilder#ignoreEmptyBuilders
     */
    static JsonArrayBuilder arrayBuilder(boolean ignoreEmptyBuilders) {
        return new JsonArrayBuilder(ignoreEmptyBuilders);
    }

    /**
     *
     * @return the new JSON object builder, empty builders are not ignored
     */
    static JsonObjectBuilder objectBuilder() {
        return new JsonObjectBuilder(false);
    }

    /**
     *
     * @param ignoreEmptyBuilders
     * @return the new JSON object builder
     * @see JsonBuilder#ignoreEmptyBuilders
     */
    static JsonObjectBuilder objectBuilder(boolean ignoreEmptyBuilders) {
        return new JsonObjectBuilder(ignoreEmptyBuilders);
    }

    /**
     *
     * @author Martin Kouba
     *
     * @param <T> Builder type
     */
    abstract static class JsonBuilder<T> {

        protected boolean ignoreEmptyBuilders = false;

        /**
         *
         * @param ignoreEmptyBuilders If set to true all empty builders added to this builder will be ignored during {@link #build()}
         */
        JsonBuilder(boolean ignoreEmptyBuilders) {
            this.ignoreEmptyBuilders = ignoreEmptyBuilders;
        }

        /**
         *
         * @return <code>true</code> if there are no elements/properties, <code>false</code> otherwise
         */
        abstract boolean isEmpty();

        /**
         *
         * @return a string representation
         */
        abstract String build();

        /**
         *
         * @param value
         * @return <code>true</code> if the value is null or an empty builder and {@link #ignoreEmptyBuilders} is set to <code>true</code>, <code>false</code>
         *         otherwise
         */
        protected boolean isIgnored(Object value) {
            return value == null || (ignoreEmptyBuilders && value instanceof JsonBuilder && ((JsonBuilder<?>) value).isEmpty());
        }

        protected boolean isValuesEmpty(Collection<Object> values) {
            if (values.isEmpty()) {
                return true;
            }
            for (Object object : values) {
                if (object instanceof JsonBuilder) {
                    if (!((JsonBuilder<?>) object).isEmpty()) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;

        }

        protected abstract T self();

    }

    /**
     * JSON array builder.
     *
     * @author Martin Kouba
     */
    static class JsonArrayBuilder extends JsonBuilder<JsonArrayBuilder> {

        private final List<Object> values;

        private JsonArrayBuilder(boolean ignoreEmptyBuilders) {
            super(ignoreEmptyBuilders);
            this.values = new ArrayList<Object>();
        }

        JsonArrayBuilder add(JsonArrayBuilder value) {
            addInternal(value);
            return this;
        }

        JsonArrayBuilder add(JsonObjectBuilder value) {
            addInternal(value);
            return this;
        }

        JsonArrayBuilder add(String value) {
            addInternal(value);
            return this;
        }

        JsonArrayBuilder add(Boolean value) {
            addInternal(value);
            return this;
        }

        JsonArrayBuilder add(Integer value) {
            addInternal(value);
            return this;
        }

        JsonArrayBuilder add(Long value) {
            addInternal(value);
            return this;
        }

        private void addInternal(Object value) {
            if (value != null) {
                values.add(value);
            }
        }

        boolean isEmpty() {
            return isValuesEmpty(values);
        }

        String build() {
            StringBuilder builder = new StringBuilder();
            builder.append(ARRAY_START);
            int idx = 0;
            for (ListIterator<Object> iterator = values.listIterator(); iterator.hasNext();) {
                Object value = iterator.next();
                if (isIgnored(value)) {
                    continue;
                }
                if (++idx > 1) {
                    builder.append(ENTRY_SEPARATOR);
                }
                appendValue(builder, value);
            }
            builder.append(ARRAY_END);
            return builder.toString();
        }

        @Override
        protected JsonArrayBuilder self() {
            return this;
        }

    }

    /**
     * JSON object builder.
     *
     * @author Martin Kouba
     */
    static class JsonObjectBuilder extends JsonBuilder<JsonObjectBuilder> {

        private final Map<String, Object> properties;

        private JsonObjectBuilder(boolean ignoreEmptyBuilders) {
            super(ignoreEmptyBuilders);
            this.properties = new LinkedHashMap<String, Object>();
        }

        JsonObjectBuilder add(String name, String value) {
            addInternal(name, value);
            return this;
        }

        JsonObjectBuilder add(String name, JsonObjectBuilder value) {
            addInternal(name, value);
            return this;
        }

        JsonObjectBuilder add(String name, JsonArrayBuilder value) {
            addInternal(name, value);
            return this;
        }

        JsonObjectBuilder add(String name, Boolean value) {
            addInternal(name, value);
            return this;
        }

        JsonObjectBuilder add(String name, Integer value) {
            addInternal(name, value);
            return this;
        }

        JsonObjectBuilder add(String name, Long value) {
            addInternal(name, value);
            return this;
        }

        boolean has(String name) {
            return properties.containsKey(name);
        }

        private void addInternal(String name, Object value) {
            Preconditions.checkArgumentNotNull(name, NAME);
            if (value != null) {
                properties.put(name, value);
            }
        }

        boolean isEmpty() {
            if (properties.isEmpty()) {
                return true;
            }
            return isValuesEmpty(properties.values());
        }

        String build() {
            StringBuilder builder = new StringBuilder();
            builder.append(OBJECT_START);
            int idx = 0;
            for (Iterator<Entry<String, Object>> iterator = properties.entrySet().iterator(); iterator.hasNext();) {
                Entry<String, Object> entry = iterator.next();
                if (isIgnored(entry.getValue())) {
                    continue;
                }
                if (++idx > 1) {
                    builder.append(ENTRY_SEPARATOR);
                }
                appendStringValue(builder, entry.getKey());
                builder.append(NAME_VAL_SEPARATOR);
                appendValue(builder, entry.getValue());
            }
            builder.append(OBJECT_END);
            return builder.toString();
        }

        @Override
        protected JsonObjectBuilder self() {
            return this;
        }

    }

    static void appendValue(StringBuilder builder, Object value) {
        if (value instanceof JsonObjectBuilder) {
            builder.append(((JsonObjectBuilder) value).build());
        } else if (value instanceof JsonArrayBuilder) {
            builder.append(((JsonArrayBuilder) value).build());
        } else if (value instanceof String) {
            appendStringValue(builder, value.toString());
        } else if (value instanceof Boolean || value instanceof Integer || value instanceof Long) {
            builder.append(value.toString());
        } else {
            throw new IllegalStateException("Unsupported value type: " + value);
        }
    }

    static void appendStringValue(StringBuilder builder, String value) {
        builder.append(CHAR_QUOTATION_MARK);
        builder.append(escape(value));
        builder.append(CHAR_QUOTATION_MARK);
    }

    /**
     * Escape quotation mark, reverse solidus and control characters (U+0000 through U+001F).
     *
     * @param value
     * @return escaped value
     * @see <a href="http://www.ietf.org/rfc/rfc4627.txt">http://www.ietf.org/rfc/rfc4627.txt</a>
     */
    static String escape(String value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            String replacement = REPLACEMENTS.get(c);
            if (replacement != null) {
                builder.append(replacement);
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

}

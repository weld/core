/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.logging;

import static org.jboss.weld.util.cache.LoadingCacheUtils.getCacheValue;

import java.lang.reflect.Field;
import java.util.Locale;

import ch.qos.cal10n.MessageConveyor;
import ch.qos.cal10n.MessageConveyorException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class WeldMessageConveyor extends MessageConveyor {

    private static class ComputeMessagePrefix extends CacheLoader<Enum<?>, String> {

        private final String subsystem;

        private static final String KEY_TYPE = "; Key Type: ";

        private ComputeMessagePrefix(String subsystem) {
            this.subsystem = subsystem;
        }

        public String load(Enum<?> from) {
            Field field;
            try {
                field = from.getClass().getField(from.name());
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("Cannot reflect on key to obtain @MessageId. Key: " + from + KEY_TYPE + from.getClass());
            }
            if (!field.isAnnotationPresent(MessageId.class)) {
                throw new IllegalArgumentException("@MessageId must be present. Key: " + from + KEY_TYPE + from.getClass());
            }
            String messageId = field.getAnnotation(MessageId.class).value();
            return new StringBuilder().append(subsystem).append(SEPARATOR).append(messageId).append(" ").toString();
        }

    }

    private static final String SEPARATOR = "-";

    private final LoadingCache<Enum<?>, String> messagePrefixCache;

    public WeldMessageConveyor(Locale locale, String subsystem) {
        super(locale);
        this.messagePrefixCache = CacheBuilder.newBuilder().build(new ComputeMessagePrefix(subsystem));
    }

    @Override
    public <E extends Enum<?>> String getMessage(E key, Object... args) throws MessageConveyorException {
        return new StringBuilder().append(getMessagePrefix(key)).append(super.getMessage(key, args)).toString();
    }

    private <E extends Enum<?>> String getMessagePrefix(final E key) {
        return getCacheValue(messagePrefixCache, key);
    }

}

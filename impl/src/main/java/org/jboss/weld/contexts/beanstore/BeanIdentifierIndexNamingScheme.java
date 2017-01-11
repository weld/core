/*
 *
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
package org.jboss.weld.contexts.beanstore;

import org.jboss.weld.bean.StringBeanIdentifier;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.serialization.BeanIdentifierIndex;
import org.jboss.weld.serialization.spi.BeanIdentifier;

/**
 * An abstract naming scheme which makes use of {@link BeanIdentifierIndex} if possible.
 *
 * @author Martin Kouba
 */
public abstract class BeanIdentifierIndexNamingScheme extends AbstractNamingScheme {

    private static final String FALLBACK_FLAG = "F_";

    private final BeanIdentifierIndex index;

    public BeanIdentifierIndexNamingScheme(String delimiter, BeanIdentifierIndex index) {
        super(delimiter);
        this.index = index;
    }

    @Override
    public BeanIdentifier deprefix(String id) {
        String deprefixed = id.substring(getPrefix().length() + getDelimiter().length());
        if (index == null) {
            return new StringBeanIdentifier(deprefixed);
        }
        if (deprefixed.startsWith(FALLBACK_FLAG)) {
            return new StringBeanIdentifier(deprefixed.substring(FALLBACK_FLAG.length()));
        }
        try {
            return index.getIdentifier(Integer.parseInt(deprefixed));
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Unable to deprefix id:" + id, e);
        }
    }

    @Override
    public String prefix(BeanIdentifier id) {
        if (index == null) {
            return getPrefix() + getDelimiter() + id.asString();
        }
        Integer idx = index.getIndex(id);
        if (idx == null) {
            return getPrefix() + getDelimiter() + FALLBACK_FLAG + id.asString();
        }
        return getPrefix() + getDelimiter() + idx;
    }

}

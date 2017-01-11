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

import org.jboss.weld.logging.ContextLogger;
import org.jboss.weld.serialization.BeanIdentifierIndex;

/**
 * A simple naming scheme (with a solid prefix) which makes use of {@link BeanIdentifierIndex} if possible.
 *
 * @author Martin Kouba
 */
public class SimpleBeanIdentifierIndexNamingScheme extends BeanIdentifierIndexNamingScheme {

    private final String prefix;

    /**
     * @param prefix
     * @param delimiter
     */
    public SimpleBeanIdentifierIndexNamingScheme(String prefix, BeanIdentifierIndex index) {
        super("#", index);
        if (prefix.contains(getDelimiter())) {
            throw ContextLogger.LOG.delimiterInPrefix(getDelimiter(), prefix);
        }
        this.prefix = prefix;
    }

    @Override
    protected String getPrefix() {
        return prefix;
    }

}

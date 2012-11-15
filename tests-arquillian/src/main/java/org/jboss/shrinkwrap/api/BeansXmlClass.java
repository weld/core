/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.shrinkwrap.api;

import com.google.common.base.Preconditions;

/**
 * Represents a &lt;class&gt; element in the beans.xml file. Supports enabled and priority attributes. Instances of this class are immutable.
 *
 * @author Jozef Hartinger
 * @see BeansXmlStereotype
 *
 */
public class BeansXmlClass {

    private final Class<?> javaClass;
    private final Boolean enabled;
    private final Integer priority;

    public BeansXmlClass(Class<?> javaClass, Boolean enabled, Integer priority) {
        Preconditions.checkNotNull(javaClass);
        this.javaClass = javaClass;
        this.enabled = enabled;
        this.priority = priority;
    }

    public BeansXmlClass(Class<?> javaClass) {
        this(javaClass, null, null);
    }

    public BeansXmlClass(Class<?> javaClass, Boolean enabled) {
        this(javaClass, enabled, null);
    }

    public BeansXmlClass(Class<?> javaClass, Integer priority) {
        this(javaClass, null, priority);
    }

    public String asXmlElement() {
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(getElementName());
        if (enabled != null) {
            builder.append(" ");
            builder.append("enabled=\"");
            builder.append(enabled);
            builder.append("\"");
        }
        if (priority != null) {
            builder.append(" ");
            builder.append("priority=\"");
            builder.append(priority);
            builder.append("\"");
        }
        builder.append(">");
        builder.append(javaClass.getName());
        builder.append("</");
        builder.append(getElementName());
        builder.append(">");
        return builder.toString();
    }

    protected String getElementName() {
        return "class";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((enabled == null) ? 0 : enabled.hashCode());
        result = prime * result + ((javaClass == null) ? 0 : javaClass.hashCode());
        result = prime * result + ((priority == null) ? 0 : priority.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BeansXmlClass)) {
            return false;
        }
        BeansXmlClass other = (BeansXmlClass) obj;
        if (enabled == null) {
            if (other.enabled != null) {
                return false;
            }
        } else if (!enabled.equals(other.enabled)) {
            return false;
        }
        if (javaClass == null) {
            if (other.javaClass != null) {
                return false;
            }
        } else if (!javaClass.equals(other.javaClass)) {
            return false;
        }
        if (priority == null) {
            if (other.priority != null) {
                return false;
            }
        } else if (!priority.equals(other.priority)) {
            return false;
        }
        return true;
    }
}

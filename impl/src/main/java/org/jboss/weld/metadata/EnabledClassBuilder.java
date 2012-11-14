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
package org.jboss.weld.metadata;

import org.jboss.weld.bootstrap.spi.EnabledClass;
import org.jboss.weld.bootstrap.spi.EnabledStereotype;

/**
 * Builder for {@link EnabledClass} objects. This builder is mutable. The resulting {@link EnabledClass} is immutable.
 *
 * @author Jozef Hartinger
 *
 */
public class EnabledClassBuilder {

    private Boolean enabled;
    private Integer priority;
    private String value;
    private boolean stereotype;

    public EnabledClassBuilder setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public EnabledClassBuilder setPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    public EnabledClassBuilder setValue(String value) {
        this.value = value;
        return this;
    }

    public EnabledClassBuilder setStereotype(boolean value) {
        this.stereotype = value;
        return this;
    }

    public EnabledClass create() {
        if (value == null) {
            throw new IllegalStateException("Value must be set");
        }
        if (stereotype) {
            return new EnabledStereotypeImpl(enabled, priority, value);
        } else {
            return new EnabledClassImpl(enabled, priority, value);
        }
    }

    private static class EnabledClassImpl implements EnabledClass {

        private final Boolean enabled;
        private final Integer priority;
        private final String value;

        public EnabledClassImpl(Boolean enabled, Integer priority, String value) {
            this.enabled = enabled;
            this.priority = priority;
            this.value = value;
        }

        @Override
        public Boolean isEnabled() {
            return enabled;
        }

        @Override
        public Integer getPriority() {
            return priority;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((enabled == null) ? 0 : enabled.hashCode());
            result = prime * result + ((priority == null) ? 0 : priority.hashCode());
            result = prime * result + ((value == null) ? 0 : value.hashCode());
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
            if (!(obj instanceof EnabledClassImpl)) {
                return false;
            }
            EnabledClassImpl other = (EnabledClassImpl) obj;
            if (enabled == null) {
                if (other.enabled != null) {
                    return false;
                }
            } else if (!enabled.equals(other.enabled)) {
                return false;
            }
            if (priority == null) {
                if (other.priority != null) {
                    return false;
                }
            } else if (!priority.equals(other.priority)) {
                return false;
            }
            if (value == null) {
                if (other.value != null) {
                    return false;
                }
            } else if (!value.equals(other.value)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
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
            builder.append(value);
            builder.append("</");
            builder.append(getElementName());
            builder.append(">");
            return builder.toString();
        }

        protected String getElementName() {
            return "class";
        }
    }

    private static class EnabledStereotypeImpl extends EnabledClassImpl implements EnabledStereotype {

        public EnabledStereotypeImpl(Boolean enabled, Integer priority, String value) {
            super(enabled, priority, value);
        }

        @Override
        protected String getElementName() {
            return "stereotype";
        }
    }
}

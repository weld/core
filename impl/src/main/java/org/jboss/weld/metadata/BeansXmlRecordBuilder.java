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

import org.jboss.weld.bootstrap.spi.BeansXmlRecord;

/**
 * Builder for {@link BeansXmlRecord} objects. This builder is mutable. The resulting {@link BeansXmlRecord} is immutable.
 *
 * @author Jozef Hartinger
 *
 */
public class BeansXmlRecordBuilder {

    private Boolean enabled;
    private Integer priority;
    private String value;
    private boolean stereotype;

    public BeansXmlRecordBuilder setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public BeansXmlRecordBuilder setPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    public BeansXmlRecordBuilder setValue(String value) {
        this.value = value;
        return this;
    }

    public BeansXmlRecordBuilder setStereotype(boolean value) {
        this.stereotype = value;
        return this;
    }

    public BeansXmlRecord create() {
        if (value == null) {
            throw new IllegalStateException("Value must be set");
        }
        return new BeansXmlRecordImpl(enabled, priority, value, stereotype);
    }

    private static class BeansXmlRecordImpl implements BeansXmlRecord {

        private final Boolean enabled;
        private final Integer priority;
        private final String value;
        private final boolean stereotype;

        public BeansXmlRecordImpl(Boolean enabled, Integer priority, String value, boolean stereotype) {
            this.enabled = enabled;
            this.priority = priority;
            this.value = value;
            this.stereotype = stereotype;
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

        public boolean isStereotype() {
            return stereotype;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((enabled == null) ? 0 : enabled.hashCode());
            result = prime * result + ((priority == null) ? 0 : priority.hashCode());
            result = prime * result + (stereotype ? 1231 : 1237);
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
            if (!(obj instanceof BeansXmlRecordImpl)) {
                return false;
            }
            BeansXmlRecordImpl other = (BeansXmlRecordImpl) obj;
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
            if (stereotype != other.stereotype) {
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
            return "BeansXmlRecordImpl [enabled=" + enabled + ", priority=" + priority + ", value=" + value + "]";
        }
    }
}

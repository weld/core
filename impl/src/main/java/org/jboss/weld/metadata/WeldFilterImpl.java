/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

import java.util.Collection;

import org.jboss.weld.bootstrap.spi.ClassAvailableActivation;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.SystemPropertyActivation;
import org.jboss.weld.bootstrap.spi.WeldFilter;

/**
 *
 * @author Jozef Hartinger
 *
 */
public class WeldFilterImpl extends FilterImpl implements WeldFilter {

    private final String pattern;

    public WeldFilterImpl(String name, Collection<Metadata<SystemPropertyActivation>> systemPropertyActivation,
            Collection<Metadata<ClassAvailableActivation>> classAvailableActivation, String pattern) {
        super(name, systemPropertyActivation, classAvailableActivation);
        this.pattern = pattern;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (this.getName() != null) {
            builder.append("name: ").append(getName());
        }
        if (this.getPattern() != null) {
            builder.append("pattern: ").append(pattern);
        }
        if (this.getClassAvailableActivations() != null) {
            builder.append(getClassAvailableActivations());
        }
        if (this.getSystemPropertyActivations() != null) {
            builder.append(getSystemPropertyActivations());
        }
        return builder.toString();
    }
}

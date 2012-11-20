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
package org.jboss.weld.bootstrap.enablement;

import java.util.List;

import javax.enterprise.inject.spi.ProcessModule;

import org.jboss.weld.bootstrap.spi.Metadata;

import com.google.common.collect.ImmutableList;

/**
 * Allows lists of interceptors, decorators and alternatives which are enabled in a given module to be manipulated by extensions
 * in the {@link ProcessModule} phase. After this phase, immutable {@link ModuleEnablement} is created.
 *
 * @author Jozef Hartinger
 *
 */
public class ModuleEnablementBuilder {

    private final List<ClassEnablement> interceptors;
    private final List<ClassEnablement> decorators;
    private final List<ClassEnablement> alternatives;

    public ModuleEnablementBuilder(List<ClassEnablement> interceptors, List<ClassEnablement> decorators,
            List<ClassEnablement> alternatives) {
        this.interceptors = interceptors;
        this.decorators = decorators;
        this.alternatives = alternatives;
    }

    public List<ClassEnablement> getInterceptors() {
        return interceptors;
    }

    public List<ClassEnablement> getDecorators() {
        return decorators;
    }

    public List<ClassEnablement> getAlternatives() {
        return alternatives;
    }

    public ModuleEnablement create() {
        return new ModuleEnablement(ImmutableList.copyOf(interceptors), ImmutableList.copyOf(decorators),
                ImmutableList.copyOf(alternatives));
    }
}

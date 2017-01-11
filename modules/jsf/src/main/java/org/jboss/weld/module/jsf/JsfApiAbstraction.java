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
package org.jboss.weld.module.jsf;

import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.ApiAbstraction;

/**
 * Utility class for JSF related components, concepts etc. It can also
 * report on the compatibility of the current JSF implementation being used.
 *
 * @author Pete Muir
 * @author Dan Allen
 */
public class JsfApiAbstraction extends ApiAbstraction implements Service {

    // JSF FacesContext
    public final Class<?> FACES_CONTEXT;

    public final Class<?> BEHAVIOR_CLASS;

    public final double MINIMUM_API_VERSION;

    private static final String FACES_CONTEXT_CLASS_NAME = "javax.faces.context.FacesContext";

    private static final String BEHAVIOR_CLASS_NAME = "javax.faces.component.behavior.Behavior";

    private static final double COMMON_VERSION = 2.0;
    private static final double OLDER_VERSION = 1.2;

    public JsfApiAbstraction(ResourceLoader resourceLoader) {
        super(resourceLoader);
        this.FACES_CONTEXT = classForName(FACES_CONTEXT_CLASS_NAME);
        this.BEHAVIOR_CLASS = classForName(BEHAVIOR_CLASS_NAME);
        if (this.BEHAVIOR_CLASS.getName().equals(BEHAVIOR_CLASS_NAME)) {
            MINIMUM_API_VERSION = COMMON_VERSION;
        } else {
            MINIMUM_API_VERSION = OLDER_VERSION;
        }
    }

    public boolean isApiVersionCompatibleWith(double version) {
        return MINIMUM_API_VERSION >= version;
    }

    public void cleanup() {
    }

}

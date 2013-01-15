/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.environment.jetty;

import org.jboss.weld.environment.AbstractContainer;
import org.jboss.weld.environment.ContainerContext;

/**
 * Abstract Jetty container.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractJettyContainer extends AbstractContainer {
    public static final String INJECTOR_ATTRIBUTE_NAME = "org.jboss.weld.environment.jetty.JettyWeldInjector";

    public void destroy(ContainerContext context) {
        context.getContext().removeAttribute(INJECTOR_ATTRIBUTE_NAME);
    }
}

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.weld.environment.osgi.api;

/**
 * <p>This enumeration lists the two new states of a bean bundle.</p>
 * <p>A bean bundle is in {@link BundleState#VALID} state if all its required
 * service dependencies are validated otherwise is in
 * {@link BundleState#INVALID} state. Every time a bean bundle goes from one
 * state to another a corresponding
 * {@link org.jboss.weld.environment.osgi.api.events.Valid} or
 * {@link org.jboss.weld.environment.osgi.api.events.Invalid} event may be
 * fired.</p>
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 * @see org.osgi.framework.Bundle
 * @see org.jboss.weld.environment.osgi.api.events.Valid
 * @see org.jboss.weld.environment.osgi.api.events.Invalid
 */
public enum BundleState {
    VALID,
    INVALID

}

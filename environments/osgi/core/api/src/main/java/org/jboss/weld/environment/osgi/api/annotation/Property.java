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
package org.jboss.weld.environment.osgi.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>This annotation wraps an OSGi service property used for automatic OSGi service
 * publishing.</p>
 * <p>It allows to specify:<ul>
 * <li>
 * <p>The name of the property, as a required
 * <code>String</code>,</p>
 * </li>
 * <li>
 * <p>The value of the property, as a required
 * {@link String}.</p>
 * </li>
 * </ul></p>
 * <p>It may be used within the {@link Publish} annotation to provide the
 * published service implementation properties.</p>
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 * @see Publish
 * @see org.jboss.weld.environment.osgi.api.Service
 * @see org.jboss.weld.environment.osgi.api.ServiceRegistry
 */
@Target(
        {
        })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Property {
    /**
     * The property name.
     *
     * @return the property name.
     */
    String name();

    /**
     * The property value.
     *
     * @return the property value.
     */
    String value();

}

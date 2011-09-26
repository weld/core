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

import static java.lang.annotation.ElementType.TYPE;

/**
 * <p>This annotation notices that this type is an OSGi service implementation and
 * should be automatically published in the OSGi service registry.</p>
 * <p>It allows to specify:<ul>
 * <li>
 * <p>The contract interfaces of implemented service, as an optional
 * array of {@link Class}es,</p>
 * </li>
 * <li>
 * <p>The properties of the published service implementation, as an
 * optional array of {@link Property},</p>
 * </li>
 * </ul></p>
 * <p>The published implementation might be discriminated using regular
 * {@link javax.inject.Qualifier} annotations or a LDAP filter with {@link Filter}
 * annotation.</p>
 * <p/>
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 * @see javax.inject.Qualifier
 * @see Filter
 * @see Property
 * @see org.jboss.weld.environment.osgi.api.Service
 * @see org.jboss.weld.environment.osgi.api.ServiceRegistry
 */
@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Publish {
    /**
     * The contracts the annotated class fulfills.
     *
     * @return the contracts of the annotated implementation as an array of interfaces.
     */
    Class[] contracts() default {};

    /**
     * The rank of the service to find the best available service on lookups.
     *
     * @return the rank of the service. Default is 0.
     */
    int rank() default 0;

}

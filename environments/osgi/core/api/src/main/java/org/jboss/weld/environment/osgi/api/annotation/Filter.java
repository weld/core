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

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

/**
 * <p>This annotation qualifies an injection point that represents a LDAP
 * filtered service.</p>
 * <p>It allows to specify the LDAP filter, as a required {@link String}.</p>
 * <p>It may be coupled with a {@link OSGiService} annotation in order to
 * filter the injected service implementations.
 * The LDAP filtering acts on {@link Qualifier} or {@link Properties}
 * annotations or regular OSGi LDAP properties used in service publishing.</p>
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 * @see Properties
 * @see Qualifier
 * @see OSGiService
 * @see org.jboss.weld.environment.osgi.api.Service
 * @see org.jboss.weld.environment.osgi.api.ServiceRegistry
 */
@Target(
        {
                TYPE, METHOD, PARAMETER, FIELD
        })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Qualifier
public @interface Filter {
    /**
     * The LDAP filter.
     *
     * @return the LDAP filter as a String.
     */
    String value();

}

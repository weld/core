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

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

/**
 * <p>This annotation qualifies an injection point that represents a service from
 * the OSGi service registry.</p>
 * <p>It may be use to obtain an injected OSGi service using
 * {@link org.jboss.weld.environment.osgi.api.Service} interface or directly the
 * service contract interface. The injected service might be filtered using
 * regular {@link Qualifier} annotations or a LDAP filter with {@link Filter}
 * annotation. It also might be mark as required for bundle running using
 * {@link Required} annotation.</p>
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 * @see Qualifier
 * @see Filter
 * @see Required
 * @see org.jboss.weld.environment.osgi.api.Service
 * @see org.jboss.weld.environment.osgi.api.ServiceRegistry
 */
@Target(
        {
                METHOD, PARAMETER, FIELD
        })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Qualifier
public @interface OSGiService {
    @Nonbinding int value() default 1;

}

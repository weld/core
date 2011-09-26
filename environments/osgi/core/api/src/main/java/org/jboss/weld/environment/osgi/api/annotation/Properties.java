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

import static java.lang.annotation.ElementType.TYPE;

/**
 * <p>This annotation qualifies a class that represents a LDAP filtered service.</p>
 * <p>It allows to specify multiple LDAP properties, as a array of {@link Property}.</p>
 * <p>It may be coupled with a {@link Publish} annotation in order to qualify
 * the published service implementations. The LDAP filtering acts on
 * {@link Qualifier} or {@link Properties} annotations or regular OSGi LDAP
 * properties used in service publishing.</p>
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 * @see Filter
 * @see Qualifier
 * @see Property
 * @see Publish
 * @see org.jboss.weld.environment.osgi.api.Service
 * @see org.jboss.weld.environment.osgi.api.ServiceRegistry
 */
@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Qualifier
public @interface Properties {
    /**
     * The properties of the annotated class as OSGi service properties (for LDAP
     * filtering).
     *
     * @return the properties of the service implementation as an array of
     *         {@link Property}.
     */
     Property[] value() default {};

}

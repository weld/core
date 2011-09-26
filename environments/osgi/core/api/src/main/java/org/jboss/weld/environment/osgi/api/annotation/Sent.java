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

import static java.lang.annotation.ElementType.PARAMETER;

/**
 * <p>This annotation qualifies an injection point that represents an
 * {@link org.jboss.weld.environment.osgi.api.events.InterBundleEvent} from
 * outside the current {@link org.osgi.framework.Bundle}.</p>
 * <p>It may be used in an {@link javax.enterprise.event.Observes} method to
 * restrict the listened
 * {@link org.jboss.weld.environment.osgi.api.events.InterBundleEvent}.
 * It allows to ignore the
 * {@link org.jboss.weld.environment.osgi.api.events.InterBundleEvent}
 * from within the current bundle.</p>
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 * @see Qualifier
 * @see org.jboss.weld.environment.osgi.api.events.InterBundleEvent
 * @see org.osgi.framework.Bundle
 */
@Target(
        {
                PARAMETER
        })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Qualifier
public @interface Sent {
}

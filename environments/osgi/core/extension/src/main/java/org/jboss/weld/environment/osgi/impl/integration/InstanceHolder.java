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
package org.jboss.weld.environment.osgi.impl.integration;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Inject;
import java.lang.annotation.Annotation;

/**
 * It allows a bean bundle to retrieve any of its bean using an {@link Instance}.
 * <p/>
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
public class InstanceHolder {

    @Inject
    @Any
    private Instance<Object> instance;

    public <T> Instance<T> select(Class<T> clazz, Annotation... annotations) {
        return instance.select(clazz, annotations);
    }

    public <T> Instance<T> select(TypeLiteral<T> type, Annotation... annotations) {
        return instance.select(type, annotations);
    }

}

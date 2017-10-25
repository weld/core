/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bean.builtin;

import java.util.Comparator;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Prioritized;

import org.jboss.weld.bean.ClassBean;
import org.jboss.weld.inject.WeldInstance;
import org.jboss.weld.inject.WeldInstance.Handler;
import org.jboss.weld.util.AnnotationApiAbstraction;

/**
 *
 * @author Martin Kouba
 * @see WeldInstance#getPriorityComparator()
 */
public class PriorityComparator implements Comparator<Handler<?>> {

    private final AnnotationApiAbstraction annotationApi;

    public PriorityComparator(AnnotationApiAbstraction annotationApi) {
        this.annotationApi = annotationApi;
    }

    @Override
    public int compare(Handler<?> h1, Handler<?> h2) {
        return Integer.compare(getPriority(h2), getPriority(h1));
    }

    private int getPriority(Handler<?> handler) {
        Bean<?> bean = handler.getBean();
        if (bean instanceof ClassBean) {
            ClassBean<?> classBean = (ClassBean<?>) bean;
            Object priority = classBean.getAnnotated().getAnnotation(annotationApi.PRIORITY_ANNOTATION_CLASS);
            if (priority != null) {
                return annotationApi.getPriority(priority);
            }
        } else if (bean instanceof Prioritized) {
            return ((Prioritized) bean).getPriority();
        }
        return 0;
    }

}
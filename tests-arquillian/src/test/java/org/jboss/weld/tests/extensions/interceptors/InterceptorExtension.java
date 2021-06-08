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
package org.jboss.weld.tests.extensions.interceptors;

import org.jboss.weld.test.util.annotated.TestAnnotatedTypeBuilder;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.lang.reflect.Method;

/**
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 */
public class InterceptorExtension implements Extension {
    /**
     * registers two interceptors via the SPI
     */
    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event, BeanManager beanManager) throws SecurityException, NoSuchMethodException {
        event.addInterceptorBinding(Incremented.class);
        event.addInterceptorBinding(FullMarathon.class);

        TestAnnotatedTypeBuilder<IncrementingInterceptor> incBuilder = new TestAnnotatedTypeBuilder<IncrementingInterceptor>(IncrementingInterceptor.class);
        incBuilder.addToClass(new InterceptorLiteral());
        incBuilder.addToClass(new IncrementedLiteral());

        Method around = IncrementingInterceptor.class.getMethod("doAround", InvocationContext.class);
        incBuilder.addToMethod(around, new AroundInvokeLiteral());
        event.addAnnotatedType(incBuilder.create(), IncrementingInterceptor.class.getSimpleName());

        TestAnnotatedTypeBuilder<LifecycleInterceptor> marBuilder = new TestAnnotatedTypeBuilder<LifecycleInterceptor>(LifecycleInterceptor.class);
        marBuilder.addToClass(new InterceptorLiteral());
        marBuilder.addToClass(new FullMarathonImpl());

        Method pre = LifecycleInterceptor.class.getMethod("preDestroy", InvocationContext.class);
        marBuilder.addToMethod(pre, new PreDestroyLiteral());

        Method post = LifecycleInterceptor.class.getMethod("postConstruct", InvocationContext.class);
        marBuilder.addToMethod(post, new PostConstructLiteral());

        event.addAnnotatedType(marBuilder.create(), LifecycleInterceptor.class.getSimpleName());
    }

    private static class InterceptorLiteral extends AnnotationLiteral<Interceptor> implements Interceptor {
    }

    private static class IncrementedLiteral extends AnnotationLiteral<Incremented> implements Incremented {
    }

    private static class AroundInvokeLiteral extends AnnotationLiteral<AroundInvoke> implements AroundInvoke {
    }

    private static class PreDestroyLiteral extends AnnotationLiteral<PreDestroy> implements PreDestroy {
    }

    private static class PostConstructLiteral extends AnnotationLiteral<PostConstruct> implements PostConstruct {
    }

    private static class FullMarathonImpl extends AnnotationLiteral<FullMarathon> implements FullMarathon {
    }
}

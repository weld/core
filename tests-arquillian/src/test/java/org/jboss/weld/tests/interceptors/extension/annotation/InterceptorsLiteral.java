package org.jboss.weld.tests.interceptors.extension.annotation;

import javax.enterprise.util.AnnotationLiteral;
import javax.interceptor.Interceptors;

@SuppressWarnings("all")
public class InterceptorsLiteral extends AnnotationLiteral<Interceptors> implements Interceptors {

    @Override
    @SuppressWarnings({ "rawtypes" })
    public Class[] value() {
        return new Class[] { BooInterceptor.class };
    }

}

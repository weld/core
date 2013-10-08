package org.jboss.weld.tests.interceptors.extension.annotation;

import static org.junit.Assert.assertEquals;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class InterceptorAnnotationExtensionTest {

    @Inject
    SimpleBean bean;

    @Deployment
    public static JavaArchive getDeployment() {
        return ShrinkWrap.create(BeanArchive.class).addClasses(SimpleBean.class, BooInterceptor.class, InterceptorsExtension.class, InterceptorsLiteral.class)
                .addAsServiceProvider(Extension.class, InterceptorsExtension.class);
    }


    @Test
    public void testInterceptorAddedByExtension() {
        assertEquals("intercepted", bean.simpleMethod());
    }
}

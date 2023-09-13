package org.jboss.weld.tests.proxy.superclass.unproxyable;

import static org.junit.Assert.assertEquals;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.proxy.superclass.NotSimpleConstructorClass;
import org.jboss.weld.tests.proxy.superclass.SimpleBean;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class GetInjectableReferenceUnproxyableCheckTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(GetInjectableReferenceUnproxyableCheckTest.class))
                .addPackage(GetInjectableReferenceUnproxyableCheckTest.class.getPackage())
                .addClasses(SimpleBean.class, NotSimpleConstructorClass.class);
    }

    @Test
    public void testClientProxy(BeanManager beanManager) {
        SimpleBean simple = (SimpleBean) beanManager.getInjectableReference(new InjectionPoint() {

            @Override
            public boolean isTransient() {
                return false;
            }

            @Override
            public boolean isDelegate() {
                return false;
            }

            @Override
            public Type getType() {
                return SimpleBean.class;
            }

            @Override
            public Set<Annotation> getQualifiers() {
                return Collections.emptySet();
            }

            @Override
            public Member getMember() {
                return null;
            }

            @Override
            public Bean<?> getBean() {
                return null;
            }

            @Override
            public Annotated getAnnotated() {
                return null;
            }
        }, beanManager.createCreationalContext(null));

        assertEquals("nothing", simple.getValue());
    }

}

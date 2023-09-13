package org.jboss.weld.tests.beanManager.predestroy;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.util.reflection.Reflections;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BeanPreDestroyTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(BeanPreDestroyTest.class))
                .addPackage(BeanPreDestroyTest.class.getPackage())
                .addClass(Reflections.class);
    }

    @Test
    public void destroyCalledWithBeanCreate(BeanManagerImpl beanManager) {
        SomeBean.destroyCalled = false;
        Set<Bean<?>> beans = beanManager.getBeans(SomeBean.class);
        Bean<SomeBean> bean = Reflections.cast(beanManager.resolve(beans));
        SomeBean instance = bean.create(null);
        bean.destroy(instance, null);
        assertTrue(SomeBean.destroyCalled);
    }

    @Test
    public void destroyCalledWithBeanManagerGetReference(BeanManagerImpl beanManager) {
        SomeBean.destroyCalled = false;
        Set<Bean<?>> beans = beanManager.getBeans(SomeBean.class);
        Bean<SomeBean> bean = Reflections.cast(beanManager.resolve(beans));
        CreationalContext<SomeBean> ctx = beanManager.createCreationalContext(bean);
        SomeBean instance = (SomeBean) beanManager.getReference(bean, SomeBean.class, ctx);
        bean.destroy(instance, ctx);
        assertTrue(SomeBean.destroyCalled);
    }
}

package org.jboss.weld.tests.autoclose.disposer;

import java.lang.annotation.Annotation;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.ActionSequence;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class AutoCloseDisposerTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(AutoCloseDisposerTest.class))
                .addPackage(AutoCloseDisposerTest.class.getPackage())
                .addClass(ActionSequence.class);
    }

    @Inject
    BeanManager beanManager;

    @Test
    public void testCloseCalledEvenWhenDisposerThrows() {
        ActionSequence.reset();
        createAndDestroy(CloseableResource.class, ThrowingDisposerQualifier.Literal.INSTANCE);
        ActionSequence.assertSequenceDataEquals("disposer.throwing", "throwingDisposer.close");
    }

    @SuppressWarnings("unchecked")
    private <T> void createAndDestroy(Class<T> type, Annotation... qualifiers) {
        Bean<T> bean = (Bean<T>) beanManager.resolve(beanManager.getBeans(type, qualifiers));
        CreationalContext<T> cc = beanManager.createCreationalContext(bean);
        T instance = bean.create(cc);
        bean.destroy(instance, cc);
    }
}

package org.jboss.weld.tests.autoclose.synthetic;

import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
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
public class AutoCloseSyntheticBeanTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(AutoCloseSyntheticBeanTest.class))
                .addPackage(AutoCloseSyntheticBeanTest.class.getPackage())
                .addAsServiceProvider(Extension.class, AutoClosePortableExtension.class)
                .addAsServiceProvider(BuildCompatibleExtension.class, AutoCloseBceExtension.class);
    }

    @Inject
    BeanManager beanManager;

    @Test
    public void testPortableExtensionAutoCloseFlag() {
        Bean<?> bean = beanManager.resolve(
                beanManager.getBeans(SyntheticCloseableResource.class, PortableExtQualifier.Literal.INSTANCE));
        assertTrue("Synthetic bean via portable extension should have isAutoClose() == true",
                bean.isAutoClose());
    }

    @Test
    public void testPortableExtensionAutoClose() {
        ActionSequence.reset();
        createAndDestroy(SyntheticCloseableResource.class, PortableExtQualifier.Literal.INSTANCE);
        ActionSequence.assertSequenceDataEquals("portableExt.close");
    }

    @Test
    public void testBceSyntheticBeanAutoCloseFlag() {
        Bean<?> bean = beanManager.resolve(
                beanManager.getBeans(SyntheticCloseableResource.class, BceQualifier.Literal.INSTANCE));
        assertTrue("Synthetic bean via BCE should have isAutoClose() == true",
                bean.isAutoClose());
    }

    @Test
    public void testBceSyntheticBeanAutoClose() {
        ActionSequence.reset();
        createAndDestroy(SyntheticCloseableResource.class, BceQualifier.Literal.INSTANCE);
        ActionSequence.assertSequenceDataEquals("bce.close");
    }

    @Test
    public void testSyntheticBeanDisposerCalledBeforeClose() {
        ActionSequence.reset();
        createAndDestroy(SyntheticCloseableResource.class, WithDisposerQualifier.Literal.INSTANCE);
        ActionSequence.assertSequenceDataEquals("syntheticDisposer", "withDisposer.close");
    }

    @SuppressWarnings("unchecked")
    private <T> void createAndDestroy(Class<T> type, Annotation... qualifiers) {
        Bean<T> bean = (Bean<T>) beanManager.resolve(beanManager.getBeans(type, qualifiers));
        CreationalContext<T> cc = beanManager.createCreationalContext(bean);
        T instance = bean.create(cc);
        bean.destroy(instance, cc);
    }
}

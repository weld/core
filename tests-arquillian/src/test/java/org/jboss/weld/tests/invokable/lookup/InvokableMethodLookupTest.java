package org.jboss.weld.tests.invokable.lookup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import jakarta.enterprise.inject.AmbiguousResolutionException;
import jakarta.enterprise.inject.UnsatisfiedResolutionException;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class InvokableMethodLookupTest {

    @Deployment
    public static Archive getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InvokableMethodLookupTest.class))
                .addPackage(InvokableMethodLookupTest.class.getPackage())
                .addAsServiceProvider(Extension.class, InvokerRegistreringExtension.class);
    }

    @Inject
    InvokerRegistreringExtension extension;

    @Inject
    @MyQualifier1("abc")
    InvokableBean bean;

    @Test
    public void testInstanceLookupWithQualifiers() {
        Object invokerResult = extension.getInstanceLookupInvoker().invoke(null, new Object[]{});
        assertTrue(invokerResult instanceof String);
        assertEquals(InvokableBean.class.getSimpleName(), invokerResult);
    }

    @Test
    public void testCorrectArqLookupWithQualifiers() {
        Object invokerResult = extension.getCorrectLookupInvoker().invoke(bean, new Object[]{null, null});
        assertTrue(invokerResult instanceof String);
        assertEquals(MyQualifier1.class.getSimpleName() + MyQualifier4.class.getSimpleName() + MyQualifier2.class.getSimpleName(), invokerResult);
    }

    @Test
    public void testLookupWithRegisteredQualifier() {
        Object invokerResult = extension.getLookupWithRegisteredQualifier().invoke(bean, new Object[]{null});
        assertTrue(invokerResult instanceof String);
        assertEquals(ToBeQualifier.class.getSimpleName(), invokerResult);
    }

    @Test
    public void testUnsatisfiedLookupWithQualifier() {
        try {
            Object invokerResult = extension.getUnsatisfiedLookupInvoker().invoke(bean, new Object[]{null});
            fail();
        } catch (UnsatisfiedResolutionException e) {
            // expected
        }
    }

    @Test
    public void testAmbigLookupWithQualifiers() {
        try {
            Object invokerResult = extension.getAmbiguousLookupInvoker().invoke(bean, new Object[]{null});
            fail();
        } catch (AmbiguousResolutionException e) {
            // expected
        }
    }
}

package org.jboss.weld.tests.bce.syntheticInjectionPoint.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.spi.BeanContainer;
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
public class BasicSyntheticInjectionPointTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(BasicSyntheticInjectionPointTest.class))
                .addPackage(BasicSyntheticInjectionPointTest.class.getPackage())
                .addAsServiceProvider(BuildCompatibleExtension.class,
                        BasicSyntheticInjectionPointExtension.class);
    }

    @Inject
    BeanContainer container;

    @Test
    public void testOldApi() {
        SyntheticResult result = (SyntheticResult) container.createInstance()
                .select(SyntheticResult.class, new OldApiQualifier() {
                    @Override
                    public Class<? extends java.lang.annotation.Annotation> annotationType() {
                        return OldApiQualifier.class;
                    }
                }).get();
        assertNotNull(result);
        assertEquals("old:alpha", result.getValue());
    }

    @Test
    public void testNewApiClassAnnotation() {
        SyntheticResult result = (SyntheticResult) container.createInstance()
                .select(SyntheticResult.class, new Scenario2aQualifier() {
                    @Override
                    public Class<? extends java.lang.annotation.Annotation> annotationType() {
                        return Scenario2aQualifier.class;
                    }
                }).get();
        assertNotNull(result);
        assertEquals("newClassAnnotation:alpha", result.getValue());
    }

    @Test
    public void testNewApiClassAnnotationInfoTwoQualifiers() {
        SyntheticResult result = (SyntheticResult) container.createInstance()
                .select(SyntheticResult.class, new Scenario2bQualifier() {
                    @Override
                    public Class<? extends java.lang.annotation.Annotation> annotationType() {
                        return Scenario2bQualifier.class;
                    }
                }).get();
        assertNotNull(result);
        assertEquals("newClassAnnotationInfo:charlie", result.getValue());
    }

    @Test
    public void testNewApiTypeAnnotation() {
        SyntheticResult result = (SyntheticResult) container.createInstance()
                .select(SyntheticResult.class, new Scenario3aQualifier() {
                    @Override
                    public Class<? extends java.lang.annotation.Annotation> annotationType() {
                        return Scenario3aQualifier.class;
                    }
                }).get();
        assertNotNull(result);
        assertEquals("newTypeAnnotation:bravo", result.getValue());
    }

    @Test
    public void testNewApiTypeAnnotationInfoTwoQualifiers() {
        SyntheticResult result = (SyntheticResult) container.createInstance()
                .select(SyntheticResult.class, new Scenario3bQualifier() {
                    @Override
                    public Class<? extends java.lang.annotation.Annotation> annotationType() {
                        return Scenario3bQualifier.class;
                    }
                }).get();
        assertNotNull(result);
        assertEquals("newTypeAnnotationInfo:charlie", result.getValue());
    }

    @Test
    public void testDependentInstanceCleanedUpOnSyntheticBeanDestruction() {
        DependentHelper.reset();
        assertEquals(0, DependentHelper.destroyedCounter.get());

        Instance<Object> instance = container.createInstance();
        Instance.Handle<SyntheticResult> handle = instance
                .select(SyntheticResult.class, DependentCleanupQualifier.Literal.INSTANCE)
                .getHandle();

        SyntheticResult result = handle.get();
        assertNotNull(result);
        assertEquals("dependentCleanup:dependent", result.getValue());

        // The DependentHelper obtained via SyntheticInjections.get() should
        // still be alive — the synthetic bean hasn't been destroyed yet
        assertEquals(0, DependentHelper.destroyedCounter.get());

        // Destroying the synthetic bean should also destroy the dependent helper
        handle.destroy();
        assertEquals(1, DependentHelper.destroyedCounter.get());
    }
}

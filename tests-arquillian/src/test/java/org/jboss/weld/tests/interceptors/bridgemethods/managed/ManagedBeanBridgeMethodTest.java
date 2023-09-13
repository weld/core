package org.jboss.weld.tests.interceptors.bridgemethods.managed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.interceptors.bridgemethods.common.BaseService;
import org.jboss.weld.tests.interceptors.bridgemethods.common.SomeInterceptor;
import org.jboss.weld.tests.interceptors.bridgemethods.common.SpecialService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(Arquillian.class)
public class ManagedBeanBridgeMethodTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ManagedBeanBridgeMethodTest.class))
                .intercept(SomeInterceptor.class)
                .addPackage(BaseService.class.getPackage())
                .addPackage(ManagedBeanBridgeMethodTest.class.getPackage());
    }

    @SuppressWarnings("rawtypes")
    private BaseService baseService;

    @Inject
    private BaseService<String> stringBaseService;

    @Inject
    private SpecialService specialService;

    @Inject
    private ManagedSpecialServiceImpl managedSpecialServiceImpl;

    @Inject
    public void init(BaseService<?> baseService) {
        this.baseService = baseService;
    }

    @Before
    public void setUp() throws Exception {
        SomeInterceptor.invocationCount = 0;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBaseServiceDoSomething() {
        baseService.doSomething("foo");
        assertEquals(1, SomeInterceptor.invocationCount);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBaseServiceDoSomethingWithInvalidArgumentType() {
        try {
            baseService.doSomething(Boolean.TRUE);
            fail("Expected ClassCastException");
        } catch (ClassCastException e) {
        }
        assertEquals("ClassCastException should be thrown before interceptor is invoked", 0, SomeInterceptor.invocationCount);
    }

    @Test
    public void testBaseServiceReturnSomething() {
        baseService.returnSomething();
        assertEquals(1, SomeInterceptor.invocationCount);
    }

    @Test
    public void testStringBaseServiceDoSomething() {
        stringBaseService.doSomething("foo");
        assertEquals(1, SomeInterceptor.invocationCount);
    }

    @Test
    public void testStringBaseServiceReturnSomething() {
        stringBaseService.returnSomething();
        assertEquals(1, SomeInterceptor.invocationCount);
    }

    @Test
    public void testSpecialServiceDoSomething() {
        specialService.doSomething("foo");
        assertEquals(1, SomeInterceptor.invocationCount);
    }

    @Test
    public void testSpecialServiceReturnSomething() {
        specialService.returnSomething();
        assertEquals(1, SomeInterceptor.invocationCount);
    }

    @Test
    public void testManagedSpecialServiceImplDoSomething() {
        managedSpecialServiceImpl.doSomething("foo");
        assertEquals(1, SomeInterceptor.invocationCount);
    }

    @Test
    public void testManagedSpecialServiceImplReturnSomething() {
        managedSpecialServiceImpl.returnSomething();
        assertEquals(1, SomeInterceptor.invocationCount);
    }

}

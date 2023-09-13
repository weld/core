package org.jboss.weld.tests.interceptors.bridgemethods.ejb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import jakarta.ejb.EJBException;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.jboss.weld.tests.interceptors.bridgemethods.common.BaseService;
import org.jboss.weld.tests.interceptors.bridgemethods.common.SomeInterceptor;
import org.jboss.weld.tests.interceptors.bridgemethods.common.SpecialService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 *
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class EJBBridgeMethodTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(EJBBridgeMethodTest.class))
                .intercept(SomeInterceptor.class)
                .addPackage(BaseService.class.getPackage())
                .addPackage(EJBBridgeMethodTest.class.getPackage());
    }

    @Inject
    private SpecialService specialService;

    @Inject
    private BaseService<String> stringBaseService;

    @SuppressWarnings("rawtypes")
    private BaseService baseService;

    @Inject
    public void init(BaseService<?> baseService) {
        this.baseService = baseService;
    }

    @Before
    public void setUp() throws Exception {
        SomeInterceptor.invocationCount = 0;
    }

    @Test
    public void testSpecialService() {
        specialService.doSomething("foo");
        assertEquals(1, SomeInterceptor.invocationCount);
    }

    @Test
    public void testStringBaseService() {
        stringBaseService.doSomething("foo");
        assertEquals(1, SomeInterceptor.invocationCount);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBaseService() {
        baseService.doSomething("foo");
        assertEquals(1, SomeInterceptor.invocationCount);
    }

    @SuppressWarnings("unchecked")
    @Ignore
    @Test
    public void testBaseServiceWithInvalidArgumentType() {
        try {
            baseService.doSomething(Boolean.TRUE);
            fail("Expected ClassCastException");
        } catch (ClassCastException e) {
        } catch (EJBException e) {
            if (!(e.getCause() instanceof ClassCastException)) {
                throw e;
            }
        }
        assertEquals("ClassCastException should be thrown before interceptor is invoked", 0, SomeInterceptor.invocationCount);
    }

    @Test
    public void testSpecialServiceDoSomethingElse() {
        specialService.returnSomething();
        assertEquals(1, SomeInterceptor.invocationCount);
    }

    @Test
    public void testBaseServiceDoSomethingElse() {
        baseService.returnSomething();
        assertEquals(1, SomeInterceptor.invocationCount);
    }

}

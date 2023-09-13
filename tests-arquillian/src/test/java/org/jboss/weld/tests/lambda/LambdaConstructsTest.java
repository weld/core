package org.jboss.weld.tests.lambda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Method;
import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 * @see WELD-1644
 */
@RunWith(Arquillian.class)
public class LambdaConstructsTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(LambdaConstructsTest.class))
                .addPackage(LambdaConstructsTest.class.getPackage());
    }

    @Test
    public void testLambda(LambdaBean lambdaBean) {
        List<Integer> data = lambdaBean.ping();
        assertEquals(3, data.size());
        assertEquals(Integer.valueOf(1), data.get(0));
        assertEquals(Integer.valueOf(5), data.get(1));
        assertEquals(Integer.valueOf(10), data.get(2));
    }

    @Test
    public void testLambdaStream(LambdaBean lambdaBean) {
        List<String> data = lambdaBean.pingStream();
        assertEquals(3, data.size());
        assertEquals("1", data.get(0));
        assertEquals("5", data.get(1));
        assertEquals("10", data.get(2));
    }

    @Test
    public void testLambdaInstanceMethod(LambdaBean lambdaBean) {
        List<Integer> data = lambdaBean.lambdaAsInstanceMethod();
        assertEquals(1, data.size());
        assertEquals(Integer.valueOf(10), data.get(0));
    }

    @Test
    public void testLambdaInstanceMethod02(LambdaBean lambdaBean) {
        List<Integer> data = lambdaBean.lambdaAsInstanceMethod02();
        assertEquals(3, data.size());
        assertNull(data.get(0));
        assertNull(data.get(1));
        assertNull(data.get(2));
    }

    @Test
    public void testGenericReturnType() throws NoSuchMethodException, SecurityException {
        Class<LambdaBean> clazz = LambdaBean.class;
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isSynthetic()) {
                assertNotNull("Null generic return type: " + method.getName(), method.getGenericReturnType());
            }
        }
    }

}

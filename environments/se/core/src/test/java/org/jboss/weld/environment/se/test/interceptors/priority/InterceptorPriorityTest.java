package org.jboss.weld.environment.se.test.interceptors.priority;

import static org.junit.Assert.assertTrue;

import org.jboss.weld.environment.se.test.WeldSETest;
import org.junit.Test;

public class InterceptorPriorityTest extends WeldSETest {

    @Test
    public void testInterceptorActivatedByPriority() throws Exception {
        container.instance().select(SimpleBean.class).get().simpleMethod();
        assertTrue(SimpleInterceptor.intercepted);
    }

}

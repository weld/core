package org.jboss.weld.environment.servlet.test.injection;


import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import javax.enterprise.inject.spi.BeanManager;

import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LookupTestBase {

    public static WebArchive deployment() {
        return baseDeployment().addClasses(Mouse.class, Vole.class, LookupTestBase.class);
    }

    @Test
    public void testManagerInJndi(Mouse mouse, BeanManager beanManager) throws Exception {
        assertNotNull(mouse.getManager());
        assertEquals(mouse.getManager(), beanManager);
    }

    @Test
    public void testResource(Vole vole, BeanManager beanManager) throws Exception {
        assertNotNull(vole.getManager());
        assertEquals(vole.getManager(), beanManager);
    }

}

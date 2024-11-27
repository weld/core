package org.jboss.weld.environment.se.test.instance;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Mark Proctor
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SimpleInstanceTest {
    @Test
    public void testSelect() throws Exception {
        Weld weld = new Weld();
        try {
            WeldContainer wc = weld.initialize();
            Assert.assertNotNull(wc.select(KPT.class).select(new KPQLiteral()).get());
            Assert.assertNotNull(wc.select().select(KPT.class, new KPQLiteral()).get());
        } finally {
            weld.shutdown();
        }
    }
}

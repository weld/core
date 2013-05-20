package org.jboss.weld.environment.se.test;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.After;
import org.junit.Before;

public abstract class WeldSETest {

    protected Weld weld;

    protected WeldContainer container;

    @Before
    public void init() {
        weld = new Weld();
        container = weld.initialize();
    }

    @After
    public void destroy() {
        weld.shutdown();
    }
}

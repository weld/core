package org.jboss.weld.environment.jetty;

import org.jboss.weld.environment.servlet.inject.AbstractInjector;
import org.jboss.weld.manager.api.WeldManager;

/**
 * @author <a href="mailto:matija.mazi@gmail.com">Matija Mazi</a>
 */
public class JettyWeldInjector extends AbstractInjector {
    public JettyWeldInjector(WeldManager manager) {
        super(manager);
    }

    public void inject(Object targetInstance) {
        super.inject(targetInstance);
    }
}

package org.jboss.weld.environment.osgi.ee;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jboss.weld.environment.osgi.impl.extension.service.WeldOSGiExtension;
import org.osgi.framework.BundleContext;

@Singleton
@Startup
public class BundleContextAccessor {

    @Inject
    private WeldOSGiExtension ext;

    @Resource
    private BundleContext ctx;

    @PostConstruct
    public void start() {
        BundleContext bc = null;
        try {
            Context context = new InitialContext();
            bc = (BundleContext) context.lookup("java:comp/BundleContext");
        } catch (NamingException ex) {
        }
        if (bc == null) {
            if (ctx != null) {
                ext.startHybridMode(ctx);
            } else {
                throw new RuntimeException("Can't start Weld-OSGi in hybrid mode.");
            }
        } else {
            ext.startHybridMode();
        }
    }
}

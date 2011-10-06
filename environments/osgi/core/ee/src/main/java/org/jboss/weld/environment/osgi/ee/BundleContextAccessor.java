/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.weld.environment.osgi.ee;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jboss.weld.environment.osgi.impl.extension.beans.BundleHolder;
import org.jboss.weld.environment.osgi.impl.extension.beans.ServiceRegistryImpl;
import org.jboss.weld.environment.osgi.impl.extension.service.WeldOSGiExtension;
import org.osgi.framework.BundleContext;

@Singleton
@Startup
public class BundleContextAccessor {

    @Inject
    private WeldOSGiExtension ext;
    @Resource
    private BundleContext ctx;
    @Inject
    private BundleHolder holder;
    @Inject
    private ServiceRegistryImpl sr;

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
            ext.startHybridMode(bc);
        }
        holder.setBundle(ctx.getBundle());
        holder.setContext(ctx);
        sr.listenStartup(null);
    }
}

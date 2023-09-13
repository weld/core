/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.module.ejb;

import java.lang.annotation.Annotation;

import org.jboss.weld.bootstrap.ContextHolder;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.context.ejb.EjbLiteral;
import org.jboss.weld.context.ejb.EjbRequestContext;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.injection.ResourceInjectionFactory;
import org.jboss.weld.module.EjbSupport;
import org.jboss.weld.module.WeldModule;
import org.jboss.weld.module.ejb.context.EjbRequestContextImpl;
import org.jboss.weld.util.Bindings;
import org.jboss.weld.util.collections.ImmutableSet;

/**
 * Module that provides EJB integration
 *
 * @author Jozef Hartinger
 *
 */

public class WeldEjbModule implements WeldModule {
    @Override
    public String getName() {
        return "weld-ejb";
    }

    @Override
    public void postServiceRegistration(PostServiceRegistrationContext ctx) {
        ctx.getServices().add(CurrentInvocationInjectionPoint.class, new CurrentInvocationInjectionPoint());
        ctx.registerPlugableValidator(new WeldEjbValidator());
        ctx.getServices().get(ResourceInjectionFactory.class)
                .addResourceInjectionProcessor(new EjbResourceInjectionProcessor());
    }

    @Override
    public void postContextRegistration(PostContextRegistrationContext ctx) {
        // Register the EJB Request context
        ctx.addContext(new ContextHolder<EjbRequestContext>(new EjbRequestContextImpl(ctx.getContextId()),
                EjbRequestContext.class,
                ImmutableSet.<Annotation> builder().addAll(Bindings.DEFAULT_QUALIFIERS).add(EjbLiteral.INSTANCE).build()));
    }

    @Override
    public void postBeanArchiveServiceRegistration(PostBeanArchiveServiceRegistrationContext ctx) {
        final ServiceRegistry services = ctx.getServices();
        final EjbServices ejbServices = services.get(EjbServices.class);
        if (ejbServices != null) {
            // Must populate EJB cache first, as we need it to detect whether a
            // bean is an EJB!
            services.add(EjbSupport.class, new EjbSupportImpl(ejbServices, ctx.getBeanDeploymentArchive().getEjbs()));
        }
    }

    @Override
    public void preBeanRegistration(PreBeanRegistrationContext ctx) {
        ctx.registerBean(new SessionBeanAwareInjectionPointBean(ctx.getBeanManager()));
    }
}

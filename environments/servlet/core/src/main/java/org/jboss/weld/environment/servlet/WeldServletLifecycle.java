/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.weld.environment.servlet;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import javax.el.ELContextListener;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.servlet.ServletContext;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;

import org.jboss.logging.Logger;
import org.jboss.weld.bootstrap.api.CDI11Bootstrap;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.bootstrap.api.TypeDiscoveryConfiguration;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.CDI11Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.environment.Container;
import org.jboss.weld.environment.ContainerContext;
import org.jboss.weld.environment.deployment.WeldBeanDeploymentArchive;
import org.jboss.weld.environment.deployment.WeldDeployment;
import org.jboss.weld.environment.deployment.WeldResourceLoader;
import org.jboss.weld.environment.deployment.discovery.DiscoveryStrategy;
import org.jboss.weld.environment.deployment.discovery.DiscoveryStrategyFactory;
import org.jboss.weld.environment.gwtdev.GwtDevHostedModeContainer;
import org.jboss.weld.environment.jetty.JettyContainer;
import org.jboss.weld.environment.servlet.deployment.ServletContextBeanArchiveHandler;
import org.jboss.weld.environment.servlet.deployment.WebAppBeanArchiveScanner;
import org.jboss.weld.environment.servlet.services.ServletResourceInjectionServices;
import org.jboss.weld.environment.servlet.util.Reflections;
import org.jboss.weld.environment.servlet.util.ServiceLoader;
import org.jboss.weld.environment.tomcat.TomcatContainer;
import org.jboss.weld.injection.spi.ResourceInjectionServices;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.resources.spi.ClassFileServices;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.servlet.api.ServletListener;

/**
 *
 * @author Martin Kouba
 * @see Listener
 * @see EnhancedListener
 */
public class WeldServletLifecycle {

    public static final String BEAN_MANAGER_ATTRIBUTE_NAME = WeldServletLifecycle.class.getPackage().getName() + "." + BeanManager.class.getName();

    /**
     * Must be synchronized with org.jboss.weld.Container.CONTEXT_ID_KEY
     */
    private static final String CONTEXT_ID_KEY = "WELD_CONTEXT_ID_KEY";

    static final String INSTANCE_ATTRIBUTE_NAME = WeldServletLifecycle.class.getPackage().getName() + ".lifecycleInstance";

    private static final Logger log = Logger.getLogger(WeldServletLifecycle.class);

    private static final String BOOTSTRAP_IMPL_CLASS_NAME = "org.jboss.weld.bootstrap.WeldBootstrap";

    private static final String WELD_LISTENER_CLASS_NAME = "org.jboss.weld.servlet.WeldInitialListener";

    private static final String EXPRESSION_FACTORY_NAME = "org.jboss.weld.el.ExpressionFactory";

    private static final String CONTEXT_PARAM_ARCHIVE_ISOLATION = WeldServletLifecycle.class.getPackage().getName() + ".archive.isolation";

    private final transient CDI11Bootstrap bootstrap;

    private final transient ServletListener weldListener;

    private Container container;

    // WELD-1665 Bootstrap might be already performed
    private boolean isBootstrapNeeded = true;

    WeldServletLifecycle() {
        try {
            bootstrap = Reflections.newInstance(BOOTSTRAP_IMPL_CLASS_NAME);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Error loading Weld bootstrap, check that Weld is on the classpath", e);
        }
        try {
            weldListener = Reflections.newInstance(WELD_LISTENER_CLASS_NAME);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Error loading Weld listener, check that Weld is on the classpath", e);
        }
    }

    void initialize(ServletContext context) {

        WeldManager manager = (WeldManager) context.getAttribute(BEAN_MANAGER_ATTRIBUTE_NAME);
        if (manager != null) {
            isBootstrapNeeded = false;
        }

        if (isBootstrapNeeded) {
            CDI11Deployment deployment = createDeployment(context, bootstrap);
            ResourceInjectionServices resourceInjectionServices = new ServletResourceInjectionServices() {
            };
            try {
                for (BeanDeploymentArchive archive : deployment.getBeanDeploymentArchives()) {
                    archive.getServices().add(ResourceInjectionServices.class, resourceInjectionServices);
                }
            } catch (NoClassDefFoundError e) {
                // Support GAE
                log.warn("@Resource injection not available in simple beans");
            }
            String id = context.getInitParameter(CONTEXT_ID_KEY);
            if (id != null) {
                bootstrap.startContainer(id, Environments.SERVLET, deployment);
            } else {
                bootstrap.startContainer(Environments.SERVLET, deployment);
            }
            bootstrap.startInitialization();

            /*
             * This should work fine as all bean archives share the same classloader. The only difference this can make is per-BDA (CDI 1.0 style) enablement of
             * alternatives, interceptors and decorators. Nothing we can do about that.
             */
            manager = bootstrap.getManager(deployment.getBeanDeploymentArchives().iterator().next());

            // Push the manager into the servlet context so we can access in JSF
            context.setAttribute(BEAN_MANAGER_ATTRIBUTE_NAME, manager);
        }

        ContainerContext containerContext = new ContainerContext(context, manager);
        StringBuilder dump = new StringBuilder();
        Container container = findContainer(containerContext, dump);
        if (container == null) {
            log.info("No supported servlet container detected, CDI injection will NOT be available in Servlets, Filters or Listeners");
            log.debugv("Exception dump from Container lookup: {0}", dump);
        } else {
            container.initialize(containerContext);
            this.container = container;
        }

        if (JspFactory.getDefaultFactory() != null) {
            JspApplicationContext jspApplicationContext = JspFactory.getDefaultFactory().getJspApplicationContext(context);

            // Register the ELResolver with JSP
            jspApplicationContext.addELResolver(manager.getELResolver());

            // Register ELContextListener with JSP
            try {
                jspApplicationContext.addELContextListener(Reflections.<ELContextListener> newInstance("org.jboss.weld.el.WeldELContextListener"));
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Error loading Weld ELContext Listener, check that Weld is on the classpath", e);
            }

            // Push the wrapped expression factory into the servlet context so that Tomcat or Jetty can hook it in using a container code
            context.setAttribute(EXPRESSION_FACTORY_NAME, manager.wrapExpressionFactory(jspApplicationContext.getExpressionFactory()));
        }

        if (isBootstrapNeeded) {
            bootstrap.deployBeans().validateBeans().endInitialization();
        }
    }

    void destroy(ServletContext context) {

        if (isBootstrapNeeded) {
            // Shutdown only if bootstrap not skipped
            bootstrap.shutdown();
        }

        if (container != null) {
            container.destroy(new ContainerContext(context, null));
        }
    }

    /**
     *
     * @return the original Weld listener all notifications should be delegated to
     */
    ServletListener getWeldListener() {
        return weldListener;
    }

    /**
     * Create servlet deployment.
     *
     * Can be overridden with custom servlet deployment. e.g. exact resources listing in restricted env like GAE
     *
     * @param context the servlet context
     * @param bootstrap the bootstrap
     * @return new servlet deployment
     */
    protected CDI11Deployment createDeployment(ServletContext context, CDI11Bootstrap bootstrap) {

        ResourceLoader resourceLoader = new WeldResourceLoader();

        final Iterable<Metadata<Extension>> extensions = bootstrap.loadExtensions(WeldResourceLoader.getClassLoader());
        final TypeDiscoveryConfiguration typeDiscoveryConfiguration = bootstrap.startExtensions(extensions);

        DiscoveryStrategy strategy = DiscoveryStrategyFactory.create(resourceLoader, bootstrap, typeDiscoveryConfiguration, true);
        strategy.registerHandler(new ServletContextBeanArchiveHandler(context));

        strategy.setScanner(new WebAppBeanArchiveScanner(resourceLoader, bootstrap, context));
        Set<WeldBeanDeploymentArchive> beanDeploymentArchives = strategy.performDiscovery();

        String isolation = context.getInitParameter(CONTEXT_PARAM_ARCHIVE_ISOLATION);

        if (isolation != null && Boolean.valueOf(isolation).equals(Boolean.FALSE)) {
            log.debug("Archive isolation disabled - only one bean archive will be created");
            beanDeploymentArchives = Collections.singleton(WeldBeanDeploymentArchive.merge(bootstrap, beanDeploymentArchives));
        } else {
            log.debug("Archive isolation enabled - creating multiple isolated bean archives if needed");
        }

        CDI11Deployment deployment = new WeldDeployment(resourceLoader, bootstrap, beanDeploymentArchives, extensions);

        if (strategy.getClassFileServices() != null) {
            deployment.getServices().add(ClassFileServices.class, strategy.getClassFileServices());
        }
        return deployment;
    }

    /**
     * Find container env.
     *
     * @param ctx the container context
     * @param dump the exception dump
     * @return valid container or null
     */
    protected Container findContainer(ContainerContext ctx, StringBuilder dump) {
        Container container = null;
        // 1. Custom container class
        String containerClass = ctx.getServletContext().getInitParameter(Container.CONTEXT_PARAM_CONTAINER_CLASS);
        if (containerClass != null) {
            try {
                container = Reflections.newInstance(containerClass);
                log.info("Container detection skipped - custom container class loaded: " + containerClass);
            } catch (IllegalArgumentException e) {
                log.warn("Unable to instantiate custom container class: " + containerClass);
            }
        }
        if (container == null) {
            // 2. Service providers
            Iterable<Container> extContainers = ServiceLoader.load(Container.class, getClass().getClassLoader());
            container = checkContainers(ctx, dump, extContainers);
            if (container == null) {
                // 3. Built-in containers in predefined order
                container = checkContainers(ctx, dump, Arrays.asList(TomcatContainer.INSTANCE, JettyContainer.INSTANCE, GwtDevHostedModeContainer.INSTANCE));
            }
        }
        return container;
    }

    protected Container checkContainers(ContainerContext cc, StringBuilder dump, Iterable<Container> containers) {
        for (Container c : containers) {
            try {
                if (c.touch(cc)) {
                    return c;
                }
            } catch (Throwable t) {
                dump.append(c).append("->").append(t.getMessage()).append("\n");
            }
        }
        return null;
    }

}

package org.jboss.weld.environment.se.discovery;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.Deployment;

/**
 * Implements the basic requirements of a {@link Deployment}. Provides a service
 * registry.
 * 
 * Suitable for extension by those who need to build custom {@link Deployment}
 * implementations.
 * 
 * @author Pete Muir
 * 
 */
public abstract class AbstractWeldSEDeployment implements Deployment
{

   public static final String[] RESOURCES = { "META-INF/beans.xml" };

   private final ServiceRegistry serviceRegistry;

   public AbstractWeldSEDeployment()
   {
      this.serviceRegistry = new SimpleServiceRegistry();
   }

   public ServiceRegistry getServices()
   {
      return serviceRegistry;
   }

}
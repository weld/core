package org.jboss.weld.environment.se.discovery;

import java.util.Collection;
import java.util.Collections;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.ejb.spi.EjbDescriptor;

/**
 * Implements the basic requirements of a {@link BeanDeploymentArchive} (bean
 * archive id and service registry).
 * 
 * Suitable for extension by those who need to build custom
 * {@link BeanDeploymentArchive} implementations.
 * 
 * @see MutableBeanDeploymentArchive
 * @see ImmutableBeanDeploymentArchive
 * 
 * @author Pete Muir
 * 
 */
public abstract class AbstractWeldSEBeanDeploymentArchive implements BeanDeploymentArchive
{

   private final ServiceRegistry serviceRegistry;
   private final String id;

   public AbstractWeldSEBeanDeploymentArchive(String id)
   {
      this.id = id;
      this.serviceRegistry = new SimpleServiceRegistry();
   }

   public Collection<EjbDescriptor<?>> getEjbs()
   {
      return Collections.emptyList();
   }

   public String getId()
   {
      return id;
   }

   public ServiceRegistry getServices()
   {
      return serviceRegistry;
   }

}
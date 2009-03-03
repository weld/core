package org.jboss.webbeans.bootstrap.api;

import javax.inject.manager.Manager;

import org.jboss.webbeans.bootstrap.spi.EjbDiscovery;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.ejb.spi.EjbResolver;
import org.jboss.webbeans.resources.spi.NamingContext;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.transaction.spi.TransactionServices;

/**
 * Bootstrap API for Web Beans.
 * 
 * @author Pete Muir
 * 
 */
public interface Bootstrap
{
   
   /**
    * Set the Web Bean Discovery to use
    * 
    * @param webBeanDiscovery
    */
   public void setWebBeanDiscovery(WebBeanDiscovery webBeanDiscovery);
   
   /**
    * Set the EjbDiscovery to use
    * 
    * @param ejbDiscovery
    */
   public void setEjbDiscovery(EjbDiscovery ejbDiscovery);
   
   /**
    * Set the EjbResolver to use
    * 
    * @param ejbResolver
    */
   public void setEjbResolver(EjbResolver ejbResolver);
   
   /**
    * Set the NamingContext to use.
    * 
    * By default @{link org.jboss.webbeans.resources.DefaultNamingContext} will 
    * be used
    * 
    * @param namingContext
    */
   public void setNamingContext(NamingContext namingContext);
   
   /**
    * Set the ResourceLoader to use. By default @{link
    * org.jboss.webbeans.resources.DefaultResourceLoader} will be used
    * 
    * @param resourceLoader
    */
   public void setResourceLoader(ResourceLoader resourceLoader);
   
   public void setApplicationContext(BeanStore beanStore);
   
   /**
    * Set the transaction services provider to use.
    * 
    * @param transactionServices An implementation of TransactionService
    */
   public void setTransactionServices(TransactionServices transactionServices);
   
   /**
    * Initialize the bootstrap:
    * <ul>
    *   <li>Create the manager and bind it to JNDI</li>
    * </ul>
    */
   public void initialize();
   
   /**
    * Get the manager being used for bootstrap.
    * 
    * @return the manager. Unless {@link #initialize()} has been called, this
    *         method will return null.
    */
   public Manager getManager();
   
   /**
    * Starts the boot process.
    * 
    * Discovers the beans and registers them with the getManager(). Also
    * resolves the injection points. Before running {@link #boot()} the contexts
    * should be available
    * 
    */
   public void boot();
   
   /**
    * Causes the container to clean up and shutdown
    * 
    */
   public void shutdown();
   
}

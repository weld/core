package org.jboss.shrinkwrap.api;
 
import org.jboss.shrinkwrap.api.spec.JavaArchive;
 
public interface BeanArchive extends JavaArchive       
{
   
   /**
    * Adds Decorators to the beans.xml.
    * @param classes
    * @return
    */
   BeanArchive decorate(Class<?>... classes);
   
   /**
    * Adds Interceptors to the beans.xml.
    * 
    * @param classes
    * @return
    */
   BeanArchive intercept(Class<?>... classes);
   
   /**
    * Adds Alternatives to the beans.xml.
    * 
    * @param classes
    * @return
    */
   BeanArchive alternate(Class<?>... classes);
   
   /**
    * Adds a Stereotype Alternative to beans.xml.
    * 
    * @param classes
    * @return
    */
   BeanArchive stereotype(Class<?>... classes);
}
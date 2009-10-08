package org.jboss.weld.atinject.tck;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import junit.framework.Test;

import org.atinject.tck.Tck;
import org.atinject.tck.auto.Car;
import org.atinject.tck.auto.Convertible;
import org.atinject.tck.auto.FuelTank;
import org.atinject.tck.auto.Seat;
import org.atinject.tck.auto.Tire;
import org.atinject.tck.auto.V8Engine;
import org.atinject.tck.auto.accessories.Cupholder;
import org.atinject.tck.auto.accessories.SpareTire;
import org.jboss.weld.mock.MockEELifecycle;
import org.jboss.weld.mock.TestContainer;

/**
 * Configure the AtInject TCK for use with the 299 RI
 * 
 * @author pmuir
 *
 */
public class AtInjectTCK
{
   /**
    * The classes that should be deployed as Managed Beans
    */
   public static final List<Class<?>> classes = Arrays.<Class<?>>asList(
         Convertible.class,
         Seat.class,
         V8Engine.class,
         Cupholder.class,
         FuelTank.class,
         Tire.class,
         SpareTire.class,
         // Two producer method which allow us to expose SpareTire and Drivers seat with qualifiers
         DriversSeatProducer.class,
         SpareTireProducer.class
      );
   
   /**
    * Create JUnit TestSuite
    * 
    * @return
    */
   public static Test suite()
   {
      // Create and start the TestContainer, which takes care of starting the container, deploying the
      // classes, starting the contexts etc.
      TestContainer container = new TestContainer(new MockEELifecycle(), classes, null);
      container.startContainer();
      
      BeanManager beanManager = container.getBeanManager();
      
      // Obtain a reference to the Car and pass it to the TCK to generate the testsuite
      Bean<?> bean = beanManager.resolve(beanManager.getBeans(Car.class));
      Car instance = (Car) beanManager.getReference(bean, Car.class, beanManager.createCreationalContext(bean));
      
      return Tck.testsFor(instance, false /* supportsStatic */, true /* supportsPrivate */);
   }
}
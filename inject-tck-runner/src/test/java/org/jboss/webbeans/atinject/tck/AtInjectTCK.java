package org.jboss.webbeans.atinject.tck;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import junit.framework.Test;

import org.atinject.tck.Tck;
import org.atinject.tck.auto.Car;
import org.atinject.tck.auto.Convertible;
import org.atinject.tck.auto.DriversSeat;
import org.atinject.tck.auto.FuelTank;
import org.atinject.tck.auto.V8Engine;
import org.atinject.tck.auto.accessories.Cupholder;
import org.atinject.tck.auto.accessories.SpareTire;
import org.jboss.webbeans.mock.MockEELifecycle;
import org.jboss.webbeans.mock.TestContainer;

public class AtInjectTCK
{
   
   private static final List<Class<?>> classes = Arrays.<Class<?>>asList(Convertible.class, DriversSeat.class, DriversSeatProducer.class, V8Engine.class, Cupholder.class, FuelTank.class, SpareTireProducer.class, SpareTire.class);
   
   public static Test suite()
   {
      TestContainer container = new TestContainer(new MockEELifecycle(), classes, null);
      container.startContainer();
      BeanManager beanManager = container.getBeanManager();
      Bean<?> bean = beanManager.resolve(beanManager.getBeans(Car.class));
      Car instance = (Car) beanManager.getReference(bean, Car.class, beanManager.createCreationalContext(bean));
      return Tck.testsFor(instance, false /* supportsStatic */, false /* supportsPrivate */);
   }
}
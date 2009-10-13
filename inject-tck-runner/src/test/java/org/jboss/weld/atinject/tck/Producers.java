package org.jboss.weld.atinject.tck;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.inject.BeanTypes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;

import org.atinject.tck.auto.Drivers;
import org.atinject.tck.auto.DriversSeat;
import org.atinject.tck.auto.Seat;
import org.atinject.tck.auto.Tire;
import org.atinject.tck.auto.accessories.SpareTire;
import org.jboss.weld.atinject.tck.util.NonContextual;

/**
 * Producer methods for the @Inject TCK beans we need greater control over
 * 
 * @author pmuir
 *
 */
public class Producers
{
   
   private final NonContextual<SpareTire> spareTire;
   private final NonContextual<DriversSeat> driversSeat;
   
   @Inject
   public Producers(BeanManager beanManager)
   {
      this.spareTire = new NonContextual<SpareTire>(beanManager, SpareTire.class);
      this.driversSeat = new NonContextual<DriversSeat>(beanManager, DriversSeat.class);
   }

   /**
    * Producer method for a bean with qualifier @Drivers and types Seat, Object
    * 
    * @return
    */
   @Produces @Drivers
   public Seat produceAtDriversSeat()
   {
      return driversSeat.newInstance().produce().inject().get();
   }
   
   /**
    * Producer method for a bean with default qualifiers and type DriversSeat only
    * 
    * @return
    */
   @Produces @BeanTypes(DriversSeat.class)
   public DriversSeat produceDriversSeat()
   {
      return driversSeat.newInstance().produce().inject().get();
   }
   
   @Qualifier
   @Retention(RUNTIME)
   @Target( { TYPE, METHOD, FIELD, PARAMETER })
   @Documented
   private @interface Spare
   {

   }
   
   /**
    * Producer method for a bean with qualifier @Named("spare") and types Tire, Object.
    * 
    * Use the @Spare qualifier to stop @Default being applied 
    * 
    */
   @Produces @Named("spare") @Spare
   public Tire produceAtNamedSpareTire()
   {
      return spareTire.newInstance().produce().inject().get();
   }
   
   /**
    * Producer method for bean with default qualifiers and type SpareTire only
    */
   @Produces @BeanTypes(SpareTire.class)
   public SpareTire produceSpareTire()
   {
      return spareTire.newInstance().produce().inject().get();
   }
   
}

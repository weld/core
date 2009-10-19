package org.jboss.weld.atinject.tck;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.inject.New;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Qualifier;

import org.atinject.tck.auto.Drivers;
import org.atinject.tck.auto.DriversSeat;
import org.atinject.tck.auto.Seat;
import org.atinject.tck.auto.Tire;
import org.atinject.tck.auto.accessories.SpareTire;

/**
 * Producer methods for the @Inject TCK beans we need greater control over
 * 
 * @author pmuir
 *
 */
public class Producers
{

   /**
    * Producer method for a bean with qualifier @Drivers and types Seat, Object
    * 
    * @return
    */
   @Produces @Drivers
   public Seat produceAtDriversSeat(@New DriversSeat driversSeat)
   {
      return driversSeat;
   }
   
   /**
    * Producer method for a bean with default qualifiers and type DriversSeat only
    * 
    * @return
    */
   @Produces @Typed(DriversSeat.class)
   public DriversSeat produceDriversSeat(@New DriversSeat driversSeat)
   {
      return driversSeat;
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
   public Tire produceAtNamedSpareTire(@New SpareTire spareTire)
   {
      return spareTire;
   }
   
   /**
    * Producer method for bean with default qualifiers and type SpareTire only
    */
   @Produces @Typed(SpareTire.class)
   public SpareTire produceSpareTire(@New SpareTire spareTire)
   {
      return spareTire;
   }
   
}

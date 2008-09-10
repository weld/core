package org.jboss.webbeans.test.components;

import javax.webbeans.Dependent;
import javax.webbeans.Named;
import javax.webbeans.Produces;
import javax.webbeans.Production;

import org.jboss.webbeans.test.annotations.Tame;

@Production
public class SpiderProducer
{
   
   @Produces @Tame public Tarantula produceTameTarantula()
   {
      return new DefangedTarantula();
   }
   
   @Produces public Tarantula produceTarantula()
   {
      return new Tarantula();
   }
   
   @Produces @Dependent public final TrapdoorSpider produceTrapdoorSpider()
   {
      return new TrapdoorSpider();
   }
   
   @Produces @Named("blackWidow") public BlackWidow produceBlackWidow()
   {
      return new BlackWidow();
   }
   
   @Produces @Named public DaddyLongLegs produceDaddyLongLegs()
   {
      return new DaddyLongLegs();
   }
   
   @Produces @Named public LadybirdSpider getLadybirdSpider()
   {
      return new LadybirdSpider();
   }

}

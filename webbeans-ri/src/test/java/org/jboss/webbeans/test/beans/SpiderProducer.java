package org.jboss.webbeans.test.beans;

import javax.webbeans.Dependent;
import javax.webbeans.Named;
import javax.webbeans.Produces;
import javax.webbeans.RequestScoped;

import org.jboss.webbeans.test.annotations.AnimalStereotype;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.Tame;

@AnotherDeploymentType
public class SpiderProducer
{
   
   private static Spider[] ALL_SPIDERS = { new Tarantula(), new LadybirdSpider(), new DaddyLongLegs() };
   
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
   
   @Produces @Named @RequestScoped public DaddyLongLegs produceDaddyLongLegs()
   {
      return new DaddyLongLegs();
   }
   
   @Produces @Named @AnotherDeploymentType public LadybirdSpider getLadybirdSpider()
   {
      return new LadybirdSpider();
   }

   @Produces @Named("Shelob") public Tarantula produceShelob() 
   {
      return null;
   }
   
   @Produces @AnimalStereotype public WolfSpider produceWolfSpider()
   {
      return new WolfSpider();
   }
   
   @Produces public Animal makeASpider()
   {
      return new WolfSpider();
   }
   
   @Produces public int getWolfSpiderSize()
   {
      return 4;
   }
   
   @Produces public Spider[] getSpiders()
   {
      return ALL_SPIDERS;
   }
   
   @Produces public String[] getStrings()
   {
      return new String[0];
   }
   
   @Produces public <T> FunnelWeaver<T> getFunnelWeaver()
   {
      return new FunnelWeaver<T>();
   }
   
   @Produces public FunnelWeaver<?> getAnotherFunnelWeaver()
   {
      return new FunnelWeaver<Object>();
   }
   
   @Produces public FunnelWeaver<Spider> getFunnelWeaverSpider()
   {
      return new FunnelWeaver<Spider>();
   }
   
   @Produces public Spider getNullSpider()
   {
      return null;
   }
   
   @Produces public Spider produceSpiderFromInjection(@Tame Tarantula tarantula) 
   {
      return tarantula;
   }

}

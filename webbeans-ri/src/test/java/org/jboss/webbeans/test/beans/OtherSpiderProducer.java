package org.jboss.webbeans.test.beans;

import javax.webbeans.Dependent;
import javax.webbeans.Named;
import javax.webbeans.Produces;
import javax.webbeans.Production;
import javax.webbeans.RequestScoped;

import org.jboss.webbeans.test.annotations.AnimalStereotype;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.Tame;

@AnotherDeploymentType
public class OtherSpiderProducer
{
   
   private static Spider[] ALL_SPIDERS = { new Tarantula(), new LadybirdSpider(), new DaddyLongLegs() };
   
   @Produces @Tame public Tarantula produceTameTarantula = new DefangedTarantula();
   
   @Produces public Tarantula produceTarantula = new Tarantula();
   
   @Produces @Dependent public final TrapdoorSpider produceTrapdoorSpider = new TrapdoorSpider();
   
   @Produces @Named("blackWidow") public BlackWidow produceBlackWidow = new BlackWidow();
   
   @Produces @Named @RequestScoped public DaddyLongLegs produceDaddyLongLegs = new DaddyLongLegs();
   
   @Produces @Named @Production public LadybirdSpider getLadybirdSpider = new LadybirdSpider();

   @Produces @Named("Shelob") public Tarantula produceShelob;
   
   @Produces @AnimalStereotype public WolfSpider produceWolfSpider = new WolfSpider();
   
   @Produces public Animal makeASpider = new WolfSpider();
   
   @Produces public int getWolfSpiderSize = 4;
   
   @Produces public Spider[] getSpiders = ALL_SPIDERS;
   
   @Produces public String[] getStrings = new String[0];
   
   @Produces public FunnelWeaver<?> getAnotherFunnelWeaver = new FunnelWeaver<Object>();
   
   @Produces public FunnelWeaver<Spider> getFunnelWeaverSpider = new FunnelWeaver<Spider>();
   
   @Produces public Spider getNullSpider =  null;

}

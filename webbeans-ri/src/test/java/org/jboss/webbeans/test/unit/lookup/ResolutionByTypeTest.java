package org.jboss.webbeans.test.unit.lookup;

import java.util.Set;

import javax.webbeans.manager.Bean;

import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.jboss.webbeans.introspector.jlr.AnnotatedFieldImpl;
import org.jboss.webbeans.literal.CurrentLiteral;
import org.jboss.webbeans.test.unit.AbstractTest;
import org.testng.annotations.Test;

public class ResolutionByTypeTest extends AbstractTest
{
   
   private AnnotatedClass<FishFarm> fishFarmClass = new AnnotatedClassImpl<FishFarm>(FishFarm.class);

   @Test(groups="resolution")
   public void testAnnotatedField() throws Exception
   {
      AnnotatedField<Tuna> tuna = new AnnotatedFieldImpl<Tuna>(FishFarm.class.getDeclaredField("tuna"), fishFarmClass);
      assert tuna.getType().isAssignableFrom(Tuna.class);
      assert tuna.getBindingTypes().size() == 1;
      assert tuna.getBindingTypes().contains(new CurrentLiteral());
      assert tuna.getType().isAssignableFrom(Tuna.class);
   }
   
   @Test
   public void testOneBindingType() throws Exception
   {
      AnnotatedField<ScottishFish> whiteScottishFishField = new AnnotatedFieldImpl<ScottishFish>(FishFarm.class.getDeclaredField("whiteScottishFish"), fishFarmClass);
      Bean<Cod> codBean = SimpleBean.of(Cod.class, manager);
      Bean<Salmon> salmonBean = SimpleBean.of(Salmon.class, manager);
      Bean<Sole> soleBean = SimpleBean.of(Sole.class, manager);
      manager.addBean(codBean);
      manager.addBean(salmonBean);
      manager.addBean(soleBean);
      Set<Bean<ScottishFish>> possibleTargets = manager.resolveByType(whiteScottishFishField);
      assert possibleTargets.size() == 2;
      assert possibleTargets.contains(codBean);
      assert possibleTargets.contains(soleBean);
   }
   
   @Test
   public void testABindingType() throws Exception
   {
      AnnotatedField<Animal> whiteChunkyFishField = new AnnotatedFieldImpl<Animal>(FishFarm.class.getDeclaredField("realChunkyWhiteFish"), fishFarmClass);
      
      Bean<Cod> codBean = SimpleBean.of(Cod.class, manager);
      Bean<Salmon> salmonBean = SimpleBean.of(Salmon.class, manager);
      Bean<Sole> soleBean = SimpleBean.of(Sole.class, manager);
      
      manager.addBean(codBean);
      manager.addBean(salmonBean);
      manager.addBean(soleBean);
      Set<Bean<Animal>> possibleTargets = manager.resolveByType(whiteChunkyFishField); 
      assert possibleTargets.size() == 1;
      assert possibleTargets.contains(codBean);
   }
   
   @Test
   public void testMultipleApiTypeWithCurrent() throws Exception
   {
      AnnotatedField<Animal> animalField = new AnnotatedFieldImpl<Animal>(FishFarm.class.getDeclaredField("animal"), fishFarmClass);
      Bean<SeaBass> seaBassBean = SimpleBean.of(SeaBass.class, manager);
      Bean<Haddock> haddockBean = SimpleBean.of(Haddock.class, manager);
      manager.addBean(seaBassBean);
      manager.addBean(haddockBean);
      Set<Bean<Animal>> possibleTargets = manager.resolveByType(animalField);
      assert possibleTargets.size() == 2;
      assert possibleTargets.contains(seaBassBean);
      assert possibleTargets.contains(haddockBean);
   }
      
}

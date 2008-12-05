package org.jboss.webbeans.test;

import static org.jboss.webbeans.util.BeanFactory.createSimpleBean;

import javax.webbeans.DefinitionException;
import javax.webbeans.Dependent;
import javax.webbeans.RequestScoped;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.test.annotations.AnotherScopeType;
import org.jboss.webbeans.test.beans.BeanWithTooManyScopeTypes;
import org.jboss.webbeans.test.beans.Mullet;
import org.jboss.webbeans.test.beans.Order;
import org.jboss.webbeans.test.beans.RedSnapper;
import org.jboss.webbeans.test.beans.SeaBass;
import org.testng.annotations.Test;

@SpecVersion("PDR")
public class ScopeTypeTest extends AbstractTest
{
   
   @Test @SpecAssertion(section="2.4")
   public void testScopeTypesAreExtensible()
   {
      Bean<Mullet> mullet = createSimpleBean(Mullet.class);
      assert mullet.getScopeType().equals(AnotherScopeType.class);
   }
   
   @Test(groups={"stub", "annotationDefinition"}) @SpecAssertion(section="2.4.2")
   public void testScopeTypeHasCorrectTarget()
   {
      assert false;
   }

   @Test(groups={"stub", "annotationDefinition"}) @SpecAssertion(section="2.4.2")
   public void testScopeTypeHasCorrectRetention()
   {
      assert false;
   }

   @Test(groups={"stub", "annotationDefinition"}) @SpecAssertion(section="2.4.2")
   public void testScopeTypeDeclaresScopeTypeAnnotation()
   {
      assert false;
   }
   
   @Test @SpecAssertion(section="2.4.3")
   public void testScopeDeclaredInJava()
   {
      SimpleBean<SeaBass> trout = createSimpleBean(SeaBass.class);
      assert trout.getScopeType().equals(RequestScoped.class);
   }
   
   @Test(expectedExceptions=DefinitionException.class) @SpecAssertion(section="2.4.3")
   public void testTooManyScopesSpecifiedInJava()
   {
      createSimpleBean(BeanWithTooManyScopeTypes.class);
   }
   
   @Test(expectedExceptions=DefinitionException.class, groups={"stub", "webbeansxml"})
   public void testTooManyScopesSpecifiedInXml()
   {
      //Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      //annotations.put(RequestScoped.class, new RequestScopedAnnotationLiteral());
      //annotations.put(ConversationScoped.class, new ConversationScopedAnnotationLiteral());
      //AnnotatedClass<Antelope> antelopeAnnotatedItem = new SimpleAnnotatedClass<Antelope>(Antelope.class, annotations);
      //createSimpleBean(null, antelopeAnnotatedItem, manager);
   }
   
   @Test(groups={"stub", "webbeansxml"}) @SpecAssertion(section="2.4.4")
   public void testScopeDeclaredInXml()
   {
      //Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      //annotations.put(RequestScoped.class, new RequestScopedAnnotationLiteral());
      //AnnotatedClass<Order> annotatedItem = new SimpleAnnotatedClass<Order>(Order.class, annotations);
      
      //SimpleBean<Order> order = createSimpleBean(Order.class, annotatedItem, manager);
      //assert order.getScopeType().equals(RequestScoped.class);
      assert false;
   }
   
   @Test(groups={"stub", "webbeansxml"}) @SpecAssertion(section="2.4.4")
   public void testScopeMissingInXml()
   {
      //Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      //AnnotatedClass<SeaBass> annotatedItem = new SimpleAnnotatedClass<SeaBass>(SeaBass.class, annotations);
      
      //SimpleBean<SeaBass> trout =createSimpleBean(SeaBass.class, annotatedItem, manager);
      //assert trout.getScopeType().equals(RequestScoped.class);
      assert false;
   }

   @Test(groups={"stub", "webbeansxml"}) @SpecAssertion(section="2.4.4")
   public void testScopeDeclaredInXmlOverridesJava()
   {
      //Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      //annotations.put(ConversationScoped.class, new ConversationScopedAnnotationLiteral());
      //AnnotatedClass<SeaBass> annotatedItem = new SimpleAnnotatedClass<SeaBass>(SeaBass.class, annotations);
      //SimpleBean<SeaBass> trout = createSimpleBean(SeaBass.class, annotatedItem, manager);
      //assert trout.getScopeType().equals(ConversationScoped.class);
      assert false;
   }
   
   @Test @SpecAssertion(section="2.4.5")
   public void testDefaultScope()
   {
      SimpleBean<Order> order = createSimpleBean(Order.class);
      assert order.getScopeType().equals(Dependent.class);
   }
   
   @Test(groups={"stub", "webbeansxml"})@SpecAssertion(section={"2.4.5", "2.7.2"})
   public void testScopeSpecifiedAndStereotyped()
   {
      //Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      //annotations.put(FishStereotype.class, new FishStereotypeAnnotationLiteral());
      //AnnotatedClass<SeaBass> annotatedItem = new SimpleAnnotatedClass<SeaBass>(SeaBass.class, annotations);
      //SimpleBean<SeaBass> trout = createSimpleBean(SeaBass.class, annotatedItem, manager);
      //assert trout.getScopeType().equals(RequestScoped.class);
      assert false;
   }
   
   @Test(groups={"webbeansxml", "stub"}) @SpecAssertion(section="2.4.5")
   public void testMutipleIncompatibleScopeStereotypes()
   {
      assert false;
   }
   
   @Test(groups={"stub", "webbeansxml"}) @SpecAssertion(section="2.4.5")
   public void testMutipleIncompatibleScopeStereotypesWithScopeSpecified()
   {
      /*Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(FishStereotype.class, new FishStereotypeAnnotationLiteral());
      annotations.put(AnimalStereotype.class, new AnimalStereotypeAnnotationLiteral());
      AnnotatedClass<SeaBass> annotatedItem = new SimpleAnnotatedClass<SeaBass>(SeaBass.class, annotations);*/
      
      //SimpleBean<SeaBass> trout = createSimpleBean(SeaBass.class, annotatedItem, manager);
      //assert trout.getScopeType().equals(RequestScoped.class);
      assert false;
   }
   
   @Test(groups={"stub", "webbeansxml"})@SpecAssertion(section="2.4.5")
   public void testMutipleCompatibleScopeStereotypes()
   {
      /*Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(FishStereotype.class, new FishStereotypeAnnotationLiteral());
      annotations.put(RiverFishStereotype.class, new RiverFishStereotypeAnnotationLiteral());
      AnnotatedClass<Haddock> annotatedItem = new SimpleAnnotatedClass<Haddock>(Haddock.class, annotations);*/
      
      //SimpleBean<Haddock> haddock = createSimpleBean(Haddock.class, annotatedItem, manager);
      //assert haddock.getScopeType().equals(ApplicationScoped.class);
      assert false;
   }
   
   @Test @SpecAssertion(section="2.7.2")
   public void testWebBeanScopeTypeOverridesStereotype()
   {
      Bean<RedSnapper> bean = createSimpleBean(RedSnapper.class);
      assert bean.getScopeType().equals(RequestScoped.class);
   }
   
}

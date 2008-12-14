package org.jboss.webbeans.test;

import static org.jboss.webbeans.util.BeanFactory.createSimpleBean;

import javax.webbeans.ApplicationScoped;
import javax.webbeans.DefinitionException;
import javax.webbeans.Dependent;
import javax.webbeans.RequestScoped;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.test.annotations.AnotherScopeType;
import org.jboss.webbeans.test.beans.BeanWithTooManyScopeTypes;
import org.jboss.webbeans.test.beans.Grayling;
import org.jboss.webbeans.test.beans.Minnow;
import org.jboss.webbeans.test.beans.Mullet;
import org.jboss.webbeans.test.beans.Order;
import org.jboss.webbeans.test.beans.Pollock;
import org.jboss.webbeans.test.beans.RedSnapper;
import org.jboss.webbeans.test.beans.SeaBass;
import org.jboss.webbeans.test.beans.broken.Scallop;
import org.testng.annotations.Test;

@SpecVersion("20081206")
public class ScopeTypeTest extends AbstractTest
{
   
   @Test @SpecAssertion(section="2.4")
   public void testScopeTypesAreExtensible()
   {
      Bean<Mullet> mullet = createSimpleBean(Mullet.class, manager);
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
      SimpleBean<SeaBass> trout = createSimpleBean(SeaBass.class, manager);
      assert trout.getScopeType().equals(RequestScoped.class);
   }
   
   @Test(expectedExceptions=DefinitionException.class) @SpecAssertion(section="2.4.3")
   public void testTooManyScopesSpecifiedInJava()
   {
      createSimpleBean(BeanWithTooManyScopeTypes.class, manager);
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
      SimpleBean<Order> order = createSimpleBean(Order.class, manager);
      assert order.getScopeType().equals(Dependent.class);
   }
   
   @Test @SpecAssertion(section={"2.4.5", "2.7.2"})
   public void testScopeSpecifiedAndStereotyped()
   {
      Bean<Minnow> minnow = createSimpleBean(Minnow.class, manager);
      assert minnow.getScopeType().equals(RequestScoped.class);
   }
   
   @Test(expectedExceptions=DefinitionException.class) @SpecAssertion(section="2.4.5")
   public void testMutipleIncompatibleScopeStereotypes()
   {
      createSimpleBean(Scallop.class, manager);
   }
   
   @Test @SpecAssertion(section="2.4.5")
   public void testMutipleIncompatibleScopeStereotypesWithScopeSpecified()
   {
      Bean<Pollock> pollock = createSimpleBean(Pollock.class, manager);
      assert pollock.getScopeType().equals(Dependent.class);
   }
   
   @Test @SpecAssertion(section="2.4.5")
   public void testMutipleCompatibleScopeStereotypes()
   {
      Bean<Grayling> grayling = createSimpleBean(Grayling.class, manager);
      assert grayling.getScopeType().equals(ApplicationScoped.class);
   }
   
   @Test @SpecAssertion(section="2.7.2")
   public void testWebBeanScopeTypeOverridesStereotype()
   {
      Bean<RedSnapper> bean = createSimpleBean(RedSnapper.class, manager);
      assert bean.getScopeType().equals(RequestScoped.class);
   }
   
}

package org.jboss.webbeans.test;

import static org.jboss.webbeans.util.BeanFactory.createSimpleBean;

import javax.webbeans.DefinitionException;

import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.test.beans.Haddock;
import org.jboss.webbeans.test.beans.Moose;
import org.jboss.webbeans.test.beans.RedSnapper;
import org.jboss.webbeans.test.beans.SeaBass;
import org.testng.annotations.Test;

@SpecVersion("PDR")
public class NameTest extends AbstractTest
{

   @Test(groups={"stub", "el"}, expectedExceptions=DefinitionException.class) @SpecAssertion(section="2.6")
   public void testInvalidElIdentifierUsedAsWebBeanName()
   {
      assert false;
   }
   
   @Test @SpecAssertion(section="2.6.1")
   public void testNonDefaultNamed()
   {
      SimpleBean<Moose> moose = createSimpleBean(Moose.class);
      assert moose.getName().equals("aMoose");
   }
   
   @Test(groups={"stub", "webbeansxml"}) @SpecAssertion(section="2.6.2")
   public void testNonDefaultXmlNamed()
   {
      /*Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Named.class, new NamedAnnotationLiteral(){
         
         public String value()
         {
            return "aTrout";
         }
         
      });
      AnnotatedClass<SeaBass> annotatedItem = new SimpleAnnotatedClass<SeaBass>(SeaBass.class, annotations);*/
      //SimpleBean<SeaBass> trout = createSimpleBean(SeaBass.class, annotatedItem, manager);
      
      //assert trout.getName().equals("aTrout");
      assert false;
   }
   
   @Test(groups={"stub", "webbeansxml"}) @SpecAssertion(section="2.6.2")
   public void testXmlNamedOverridesJavaNamed()
   {
      /*Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Named.class, new NamedAnnotationLiteral(){
         
         public String value()
         {
            return "aTrout";
         }
         
      });
      AnnotatedClass<Cod> annotatedItem = new SimpleAnnotatedClass<Cod>(Cod.class, annotations);*/
      //SimpleBean<Cod> cod = createSimpleBean(Cod.class, annotatedItem, manager);
      
      //assert cod.getName().equals("aTrout");
      assert false;
   }
   
   @Test(groups={"stub", "webbeansxml"}) @SpecAssertion(section={"2.6.2", "2.6.3"})
   public void testJavaNamedUsedWhenNoXmlSpecified()
   {
      //Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      //AnnotatedClass<Cod> annotatedItem = new SimpleAnnotatedClass<Cod>(Cod.class, annotations);
      //SimpleBean<Cod> cod = createSimpleBean(Cod.class, annotatedItem, manager);
      
      //assert cod.getName().equals("whitefish");
      assert false;
   }
   
   @Test @SpecAssertion(section={"2.6.3", "3.2.7"})
   public void testDefaultNamed()
   {
      SimpleBean<Haddock> haddock = createSimpleBean(Haddock.class);
      assert haddock.getName() != null;
      assert haddock.getName().equals("haddock");
   }
   
   @Test(groups={"stub", "webbeansxml"}) @SpecAssertion(section="2.6.3")
   public void testDefaultXmlNamed()
   {
      /*Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Named.class, new NamedAnnotationLiteral() {
         
         public String value()
         {
            return "";
         }
         
      });
      AnnotatedClass<SeaBass> annotatedItem = new SimpleAnnotatedClass<SeaBass>(SeaBass.class, annotations);*/
      //SimpleBean<SeaBass> trout = createSimpleBean(SeaBass.class, annotatedItem, manager);
      
      //assert trout.getName() != null;
      //assert trout.getName().equals("seaBass");
      assert false;
   }
   
   @Test @SpecAssertion(section={"2.6.3", "2.7"})
   public void testSterotypeDefaultsName()
   {
      SimpleBean<RedSnapper> model = createSimpleBean(RedSnapper.class);
      assert model.getMergedStereotypes().isBeanNameDefaulted();
      assert model.getName().equals("redSnapper");
   }
   
   @Test @SpecAssertion(section="2.6.4")
   public void testNotNamedInJava()
   {
      SimpleBean<SeaBass> model = createSimpleBean(SeaBass.class);
      assert model.getName() == null;
   }
   
   @Test @SpecAssertion(section="2.6.4")
   public void testNotNamedInXml()
   {
      SimpleBean<SeaBass> model = createSimpleBean(SeaBass.class);
      assert model.getName() == null;
   }
   
   @Test(groups={"stub", "webbeansxml"}) @SpecAssertion(section="2.6.4")
   public void testNotNamedInStereotype()
   {
      /*Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(RiverFishStereotype.class, new RiverFishStereotypeAnnotationLiteral());
      AnnotatedClass<SeaBass> annotatedItem = new SimpleAnnotatedClass<SeaBass>(SeaBass.class, annotations);*/
      //SimpleBean<SeaBass> model = createSimpleBean(SeaBass.class, annotatedItem, manager);
      //assert model.getName() == null;
      assert false;
   }
   
}

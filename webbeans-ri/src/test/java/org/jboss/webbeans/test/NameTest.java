package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.createSimpleModel;
import static org.jboss.webbeans.test.util.Util.getEmptyAnnotatedType;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.webbeans.DefinitionException;
import javax.webbeans.Named;

import org.jboss.webbeans.bindings.NamedAnnotationLiteral;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.bean.BeanModel;
import org.jboss.webbeans.model.bean.SimpleBeanModel;
import org.jboss.webbeans.test.annotations.RiverFishStereotype;
import org.jboss.webbeans.test.beans.Cod;
import org.jboss.webbeans.test.beans.Haddock;
import org.jboss.webbeans.test.beans.Moose;
import org.jboss.webbeans.test.beans.RedSnapper;
import org.jboss.webbeans.test.beans.SeaBass;
import org.jboss.webbeans.test.bindings.RiverFishStereotypeAnnotationLiteral;
import org.testng.annotations.Test;

@SpecVersion("PDR")
public class NameTest extends AbstractTest
{

   @Test(groups="el", expectedExceptions=DefinitionException.class) @SpecAssertion(section="2.6")
   public void testInvalidElIdentifierUsedAsWebBeanName()
   {
      assert false;
   }
   
   @Test @SpecAssertion(section="2.6.1")
   public void testNonDefaultNamed()
   {
      SimpleBeanModel<Moose> moose = new SimpleBeanModel<Moose>(new SimpleAnnotatedType<Moose>(Moose.class), getEmptyAnnotatedType(Moose.class), manager);
      assert moose.getName().equals("aMoose");
   }
   
   @Test @SpecAssertion(section="2.6.2")
   public void testNonDefaultXmlNamed()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Named.class, new NamedAnnotationLiteral(){
         
         public String value()
         {
            return "aTrout";
         }
         
      });
      AnnotatedType<SeaBass> annotatedItem = new SimpleAnnotatedType<SeaBass>(SeaBass.class, annotations);
      SimpleBeanModel<SeaBass> trout = new SimpleBeanModel<SeaBass>(new SimpleAnnotatedType<SeaBass>(SeaBass.class), annotatedItem, manager);
      
      assert trout.getName().equals("aTrout");
   }
   
   @Test @SpecAssertion(section="2.6.2")
   public void testXmlNamedOverridesJavaNamed()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Named.class, new NamedAnnotationLiteral(){
         
         public String value()
         {
            return "aTrout";
         }
         
      });
      AnnotatedType<Cod> annotatedItem = new SimpleAnnotatedType<Cod>(Cod.class, annotations);
      SimpleBeanModel<Cod> cod = new SimpleBeanModel<Cod>(new SimpleAnnotatedType<Cod>(Cod.class), annotatedItem, manager);
      
      assert cod.getName().equals("aTrout");
   }
   
   @Test @SpecAssertion(section={"2.6.2", "2.6.3"})
   public void testJavaNamedUsedWhenNoXmlSpecified()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      AnnotatedType<Cod> annotatedItem = new SimpleAnnotatedType<Cod>(Cod.class, annotations);
      SimpleBeanModel<Cod> cod = new SimpleBeanModel<Cod>(new SimpleAnnotatedType<Cod>(Cod.class), annotatedItem, manager);
      
      assert cod.getName().equals("whitefish");
   }
   
   @Test @SpecAssertion(section="2.6.3")
   public void testDefaultNamed()
   {
      SimpleBeanModel<Haddock> haddock = new SimpleBeanModel<Haddock>(new SimpleAnnotatedType<Haddock>(Haddock.class), getEmptyAnnotatedType(Haddock.class), manager);
      assert haddock.getName() != null;
      assert haddock.getName().equals("haddock");
   }
   
   @Test @SpecAssertion(section="2.6.3")
   public void testDefaultXmlNamed()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Named.class, new NamedAnnotationLiteral() {
         
         public String value()
         {
            return "";
         }
         
      });
      AnnotatedType<SeaBass> annotatedItem = new SimpleAnnotatedType<SeaBass>(SeaBass.class, annotations);
      SimpleBeanModel<SeaBass> trout = new SimpleBeanModel<SeaBass>(new SimpleAnnotatedType<SeaBass>(SeaBass.class), annotatedItem, manager);
      
      assert trout.getName() != null;
      assert trout.getName().equals("seaBass");
   }
   
   @Test @SpecAssertion(section={"2.6.3", "2.7"})
   public void testSterotypeDefaultsName()
   {
      BeanModel<?, ?> model = createSimpleModel(RedSnapper.class, manager);
      assert model.getMergedStereotypes().isBeanNameDefaulted();
      assert model.getName().equals("redSnapper");
   }
   
   @Test @SpecAssertion(section="2.6.4")
   public void testNotNamedInJava()
   {
      SimpleBeanModel<SeaBass> model = new SimpleBeanModel<SeaBass>(new SimpleAnnotatedType<SeaBass>(SeaBass.class), getEmptyAnnotatedType(SeaBass.class), manager);
      assert model.getName() == null;
   }
   
   @Test @SpecAssertion(section="2.6.4")
   public void testNotNamedInXml()
   {
      SimpleBeanModel<SeaBass> model = new SimpleBeanModel<SeaBass>(new SimpleAnnotatedType<SeaBass>(SeaBass.class), getEmptyAnnotatedType(SeaBass.class), manager);
      assert model.getName() == null;
   }
   
   @Test @SpecAssertion(section="2.6.4")
   public void testNotNamedInStereotype()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(RiverFishStereotype.class, new RiverFishStereotypeAnnotationLiteral());
      AnnotatedType<SeaBass> annotatedItem = new SimpleAnnotatedType<SeaBass>(SeaBass.class, annotations);
      SimpleBeanModel<SeaBass> model = new SimpleBeanModel<SeaBass>(new SimpleAnnotatedType<SeaBass>(SeaBass.class), annotatedItem, manager);
      assert model.getName() == null;
   }
   
}

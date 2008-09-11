package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.HashMap;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.test.mock.MockContainerImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ProducerExpressionComponentModelTest
{
   
   private ContainerImpl container;
   private AnnotatedType<?> emptyAnnotatedItem;
   
   @BeforeMethod
   public void before()
   {
      emptyAnnotatedItem = new SimpleAnnotatedType(null, new HashMap<Class<? extends Annotation>, Annotation>());
      container = new MockContainerImpl(null);
   }
   
   @Test
   public void testApiTypes()
   {
      
   }
   
}

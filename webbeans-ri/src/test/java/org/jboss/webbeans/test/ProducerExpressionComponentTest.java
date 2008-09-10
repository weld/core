package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.HashMap;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.test.mock.MockContainerImpl;
import org.junit.Before;
import org.junit.Test;

public class ProducerExpressionComponentTest
{
   
   private ContainerImpl container;
   private AnnotatedType<?> emptyAnnotatedItem;
   
   @Before
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

/**
 * 
 */
package org.jboss.weld.manager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

public class DummyInjectionPoint implements InjectionPoint
{
   
   public static final DummyInjectionPoint INSTANCE = new DummyInjectionPoint();
   
   public boolean isTransient()
   {
      return true;
   }

   public boolean isDelegate()
   {
      return false;
   }

   public Type getType()
   {
      return InjectionPoint.class;
   }

   public Set<Annotation> getQualifiers()
   {
      return Collections.emptySet();
   }

   public Member getMember()
   {
      return null;
   }

   public Bean<?> getBean()
   {
      return null;
   }

   public Annotated getAnnotated()
   {
      return null;
   }
   
}

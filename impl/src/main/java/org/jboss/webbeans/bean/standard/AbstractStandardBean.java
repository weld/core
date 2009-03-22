package org.jboss.webbeans.bean.standard;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.context.Dependent;
import javax.inject.Standard;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.injection.AnnotatedInjectionPoint;
import org.jboss.webbeans.literal.CurrentLiteral;

public abstract class AbstractStandardBean<T> extends RIBean<T>
{
   
   protected AbstractStandardBean(ManagerImpl manager)
   {
      super(manager);
   }
   
   @Override
   public void initialize()
   {
      // No-op
   }

   private static final Annotation[] DEFAULT_BINDING_ARRAY = { new CurrentLiteral() };
   private static final Set<Annotation> DEFAULT_BINDING = new HashSet<Annotation>(Arrays.asList(DEFAULT_BINDING_ARRAY));
   
   @Override
   public Set<Annotation> getBindings()
   {
      return DEFAULT_BINDING;
   }
   
   @Override
   public Class<? extends Annotation> getDeploymentType()
   {
      return Standard.class;
   }
   
   @Override
   public Class<? extends Annotation> getScopeType()
   {
      return Dependent.class;
   }
   
   @Override
   public RIBean<?> getSpecializedBean()
   {
      return null;
   }
   
   @Override
   public String getName()
   {
      return null;
   }
   
   @Override
   public Set<AnnotatedInjectionPoint<?, ?>> getInjectionPoints()
   {
      return Collections.emptySet();
   }
   
   @Override
   public boolean isNullable()
   {
      return true;
   }
   
   @Override
   public boolean isPrimitive()
   {
      return false;
   }
   
   @Override
   public boolean isSerializable()
   {
      return false;
   }
   
   @Override
   public boolean isSpecializing()
   {
      return false;
   }
   
   @Override
   public boolean isProxyable()
   {
      return false;
   }
   
}

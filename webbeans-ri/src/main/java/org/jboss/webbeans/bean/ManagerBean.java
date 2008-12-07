/**
 * 
 */
package org.jboss.webbeans.bean;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.webbeans.Dependent;
import javax.webbeans.Standard;
import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Manager;

import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.util.Reflections;

public class ManagerBean extends Bean<Manager>
{
   
   private static Set<Class<?>> types = Reflections.getTypeHierachy(Manager.class);
   private static final Set<Annotation> BINDING = new HashSet<Annotation>(Arrays.asList(new CurrentAnnotationLiteral()));

   public ManagerBean(Manager manager)
   {
      super(manager);
   }
   
   @Override
   public Manager create()
   {
      return getManager();
   }

   @Override
   public void destroy(Manager instance)
   {
      //No -op
   }

   @Override
   public Set<Annotation> getBindingTypes()
   {
      return BINDING;
   }

   @Override
   public Class<? extends Annotation> getDeploymentType()
   {
      return Standard.class;
   }

   @Override
   public String getName()
   {
      return null;
   }

   @Override
   public Class<? extends Annotation> getScopeType()
   {
      return Dependent.class;
   }

   @Override
   public Set<Class<?>> getTypes()
   {
      return types;
   }

   @Override
   public boolean isNullable()
   {
      return true;
   }

   @Override
   public boolean isSerializable()
   {
      return false;
   }
   
}
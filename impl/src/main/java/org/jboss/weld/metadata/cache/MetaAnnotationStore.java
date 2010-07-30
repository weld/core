/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.metadata.cache;

import java.lang.annotation.Annotation;
import java.util.concurrent.ConcurrentMap;

import com.google.common.base.Function;
import com.google.common.collect.ComputationException;
import com.google.common.collect.MapMaker;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.resources.ClassTransformer;

/**
 * Metadata singleton for holding EJB metadata, scope models etc.
 * 
 * @author Pete Muir
 * 
 */
public class MetaAnnotationStore implements Service
{
   
   private static abstract class AbstractMetaAnnotationFunction<M extends AnnotationModel<Annotation>> implements Function<Class<Annotation>, M>
   {
      
      private final ClassTransformer classTransformer;

      private AbstractMetaAnnotationFunction(ClassTransformer classTransformer)
      {
         this.classTransformer = classTransformer;
      }
      
      public ClassTransformer getClassTransformer()
      {
         return classTransformer;
      }
      
   }
   
   private static class StereotypeFunction extends AbstractMetaAnnotationFunction<StereotypeModel<Annotation>>
   {
      
      public StereotypeFunction(ClassTransformer classTransformer)
      {
         super(classTransformer);
      }

      public StereotypeModel<Annotation> apply(Class<Annotation> from)
      {
         return new StereotypeModel<Annotation>(from, getClassTransformer());
      }
      
   }
   
   private static class ScopeFunction extends AbstractMetaAnnotationFunction<ScopeModel<Annotation>>
   {
      
      public ScopeFunction(ClassTransformer classTransformer)
      {
         super(classTransformer);
      }

      public ScopeModel<Annotation> apply(Class<Annotation> from)
      {
         return new ScopeModel<Annotation>(from, getClassTransformer());
      }
      
   }
   
   private static class QualifierFunction extends AbstractMetaAnnotationFunction<QualifierModel<Annotation>>
   {
      
      public QualifierFunction(ClassTransformer classTransformer)
      {
         super(classTransformer);
      }

      public QualifierModel<Annotation> apply(Class<Annotation> from)
      {
         return new QualifierModel<Annotation>(from, getClassTransformer());
      }
      
   }
   
   private static class InterceptorBindingFunction extends AbstractMetaAnnotationFunction<InterceptorBindingModel<Annotation>>
   {
      
      public InterceptorBindingFunction(ClassTransformer classTransformer)
      {
         super(classTransformer);
      }

      public InterceptorBindingModel<Annotation> apply(Class<Annotation> from)
      {
         return new InterceptorBindingModel<Annotation>(from, getClassTransformer());
      }
      
   }

   // The stereotype models
   private ConcurrentMap<Class<Annotation>, StereotypeModel<Annotation>> stereotypes;
   // The scope models
   private ConcurrentMap<Class<Annotation>, ScopeModel<Annotation>> scopes;
   // The binding type models
   private ConcurrentMap<Class<Annotation>, QualifierModel<Annotation>> qualifiers;
   // the interceptor bindings
   private ConcurrentMap<Class<Annotation>, InterceptorBindingModel<Annotation>> interceptorBindings;

   private final ClassTransformer classTransformer;

   public MetaAnnotationStore(ClassTransformer classTransformer)
   {
      MapMaker mapMaker = new MapMaker();
      this.classTransformer = classTransformer;
      this.stereotypes = mapMaker.makeComputingMap(new StereotypeFunction(classTransformer));
      this.scopes = mapMaker.makeComputingMap(new ScopeFunction(classTransformer));
      this.qualifiers = mapMaker.makeComputingMap(new QualifierFunction(classTransformer));
      this.interceptorBindings = mapMaker.makeComputingMap(new InterceptorBindingFunction(classTransformer));
   }

   /**
    * Gets a stereotype model
    * 
    * Adds the model if it is not present.
    * 
    * @param <T> The type
    * @param stereotype The stereotype
    * @return The stereotype model
    */
   public <T extends Annotation> StereotypeModel<T> getStereotype(final Class<T> stereotype)
   {
      return (StereotypeModel<T>) stereotypes.get(stereotype);
   }

   /**
    * Gets a scope model
    * 
    * Adds the model if it is not present.
    * 
    * @param <T> The type
    * @param scope The scope type
    * @return The scope type model
    */
   public <T extends Annotation> ScopeModel<T> getScopeModel(final Class<T> scope)
   {
      return (ScopeModel<T>) scopes.get(scope);
   }

   /**
    * Gets a binding type model.
    * 
    * Adds the model if it is not present.
    * 
    * @param <T> The type
    * @param bindingType The binding type
    * @return The binding type model
    */
   @SuppressWarnings("unchecked")
   public <T extends Annotation> QualifierModel<T> getBindingTypeModel(final Class<T> bindingType)
   {
      return (QualifierModel<T>) qualifiers.get(bindingType);
   }

   /**
    * Gets a string representation
    * 
    * @return A string representation
    */
   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("Metadata cache\n");
      buffer.append("Registered binding type models: ").append(qualifiers.size()).append("\n");
      buffer.append("Registered scope type models: ").append(scopes.size()).append("\n");
      buffer.append("Registered stereotype models: ").append(stereotypes.size()).append("\n");
      buffer.append("Registered interceptor binding models: ").append(interceptorBindings.size()).append("\n");
      return buffer.toString();
   }
   
   public void cleanup() 
   {
      this.qualifiers.clear();
      this.scopes.clear();
      this.stereotypes.clear();
      this.interceptorBindings.clear();
   }

   public <T extends Annotation> InterceptorBindingModel<T> getInterceptorBindingModel(final Class<T> interceptorBinding)
   {
      // Unwrap Definition/Deployment exceptions wrapped in a ComputationException
      // TODO: generalize this and move to a higher level (MBG)
      try
      {
         return (InterceptorBindingModel<T>) interceptorBindings.get(interceptorBinding);
      }
      catch (ComputationException e)
      {
         if (e.getCause() instanceof DeploymentException || e.getCause() instanceof DefinitionException)
         {
            throw (WeldException)e.getCause();
         }
         else
         {
            throw e;
         }
      }
   }
}

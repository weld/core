/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

package javax.webbeans;

/**
 * Supports inline instantiation of annotation types.
 * 
 * @author Pete Muir
 * @author Gavin King
 * 
 *  @param <T>
 *            the annotation type
 */
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AnnotationLiteral<T extends Annotation> implements
      Annotation
{
   
   private Class<T> annotationType;

   protected AnnotationLiteral()
   {
      Class<?> annotationLiteralSubclass = getAnnotationLiteralSubclass(this.getClass());
      if (annotationLiteralSubclass == null)
      {
         throw new RuntimeException(getClass() + "is not a subclass of AnnotationLiteral ");
      }
      annotationType = getTypeParameter(annotationLiteralSubclass);
      if (annotationType == null)
      {
         throw new RuntimeException(getClass() + " is missing type parameter in AnnotationLiteral");

      }
   }
   
   @SuppressWarnings("unchecked")
   private static Class<?> getAnnotationLiteralSubclass(Class<?> clazz)
   {
      Class<?> superclass = clazz.getSuperclass();
      if (superclass.equals(AnnotationLiteral.class))
      {
         return clazz;
      }
      else if (superclass.equals(Object.class))
      {
         return null;
      }
      else
      {
         return (getAnnotationLiteralSubclass(superclass));
      }
   }
   
   @SuppressWarnings("unchecked")
   private static <T> Class<T> getTypeParameter(Class<?> annotationLiteralSuperclass)
   {
      Type type = annotationLiteralSuperclass.getGenericSuperclass();
      if (type instanceof ParameterizedType)
      {
         ParameterizedType parameterizedType = (ParameterizedType) type;
         if (parameterizedType.getActualTypeArguments().length == 1)
         {
            return (Class<T>) parameterizedType
                  .getActualTypeArguments()[0];
         }
      }
      return null;
   }

   public Class<? extends Annotation> annotationType()
   {
      return annotationType;
   }

   @Override
   public String toString()
   {
      return "@" + annotationType().getName() + "()";
   }
   
   @Override
   public boolean equals(Object other)
   {
      if (other instanceof Annotation)
      {
         Annotation that = (Annotation) other;
         return this.annotationType().equals(that.annotationType());
      }
      else
      {
         return false;
      }
   }
   
   @Override
   public int hashCode()
   {
      return 0;
   }
}

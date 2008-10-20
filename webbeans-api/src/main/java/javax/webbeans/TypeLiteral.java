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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Supports inline instantiation of objects that represent parameterized types
 * with actual type parameters.
 * 
 * @author Gavin King
 * 
 * @param <T>
 *            the type, including all actual type parameters
 */
public abstract class TypeLiteral<T> {

   protected TypeLiteral() {
      if (!(getClass().getSuperclass() == TypeLiteral.class)) {
         throw new RuntimeException("Not a direct subclass of TypeLiteral");
      }
      if (!(getClass().getGenericSuperclass() instanceof ParameterizedType)) {
         throw new RuntimeException("Missing type parameter in TypeLiteral");
      }
   }

   public final Type getType() {
      ParameterizedType parameterized = (ParameterizedType) getClass()
            .getGenericSuperclass();
      return parameterized.getActualTypeArguments()[0];
   }

   @SuppressWarnings("unchecked")
   public final Class<T> getRawType() {
      Type type = getType();
      if (type instanceof Class) {
         return (Class<T>) type;
      } else if (type instanceof ParameterizedType) {
         return (Class<T>) ((ParameterizedType) type).getRawType();
      } else if (type instanceof GenericArrayType) {
         return (Class<T>) Object[].class;
      } else {
         throw new RuntimeException("Illegal type");
      }
   }
   // TODO: equals(), hashCode()
}

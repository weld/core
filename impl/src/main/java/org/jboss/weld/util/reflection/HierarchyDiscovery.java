/**
 * 
 */
package org.jboss.weld.util.reflection;

import static org.jboss.weld.logging.messages.UtilMessage.SECURITY_EXCEPTION_SCANNING;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.security.AccessControlException;
import java.util.Map;
import java.util.Set;

import org.slf4j.ext.XLogger.Level;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class HierarchyDiscovery
{

   private final Type type;

   private BiMap<Type, Class<?>> types;

   public HierarchyDiscovery(Type type)
   {
      this.type = type;
   }

   protected void add(Class<?> clazz, Type type)
   {
      types.forcePut(type, clazz);
   }

   public Set<Type> getTypeClosure()
   {
      if (types == null)
      {
         init();
      }
      return types.keySet();
   }
   
   public Map<Class<?>, Type> getTypeMap()
   {
      if (types == null)
      {
         init();
      }
      return types.inverse();
   }
   
   private void init()
   {
      this.types = HashBiMap.create();
      discoverTypes(type);
   }

   public Type getResolvedType()
   {
      if (type instanceof Class<?>)
      {
         Class<?> clazz = (Class<?>) type;
         return resolveType(clazz);
      }
      return type;
   }

   private void discoverTypes(Type type)
   {
      if (type != null)
      {
         if (type instanceof Class<?>)
         {
            Class<?> clazz = (Class<?>) type;
            add(clazz, resolveType(clazz));
            discoverFromClass(clazz);
         }
         else
         {
            Class<?> clazz = null;
            if (type instanceof ParameterizedType)
            {
               Type rawType = ((ParameterizedType) type).getRawType();
               if (rawType instanceof Class<?>)
               {
                  discoverFromClass((Class<?>) rawType);
                  clazz = (Class<?>) rawType;
               }
            }
            add(clazz, type);
         }
      }
   }

   private Type resolveType(Class<?> clazz)
   {
      if (clazz.getTypeParameters().length > 0)
      {
         TypeVariable<?>[] actualTypeParameters = clazz.getTypeParameters();
         ParameterizedType parameterizedType = new ParameterizedTypeImpl(clazz, actualTypeParameters, clazz.getDeclaringClass());
         return parameterizedType;
      }
      else
      {
         return clazz;
      }
   }

   @SuppressWarnings("unchecked")
   private void discoverFromClass(Class<?> clazz)
   {
      try
      {
         discoverTypes(resolveType(type, clazz.getGenericSuperclass()));
         for (Type c : clazz.getGenericInterfaces())
         {
            discoverTypes(resolveType(type, c));
         }
      }
      catch (AccessControlException e)
      {
         // TODO Hmm, is this a hack?
         Reflections.log.trace(SECURITY_EXCEPTION_SCANNING, clazz);
         Reflections.xLog.throwing(Level.TRACE, e);
      }
   }

   /**
    * Gets the actual types by resolving TypeParameters.
    * 
    * @param beanType
    * @param type
    * @return actual type
    */
   private Type resolveType(Type beanType, Type type)
   {
      if (type instanceof ParameterizedType)
      {
         if (beanType instanceof ParameterizedType)
         {
            return resolveParameterizedType((ParameterizedType) beanType, (ParameterizedType) type);
         }
         if (beanType instanceof Class<?>)
         {
            return resolveType(((Class<?>) beanType).getGenericSuperclass(), type);
         }
      }

      if (type instanceof TypeVariable<?>)
      {
         if (beanType instanceof ParameterizedType)
         {
            return resolveTypeParameter((ParameterizedType) beanType, (TypeVariable<?>) type);
         }
         if (beanType instanceof Class<?>)
         {
            return resolveType(((Class<?>) beanType).getGenericSuperclass(), type);
         }
      }
      return type;
   }

   private Type resolveParameterizedType(ParameterizedType beanType, ParameterizedType parameterizedType)
   {
      Type rawType = parameterizedType.getRawType();
      Type[] actualTypes = parameterizedType.getActualTypeArguments();

      Type resolvedRawType = resolveType(beanType, rawType);
      Type[] resolvedActualTypes = new Type[actualTypes.length];

      for (int i = 0; i < actualTypes.length; i++)
      {
         resolvedActualTypes[i] = resolveType(beanType, actualTypes[i]);
      }
      // reconstruct ParameterizedType by types resolved TypeVariable.
      return new ParameterizedTypeImpl(resolvedRawType, resolvedActualTypes, parameterizedType.getOwnerType());
   }

   private Type resolveTypeParameter(ParameterizedType beanType, TypeVariable<?> typeVariable)
   {
      // step1. raw type
      Class<?> actualType = (Class<?>) beanType.getRawType();
      TypeVariable<?>[] typeVariables = actualType.getTypeParameters();
      Type[] actualTypes = beanType.getActualTypeArguments();
      for (int i = 0; i < typeVariables.length; i++)
      {
         if (typeVariables[i].equals(typeVariable) && !actualTypes[i].equals(typeVariable))
         {
            return resolveType(type, actualTypes[i]);
         }
      }

      // step2. generic super class
      Type genericSuperType = actualType.getGenericSuperclass();
      Type type = resolveType(genericSuperType, typeVariable);
      if (!(type instanceof TypeVariable<?>))
      {
         return type;
      }

      // step3. generic interfaces
      for (Type interfaceType : actualType.getGenericInterfaces())
      {
         Type resolvedType = resolveType(interfaceType, typeVariable);
         if (!(resolvedType instanceof TypeVariable<?>))
         {
            return resolvedType;
         }
      }

      // don't resolve type variable
      return typeVariable;
   }

}
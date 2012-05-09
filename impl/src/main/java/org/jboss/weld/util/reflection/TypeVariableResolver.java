package org.jboss.weld.util.reflection;

import javax.enterprise.inject.spi.Bean;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;

/**
 * @author Marko Luksa
 */
public class TypeVariableResolver {

    private Class<?> beanClass;
    private HashMap<TypeVariable<?>,Type> resolvedVariables;

    public TypeVariableResolver(Class<?> beanClass) {
        if (beanClass == null) {
            throw new IllegalArgumentException("beanClass should not be null");
        }
        this.beanClass = beanClass;
    }

    public static Type resolveVariables(Bean<?> bean, Type type) {
        if (bean == null) {
            // bean is null when we're dealing with an InjectionTarget created through BeanManager.createInjectionTarget()
            // we can't resolve variables since we're missing critical info, thus we simply return the original type for now
            return type;
        }
        if (bean.getBeanClass() == null) {
            throw new IllegalArgumentException("Bean " + bean + " has null beanClass!");
        }
        return new TypeVariableResolver(bean.getBeanClass()).resolveVariablesInType(type);
    }

    public Type resolveVariablesInType(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return new ParameterizedTypeImpl(
                parameterizedType.getRawType(),
                resolveVariablesInTypes(parameterizedType.getActualTypeArguments()),
                parameterizedType.getOwnerType());
        } else if (type instanceof TypeVariable) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) type;
            return resolveTypeVariable(typeVariable);
        } else if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            Type resolvedComponentType = resolveVariablesInType(genericArrayType.getGenericComponentType());
            if (resolvedComponentType instanceof Class) {
                Class<?> componentClass = (Class<?>) resolvedComponentType;
                return Array.newInstance(componentClass, 0).getClass();
            } else {
                return new GenericArrayTypeImpl(resolvedComponentType);
            }
        } else {
            return type;
        }
    }

    private Type[] resolveVariablesInTypes(Type[] types) {
        Type[] resolvedTypes = new Type[types.length];
        for (int i = 0; i < types.length; i++) {
            resolvedTypes[i] = resolveVariablesInType(types[i]);
        }
        return resolvedTypes;
    }

    private Type resolveTypeVariable(TypeVariable<?> typeVariable) {
        if (resolvedVariables == null) {
            resolvedVariables = new HashMap<TypeVariable<?>, Type>();
            fillResolvedVariablesMap(beanClass);
        }
        return resolve(typeVariable);   // instead of resolving every time, we could also resolve all entries immediately after filling map
    }

    private void fillResolvedVariablesMap(Class<?> beanClass) {
        Type genericSuperclass = beanClass.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType parameterizedSuperClassType = (ParameterizedType) genericSuperclass;

            Type[] actualTypeArguments = parameterizedSuperClassType.getActualTypeArguments();
            TypeVariable<?>[] typeParameters = beanClass.getSuperclass().getTypeParameters();
            for (int i = 0; i < typeParameters.length; i++) {
                resolvedVariables.put(typeParameters[i], actualTypeArguments[i]);
            }
        }

        if (beanClass.getSuperclass() != null) {
            fillResolvedVariablesMap(beanClass.getSuperclass());
        }
    }

    private Type resolve(Type type) {
        if (type instanceof TypeVariable) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) type;
            if (resolvedVariables.containsKey(typeVariable)) {
                return resolve(resolvedVariables.get(typeVariable));
            }
        }
        return type;
    }

}

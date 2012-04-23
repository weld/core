package org.jboss.weld.util.reflection;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;

/**
 * @author Marko Luksa
 */
public class TypeVariableResolver {

    private Class beanClass;
    private HashMap<TypeVariable,Type> resolvedVariables;

    public TypeVariableResolver(Class beanClass) {
        if (beanClass == null) {
            throw new IllegalArgumentException("beanClass should not be null");
        }
        this.beanClass = beanClass;
    }

    public static Type resolveVariables(Class beanClass, Type type) {
        return new TypeVariableResolver(beanClass).resolveVariablesInType(type);
    }

    public Type resolveVariablesInType(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return new ParameterizedTypeImpl(
                parameterizedType.getRawType(),
                resolveVariablesInTypes(parameterizedType.getActualTypeArguments()),
                parameterizedType.getOwnerType());
        } else if (type instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable) type;
            return resolveTypeVariable(typeVariable);
        } else if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            return new GenericArrayTypeImpl(resolveVariablesInType(genericArrayType.getGenericComponentType()));
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

    private Type resolveTypeVariable(TypeVariable typeVariable) {
        if (resolvedVariables == null) {
            resolvedVariables = new HashMap<TypeVariable, Type>();
            fillResolvedVariablesMap(beanClass);
        }
        return resolve(typeVariable);   // instead of resolving every time, we could also resolve all entries immediately after filling map
    }

    private void fillResolvedVariablesMap(Class beanClass) {
        Type genericSuperclass = beanClass.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType parameterizedSuperClassType = (ParameterizedType) genericSuperclass;

            Type[] actualTypeArguments = parameterizedSuperClassType.getActualTypeArguments();
            TypeVariable[] typeParameters = beanClass.getSuperclass().getTypeParameters();
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
            TypeVariable typeVariable = (TypeVariable) type;
            if (resolvedVariables.containsKey(typeVariable)) {
                return resolve(resolvedVariables.get(typeVariable));
            }
        }
        return type;
    }

}

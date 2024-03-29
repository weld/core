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
package org.jboss.weld.util.reflection;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class ParameterizedTypeImpl implements ParameterizedType, Serializable {

    private static final long serialVersionUID = -3005183010706452884L;

    private final Type[] actualTypeArguments;
    private final Type rawType;
    private final Type ownerType;

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public ParameterizedTypeImpl(Type rawType, Type... actualTypeArguments) {
        this(rawType, actualTypeArguments, null);
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public ParameterizedTypeImpl(Type rawType, Type[] actualTypeArguments, Type ownerType) {
        this.actualTypeArguments = actualTypeArguments;
        this.rawType = rawType;
        this.ownerType = ownerType;
    }

    public Type[] getActualTypeArguments() {
        return Arrays.copyOf(actualTypeArguments, actualTypeArguments.length);
    }

    public Type getOwnerType() {
        return ownerType;
    }

    public Type getRawType() {
        return rawType;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(actualTypeArguments) ^ (ownerType == null ? 0 : ownerType.hashCode())
                ^ (rawType == null ? 0 : rawType.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof ParameterizedType) {
            ParameterizedType that = (ParameterizedType) obj;
            Type thatOwnerType = that.getOwnerType();
            Type thatRawType = that.getRawType();
            return (ownerType == null ? thatOwnerType == null : ownerType.equals(thatOwnerType))
                    && (rawType == null ? thatRawType == null : rawType.equals(thatRawType))
                    && Arrays.equals(actualTypeArguments, that.getActualTypeArguments());
        } else {
            return false;
        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(rawType);
        if (actualTypeArguments.length > 0) {
            sb.append("<");
            for (Type actualType : actualTypeArguments) {
                sb.append(actualType);
                sb.append(",");
            }
            sb.delete(sb.length() - 1, sb.length());
            sb.append(">");
        }
        return sb.toString();
    }
}

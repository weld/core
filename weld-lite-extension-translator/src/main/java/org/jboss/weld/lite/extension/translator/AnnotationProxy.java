package org.jboss.weld.lite.extension.translator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import jakarta.enterprise.lang.model.AnnotationMember;

final class AnnotationProxy {

    private AnnotationProxy() {
    }

    static <T extends Annotation> T create(Class<T> clazz, Map<String, AnnotationMember> members) {
        Class<?>[] interfaces = new Class[] { clazz };
        Map<String, Object> values = new HashMap<>();
        for (Map.Entry<String, AnnotationMember> member : members.entrySet()) {
            values.put(member.getKey(), ((AnnotationMemberImpl) member.getValue()).value);
        }
        // include default values methods where no values were specified
        for (Method method : clazz.getDeclaredMethods()) {
            String methodName = method.getName();
            if (!values.containsKey(methodName)) {
                Object value = method.getDefaultValue();
                if (value != null) {
                    values.put(methodName, value);
                }
            }
        }
        return (T) java.lang.reflect.Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces,
                new AnnotationInvocationHandler(clazz, values));
    }

    // Per java.lang.annotation.Annotation#hashCode, the hash code of an annotation member value
    // must use Arrays.hashCode for array types (both primitive and reference) and Object.hashCode
    // for scalar values.
    private static int memberValueHashCode(Object value) {
        if (value instanceof boolean[]) {
            return Arrays.hashCode((boolean[]) value);
        } else if (value instanceof byte[]) {
            return Arrays.hashCode((byte[]) value);
        } else if (value instanceof char[]) {
            return Arrays.hashCode((char[]) value);
        } else if (value instanceof double[]) {
            return Arrays.hashCode((double[]) value);
        } else if (value instanceof float[]) {
            return Arrays.hashCode((float[]) value);
        } else if (value instanceof int[]) {
            return Arrays.hashCode((int[]) value);
        } else if (value instanceof long[]) {
            return Arrays.hashCode((long[]) value);
        } else if (value instanceof short[]) {
            return Arrays.hashCode((short[]) value);
        } else if (value.getClass().isArray()) {
            return Arrays.hashCode((Object[]) value);
        }
        return value.hashCode();
    }

    private static final class AnnotationInvocationHandler implements java.lang.reflect.InvocationHandler {
        private final Class<? extends Annotation> clazz;
        private final Map<String, Object> members;

        AnnotationInvocationHandler(Class<? extends Annotation> clazz, Map<String, Object> members) {
            this.clazz = clazz;
            this.members = members;
        }

        @Override
        public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Exception {
            if ("annotationType".equals(method.getName())) {
                return clazz;
            } else if ("toString".equals(method.getName())) {
                StringJoiner joiner = new StringJoiner(", ", "(", ")");
                joiner.setEmptyValue("");
                for (Map.Entry<String, Object> member : members.entrySet()) {
                    joiner.add(member.getKey() + "=" + member.getValue());
                }
                return "@" + clazz.getName() + joiner.toString();
            } else if ("equals".equals(method.getName())) {
                // Per java.lang.annotation.Annotation#equals: two annotations are equal if they
                // have the same annotation type and all members are equal.
                Object other = args[0];
                if (other instanceof Annotation) {
                    Annotation that = (Annotation) other;
                    if (clazz.equals(that.annotationType())) {
                        // Note: 'member' (the loop variable) is the annotation member accessor method,
                        // NOT 'method' (the invoke() parameter, which is the equals method itself).
                        for (java.lang.reflect.Method member : clazz.getDeclaredMethods()) {
                            Object thisValue = members.get(member.getName());
                            Object thatValue = member.invoke(that);
                            if (!Objects.deepEquals(thisValue, thatValue)) {
                                return false;
                            }
                        }
                        return true;
                    }
                }
                return false;
            } else if ("hashCode".equals(method.getName())) {
                // Per java.lang.annotation.Annotation#hashCode: the hash code is the sum of
                // (127 * memberName.hashCode()) ^ memberValueHashCode for each member.
                // For zero-member annotations, this is 0.
                int result = 0;
                for (Map.Entry<String, Object> entry : members.entrySet()) {
                    result += (127 * entry.getKey().hashCode()) ^ memberValueHashCode(entry.getValue());
                }
                return result;
            } else {
                return members.get(method.getName());
            }
        }
    }
}

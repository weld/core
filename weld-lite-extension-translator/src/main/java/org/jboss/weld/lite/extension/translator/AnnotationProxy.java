package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.lang.model.AnnotationMember;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

final class AnnotationProxy {

    private AnnotationProxy() {
    }

    static <T extends Annotation> T create(Class<T> clazz, Map<String, AnnotationMember> members) {
        Class<?>[] interfaces = new Class[]{clazz};
        Map<String, Object> values = new HashMap<>();
        for (Map.Entry<String, AnnotationMember> member : members.entrySet()) {
            values.put(member.getKey(), ((AnnotationMemberImpl) member.getValue()).value);
        }
        return (T) java.lang.reflect.Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces,
                new AnnotationInvocationHandler(clazz, values));
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
                Object other = args[0];
                if (other instanceof Annotation) {
                    Annotation that = (Annotation) other;
                    if (clazz.equals(that.annotationType())) {
                        for (java.lang.reflect.Method member : clazz.getDeclaredMethods()) {
                            Object thisValue = members.get(member.getName());
                            Object thatValue = method.invoke(that);
                            if (!Objects.deepEquals(thisValue, thatValue)) {
                                return false;
                            }
                        }
                        return true;
                    }
                }
                return false;
            } else if ("hashCode".equals(method.getName())) {
                Object[] components = new Object[members.size() + 1];
                components[0] = clazz;
                int i = 1;
                for (Object memberValue : members.values()) {
                    components[i++] = memberValue;
                }
                return Objects.hash(components);
            } else {
                return members.get(method.getName());
            }
        }
    }
}

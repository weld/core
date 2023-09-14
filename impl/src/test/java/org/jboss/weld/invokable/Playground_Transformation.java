package org.jboss.weld.invokable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Consumer;

public class Playground_Transformation {
    public static void main(String[] args) throws Throwable {
        Method targetMethod = Playground_Transformation.class.getMethod("hello", int.class, String.class, int.class);
        boolean isStaticMethod = Modifier.isStatic(targetMethod.getModifiers());

        MethodHandle mh = MethodHandles.lookup().unreflect(targetMethod);
        System.out.println("!!!!!!! 1 " + mh.type());

        int instanceArguments = isStaticMethod ? 0 : 1;

        // target instance
        Method instanceTransformer = Playground_Transformation.class.getMethod("transformInstance",
                Playground_Transformation.class, Consumer.class);
        if (instanceTransformer != null && !isStaticMethod) {
            MethodHandle transformer = MethodHandles.lookup().unreflect(instanceTransformer);
            if (transformer.type().parameterCount() == 1) { // no cleanup
                mh = MethodHandles.filterArguments(mh, 0, transformer);
            } else if (transformer.type().parameterCount() == 2) { // cleanup
                transformer = transformer.asType(transformer.type().changeParameterType(1, CleanupActions.class));
                mh = MethodHandles.collectArguments(mh, 0, transformer);
                instanceArguments++;
            } else {
                throw new AssertionError();
            }
        }
        System.out.println("!!!!!!! 2 " + mh.type());

        // arguments
        Method[] argumentTransformers = {
                null, //Playground_Transformation.class.getMethod("transform1", int.class, Consumer.class),
                Playground_Transformation.class.getMethod("transform2", int.class),
                Playground_Transformation.class.getMethod("transform1", int.class, Consumer.class),
        };
        for (int i = targetMethod.getParameterCount() - 1; i >= 0; i--) {
            if (argumentTransformers[i] != null) {
                int position = instanceArguments + i;
                MethodHandle transformer = MethodHandles.lookup().unreflect(argumentTransformers[i]);
                if (transformer.type().parameterCount() == 1) { // no cleanup
                    mh = MethodHandles.filterArguments(mh, position, transformer);
                } else if (transformer.type().parameterCount() == 2) { // cleanup
                    transformer = transformer.asType(transformer.type().changeParameterType(1, CleanupActions.class));
                    mh = MethodHandles.collectArguments(mh, position, transformer);
                } else {
                    throw new AssertionError();
                }
            }
        }
        System.out.println("!!!!!!! 3 " + mh.type());

        // return value
        Method returnValueTransformer = Playground_Transformation.class.getMethod("transformResult", Object.class);
        if (returnValueTransformer != null) {
            MethodHandle transformer = MethodHandles.lookup().unreflect(returnValueTransformer);
            transformer = transformer.asType(transformer.type().changeParameterType(0, targetMethod.getReturnType()));
            mh = MethodHandles.filterReturnValue(mh, transformer);
        }
        System.out.println("!!!!!!! 4 " + mh.type());

        MethodType incomingType = MethodType.methodType(mh.type().returnType(), CleanupActions.class);
        for (Class<?> paramType : mh.type().parameterArray()) {
            if (paramType != CleanupActions.class) {
                incomingType = incomingType.appendParameterTypes(paramType);
            }
        }
        int[] reordering = new int[mh.type().parameterCount()];
        int paramCounter = 1;
        for (int i = 0; i < reordering.length; i++) {
            if (mh.type().parameterType(i) == CleanupActions.class) {
                reordering[i] = 0;
            } else {
                reordering[i] = paramCounter;
                paramCounter++;
            }
        }
        mh = MethodHandles.permuteArguments(mh, incomingType, reordering);
        System.out.println("!!!!!!! 5 " + mh.type());

        CleanupActions cleanup = new CleanupActions();
        Object result = mh.invoke(cleanup, new Playground_Transformation(), 10, 20, 30);
        System.out.println(Arrays.toString((char[]) result));
        cleanup.cleanup();
    }

    public static Playground_Transformation transformInstance(Playground_Transformation instance, Consumer<Runnable> cleanup) {
        cleanup.accept(() -> {
            System.out.println("cleanup instance");
        });
        return instance;
    }

    public static char[] transformResult(Object value) {
        return value.toString().toCharArray();
    }

    public static int transform1(int param, Consumer<Runnable> cleanup) {
        cleanup.accept(() -> {
            System.out.println("cleanup " + param);
        });
        return param + 5;
    }

    public static String transform2(int param) {
        return "" + (param + 1);
    }

    public String hello(int param1, String param2, int param3) {
        return "hello_" + param1 + "_" + param2 + "_" + param3;
    }
}

package org.jboss.weld.invokable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class Playground_Cleanup {
    public static void main(String[] args) throws Throwable {
        MethodHandle mh = MethodHandleUtils.createMethodHandle(Playground_Cleanup.class.getMethod("hello", int.class));
        System.out.println("!!!!!!! 1 " + mh.type());

        mh = MethodHandles.dropArguments(mh, 0, CleanupActions.class);
        System.out.println("!!!!!!! 2 " + mh.type());

        MethodHandle cleanupMethod = mh.type().returnType() == void.class
                ? MethodHandleUtils.createMethodHandle(
                        CleanupActions.class.getDeclaredMethod("run", Throwable.class, CleanupActions.class))
                : MethodHandleUtils.createMethodHandle(
                        CleanupActions.class.getDeclaredMethod("run", Throwable.class, Object.class, CleanupActions.class));
        System.out.println("cleanup pre-adapt: " + cleanupMethod.type());

        if (mh.type().returnType() != void.class) {
            cleanupMethod = cleanupMethod.asType(cleanupMethod.type()
                    .changeReturnType(mh.type().returnType())
                    .changeParameterType(1, mh.type().returnType()));
        }
        System.out.println("cleanup post-adapt: " + cleanupMethod.type());

        mh = MethodHandles.tryFinally(mh, cleanupMethod);
        System.out.println("!!!!!!! 3 " + mh.type());

        CleanupActions cleanup = new CleanupActions();
        cleanup.accept(() -> {
            System.out.println("cleanup!");
        });
        System.out.println(mh.invoke(cleanup, new Playground_Cleanup(), 42));
    }

    public String hello(int param) {
        return "hello_" + param;
    }
}

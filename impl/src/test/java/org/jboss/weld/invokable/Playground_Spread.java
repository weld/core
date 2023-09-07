package org.jboss.weld.invokable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Playground_Spread {
    public static void main(String[] args) throws Throwable {
        Method targetMethod = Playground_Spread.class.getMethod("hello", int.class);
        boolean isStaticMethod = Modifier.isStatic(targetMethod.getModifiers());

        MethodHandle mh = MethodHandleUtils.createMethodHandle(targetMethod);
        System.out.println("!!!!!!! 1 " + mh.type());

        mh = MethodHandles.dropArguments(mh, 0, CleanupActions.class);
        System.out.println("!!!!!!! 2 " + mh.type());

        // spread argument array into individual arguments
        if (isStaticMethod) {
            // keep 1 leading argument, CleanupActions
            MethodHandle invoker = MethodHandles.spreadInvoker(mh.type(), 1);
            invoker = MethodHandles.insertArguments(invoker, 0, mh);
            invoker = MethodHandles.dropArguments(invoker, 1, Object.class);
            mh = invoker;
        } else {
            // keep 2 leading arguments, CleanupActions and the target instance
            MethodHandle invoker = MethodHandles.spreadInvoker(mh.type(), 2);
            invoker = MethodHandles.insertArguments(invoker, 0, mh);
            mh = invoker;
        }

        System.out.println("!!!!!!! 3 " + mh.type());

        System.out.println(mh.invoke(new CleanupActions(), new Playground_Spread(), new Object[] {42}));
    }

    public String hello(int param) {
        return "hello_" + param;
    }

    public static String staticHello(int param) {
        return "static_hello_" + param;
    }
}

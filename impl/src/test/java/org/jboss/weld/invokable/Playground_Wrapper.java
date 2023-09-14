package org.jboss.weld.invokable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;

import jakarta.enterprise.invoke.Invoker;

public class Playground_Wrapper {
    public static void main(String[] args) throws Throwable {
        MethodHandle mh = MethodHandleUtils.createMethodHandle(Playground_Wrapper.class.getMethod("hello", Object[].class));
        System.out.println("!!!!!!! 1 " + mh.type());

        InvokerImpl<?, ?> invoker = new InvokerImpl<>(mh);

        MethodHandle mh2 = MethodHandleUtils.createMethodHandle(
                Playground_Wrapper.class.getMethod("wrap", Playground_Wrapper.class, Object[].class, Invoker.class));
        mh = MethodHandles.insertArguments(mh2, 2, invoker);
        System.out.println("!!!!!!! 2 " + mh.type());

        System.out.println(mh.invoke(new Playground_Wrapper(), new Object[] { 42 }));
    }

    public static String wrap(Playground_Wrapper instance, Object[] arguments, Invoker<Playground_Wrapper, String> invoker) {
        return "wrapped_" + instance + "_" + Arrays.toString(arguments) + "___" + invoker.invoke(instance, arguments);
    }

    public String hello(Object[] params) {
        return "hello_" + Arrays.toString(params);
    }

    public static String staticHello(Object instance, Object[] params) {
        return "static_hello_" + instance + "_" + Arrays.toString(params);
    }
}

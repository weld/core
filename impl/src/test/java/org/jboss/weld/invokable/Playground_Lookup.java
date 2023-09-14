package org.jboss.weld.invokable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

public class Playground_Lookup {
    public static void main(String[] args) throws Throwable {
        MethodHandle lookupMethod = MethodHandleUtils
                .createMethodHandle(Playground_Lookup.class.getMethod("lookup", CleanupActions.class));

        Method targetMethod = Playground_Lookup.class.getMethod("hello", CleanupActions.class, Playground_Lookup.class,
                int.class, long.class, char.class);
        boolean isStaticMethod = false;

        MethodHandle mh = MethodHandleUtils.createMethodHandle(targetMethod);
        System.out.println("!!!!!!! 1 " + mh.type());

        MethodType originalType = mh.type();

        int positionsBeforeArguments = 1; // first `CleanupActions` we need to preserve for transformations
        if (!isStaticMethod) {
            positionsBeforeArguments++; // the target instance
        }

        // instance lookup
        boolean instanceLookup = true;
        if (instanceLookup && !isStaticMethod) {
            Class<?> type = originalType.parameterType(1);
            MethodHandle instanceLookupMethod = lookupMethod;
            instanceLookupMethod = instanceLookupMethod.asType(instanceLookupMethod.type()
                    .changeReturnType(type));
            instanceLookupMethod = MethodHandles.dropArguments(instanceLookupMethod, 0, type);
            mh = MethodHandles.collectArguments(mh, 1, instanceLookupMethod);
            positionsBeforeArguments++; // second `CleanupActions`
        }
        System.out.println("!!!!!!! 2 " + mh.type());

        // arguments lookup
        // backwards iteration for correct construction of the resulting parameter list
        boolean[] argumentsLookup = { true, false, true };
        for (int i = argumentsLookup.length - 1; i >= 0; i--) {
            if (argumentsLookup[i]) {
                Class<?> type = originalType.parameterType(i + (isStaticMethod ? 1 : 2));
                int position = positionsBeforeArguments + i;
                MethodHandle argumentLookupMethod = lookupMethod;
                argumentLookupMethod = argumentLookupMethod.asType(argumentLookupMethod.type()
                        .changeReturnType(type));
                argumentLookupMethod = MethodHandles.dropArguments(argumentLookupMethod, 0, type);
                mh = MethodHandles.collectArguments(mh, position, argumentLookupMethod);
            }
        }
        System.out.println("!!!!!!! 3 " + mh.type());

        // argument reshuffling to support cleanup tasks for input lookups
        //
        // for each input that has a lookup, the corresponding argument
        // has a second argument inserted immediately after it, the `CleanupActions` instance;
        // application of the lookup replaces the two arguments with the result
        //
        // inputs without lookup are left intact and application of the transformer
        // only replaces the single argument
        {
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
            mh = MethodHandles.permuteArguments(mh, originalType, reordering);
        }

        System.out.println("!!!!!!! 4 " + mh.type());

        CleanupActions cleanup = new CleanupActions();
        System.out.println(mh.invoke(cleanup, new Playground_Lookup(), 0, 13L, '\0'));
    }

    // this is a little crazy, but good enough for a playground
    //
    // due to how the method handle tree is constructed, the `lookup` method
    // is called in the following order:
    // - arguments first, from left to right
    // - target instance last
    //
    // note that this only applies here; in general, the order of lookups is not specified
    private static int lookupCounter = -1;

    public static Object lookup(CleanupActions cleanup) {
        lookupCounter++;
        if (lookupCounter == 0) {
            return 42;
        } else if (lookupCounter == 1) {
            return 'x';
        } else if (lookupCounter == 2) {
            return new Playground_Lookup();
        } else {
            throw new AssertionError();
        }
    }

    public static String hello(CleanupActions cleanup, Playground_Lookup instance, int param1, long param2, char param3) {
        return "hello_" + param1 + "_" + param2 + "_" + param3;
    }
}

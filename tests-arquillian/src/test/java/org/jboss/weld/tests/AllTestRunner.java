package org.jboss.weld.tests;

import org.jboss.shrinkwrap.impl.base.URLPackageScanner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AllTestRunner extends Suite {
    public AllTestRunner(Class<?> superClass, RunnerBuilder builder) throws InitializationError {
        super(builder, superClass, getAllClasses());
    }

    private static Class<?>[] getAllClasses() {
        final List<Class<?>> classes = new ArrayList<Class<?>>();
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URLPackageScanner.newInstance(
                true,
                classLoader,
                new URLPackageScanner.Callback() {
                    public void classFound(String className) {
                        if (!className.endsWith("Test")) {
                            return;
                        }
                        if (className.substring(className.lastIndexOf('.') + 1).length() <= 4) {
                            return;
                        }
                        try {
                            classes.add(classLoader.loadClass(className));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                AllTestRunner.class.getPackage().getName()).scanPackage();
        //ExampleTest.class.getPackage()).scanPackage();

        Collections.sort(classes, new Comparator<Class<?>>() {
            public int compare(Class<?> o1, Class<?> o2) {
                return o1.getPackage().getName().compareTo(o2.getPackage().getName());
            }
        });
        return classes.toArray(new Class<?>[]{});
    }
}

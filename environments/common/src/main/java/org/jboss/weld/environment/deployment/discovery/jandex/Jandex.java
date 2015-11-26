package org.jboss.weld.environment.deployment.discovery.jandex;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.environment.util.Reflections;
import org.jboss.weld.resources.spi.ResourceLoader;

/**
 * @author Tomas Remes
 */
public class Jandex {

    public static final String INDEX_ATTRIBUTE_NAME = JandexDiscoveryStrategy.class.getPackage().getName() + ".index";

    private static final String JANDEX_INDEX_CLASS_NAME = "org.jboss.jandex.Index";
    public static final String JANDEX_DISCOVERY_STRATEGY_CLASS_NAME = "org.jboss.weld.environment.deployment.discovery.jandex.JandexDiscoveryStrategy";

    private Jandex() {
    }

    public static boolean isJandexAvailable(ResourceLoader resourceLoader) {
        return Reflections.isClassLoadable(resourceLoader, JANDEX_INDEX_CLASS_NAME);
    }

    public static JandexDiscoveryStrategy createJandexDiscoveryStrategy(ResourceLoader resourceLoader, Bootstrap bootstrap,
            Set<Class<? extends Annotation>> initialBeanDefiningAnnotations)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Class<JandexDiscoveryStrategy> strategyClass = Reflections.loadClass(resourceLoader, JANDEX_DISCOVERY_STRATEGY_CLASS_NAME);
        return SecurityActions
                .newConstructorInstance(strategyClass, new Class<?>[] { ResourceLoader.class, Bootstrap.class, Set.class }, resourceLoader, bootstrap,
                        initialBeanDefiningAnnotations);

    }

}

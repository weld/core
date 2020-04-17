package org.jboss.weld.environment.se.test.beandiscovery.priority;

import static jakarta.interceptor.Interceptor.Priority.APPLICATION;

import jakarta.annotation.Priority;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;

@Priority(APPLICATION)
@Decorator
public class EqualizingDecorator implements SoundSource {

    public static int invocations = 0;

    @Inject
    @Delegate
    private SoundSource source;

    @Override
    public String generateSound() {
        invocations++;
        return source.generateSound();
    }

    static void reset() {
        invocations = 0;
    }

}

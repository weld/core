package org.jboss.weld.environment.se.test.beandiscovery.priority;

import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;
import static javax.interceptor.Interceptor.Priority.APPLICATION;

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

}

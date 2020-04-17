package org.jboss.weld.environment.se.test.beandiscovery.priority;

import static jakarta.interceptor.Interceptor.Priority.APPLICATION;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;

@Priority(APPLICATION)
@Alternative
@Normalized
@Dependent
public class SineWaveGenerator implements SoundSource {

    @Override
    public String generateSound() {
        return "beeeeeeeeep";
    }

}

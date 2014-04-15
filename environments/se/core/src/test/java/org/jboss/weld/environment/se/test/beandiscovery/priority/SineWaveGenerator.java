package org.jboss.weld.environment.se.test.beandiscovery.priority;

import static javax.interceptor.Interceptor.Priority.APPLICATION;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

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

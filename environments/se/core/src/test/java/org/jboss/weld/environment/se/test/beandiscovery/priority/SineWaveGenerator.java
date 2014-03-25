package org.jboss.weld.environment.se.test.beandiscovery.priority;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import static javax.interceptor.Interceptor.Priority.APPLICATION;

@Priority(APPLICATION)
@Alternative
@Normalized
public class SineWaveGenerator implements SoundSource {

    @Override
    public String generateSound() {
        return "beeeeeeeeep";
    }

}

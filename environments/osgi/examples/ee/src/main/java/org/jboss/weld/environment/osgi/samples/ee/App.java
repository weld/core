package org.jboss.weld.environment.osgi.samples.ee;

import java.util.concurrent.atomic.AtomicBoolean;
import org.jboss.weld.environment.osgi.api.events.Invalid;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import org.jboss.weld.environment.osgi.api.events.Valid;

@ApplicationScoped
public class App {

    private AtomicBoolean valid = new AtomicBoolean(false);

    public void validate(@Observes Valid event) {
        valid.getAndSet(true);
    }

    public void invalidate(@Observes Invalid event) {
        valid.getAndSet(false);
    }

    public boolean isValid() {
        return valid.get();
    }
}

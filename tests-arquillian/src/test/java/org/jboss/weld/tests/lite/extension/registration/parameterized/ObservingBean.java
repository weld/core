package org.jboss.weld.tests.lite.extension.registration.parameterized;

import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;

@Dependent
public class ObservingBean implements MyGenericService<String> {

    @Override
    public String hello() {
        return "observing";
    }

    void observeListOfString(@Observes List<String> list) {
    }

    // This observer should NOT match Collection<String> or List<String>
    void observeListOfInteger(@Observes List<Integer> list) {
    }
}

package org.jboss.weld.tests.producer.method.weld994;

import javax.enterprise.context.ConversationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author Marko Luksa
 */
@ConversationScoped
public class PassivatingBean implements Serializable {

    @Inject
    private Instance<UnserializableObject> unserializableObjectInstance;

    public Instance<UnserializableObject> getUnserializableObjectInstance() {
        return unserializableObjectInstance;
    }
}

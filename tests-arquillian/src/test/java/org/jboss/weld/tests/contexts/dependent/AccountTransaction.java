package org.jboss.weld.tests.contexts.dependent;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.Dependent;

@Transactional
@Dependent
public class AccountTransaction {
    public static boolean destroyed = false;

    public void execute() {
    }

    @PreDestroy
    public void destroy() {
        destroyed = true;
    }
}

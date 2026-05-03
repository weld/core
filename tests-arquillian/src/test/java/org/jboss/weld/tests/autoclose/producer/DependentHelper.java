package org.jboss.weld.tests.autoclose.producer;

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.Dependent;

import org.jboss.weld.test.util.ActionSequence;

@Dependent
public class DependentHelper {
    public static final AtomicBoolean destroyed = new AtomicBoolean(false);

    public static void reset() {
        destroyed.set(false);
    }

    public String ping() {
        return "alive";
    }

    @PreDestroy
    public void preDestroy() {
        destroyed.set(true);
        ActionSequence.addAction("DependentHelper.preDestroy");
    }
}

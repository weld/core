package org.jboss.weld.tests.resolution.weld1075;

import java.util.UUID;

import jakarta.enterprise.context.Dependent;

@Dependent
public class ConcreteClass3 implements Interface1<UUID, String> {
}

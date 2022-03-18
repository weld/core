package org.jboss.weld.tests.resolution.weld1075;


import jakarta.enterprise.context.Dependent;

import java.util.UUID;

@Dependent
public class ConcreteClass1 extends AbstractClass2<String, Integer, UUID> {
}

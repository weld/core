package org.jboss.weld.tests.producer.alternative.priority.broken;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

@ApplicationScoped
@MyOtherStereotype
@MyStereotype
@Alternative
public class ClassBean {
}

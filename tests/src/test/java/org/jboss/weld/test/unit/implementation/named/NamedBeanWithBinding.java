package org.jboss.weld.test.unit.implementation.named;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

/**
 * @author Dan Allen
 */
public
@Named
@Important
@RequestScoped
class NamedBeanWithBinding {
}

package org.jboss.weld.test.resolution.named;

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

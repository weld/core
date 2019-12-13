package org.jboss.weld.tests.extensions;

import javax.ejb.Stateless;
import jakarta.enterprise.inject.Alternative;

@Alternative
@Stateless
public class Terminus implements Station {

}

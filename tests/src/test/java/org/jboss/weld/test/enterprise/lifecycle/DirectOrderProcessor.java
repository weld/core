package org.jboss.weld.test.enterprise.lifecycle;

import javax.ejb.Stateful;

@Stateful
public class DirectOrderProcessor extends OrderProcessor implements DirectOrderProcessorLocal
{

}

package org.jboss.webbeans.test.unit.implementation.enterprise.lifecycle;

import javax.ejb.Stateful;

@Stateful
public class DirectOrderProcessor extends OrderProcessor implements DirectOrderProcessorLocal
{

}

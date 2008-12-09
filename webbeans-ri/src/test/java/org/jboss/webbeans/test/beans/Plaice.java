package org.jboss.webbeans.test.beans;

import javax.webbeans.Named;

import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.Whitefish;

@AnotherDeploymentType
@Whitefish
@Named("whitefish")
public final class Plaice implements Animal
{

}

package org.jboss.webbeans.test.components;

import javax.webbeans.Named;

import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.Whitefish;

@AnotherDeploymentType
@Whitefish
@Named("whitefish")
public class Plaice implements Animal
{

}

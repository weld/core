package org.jboss.webbeans.test.beans;

import javax.webbeans.Named;
import javax.webbeans.RequestScoped;

import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.Whitefish;

@AnotherDeploymentType
@Whitefish
@Named("whitefish")
@RequestScoped
public final class Plaice implements Animal
{

}

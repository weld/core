package org.jboss.webbeans.test;

import javax.webbeans.Event;
import javax.webbeans.Observable;
import javax.webbeans.RequestScoped;

@RequestScoped
public class MyTest
{
   @Observable Event<Param> paramEvent;
}

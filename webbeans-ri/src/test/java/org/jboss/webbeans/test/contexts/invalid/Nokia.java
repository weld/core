package org.jboss.webbeans.test.contexts.invalid;

import java.io.Serializable;

import javax.webbeans.Current;
import javax.webbeans.SessionScoped;

import org.jboss.webbeans.test.contexts.valid.City;

@SessionScoped
public class Nokia extends City implements Serializable
{
   @Current
   private Violation reference;
}

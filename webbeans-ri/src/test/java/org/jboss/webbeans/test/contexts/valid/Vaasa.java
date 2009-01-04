package org.jboss.webbeans.test.contexts.valid;

import java.io.Serializable;

import javax.webbeans.Current;
import javax.webbeans.SessionScoped;

@SessionScoped
public class Vaasa implements Serializable
{
   @Current private Helsinki ejb;
}

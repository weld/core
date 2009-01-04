package org.jboss.webbeans.test.contexts.valid;

import java.io.Serializable;

import javax.webbeans.Current;
import javax.webbeans.SessionScoped;

import org.jboss.webbeans.test.contexts.invalid.Violation;

@SessionScoped
public class Hyvinkaa implements Serializable
{
   @Current
   private transient Violation reference;
}

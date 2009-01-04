package org.jboss.webbeans.test.contexts.invalid;

import javax.webbeans.Current;

public class Lieto
{
   public Lieto(@Current Violation reference) {
   }
}

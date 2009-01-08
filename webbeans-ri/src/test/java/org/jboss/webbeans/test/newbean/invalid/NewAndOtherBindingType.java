package org.jboss.webbeans.test.newbean.invalid;

import javax.webbeans.Current;
import javax.webbeans.New;

import org.jboss.webbeans.test.newbean.valid.WrappedSimpleBean;

public class NewAndOtherBindingType
{
   public @New @Current WrappedSimpleBean violation;
}

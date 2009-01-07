package org.jboss.webbeans.test.newbean.invalid;

import javax.webbeans.Current;
import javax.webbeans.New;

import org.jboss.webbeans.test.newbean.valid.WrappedBean;

public class NewAndOtherBindingType
{
   public @New @Current WrappedBean violation;
}

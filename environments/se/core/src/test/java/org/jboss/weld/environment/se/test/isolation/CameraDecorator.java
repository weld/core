package org.jboss.weld.environment.se.test.isolation;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

@Decorator
public class CameraDecorator implements Camera {
    
    @Inject
    @Delegate
    private Camera delegate;
    
    public static int invocations = 0;

    @Override
    public void capture() {
        invocations++;
        delegate.capture();
    }
    
}

package javax.enterprise.inject.spi;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;

public interface InjectionTarget<X> 
{
    public X produce(CreationalContext<X> ctx);
    
    public void inject(X instance, CreationalContext<X> ctx);
    
    public void dispose(X instance);
    
    public void destroy(X instance);
    
    public Set<InjectionPoint> getInjectionPoints();
    
}

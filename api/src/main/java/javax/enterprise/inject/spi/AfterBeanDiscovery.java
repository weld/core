package javax.enterprise.inject.spi;

public interface AfterBeanDiscovery {
    public void addDefinitionError(Throwable t);
}

module org.jboss.weld.module.ejb {
    requires org.jboss.weld.core;
    requires org.jboss.weld.spi;
    requires static jakarta.ejb;
    requires static jakarta.transaction;

    provides org.jboss.weld.module.WeldModule
        with org.jboss.weld.module.ejb.WeldEjbModule;
}

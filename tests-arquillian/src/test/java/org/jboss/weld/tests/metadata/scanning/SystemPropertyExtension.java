package org.jboss.weld.tests.metadata.scanning;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

public class SystemPropertyExtension implements Extension {

    public static final String SET_PROPERTY_1 = SystemPropertyExtension.class.getPackage().getName() + ".setProperty1";
    public static final String SET_PROPERTY_1_VALUE = SystemPropertyExtension.class.getPackage().getName() + ".setProperty1Value";
    public static final String SET_PROPERTY_2 = SystemPropertyExtension.class.getPackage().getName() + ".setProperty2";
    public static final String SET_PROPERTY_2_VALUE = SystemPropertyExtension.class.getPackage().getName() + ".setProperty2Value";
    public static final String SET_PROPERTY_2_OTHER_VALUE = SystemPropertyExtension.class.getPackage().getName() + ".setProperty2ValueOther";
    public static final String UNSET_PROPERTY_1 = SystemPropertyExtension.class.getPackage().getName() + ".testUnsetProperty1";

    public void setProperties(@Observes BeforeBeanDiscovery event) {
        System.setProperty(SET_PROPERTY_1, SET_PROPERTY_1_VALUE);
        System.setProperty(SET_PROPERTY_2, SET_PROPERTY_2_VALUE);
    }
}

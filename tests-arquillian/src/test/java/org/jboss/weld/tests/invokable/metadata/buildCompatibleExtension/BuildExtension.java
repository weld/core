package org.jboss.weld.tests.invokable.metadata.buildCompatibleExtension;

import jakarta.enterprise.inject.build.compatible.spi.BeanInfo;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.ClassConfig;
import jakarta.enterprise.inject.build.compatible.spi.Discovery;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.enterprise.inject.build.compatible.spi.MetaAnnotations;
import jakarta.enterprise.inject.build.compatible.spi.Registration;
import org.jboss.weld.tests.invokable.metadata.common.ClassLevelDirectDeclaration;
import org.jboss.weld.tests.invokable.metadata.common.ClassLevelIndirectDeclaration;
import org.jboss.weld.tests.invokable.metadata.common.ClassLevelViaExtension;
import org.jboss.weld.tests.invokable.metadata.common.DefinitelyNotInvokable;
import org.jboss.weld.tests.invokable.metadata.common.MethodLevelDirectDeclaration;
import org.jboss.weld.tests.invokable.metadata.common.MethodLevelIndirectDeclaration;
import org.jboss.weld.tests.invokable.metadata.common.MethodLevelViaExtension;
import org.jboss.weld.tests.invokable.metadata.common.TransitivelyInvokable;
import org.jboss.weld.tests.invokable.metadata.common.UnannotatedBean;
import org.junit.Assert;

public class BuildExtension implements BuildCompatibleExtension {

    public static int timesInvoked = 0;

    @Discovery
    public void discovery(MetaAnnotations metaAnnotations) {
        timesInvoked++;
        metaAnnotations.addInvokable(DefinitelyNotInvokable.class);
    }

    @Enhancement(types = UnannotatedBean.class)
    public void enhancement(ClassConfig c) {
        timesInvoked++;
        c.addAnnotation(TransitivelyInvokable.class);
    }

    @Registration(types = ClassLevelDirectDeclaration.class)
    public void registration1(BeanInfo b) {
        timesInvoked++;
        Assert.assertEquals(2, b.invokableMethods().size());
    }

    @Registration(types = ClassLevelIndirectDeclaration.class)
    public void registration2(BeanInfo b) {
        timesInvoked++;
        Assert.assertEquals(2, b.invokableMethods().size());
    }

    @Registration(types = MethodLevelDirectDeclaration.class)
    public void registration3(BeanInfo b) {
        timesInvoked++;
        Assert.assertEquals(1, b.invokableMethods().size());
    }

    @Registration(types = MethodLevelIndirectDeclaration.class)
    public void registration4(BeanInfo b) {
        timesInvoked++;
        Assert.assertEquals(1, b.invokableMethods().size());
    }

    @Registration(types = ClassLevelViaExtension.class)
    public void registration5(BeanInfo b) {
        timesInvoked++;
        Assert.assertEquals(2, b.invokableMethods().size());
    }

    @Registration(types = MethodLevelViaExtension.class)
    public void registration6(BeanInfo b) {
        timesInvoked++;
        Assert.assertEquals(1, b.invokableMethods().size());
    }

    @Registration(types = UnannotatedBean.class)
    public void registration7(BeanInfo b) {
        timesInvoked++;
        Assert.assertEquals(2, b.invokableMethods().size());
    }
}

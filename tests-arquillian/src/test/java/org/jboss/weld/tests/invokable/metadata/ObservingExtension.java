package org.jboss.weld.tests.invokable.metadata;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessManagedBean;
import org.jboss.weld.tests.invokable.metadata.common.ClassLevelDirectDeclaration;
import org.jboss.weld.tests.invokable.metadata.common.ClassLevelIndirectDeclaration;
import org.jboss.weld.tests.invokable.metadata.common.ClassLevelViaExtension;
import org.jboss.weld.tests.invokable.metadata.common.DefinitelyNotInvokable;
import org.jboss.weld.tests.invokable.metadata.common.MethodLevelDirectDeclaration;
import org.jboss.weld.tests.invokable.metadata.common.MethodLevelIndirectDeclaration;
import org.jboss.weld.tests.invokable.metadata.common.MethodLevelViaExtension;
import org.jboss.weld.tests.invokable.metadata.common.TransitivelyInvokable;
import org.jboss.weld.tests.invokable.metadata.common.UnannotatedBean;

import java.util.Collection;

public class ObservingExtension implements Extension {

    private Collection<AnnotatedMethod<? super ClassLevelDirectDeclaration>> classDirectMethods;
    private Collection<AnnotatedMethod<? super ClassLevelIndirectDeclaration>> classIndirectMethods;
    private Collection<AnnotatedMethod<? super MethodLevelDirectDeclaration>> methodDirectMethods;
    private Collection<AnnotatedMethod<? super MethodLevelIndirectDeclaration>> methodIndirectMethods;
    private Collection<AnnotatedMethod<? super ClassLevelViaExtension>> classExtensionMethods;
    private Collection<AnnotatedMethod<? super MethodLevelViaExtension>> methodExtensionMethods;
    private Collection<AnnotatedMethod<? super UnannotatedBean>> unannotatedBeanMethods;



    public void processBean1(@Observes ProcessManagedBean<ClassLevelDirectDeclaration> pmb) {
        classDirectMethods = pmb.getInvokableMethods();
    }

    public void processBean2(@Observes ProcessManagedBean<ClassLevelIndirectDeclaration> pmb) {
        classIndirectMethods = pmb.getInvokableMethods();
    }

    public void processBean3(@Observes ProcessManagedBean<MethodLevelDirectDeclaration> pmb) {
        methodDirectMethods = pmb.getInvokableMethods();
    }

    public void processBean4(@Observes ProcessManagedBean<MethodLevelIndirectDeclaration> pmb) {
        methodIndirectMethods = pmb.getInvokableMethods();
    }

    public void processBean5(@Observes ProcessManagedBean<ClassLevelViaExtension> pmb) {
        classExtensionMethods = pmb.getInvokableMethods();
    }

    public void processBean6(@Observes ProcessManagedBean<MethodLevelViaExtension> pmb) {
        methodExtensionMethods = pmb.getInvokableMethods();
    }

    public void processBean7(@Observes ProcessManagedBean<UnannotatedBean> pmb) {
        unannotatedBeanMethods = pmb.getInvokableMethods();
        pmb.createInvoker(pmb.getInvokableMethods().iterator().next());
    }

    public void beforeDiscovery(@Observes BeforeBeanDiscovery bbd) {
        bbd.addInvokable(DefinitelyNotInvokable.class);
    }

    public void pat(@Observes ProcessAnnotatedType<UnannotatedBean> pat) {
        pat.configureAnnotatedType().add(TransitivelyInvokable.Literal.INSTANCE);
    }

    public Collection<AnnotatedMethod<? super ClassLevelDirectDeclaration>> getClassDirectMethods() {
        return classDirectMethods;
    }

    public Collection<AnnotatedMethod<? super ClassLevelIndirectDeclaration>> getClassIndirectMethods() {
        return classIndirectMethods;
    }


    public Collection<AnnotatedMethod<? super MethodLevelDirectDeclaration>> getMethodDirectMethods() {
        return methodDirectMethods;
    }

    public Collection<AnnotatedMethod<? super MethodLevelIndirectDeclaration>> getMethodIndirectMethods() {
        return methodIndirectMethods;
    }

    public Collection<AnnotatedMethod<? super ClassLevelViaExtension>> getClassExtensionMethods() {
        return classExtensionMethods;
    }

    public Collection<AnnotatedMethod<? super MethodLevelViaExtension>> getMethodExtensionMethods() {
        return methodExtensionMethods;
    }

    public Collection<AnnotatedMethod<? super UnannotatedBean>> getUnannotatedBeanMethods() {
        return unannotatedBeanMethods;
    }
}

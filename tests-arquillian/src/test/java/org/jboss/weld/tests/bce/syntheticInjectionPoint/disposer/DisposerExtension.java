package org.jboss.weld.tests.bce.syntheticInjectionPoint.disposer;

import java.lang.annotation.Annotation;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;

public class DisposerExtension implements BuildCompatibleExtension {

    @Synthesis
    public void register(SyntheticComponents syn) {
        // Old disposer API
        syn.addBean(DisposableResult.class)
                .type(DisposableResult.class)
                .qualifier(OldDisposerQualifier.class)
                .scope(Dependent.class)
                .createWith(OldDisposerCreator.class)
                .disposeWith(OldApiDisposer.class);

        // New disposer API with withInjectionPoint
        syn.addBean(DisposableResult.class)
                .type(DisposableResult.class)
                .qualifier(NewDisposerQualifier.class)
                .scope(Dependent.class)
                .withInjectionPoint(DisposerHelper.class, new Annotation[0])
                .createWith(NewDisposerCreator.class)
                .disposeWith(NewApiDisposer.class);
    }
}

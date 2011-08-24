package org.jboss.weld.osgi.examples.userdoc.helloworld.consumer.multilingual2;

import javax.enterprise.event.Observes;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import org.jboss.weld.environment.osgi.api.Service;
import org.jboss.weld.environment.osgi.api.annotation.OSGiService;
import org.jboss.weld.environment.osgi.api.events.BundleContainerEvents;
import org.jboss.weld.osgi.examples.userdoc.helloworld.provider.api.HelloWorld;
import org.jboss.weld.osgi.examples.userdoc.helloworld.provider.api.Language;

public class App {

    @Inject
    @OSGiService
    HelloWorld helloWorld;

    @Inject
    Service<HelloWorld> helloWorldService;

    @Inject
    @OSGiService
    @Language("ENGLISH")
    HelloWorld helloWorldEnglish;

    @Inject
    Service<HelloWorld> helloWorldServiceEnglish;

    @Inject
    @OSGiService
    @Language("FRENCH")
    HelloWorld helloWorldFrench;

    @Inject
    Service<HelloWorld> helloWorldServiceFrench;

    @Inject
    @OSGiService
    @Language("GERMAN")
    HelloWorld helloWorldGerman;

    @Inject
    Service<HelloWorld> helloWorldServiceGerman;

    HelloWorld helloWorld2;
    HelloWorld helloWorldEnglish2;
    HelloWorld helloWorldFrench2;
    HelloWorld helloWorldGerman2;

    public void onStartup(@Observes BundleContainerEvents.BundleContainerInitialized event) {
        helloWorld2 = helloWorldService.get();
        helloWorldEnglish2 = helloWorldServiceEnglish.select(new LanguageAnnotationEnglish()).get();
        helloWorldFrench2 = helloWorldServiceFrench.select("(language.value=FRENCH)").get();
        helloWorldGerman2 = helloWorldServiceGerman.select("(language.value=GERMAN)").get();

        helloWorld.sayHello();
        helloWorld2.sayHello();
        helloWorldEnglish.sayHello();
        helloWorldEnglish2.sayHello();
        helloWorldFrench.sayHello();
        helloWorldFrench2.sayHello();
        helloWorldGerman.sayHello();
        helloWorldGerman2.sayHello();

        for (HelloWorld service : helloWorldService) {
            service.sayHello();
        }
    }

    public void onShutdown(@Observes BundleContainerEvents.BundleContainerShutdown event) {
        helloWorld2 = helloWorldService.get();
        helloWorldEnglish2 = helloWorldServiceEnglish.select(new LanguageAnnotationEnglish()).get();
        helloWorldFrench2 = helloWorldServiceFrench.select("(language.value=FRENCH)").get();
        helloWorldGerman2 = helloWorldServiceGerman.select("(language.value=GERMAN)").get();

        helloWorld.sayGoodbye();
        helloWorld2.sayGoodbye();
        helloWorldEnglish.sayGoodbye();
        helloWorldEnglish2.sayGoodbye();
        helloWorldFrench.sayGoodbye();
        helloWorldFrench2.sayGoodbye();
        helloWorldGerman.sayGoodbye();
        helloWorldGerman2.sayGoodbye();

        for (HelloWorld service : helloWorldService) {
            service.sayGoodbye();
        }
    }

    private class LanguageAnnotationEnglish extends AnnotationLiteral<Language> implements Language {
        @Override
        public String value() {
            return "ENGLISH";
        }
    }
}

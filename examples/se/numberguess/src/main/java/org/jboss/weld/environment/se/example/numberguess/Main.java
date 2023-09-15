package org.jboss.weld.environment.se.example.numberguess;

import org.jboss.weld.environment.se.Weld;

public class Main {

    public static void main(String[] args) {
        Weld weld = new Weld().beanClasses(Game.class, Generator.class, MessageGenerator.class, NumberGuessFrame.class)
                .disableDiscovery();
        // shutdown hook is registered automatically for WeldContainer
        weld.initialize();
    }
}

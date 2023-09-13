package org.jboss.weld.tests.interceptors.visibility;

import jakarta.enterprise.context.Dependent;

import org.jboss.weld.tests.interceptors.visibility.unreachable.AbstractPanel;

@PanelInterceptionBinding
@Dependent
public class MyPanel extends AbstractPanel {

    @Override
    public String drawPanel() {
        return "implemented";
    }

}

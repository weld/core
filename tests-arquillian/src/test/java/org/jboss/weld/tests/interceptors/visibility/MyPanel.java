package org.jboss.weld.tests.interceptors.visibility;

import org.jboss.weld.tests.interceptors.visibility.unreachable.AbstractPanel;

@PanelInterceptionBinding
public class MyPanel extends AbstractPanel {

    @Override
    public String drawPanel() {
        return "implemented";
    }

}

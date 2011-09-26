/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.weld.osgi.examples.paint.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import org.jboss.weld.osgi.examples.paint.circle.CircleShape;
import org.osgi.framework.Bundle;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.jboss.weld.environment.osgi.api.Service;
import org.jboss.weld.environment.osgi.api.annotation.Required;
import org.jboss.weld.environment.osgi.api.annotation.Specification;
import org.jboss.weld.environment.osgi.api.events.InterBundleEvent;
import org.jboss.weld.environment.osgi.api.events.ServiceEvents;
import org.jboss.weld.osgi.examples.paint.api.ShapeProvider;
import org.jboss.weld.osgi.examples.paint.api.Shape;

@Singleton
public class PaintFrame extends JFrame implements MouseListener {

    private static final int BOX = 54;
    private JToolBar toolbar;
    private String selected;
    private JPanel panel;

    @Inject @Required private Service<ShapeProvider> registeredProviders;

    @Inject @CircleShape private ShapeProvider defaultProvider;

    @Inject private Event<InterBundleEvent> message;

    private ActionListener actionListener = new ShapeActionListener();

    private Map<String, ShapeProvider> providers = new HashMap<String, ShapeProvider>();

    private Map<String, Collection<ShapeComponent>> goneComponents
            = new HashMap<String, Collection<ShapeComponent>>();


    @Inject
    public PaintFrame(Bundle bundle) {
        super("PaintFrame for bundle " + bundle.getBundleId());
        toolbar = new JToolBar("Toolbar");
        panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(null);
        panel.setMinimumSize(new Dimension(400, 400));
        panel.addMouseListener(this);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolbar, BorderLayout.NORTH);
        getContentPane().add(panel, BorderLayout.CENTER);
        setSize(400, 400);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                dispose();
            }
        });
    }

    public void selectShape(String name) {
        selected = name;
    }

    public void bindService(@Observes @Specification(ShapeProvider.class) ServiceEvents.ServiceArrival event) {
        System.out.println("bind : " + event.getServiceClassNames());
        addShape(event.getService(ShapeProvider.class));
    }

    public void unbindService(@Observes @Specification(ShapeProvider.class) ServiceEvents.ServiceDeparture event) {
        System.out.println("unbind : " + event.getServiceClassNames());
        removeShape(event.getService(ShapeProvider.class).getId());
    }

    private void addShape(ShapeProvider provider) {
        if (!providers.containsKey(provider.getId())) {
            providers.put(provider.getId(), provider);
            Shape shape = provider.getShape();
            JButton button = new JButton(shape.getIcon());
            button.setActionCommand(provider.getId());
            button.setToolTipText(shape.getName());
            button.addActionListener(actionListener);
            toolbar.add(button);
            toolbar.validate();
            if (goneComponents.containsKey(provider.getId())) {
                for (ShapeComponent comp : goneComponents.get(provider.getId())) {
                    panel.add(comp);
                }
                panel.validate();
                goneComponents.get(provider.getId()).clear();
            }
            repaint();
        }
    }

    private void removeShape(String name) {
        providers.remove(name);
        if (!goneComponents.containsKey(name)) {
            goneComponents.put(name, new ArrayList<ShapeComponent>());
        }
        for (Component comp : panel.getComponents()) {
            ShapeComponent shapeComp = (ShapeComponent) comp;
            if (shapeComp.getShapeId().equals(name)) {
                goneComponents.get(name).add(shapeComp);
                panel.remove(comp);
            }
            panel.validate();
            panel.repaint();
        }
        if ((selected != null) && selected.equals(name)) {
            selected = null;
        }
        for (int i = 0; i < toolbar.getComponentCount(); i++) {
            JButton sb = (JButton) toolbar.getComponent(i);
            if (sb.getActionCommand().equals(name)) {
                toolbar.remove(i);
                toolbar.invalidate();
                validate();
                repaint();
                break;
            }
        }
        if ((selected == null) && (toolbar.getComponentCount() > 0)) {
            ((JButton) toolbar.getComponent(0)).doClick();
        }
    }

    @Override
    public void mouseClicked(MouseEvent evt) {
    }

    @Override
    public void mousePressed(MouseEvent evt) {
    }

    @Override
    public void mouseReleased(MouseEvent evt) {
        if (selected == null) {
            return;
        }
        message.fire(new InterBundleEvent("New " + selected + " added ..."));
        if (panel.contains(evt.getX(), evt.getY())) {
            ShapeComponent sc = null;
            if (providers.containsKey(selected)) {
                sc = new ShapeComponent(providers.get(selected).getShape());
                sc.setBounds(evt.getX() - BOX / 2, evt.getY() - BOX / 2, BOX, BOX);
                panel.add(sc, 0);
                panel.validate();
                panel.repaint(sc.getBounds());
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent evt) {
    }

    @Override
    public void mouseExited(MouseEvent evt) {
    }

    public void start() {
        addShape(defaultProvider);
        for (ShapeProvider provider : registeredProviders) {
            addShape(provider);
        }
        this.setVisible(true);
    }

    public void stop() {
        this.dispose();
    }

    private class ShapeActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            selectShape(evt.getActionCommand());
        }
    }
}

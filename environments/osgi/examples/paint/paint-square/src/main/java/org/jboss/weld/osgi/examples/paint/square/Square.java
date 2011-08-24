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

package org.jboss.weld.osgi.examples.paint.square;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.jboss.weld.osgi.examples.paint.api.Shape;

public class Square implements Shape {

    private Icon icon;

    public Square() {
        icon = new ImageIcon(getClass().getResource("square.png"));
    }

    @Override
    public String getName() {
        return "Square";
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public void draw(Graphics2D g2, Point p) {
        int x = p.x - 25;
        int y = p.y - 25;
        GradientPaint gradient = new GradientPaint(x, y, Color.BLUE, x + 50, y, Color.WHITE);
        g2.setPaint(gradient);
        g2.fill(new Rectangle2D.Double(x, y, 50, 50));
        BasicStroke wideStroke = new BasicStroke(2.0f);
        g2.setColor(Color.black);
        g2.setStroke(wideStroke);
        g2.draw(new Rectangle2D.Double(x, y, 50, 50));
    }
}

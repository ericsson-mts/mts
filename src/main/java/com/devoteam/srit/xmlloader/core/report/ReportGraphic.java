/* 
 * Copyright 2012 Devoteam http://www.devoteam.com
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * 
 * This file is part of Multi-Protocol Test Suite (MTS).
 * 
 * Multi-Protocol Test Suite (MTS) is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License.
 * 
 * Multi-Protocol Test Suite (MTS) is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Multi-Protocol Test Suite (MTS).
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.devoteam.srit.xmlloader.core.report;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JComponent;

import com.devoteam.srit.xmlloader.core.utils.Helper;

/**
 * @author pn007888
 */
public class ReportGraphic extends JComponent {
    static float[] dashPattern = { 10, 2, 2, 2 };

    BasicStroke plain = new BasicStroke(1);

    BasicStroke axis = new BasicStroke(1, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER, 10, dashPattern, 0);

    private static Font font = new Font("Arial", Font.PLAIN, 10);

    private static SimpleDateFormat format = new SimpleDateFormat(
            "yyyy/MM/dd-HH:mm:ss");

    //private StatEnumeration se;

    private Insets inset;

    double value, vx, vy, index;

    double minx, maxx, miny, maxy;

    private double height, width;

    double dataNumber;
    float array[];
    float max,avg;

    private boolean x_grad;

    private long start;

    private long end;

    String desc;

    private void drawAxis(Graphics2D g) {

        g.setStroke(axis);
        g.setPaint(Color.GRAY);
        // Draw graduations
        // y
        double n = height / (font.getSize() * 1.4);

        double step = max / n;
        if (step < 1)
            step = 1;
        for (int i = 0; i <= (int) max; i += step) {
            if (max == 0) {
                vy = 0;
            } else {
                vy = i * height / max;
            }
            g.draw(new Line2D.Double(minx + 1, getSize().height - miny - vy,
                    maxx, getSize().height - miny - vy));
            g.drawString(Integer.toString(i), 2,
                    (int) (getSize().height - miny - vy));
        }
        long t = 0;
        for (double x = minx; x < maxx; x += ((maxx - minx) / 5)) {

            long time = start + t * ((end - start) / 5);
            t++;
            g.draw(new Line2D.Double(x, miny, x, maxy));
            if (x_grad) {
                g.drawString(format.format(new Date(time)), (int) x,
                        getHeight() - 1);
            }

        }
        g.setStroke(plain);
        // Axis
        g.setPaint(Color.BLACK);
        g.draw(new Line2D.Double(inset.left, getSize().getHeight()
                - inset.bottom, getSize().getWidth() - inset.right, getSize()
                .getHeight()
                - inset.bottom));
        g.draw(new Line2D.Double(inset.left, getSize().getHeight()
                - inset.bottom, inset.left, inset.bottom));
    }

    private void drawBorder(Graphics2D g) {
        Dimension d = getSize();
        g.setPaint(Color.WHITE);
        g.fill(new Rectangle2D.Double(0, 0, d.getWidth(), d.getHeight()));
    }

    /**
     * Paint the graph.
     * @param g
     */
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        setFont(font);
        // 	Setup variables
        inset.left = (Integer.toString((int) max).length())
                * getFont().getSize() + 1;
        minx = inset.left + 1;
        maxx = getSize().getWidth() - inset.right - 1;
        miny = inset.bottom + 1;
        maxy = getSize().getHeight() - inset.top - 1;

        width = maxx - minx;
        height = maxy - miny;

        drawBorder(g2);
        drawAxis(g2);
        // Paint data !!!
        g2.setPaint(Color.GREEN);
        for (double i = 0; i < width; i++) {
            index = dataNumber * i / width;
            if (max == 0)
                value = 0;
            else
                value = array[(int) index] / max * height;
            vx = i + minx;
            vy = value;
            g2.draw(new Line2D.Double(vx, getSize().getHeight() - miny, vx,
                    getSize().getHeight() - miny - vy));
        }

        g2.setPaint(Color.BLACK);
        g2.drawString(desc, (int) minx, getFont().getSize() + 1);
        g2.drawString("Time", getWidth() - 40, getHeight() - 2);
        if (!x_grad) {
            // Display ellapsed time as X axis graduation
            g.drawString(Helper.getElapsedTimeString((end - start) / 1000),
                    (int) width / 4, (getSize().height - 2));
        } else {
            g.drawString(Helper.getElapsedTimeString((end - start) / 1000),
                    (int) width / 2, getFont().getSize() + 1);
        }
    }
}
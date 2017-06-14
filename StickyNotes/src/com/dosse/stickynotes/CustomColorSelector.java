/*
 * Copyright (C) 2017 Federico Dossena
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.dosse.stickynotes;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author Federico
 */
public abstract class CustomColorSelector extends JPanel {

    private static final int DEFAULT_WIDTH = (int) (160 * Main.SCALE),
            DEFAULT_HEIGHT = (int) (60 * Main.SCALE),
            GRAYSCALE_HEIGHT = (int) (8 * Main.SCALE);

    private static final float BASE_SATURATION = 0.65f;

    public CustomColorSelector() {
        setLayout(null);
        setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    //we read the color clicked by the user
                    BufferedImage b = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
                    printAll(b.getGraphics());
                    int color = b.getRGB(e.getX(), e.getY());
                    onColorSelected(new Color(color)); //callback
                } catch (Throwable t) {
                    //coordinates can be out of bounds if the user drags the cursor around, ignore
                }
            }

        });
    }

    @Override
    public void paint(Graphics g) {
        float width = getWidth(), height = getHeight() - GRAYSCALE_HEIGHT, fx, fy;
        //colors
        for (float y = 0; y < height; y++) {
            for (float x = 0; x < width; x++) {
                fx = x / width;
                fy = y / height;
                g.setColor(Color.getHSBColor(fx, fy < 0.5f ? (BASE_SATURATION - BASE_SATURATION * 2 * (0.5f - fy)) : BASE_SATURATION, fy < 0.5f ? 1f : (1 - (2 * (fy - 0.5f)))));
                g.fillRect((int) x, (int) y, 1, 1);
            }
        }
        //grays
        for (float x = 0; x < width; x++) {
            g.setColor(Color.getHSBColor(0, 0, x / width));
            g.fillRect((int) x, getHeight() - GRAYSCALE_HEIGHT, 1, GRAYSCALE_HEIGHT);
        }
    }

    public abstract void onColorSelected(Color c);

}

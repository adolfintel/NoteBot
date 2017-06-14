/*
 * Copyright (C) 2016 Federico Dossena
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
import java.awt.Dimension;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

/**
 *
 * @author Federico
 */
public abstract class ColorSelector extends JPanel {

    private static final int BUTTON_SIZE = (int) (20 * Main.SCALE);
    private static final int BUTTONS_PER_ROW = 8;
    private static final Color SELECTED_COLOR=new Color(170, 170, 170);
    private static final Border NORMAL_BORDER = new LineBorder(new Color(0, 0, 0, 0), (int) (2 * Main.SCALE)), SELECTED_BORDER = new LineBorder(SELECTED_COLOR, (int) (2 * Main.SCALE));
    private final JLabel[] buttons;

    public ColorSelector(Color[][] colorSchemes) {
        setLayout(null);
        int rows = (int) Math.ceil(colorSchemes.length / BUTTONS_PER_ROW);
        setPreferredSize(new Dimension(BUTTONS_PER_ROW * BUTTON_SIZE, rows * BUTTON_SIZE));
        int x = 0, y = 0, i = 0, bi = 0;
        buttons = new JLabel[colorSchemes.length];
        for (final Color[] c : colorSchemes) {
            final JLabel l = new JLabel();
            l.setOpaque(true);
            l.setBackground(c[0]);
            l.setBorder(NORMAL_BORDER);
            l.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    l.setBorder(SELECTED_BORDER);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    l.setBorder(NORMAL_BORDER);
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    mouseExited(e);
                    onColorSchemeSelected(c);
                }
            });
            add(l);
            l.setLocation(x, y);
            l.setSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
            buttons[bi++] = l;
            x += BUTTON_SIZE;
            i++;
            if (i == BUTTONS_PER_ROW) {
                i = 0;
                x = 0;
                y += BUTTON_SIZE;
            }
        }
        addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                for (JLabel b : buttons) {
                    b.setBorder(NORMAL_BORDER); //this brutal workaround removes the selected border when the color menu isn't closed properly
                }
            }
        });
    }

    public abstract void onColorSchemeSelected(Color[] scheme);

}

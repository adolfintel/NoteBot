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

import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * @author Federico
 */
public class Utils {
    /**
     * loads font from classpath
     *
     * @param pathInClasspath path in classpath
     * @return Font or null if it doesn't exist
     */
    public static final Font loadFont(String pathInClasspath) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, Utils.class.getResourceAsStream(pathInClasspath));
        } catch (Throwable ex) {
            return null;
        }
    }
    
    private static final ResourceBundle locBundle = ResourceBundle.getBundle("com/dosse/stickynotes/locale/locale");

    /**
     * returns localized string
     *
     * @param s key
     * @return localized String, or null the key doesn't exist
     */
    public static final String getLocString(String s) {
        return locBundle.getString(s);
    }
    
    private static final BufferedImage nullImage; //empty Image, mostly used for errors

    static {
        nullImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        nullImage.setRGB(0, 0, 0);
    }
    /**
     * load image from classpath as ImageIcon. doesn't scale it
     *
     * @param pathInClasspath path in classpath
     * @return ImageIcon, or an empty ImageIcon if something went wrong
     */
    public static final ImageIcon loadUnscaled(String pathInClasspath) {
        try {
            Image i = ImageIO.read(Utils.class.getResource(pathInClasspath));
            return new ImageIcon(i);
        } catch (IOException ex) {
            return new ImageIcon(nullImage);
        }
    }
}

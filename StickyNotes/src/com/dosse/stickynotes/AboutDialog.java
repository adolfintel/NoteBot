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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

/**
 *
 * @author Federico
 */
public class AboutDialog extends JDialog {

    private static final int DEFAULT_WIDTH = (int) (400 * Main.SCALE),
            DEFAULT_HEIGHT = (int) (380 * Main.SCALE);

    private static final ResourceBundle locBundle = ResourceBundle.getBundle("com/dosse/stickynotes/locale/locale");

    /**
     * Creates new form AboutDialog
     */
    public AboutDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(null);
        setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        setResizable(false);
        setTitle(locBundle.getString("ABOUT"));
        //main panel
        JPanel main = new JPanel();
        main.setLayout(null);
        add(main);
        main.setBounds((int) (8 * Main.SCALE), (int) (8 * Main.SCALE), (int) (DEFAULT_WIDTH - 16 * Main.SCALE - getInsets().left - getInsets().right), (int) (DEFAULT_HEIGHT - 16 * Main.SCALE - getInsets().top - getInsets().bottom));
        //title with icon
        JLabel title = new JLabel();
        title.setFont(Main.BASE_FONT.deriveFont(36f * Main.SCALE));
        title.setText("  " + locBundle.getString("APPNAME"));
        title.setIcon(loadScaled("/com/dosse/stickynotes/icon.png", 0.5f));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBounds(0, 0, main.getWidth(), (int) (96f * Main.SCALE));
        main.add(title);
        //separator line
        JSeparator sep = new JSeparator();
        sep.setBounds(0, (int) (100f * Main.SCALE), main.getWidth(), (int) (1f * Main.SCALE));
        main.add(sep);
        //text below
        Font smallFont = Main.BASE_FONT.deriveFont(11f * Main.SCALE);
        JLabel ver = new JLabel();
        ver.setFont(smallFont);
        ver.setText(locBundle.getString("ABOUT_VERSION"));
        ver.setBounds(0, (int) (112f * Main.SCALE), main.getWidth(), (int) (24f * Main.SCALE));
        setIconImage((Image) ver.getIcon());
        main.add(ver);
        final JLabel url = new JLabel();
        url.setFont(smallFont);
        url.setText(locBundle.getString("ABOUT_URL"));
        url.setBounds(0, (int) (136f * Main.SCALE), main.getWidth(), (int) (24f * Main.SCALE));
        url.setForeground(new Color(40, 40, 255));
        url.addMouseListener(new MouseAdapter() { //when URL is clicked, open in browser
            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(url.getText()));
                } catch (Throwable t) {
                }
            }
        });
        main.add(url);
        JLabel copy = new JLabel();
        copy.setFont(smallFont);
        copy.setText("<html>" + locBundle.getString("ABOUT_COPYRIGHT") + "</html>");
        copy.setBounds(0, (int) (180f * Main.SCALE), main.getWidth(), (int) (64f * Main.SCALE));
        main.add(copy);
        //close button
        JButton ok = new JButton();
        ok.setText(locBundle.getString("ABOUT_CLOSE"));
        ok.setFont(Main.BUTTON_FONT.deriveFont(11f * Main.SCALE));
        ok.setBounds((int) (main.getWidth() - (76f * Main.SCALE) - 4), (int) (main.getHeight() - (26 * Main.SCALE) - 24), (int) (76f * Main.SCALE), (int) (26f * Main.SCALE)); //todo: improve
        ok.setBackground(new Color(230, 230, 230));
        ok.setBorderPainted(false);
        ok.setFocusPainted(false);
        ok.addActionListener(new ActionListener() { //when clicked, close dialog
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        main.add(ok);
        pack();
        setLocation((int) (50 * Main.SCALE), (int) (50 * Main.SCALE));
    }

    private static final BufferedImage nullImage; //empty Image, used for errors

    static {
        nullImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        nullImage.setRGB(0, 0, 0);
    }

    /**
     * load image from classpath as ImageIcon and scales according to screen DPI
     *
     * @param pathInClasspath path in classpath
     * @param customScale how big you want the image to be. 1=normal, 0.5=half
     * the size, etc. DPI SCALING IS ALWAYS PERFORMED!
     * @return ImageIcon, or an empty ImageIcon of dimension WxH if something
     * went wrong
     */
    private static final ImageIcon loadScaled(String pathInClasspath, float customScale) {
        try {
            Image i = ImageIO.read(AboutDialog.class.getResource(pathInClasspath));
            return new ImageIcon(i.getScaledInstance((int) (i.getWidth(null) * Main.SCALE * customScale), (int) (i.getHeight(null) * Main.SCALE * customScale), Image.SCALE_SMOOTH));
        } catch (Throwable ex) {
            return new ImageIcon(nullImage);
        }
    }
}

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
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.plaf.FontUIResource;

/**
 *
 * @author Federico
 */
public class Note extends JDialog {

    private static final ResourceBundle locBundle = ResourceBundle.getBundle("com/dosse/stickynotes/locale/locale");

    /**
     * returns localized string
     *
     * @param s key
     * @return localized String, or null the key doesn't exist
     */
    private static String getLocString(String s) {
        return locBundle.getString(s);
    }

    //<editor-fold defaultstate="collapsed" desc="Color schemes">
    /**
     * SCHEME FORMAT: {external color, line border color, bar color, buttons
     * color, internal color, text color, selection background color, selected
     * text color}
     */
    private static final Color[] YELLOW_SCHEME = new Color[]{
        new Color(248, 248, 182),
        new Color(232, 232, 170),
        new Color(248, 248, 182),
        new Color(105, 105, 77),
        new Color(253, 253, 202),
        new Color(0, 0, 0),
        new Color(136, 195, 255),
        new Color(0, 0, 0)
    };
    private static final Color[] BLUE_SCHEME = new Color[]{
        new Color(201, 236, 248),
        new Color(188, 221, 232),
        new Color(201, 236, 248),
        new Color(78, 92, 96),
        new Color(217, 243, 251),
        new Color(0, 0, 0),
        new Color(136, 195, 255),
        new Color(0, 0, 0)
    };
    private static final Color[] GREEN_SCHEME = new Color[]{
        new Color(197, 247, 193),
        new Color(183, 230, 179),
        new Color(197, 247, 193),
        new Color(79, 96, 77),
        new Color(209, 254, 203),
        new Color(0, 0, 0),
        new Color(136, 195, 255),
        new Color(0, 0, 0)
    };
    private static final Color[] PINK_SCHEME = new Color[]{
        new Color(241, 195, 241),
        new Color(230, 186, 230),
        new Color(241, 195, 241),
        new Color(99, 80, 99),
        new Color(246, 211, 246),
        new Color(0, 0, 0),
        new Color(136, 195, 255),
        new Color(0, 0, 0)
    };
    private static final Color[] PURPLE_SCHEME = new Color[]{
        new Color(212, 205, 243),
        new Color(200, 194, 230),
        new Color(212, 205, 243),
        new Color(89, 86, 102),
        new Color(221, 217, 254),
        new Color(0, 0, 0),
        new Color(136, 195, 255),
        new Color(0, 0, 0)
    };
    private static final Color[] RED_SCHEME = new Color[]{
        new Color(247, 197, 193),
        new Color(230, 183, 179),
        new Color(247, 197, 193),
        new Color(96, 79, 77),
        new Color(254, 209, 203),
        new Color(0, 0, 0),
        new Color(136, 195, 255),
        new Color(0, 0, 0)
    };
    private static final Color[] WHITE_SCHEME = new Color[]{
        new Color(245, 245, 245),
        new Color(230, 230, 230),
        new Color(245, 245, 245),
        new Color(100, 100, 100),
        new Color(255, 255, 255),
        new Color(0, 0, 0),
        new Color(136, 195, 255),
        new Color(0, 0, 0)
    };

    private static final Color[] DEFAULT_SCHEME = YELLOW_SCHEME;
    //</editor-fold>

    /**
     * copy a string to the system clipboard
     *
     * @param s string
     */
    private static void setClipboard(String s) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s), null);
    }

    /**
     * read contents of system clipboard
     *
     * @return clipboard contents
     */
    private static String getClipboard() {
        try {
            Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            return (String) contents.getTransferData(DataFlavor.stringFlavor);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * load image from classpath (it will be the jar file)
     *
     * @param pathInClasspath path in classpath
     * @return the image, or an empty image if something went wrong
     */
    public static final Image loadImage(String pathInClasspath) {
        try {
            return ImageIO.read(Note.class.getResource(pathInClasspath));
        } catch (IOException ex) {
            BufferedImage i = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            i.setRGB(0, 0, 0);
            return i;
        }
    }

    //UI Elements
    private JPanel wrapper1; //outer wrapper: it's the area that the user can use to resize the window
    private JPanel wrapper2; //inner wrapper: it contains the buttons and the actual note; the empty space can be dragged to move the note
    private int mouseDragStartX, mouseDragStartY; //used for dragging
    private JButton deleteNote, newNote; //buttons to delete and create notes
    private JScrollPane jScrollPane1; //container for the text. provides the scrollbar
    private JTextArea text; //the actual note
    private JPopupMenu copyPasteMenu, //menu shown when the textarea is right-clicked
            colorMenu; //menu shown when the top is right-clicked
    private JMenuItem cut, copy, paste, delete, selectAll; //menu items inside copyPasteMenu
    private Point preferredLocation; //the preferred location is the last user-set location of the note. this is useful when the screen resolution is changed and the notes are all scrambled up

    /**
     * Creates new form Note.
     *
     * A note is initialized empty, with a yellow background and at current
     * mouse coordinates.
     */
    public Note() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); //if alt+f4 is pressed, this will cause the windowClosing event to be fired
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //if alt+f4 is pressed, save the current state and terminate the app
                Main.saveState();
                System.exit(0);
            }
        });
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                //if focus is lost, save the current state
                Main.saveState();
            }
        });
        setTitle(getLocString("APPNAME")); //set window title
        setIconImage(loadImage("/com/dosse/stickynotes/icon.png")); //set window icon
        setUndecorated(true); //removes system window border

        //now we will create and initialize all the elements inside the note
        wrapper1 = new JPanel();
        wrapper2 = new JPanel();
        newNote = new JButton();
        deleteNote = new JButton();
        jScrollPane1 = new JScrollPane();
        //create the text area
        text = new JTextArea() {
            @Override
            public boolean getScrollableTracksViewportWidth() {//configures the textarea to resize properly horizontaly (workaround for swing bug)
                return true;
            }
        };
        //enable line wrap
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        jScrollPane1.setBorder(null);
        jScrollPane1.setViewportView(text); //add text area to scrollpane

        //events used for dragging the note
        wrapper2.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                mouseDragStartX = evt.getXOnScreen() - getX();
                mouseDragStartY = evt.getYOnScreen() - getY();
            }
        });
        wrapper2.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent evt) {
                setLocation(evt.getXOnScreen() - mouseDragStartX, evt.getYOnScreen() - mouseDragStartY);
                preferredLocation=getLocation();
            }
        });
        //initialize ComponentResizer to make the window resizable
        ComponentResizer cr = new ComponentResizer();
        cr.registerComponent(this);
        cr.setSnapSize(new Dimension(1, 1)); //no snap
        cr.setMinimumSize(new Dimension((int) (160 * Main.SCALE), (int) (90 * Main.SCALE))); //min size is 160x90 @80dpi
        setSize((int) (190 * Main.SCALE), (int) (170 * Main.SCALE)); //default size is 190x170 @80dpi
        setLocation(MouseInfo.getPointerInfo().getLocation()); //new note is placed at current mouse coordinates
        preferredLocation=getLocation();

        //new note button
        newNote.setFont(new FontUIResource(Main.BUTTON_FONT));
        newNote.setText("+");
        newNote.setBorderPainted(false);
        newNote.setContentAreaFilled(false);
        newNote.setFocusPainted(false);
        newNote.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                Main.newNote();
            }
        });

        //delete note button
        deleteNote.setFont(new FontUIResource(Main.BUTTON_FONT));
        deleteNote.setText("X");
        deleteNote.setBorderPainted(false);
        deleteNote.setContentAreaFilled(false);
        deleteNote.setFocusPainted(false);
        deleteNote.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                Main.delete(Note.this);
            }
        });

        //right click on the note
        text.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.isPopupTrigger() || evt.getButton() == MouseEvent.BUTTON3) { //isPopupTrigger does not work on windows. workaround is to listen for BUTTON3 instead (right mouse button)
                    if (text.getSelectionStart() == text.getSelectionEnd()) { //if there is no text selected, disable cut, copy and delete
                        cut.setEnabled(false);
                        copy.setEnabled(false);
                        delete.setEnabled(false);
                    } else { //otherwise, enable them
                        cut.setEnabled(true);
                        copy.setEnabled(true);
                        delete.setEnabled(true);
                    }
                    paste.setEnabled(getClipboard() != null); //paste is only enabled if there's something in the clipboard
                    copyPasteMenu.show(evt.getComponent(), evt.getX(), evt.getY()); //show the menu at current mouse location
                }
            }
        });

        //right click on top bar
        wrapper2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.isPopupTrigger() || evt.getButton() == MouseEvent.BUTTON3) {
                    colorMenu.show(evt.getComponent(), evt.getX(), evt.getY()); //show color selection menu
                }
            }
        });

        //initialize copy-paste menu
        copyPasteMenu = new JPopupMenu();
        cut = new JMenuItem(getLocString("CUT"));
        cut.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int from = text.getSelectionStart(), to = text.getSelectionEnd();
                setClipboard(text.getSelectedText());
                String s = text.getText().substring(0, from) + text.getText().substring(to);
                text.setText(s);
                text.setCaretPosition(from);
            }
        });
        copy = new JMenuItem(getLocString("COPY"));
        copy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setClipboard(text.getSelectedText());
            }
        });
        paste = new JMenuItem(getLocString("PASTE"));
        paste.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int from = text.getSelectionStart(), to = text.getSelectionEnd();
                String clipboard = getClipboard();
                if (clipboard == null) {
                    return;
                }
                String s = text.getText().substring(0, from) + clipboard + text.getText().substring(to);
                text.setText(s);
                text.setCaretPosition(from + getClipboard().length());
            }
        });
        delete = new JMenuItem(getLocString("DELETE"));
        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int from = text.getSelectionStart(), to = text.getSelectionEnd();
                String s = text.getText().substring(0, from) + text.getText().substring(to);
                text.setText(s);
                text.setCaretPosition(from);
            }
        });
        selectAll = new JMenuItem(getLocString("SELECTALL"));
        selectAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                text.setSelectionStart(0);
                text.setSelectionEnd(text.getText().length());
            }
        });
        copyPasteMenu.add(cut);
        copyPasteMenu.add(copy);
        copyPasteMenu.add(paste);
        copyPasteMenu.add(delete);
        copyPasteMenu.add(new JPopupMenu.Separator());
        copyPasteMenu.add(selectAll);

        //initialize color selection menu (right click on top bar)
        colorMenu = new JPopupMenu();
        JMenuItem m = new JMenuItem(getLocString("YELLOW"));
        m.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setColorScheme(YELLOW_SCHEME);
            }
        });
        colorMenu.add(m);
        m = new JMenuItem(getLocString("BLUE"));
        m.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setColorScheme(BLUE_SCHEME);
            }
        });
        colorMenu.add(m);
        m = new JMenuItem(getLocString("GREEN"));
        m.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setColorScheme(GREEN_SCHEME);
            }
        });
        colorMenu.add(m);
        m = new JMenuItem(getLocString("PINK"));
        m.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setColorScheme(PINK_SCHEME);
            }
        });
        colorMenu.add(m);
        m = new JMenuItem(getLocString("PURPLE"));
        m.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setColorScheme(PURPLE_SCHEME);
            }
        });
        colorMenu.add(m);
        m = new JMenuItem(getLocString("RED"));
        m.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setColorScheme(RED_SCHEME);
            }
        });
        colorMenu.add(m);
        m = new JMenuItem(getLocString("WHITE"));
        m.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setColorScheme(WHITE_SCHEME);
            }
        });
        colorMenu.add(m);

        //add everything to the layout
        GroupLayout wrapper2Layout = new GroupLayout(wrapper2);
        wrapper2.setLayout(wrapper2Layout);
        wrapper2Layout.setHorizontalGroup(wrapper2Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(wrapper2Layout.createSequentialGroup().addComponent(newNote).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 134, Short.MAX_VALUE).addComponent(deleteNote)).addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE));
        wrapper2Layout.setVerticalGroup(wrapper2Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(GroupLayout.Alignment.TRAILING, wrapper2Layout.createSequentialGroup().addGap(0, 0, 0).addGroup(wrapper2Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(deleteNote, GroupLayout.PREFERRED_SIZE, (int) (19 * Main.SCALE * 0.85f), GroupLayout.PREFERRED_SIZE).addComponent(newNote, GroupLayout.PREFERRED_SIZE, (int) (19 * Main.SCALE * 0.85f), GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)));
        GroupLayout wrapper1Layout = new GroupLayout(wrapper1);
        wrapper1.setLayout(wrapper1Layout);
        wrapper1Layout.setHorizontalGroup(wrapper1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(GroupLayout.Alignment.TRAILING, wrapper1Layout.createSequentialGroup().addGap((int) (6 * Main.SCALE * 0.85f), (int) (6 * Main.SCALE * 0.85f), (int) (6 * Main.SCALE * 0.85f)).addComponent(wrapper2, GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE).addGap((int) (6 * Main.SCALE * 0.85f), (int) (6 * Main.SCALE * 0.85f), (int) (6 * Main.SCALE * 0.85f))));
        wrapper1Layout.setVerticalGroup(wrapper1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(wrapper1Layout.createSequentialGroup().addGap((int) (6 * Main.SCALE * 0.85f), (int) (6 * Main.SCALE * 0.85f), (int) (6 * Main.SCALE * 0.85f)).addComponent(wrapper2, GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE).addGap((int) (6 * Main.SCALE * 0.85f), (int) (6 * Main.SCALE * 0.85f), (int) (6 * Main.SCALE * 0.85f))));
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(wrapper1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(wrapper1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        pack(); //fuck my shit up fam

        setColorScheme(DEFAULT_SCHEME); //set default color scheme (yellow)

        //and we're done
    }

    /**
     * setLocation method is overridden to force the note to stay on the screen
     *
     * @param x new x location
     * @param y new y location
     */
    @Override
    public void setLocation(int x, int y) {
        Dimension s=Main.getExtendedScreenResolution();
        if (x+40*Main.SCALE > s.width) {
            x = (int) (s.width - 60*Main.SCALE);
        }
        if (y+40*Main.SCALE > s.height) {
            y = (int) (s.height - 60*Main.SCALE);
        }
        super.setLocation(x, y);
    }

    /**
     * setLocation method is overridden to force the note to stay on the screen
     *
     * @param p new location
     */
    @Override
    public void setLocation(Point p) {
        setLocation(p.x, p.y);
    }

    /**
     * get text currently inside the note
     *
     * @return text
     */
    public String getText() {
        return text.getText();
    }

    /**
     * set text currently inside the note
     *
     * @param s
     */
    public void setText(String s) {
        text.setText(s);
    }
    
    /**
     * gets the last user-set location of the note
     * @return location
     */
    public Point getPreferredLocation(){
        return preferredLocation;
    }
    /**
     * sets preferred location (last user-set location of the note)
     * @param p new preferred location
     */
    public void setPreferredLocation(Point p){
        preferredLocation=p;
    }

    /**
     * get current color scheme
     *
     * @return current color scheme (see format at the beginning of this file)
     */
    public Color[] getColorScheme() {
        Color[] ret = new Color[8];
        ret[0] = wrapper1.getBackground();
        ret[1] = ((LineBorder) wrapper1.getBorder()).getLineColor();
        ret[2] = wrapper2.getBackground();
        ret[3] = newNote.getForeground();
        ret[4] = text.getBackground();
        ret[5] = text.getForeground();
        ret[6] = text.getSelectionColor();
        ret[7] = text.getSelectedTextColor();
        return ret;
    }

    /**
     * set color scheme
     *
     * @param c color scheme (see format at the beginning of this file)
     */
    public void setColorScheme(Color[] c) {
        wrapper1.setBackground(c[0]);
        wrapper1.setBorder(new LineBorder(c[1]));
        wrapper2.setBackground(c[2]);
        newNote.setForeground(c[3]);
        deleteNote.setForeground(c[3]);
        text.setBackground(c[4]);
        text.setForeground(c[5]);
        text.setSelectionColor(c[6]);
        text.setSelectedTextColor(c[7]);
    }

}

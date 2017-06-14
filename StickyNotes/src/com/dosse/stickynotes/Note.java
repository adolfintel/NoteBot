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
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
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
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

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
    private static final Color[] ORANGE_SCHEME = new Color[]{
        new Color(248, 205, 161),
        new Color(230, 189, 149),
        new Color(248, 205, 161),
        new Color(102, 84, 66),
        new Color(255, 215, 173),
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
    private final JPanel wrapper1; //outer wrapper: it's the area that the user can use to resize the window
    private final JPanel wrapper2; //inner wrapper: it contains the buttons and the actual note; the empty space can be dragged to move the note
    private int mouseDragStartX, mouseDragStartY; //used for dragging
    private final JButton deleteNote, newNote; //buttons to delete and create notes
    private final JScrollPane jScrollPane1; //container for the text. provides the scrollbar
    private final JTextArea text; //the actual note
    private final UndoManager undo = new UndoManager(); //undo/redo manager (provided by swing)
    private final JPopupMenu copyPasteMenu, //menu shown when the textarea is right-clicked
            colorMenu; //menu shown when the top is right-clicked
    private final JMenuItem cut, copy, paste, delete, selectAll; //menu items inside copyPasteMenu
    private Point preferredLocation = new Point(0, 0); //the preferred location is the last user-set location of the note. this is useful when the screen resolution is changed and the notes are all scrambled up
    private float textScale = 1; //text zoom
    private static final float MIN_TEXT_SCALE = 0.2f, MAX_TEXT_SCALE = 4f; //min max text zoom

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

            @Override
            public void windowGainedFocus(WindowEvent e) {
                Main.bringToFront(Note.this);
                text.requestFocusInWindow();
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
        //allow undo/redo
        Document doc = text.getDocument();
        doc.addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                undo.addEdit(e.getEdit());
            }
        });

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
            }
        });
        //initialize ComponentResizer to make the window resizable
        ComponentResizer cr = new ComponentResizer();
        cr.registerComponent(this);
        cr.setSnapSize(new Dimension(1, 1)); //no snap
        cr.setMinimumSize(new Dimension((int) (160 * Main.SCALE), (int) (90 * Main.SCALE))); //min size is 160x90 @80dpi
        setPreferredSize(new Dimension((int) (190 * Main.SCALE), (int) (170 * Main.SCALE))); //default size is 190x170 @80dpi
        setLocation(MouseInfo.getPointerInfo().getLocation()); //new note is placed at current mouse coordinates

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

        //listener for ctrl+wheel (for zooming in and out)
        text.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown() || e.isMetaDown()) { //scroll wheel with ctrl pressed (isMetaDown is for apple faggots)
                    if (e.getWheelRotation() < 0) {
                        setTextScale(textScale + 0.1f);
                    } else if (e.getWheelRotation() > 0) {
                        setTextScale(textScale - 0.1f);
                    }
                } else { //scroll wheel without ctrl pressed simply scrolls
                    jScrollPane1.getMouseWheelListeners()[0].mouseWheelMoved(e);
                }
            }
        });

        //listener for ctrl+add, ctrl+minus, ctrl+NP0 (for zooming in and out)
        text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() || e.isMetaDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_ADD) {
                        setTextScale(textScale + 0.1f);
                    } else if (e.getKeyCode() == KeyEvent.VK_SUBTRACT) {
                        setTextScale(textScale - 0.1f);
                    } else if (e.getKeyCode() == KeyEvent.VK_NUMPAD0) {
                        setTextScale(1);
                    }
                }
            }
        });

        //listener for ctrl+N, ctrl+D
        text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() || e.isMetaDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_N) {
                        Main.newNote().setLocation((int) (preferredLocation.x + 40 * Main.SCALE), (int) (preferredLocation.y + 40 * Main.SCALE));
                    } else if (e.getKeyCode() == KeyEvent.VK_D) {
                        Main.delete(Note.this);
                    }
                }
            }
        });

        //listener for ctrl+A (select all text)
        text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() || e.isMetaDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_A) {
                        text.setSelectionStart(0);
                        text.setSelectionEnd(text.getText().length());
                    }
                }
            }
        });

        //listener for ctrl+Z, ctrl+Y (undo, redo)
        text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() || e.isMetaDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_Z) {
                        if (undo.canUndo()) {
                            undo.undo();
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_Y) {
                        if (undo.canRedo()) {
                            undo.redo();
                        }
                    }

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
        cut.setPreferredSize(new Dimension((int) (100 * Main.SCALE), (int) (36 * Main.SCALE)));
        cut.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                text.cut();
            }
        });
        copy = new JMenuItem(getLocString("COPY"));
        copy.setPreferredSize(new Dimension((int) (100 * Main.SCALE), (int) (36 * Main.SCALE)));
        copy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                text.copy();
            }
        });
        paste = new JMenuItem(getLocString("PASTE"));
        paste.setPreferredSize(new Dimension((int) (100 * Main.SCALE), (int) (36 * Main.SCALE)));
        paste.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                text.paste();
            }
        });
        delete = new JMenuItem(getLocString("DELETE"));
        delete.setPreferredSize(new Dimension((int) (100 * Main.SCALE), (int) (36 * Main.SCALE)));
        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String s = getClipboard();
                text.cut();
                setClipboard(s);
            }
        });
        selectAll = new JMenuItem(getLocString("SELECTALL"));
        selectAll.setPreferredSize(new Dimension((int) (100 * Main.SCALE), (int) (36 * Main.SCALE)));
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
        colorMenu.add(new ColorSelector(new Color[][]{YELLOW_SCHEME, ORANGE_SCHEME, BLUE_SCHEME, GREEN_SCHEME, PINK_SCHEME, PURPLE_SCHEME, RED_SCHEME, WHITE_SCHEME}) {
            @Override
            public void onColorSchemeSelected(Color[] scheme) {
                setColorScheme(scheme);
                colorMenu.setVisible(false);
                Main.saveState();
            }
        });
        //custom color selector
        colorMenu.add(new CustomColorSelector() {
            @Override
            public void onColorSelected(Color c) {
                //compute all colors from the selected color
                float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
                hsb[1] *= 0.69f; //reduce saturation
                if (hsb[2] < 0.55f) {
                    //if brightness<55%, use dark settings
                    Color internal = new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2])),
                            external = new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2] + 0.1f)),
                            bar = external,
                            buttons = new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2] + 0.3f)),
                            lineBorder = new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2] + 0.15f)),
                            text = new Color(255, 255, 255),
                            selectionBk = hsb[1] < 0.01f ? new Color(192, 192, 192) : new Color(Color.HSBtoRGB(hsb[0]-0.05f, hsb[1]+0.2f, 1f)), //if low saturation, use alternative selection color instead of computed one
                            selectedText = new Color(0, 0, 0);
                    Color[] customScheme = new Color[]{external, lineBorder, bar, buttons, internal, text, selectionBk, selectedText};
                    setColorScheme(customScheme);
                } else {
                    //otherwise, use bright settings
                    Color internal = new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2])),
                            external = new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2] - 0.04f)),
                            bar = external,
                            buttons = new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2] - 0.35f)),
                            lineBorder = new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2] - 0.1f)),
                            text = new Color(0, 0, 0),
                            selectionBk = hsb[1] < 0.01f ? new Color(72, 72, 72) : new Color(Color.HSBtoRGB(hsb[0], hsb[1]+0.2f, 0.4f)), //if low saturation, use alternative selection color instead of computed one
                            selectedText = new Color(255, 255, 255);
                    Color[] customScheme = new Color[]{external, lineBorder, bar, buttons, internal, text, selectionBk, selectedText};
                    setColorScheme(customScheme);
                }
                colorMenu.setVisible(false);
                Main.saveState();
            }
        });
        colorMenu.add(new JPopupMenu.Separator());
        JMenuItem m = new JMenuItem(getLocString("ABOUT"));
        m.setPreferredSize(new Dimension((int) (100 * Main.SCALE), (int) (36 * Main.SCALE)));
        m.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AboutDialog(null, true).setVisible(true);
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
        setBounds(x, y, getWidth(), getHeight());
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
     * setBounds method is overridden to force the note to stay on the screen
     *
     * @param x x
     * @param y y
     * @param width width
     * @param height height
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        if (preferredLocation == null) { //for some odd fucking reason, this can happen on some versions of java
            preferredLocation = new Point(0, 0);
        }
        preferredLocation.x = x;
        preferredLocation.y = y;
        Dimension s = Main.getExtendedScreenResolution();
        if (x + 60 * Main.SCALE > s.width) {
            x = (int) (s.width - 60 * Main.SCALE);
        }
        if (y + 60 * Main.SCALE > s.height) {
            y = (int) (s.height - 60 * Main.SCALE);
        }
        super.setBounds(x, y, width, height);
    }

    /**
     * setBounds method is overridden to force the note to stay on the screen
     *
     * @param r new bounds
     */
    @Override
    public void setBounds(Rectangle r) {
        setBounds(r.x, r.y, r.width, r.height);
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
        undo.discardAllEdits();
    }

    /**
     * gets the last user-set location of the note
     *
     * @return location
     */
    public Point getPreferredLocation() {
        return preferredLocation;
    }

    /**
     * get current text scale
     *
     * @return text scale
     */
    public float getTextScale() {
        return textScale;
    }

    /**
     * set new text scale
     *
     * @param scale scale as float 0.2-4.0
     */
    public void setTextScale(float scale) {
        if (scale >= 0.99 && scale <= 1.01) {
            textScale = 1;
            text.setFont(Main.BASE_FONT);
        } else {
            textScale = scale < MIN_TEXT_SCALE ? MIN_TEXT_SCALE : scale > MAX_TEXT_SCALE ? MAX_TEXT_SCALE : scale;
            text.setFont(Main.BASE_FONT.deriveFont(Main.TEXT_SIZE * textScale));
        }
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
        text.setCaretColor(c[5]);
        text.setSelectionColor(c[6]);
        text.setSelectedTextColor(c[7]);
    }

}

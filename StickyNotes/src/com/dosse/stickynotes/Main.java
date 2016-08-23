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
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import static java.lang.Thread.sleep;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;

/**
 * This class coordinates all the notes. It contains code to load, save, create,
 * delete. It also performs autosaving and initializes the UI.
 *
 * @author Federico
 */
public class Main {

    private static final String STORAGE_PATH, BACKUP_PATH, LOCK_PATH; //these variables will contain the paths to the files used by the application, initialized below

    static {
        String os = System.getProperty("os.name").toLowerCase();
        String home = "";
        try {
            if (os.startsWith("win")) {
                if (os.contains("xp")) { //on windows xp, we use %appdata%\NoteBot
                    home = System.getenv("APPDATA") + "\\NoteBot\\";
                } else { //on newer windows, we use %userprofile%\AppData\Local\NoteBot
                    home = System.getProperty("user.home") + "\\AppData\\Local\\NoteBot\\";
                }
            } else { //on other systems, we use ~/.notebot
                home = System.getProperty("user.home") + "/.notebot/";
            }
            //check if the folder exists: if it doesn't exist, create it; if a file already exists with that name, use fallback paths
            File f = new File(home);
            if (f.exists()) {
                if (!f.isDirectory()) {
                    throw new Exception();
                }
            } else {
                Path p = Paths.get(home);
                Files.createDirectories(p);
            }

        } catch (Throwable t) {
            //fallback path, local folder and hope for the best
            home = "";
        }
        STORAGE_PATH = home + "sticky.dat"; //main storage
        BACKUP_PATH = home + "sticky.dat.bak"; //backup in case main storage is corrupt
        LOCK_PATH = home + "lock"; //lock file to prevent multiple instances of StickyNotes to run on the same storage
    }

    private static final ArrayList<Note> notes = new ArrayList<Note>(); //currently open notes
    private static boolean noAutoCreate = false; //if set to true, an empty note will not be created if the app is started on an empty storage. enabled by the -autostartup parameter

    /**
     * saves currently open notes to the main storage, and turns the previous
     * storage to the backup storage.
     *
     * FORMAT: The .dat file will hold data as serialized objects. A Float
     * contains the SCALE at which the save was made, then an Integer contains
     * the number of notes, then for each note we have its location (Point), its
     * size (Dimension), the color scheme (Color[8]), the text (String).
     * Finally, a float for each note with the text scale for that note. This
     * data is written at the end to ensure compatibility with older versions of
     * the program.
     *
     * errors are ignored.
     */
    public static void saveState() {
        synchronized (notes) {
            ObjectOutputStream oos = null;
            try {
                File st = new File(STORAGE_PATH);
                File bk = new File(BACKUP_PATH);
                if (bk.exists()) {
                    bk.delete();
                }
                if (st.exists()) {
                    st.renameTo(bk);
                }
                st = new File(STORAGE_PATH);
                oos = new ObjectOutputStream(new FileOutputStream(st));
                oos.writeObject(SCALE);
                oos.writeObject(notes.size());
                for (Note n : notes) {
                    oos.writeObject(n.getPreferredLocation());
                    oos.writeObject(n.getSize());
                    oos.writeObject(n.getColorScheme());
                    oos.writeObject(n.getText());
                }
                //text scales are written at the end of the file so that older versions of the program can still load this .dat file
                for (Note n : notes) {
                    oos.writeObject(n.getTextScale());
                }
                oos.flush();
                oos.close();
            } catch (Throwable t) {
                try {
                    oos.close();
                } catch (Throwable t2) {
                }
            }
        }
    }

    /**
     * attempts to load the notes in the specified storage. notes loaded from
     * the file will also be adapted to the current screen DPI
     *
     * @param f storage
     * @return true if loading was successful, false if it was unsuccessful
     * (file not found or corrupt). if the storage is loaded correctly but there
     * are no notes inside it, it returns true and creates a new empty note
     * unless noAutoCreate is set to true, in which case it returns true and
     * does nothing
     */
    private static boolean attemptLoad(File f) {
        synchronized (notes) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(f));
                float savScale = (Float) (ois.readObject());
                float scaleMul = SCALE / savScale;
                int n = (Integer) (ois.readObject());
                if (n == 0) {
                    if (!noAutoCreate) {
                        Note note = new Note();
                        note.setVisible(true);
                        notes.add(note);
                    }
                    return true;
                }
                if (n < 0) {
                    return false;
                }
                for (int i = 0; i < n; i++) {
                    Note note = new Note();
                    Point p = (Point) (ois.readObject());
                    note.setLocation(p);
                    Dimension d = (Dimension) (ois.readObject());
                    d.height *= scaleMul;
                    d.width *= scaleMul;
                    note.setSize(d);
                    note.setColorScheme((Color[]) (ois.readObject()));
                    note.setText((String) (ois.readObject()));
                    notes.add(note);
                }
                try {
                    //attempt to load text scales. this will fail if we're loading a .dat file from a previous version
                    for (int i = 0; i < n; i++) {
                        notes.get(i).setTextScale((Float) (ois.readObject()));
                    }
                } catch (Throwable t) {
                }
                for (Note note : notes) {
                    note.setVisible(true);
                }
                ois.close();
            } catch (Throwable t) {
                try {
                    ois.close();
                } catch (Throwable t2) {
                }
                for (Note n : notes) {
                    n.setVisible(false);
                    n.dispose();
                }
                notes.clear();
                return false;
            }
            return true;
        }
    }

    /**
     * load notes from storage (main or backup)
     *
     * @return true if loading was successful, false otherwise
     */
    private static boolean loadState() {
        if (!attemptLoad(new File(STORAGE_PATH))) {
            if (!attemptLoad(new File(BACKUP_PATH))) {
                return false;
            }
        }
        return true;
    }

    /**
     * creates a new empty note
     *
     * @return the newly created note
     */
    public static Note newNote() {
        synchronized (notes) {
            Note n = new Note();
            n.setVisible(true);
            notes.add(n);
            saveState();
            return n;
        }
    }

    /**
     * deletes the specified note
     *
     * @param n note to be deleted
     */
    public static void delete(Note n) {
        synchronized (notes) {
            notes.remove(n);
            n.setVisible(false);
            n.dispose();
            saveState();
            if (notes.isEmpty()) {
                System.exit(0);
            }
        }
    }

    public static void bringToFront(Note n) {
        synchronized (notes) {
            notes.remove(n);
            notes.add(n);
        }
    }

    /**
     * checks if the application is already running by attempting to lock the
     * lockfile
     *
     * @return true if the application is already running, false otherwise (in
     * this case, the lockfile remains locked until the process is closed)
     */
    private static boolean alreadyRunning() {
        try {
            FileChannel ch = new RandomAccessFile(new File(LOCK_PATH), "rw").getChannel();
            if (ch.tryLock() != null) {
                return false;
            } else {
                ch.close();
                return true;
            }
        } catch (Throwable t) {
            return true;
        }
    }

    public static Dimension getExtendedScreenResolution() {
        //get screen resolution, also works with multiple screens
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();
        Dimension s = new Dimension(0, 0);
        for (GraphicsDevice screen : screens) {
            Rectangle r = screen.getDefaultConfiguration().getBounds();
            r.width += r.x;
            r.height += r.y;
            if (r.width > s.width) {
                s.width = r.width;
            }
            if (r.height > s.height) {
                s.height = r.height;
            }
        }
        return s;
    }

    /**
     * loads font from classpath
     *
     * @param pathInClasspath path in classpath
     * @return Font or null if it doesn't exist
     */
    private static Font loadFont(String pathInClasspath) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, Main.class.getResourceAsStream(pathInClasspath));
        } catch (Throwable ex) {
            return null;
        }
    }

    /**
     * calculates SCALE based on screen DPI. target DPI is 80, so if DPI=80,
     * SCALE=1. Min DPI is 64
     *
     * @return scale
     */
    private static float calculateScale() {
        float dpi = (float) Toolkit.getDefaultToolkit().getScreenResolution();
        return (dpi < 64 ? 64 : dpi) / 80f;
    }
    public static final float SCALE = calculateScale(); //used for DPI scaling. multiply each size by this factor.
    public static final float TEXT_SIZE = 12f * SCALE, TEXT_SIZE_SMALL = 11f * SCALE, BUTTON_TEXT_SIZE = 11f * SCALE; //default text sizes. used for DPI scaling
    /**
     * fonts
     */
    public static final Font BASE_FONT = loadFont("/com/dosse/stickynotes/fonts/OpenSans-Regular.ttf").deriveFont(TEXT_SIZE),
            SMALL_FONT = BASE_FONT.deriveFont(TEXT_SIZE_SMALL),
            BUTTON_FONT = loadFont("/com/dosse/stickynotes/fonts/OpenSans-Bold.ttf").deriveFont(BUTTON_TEXT_SIZE);
    /**
     * colors for swing MetalTheme
     */
    private static final ColorUIResource METAL_PRIMARY1 = new ColorUIResource(220, 220, 220),
            METAL_PRIMARY2 = new ColorUIResource(220, 220, 220),
            METAL_PRIMARY3 = new ColorUIResource(220, 220, 220),
            METAL_SECONDARY1 = new ColorUIResource(240, 240, 240),
            METAL_SECONDARY2 = new ColorUIResource(240, 240, 240),
            DEFAULT_BACKGROUND = new ColorUIResource(255, 255, 255);

    public static void main(String args[]) {
        if (alreadyRunning()) { //if the app is already running, it terminates the current instance
            System.exit(1);
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("-autostartup")) { //if the app is started with the -autostartup flag, it doesn't create an empty note (on windows the app is run when the system starts and it would be silly to create a new note when the system boots and there are no saved notes)
            noAutoCreate = true;
        }
        //apply swing MetalTheme, scroll down and ignore
        try {
            //<editor-fold defaultstate="collapsed" desc="MetalTheme">
            MetalLookAndFeel.setCurrentTheme(new MetalTheme() {
                @Override
                protected ColorUIResource getPrimary1() {
                    return METAL_PRIMARY1;
                }

                @Override
                protected ColorUIResource getPrimary2() {
                    return METAL_PRIMARY2;
                }

                @Override
                protected ColorUIResource getPrimary3() {
                    return METAL_PRIMARY3;
                }

                @Override
                protected ColorUIResource getSecondary1() {
                    return METAL_SECONDARY1;
                }

                @Override
                protected ColorUIResource getSecondary2() {
                    return METAL_SECONDARY2;
                }

                @Override
                protected ColorUIResource getSecondary3() {
                    return DEFAULT_BACKGROUND;
                }

                @Override
                public String getName() {
                    return "Metal Theme";
                }

                private final FontUIResource REGULAR_FONT = new FontUIResource(Main.BASE_FONT),
                        SMALL_FONT = new FontUIResource(Main.SMALL_FONT);

                @Override
                public FontUIResource getControlTextFont() {
                    return REGULAR_FONT;
                }

                @Override
                public FontUIResource getSystemTextFont() {
                    return REGULAR_FONT;
                }

                @Override
                public FontUIResource getUserTextFont() {
                    return REGULAR_FONT;
                }

                @Override
                public FontUIResource getMenuTextFont() {
                    return SMALL_FONT;
                }

                @Override
                public FontUIResource getWindowTitleFont() {
                    return REGULAR_FONT;
                }

                @Override
                public FontUIResource getSubTextFont() {
                    return REGULAR_FONT;
                }
            });
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            } catch (Throwable t) {
            }
            //</editor-fold>
        } catch (Throwable ex) {
        }
        //attempt to load from storage
        if (!loadState()) {
            if (!noAutoCreate) {
                newNote();
            }
        }
        //save current state
        saveState();
        if (notes.isEmpty()) { //if there are no saved notes and none were created automatically (-autostartup flag), close the app
            System.exit(0);
        }
        //this thread autosaves the notes every 60 seconds
        new Thread() {
            @Override
            public void run() {
                setPriority(Thread.MIN_PRIORITY);
                for (;;) {
                    try {
                        sleep(60000L);
                        synchronized (notes) {
                            if (notes.isEmpty()) {
                                return;
                            }
                            saveState();
                        }
                    } catch (Throwable t) {
                    }
                }
            }
        }.start();
        //this thread checks the notes every 1 second to make sure they're still inside the screen (in case the resolution changes)
        new Thread() {
            @Override
            public void run() {
                setPriority(Thread.MIN_PRIORITY);
                for (;;) {
                    try {
                        sleep(1000L);
                        synchronized (notes) {
                            if (notes.isEmpty()) {
                                return;
                            }
                            for (Note n : notes) {
                                n.setLocation(n.getPreferredLocation());
                            }
                        }
                    } catch (Throwable t) {
                    }
                }
            }
        }.start();
        //this thread calls the java gc every 5 minutes to keep ram usage low
        new Thread() {
            @Override
            public void run() {
                setPriority(Thread.MIN_PRIORITY);
                for (;;) {
                    try {
                        sleep(300000L);
                        synchronized (notes) {
                            if (notes.isEmpty()) {
                                return;
                            } else {
                                System.gc();
                            }
                        }
                    } catch (Throwable t) {
                    }
                }
            }
        }.start();
    }
}

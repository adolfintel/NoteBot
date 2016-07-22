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
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.Thread.sleep;
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
 *
 * @author Federico
 */
public class Main {

    private static final String STORAGE_PATH, BACKUP_PATH, LOCK_PATH;

    static {
        String os = System.getProperty("os.name").toLowerCase();
        String home = "";
        try {
            if (os.startsWith("win")) {
                if (os.contains("xp")) {
                    home = System.getenv("APPDATA") + "\\NoteBot\\";
                } else {
                    home = System.getProperty("user.home") + "\\AppData\\Local\\NoteBot\\";
                }
            } else {
                home = System.getProperty("user.home") + "/.notebot/";
            }
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
            System.out.println(t);
            home = "";
        }
        System.out.println(home);
        STORAGE_PATH = home + "sticky.dat";
        BACKUP_PATH = home + "sticky.dat.bak";
        LOCK_PATH = home + "lock";
    }

    private static final ArrayList<Note> notes = new ArrayList<Note>();
    private static boolean noAutoCreate = false;

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
                    oos.writeObject(n.getLocation());
                    oos.writeObject(n.getSize());
                    oos.writeObject(n.getColorScheme());
                    oos.writeObject(n.getText());
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
                    note.setLocation((Point) (ois.readObject()));
                    Dimension d = (Dimension) (ois.readObject());
                    d.height *= scaleMul;
                    d.width *= scaleMul;
                    note.setSize(d);
                    note.setColorScheme((Color[]) (ois.readObject()));
                    note.setText((String) (ois.readObject()));
                    note.setVisible(true);
                    notes.add(note);
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

    private static boolean loadState() {
        if (!attemptLoad(new File(STORAGE_PATH))) {
            if (!attemptLoad(new File(BACKUP_PATH))) {
                return false;
            }
        }
        return true;
    }

    public static void newNote() {
        synchronized (notes) {
            Note n = new Note();
            n.setVisible(true);
            notes.add(n);
            saveState();
        }
    }

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

    private static boolean alreadyRunning() {
        try {
            File f = new File(LOCK_PATH);
            FileOutputStream fos = new FileOutputStream(f);
            return fos.getChannel().tryLock() == null;
        } catch (Throwable t) {
            return true;
        }
    }

    public static final float SCALE = calculateScale(); //used for DPI scaling. multiply each size by this factor.
    //calculates SCALE based on screen DPI. target DPI is 80, so if DPI=80, SCALE=1. Min DPI is 64

    private static float calculateScale() {
        float dpi = (float) Toolkit.getDefaultToolkit().getScreenResolution();
        return (dpi < 64 ? 64 : dpi) / 80f;
    }
    private static float TEXT_SIZE = 12f * SCALE, TEXT_SIZE_SMALL = 11f * SCALE, BUTTON_TEXT_SIZE = 11f * SCALE;
    public static final Font BASE_FONT = Utils.loadFont("/com/dosse/stickynotes/fonts/OpenSans-Regular.ttf").deriveFont(TEXT_SIZE),
            SMALL_FONT = BASE_FONT.deriveFont(TEXT_SIZE_SMALL),
            BUTTON_FONT = Utils.loadFont("/com/dosse/stickynotes/fonts/OpenSans-Bold.ttf").deriveFont(BUTTON_TEXT_SIZE);

    private static final ColorUIResource METAL_PRIMARY1 = new ColorUIResource(220, 220, 220),
            METAL_PRIMARY2 = new ColorUIResource(220, 220, 220),
            METAL_PRIMARY3 = new ColorUIResource(220, 220, 220),
            METAL_SECONDARY1 = new ColorUIResource(240, 240, 240),
            METAL_SECONDARY2 = new ColorUIResource(240, 240, 240),
            DEFAULT_BACKGROUND = new ColorUIResource(255, 255, 255);

    public static void main(String args[]) {
        if (alreadyRunning()) {
            System.exit(1);
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("-autostartup")) {
            noAutoCreate = true;
        }
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
        if (!loadState()) {
            if (!noAutoCreate) {
                newNote();
            }
        }
        saveState();
        if (notes.isEmpty()) {
            System.exit(0);
        }
        new Thread() {
            @Override
            public void run() {
                for (;;) {
                    try {
                        sleep(60000L);//autosave every 60s
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
        new Thread() {
            @Override
            public void run() {
                for (;;) {
                    try {
                        sleep(5000L);//check position every 5s (in case the resolution changes)
                        synchronized (notes) {
                            if (notes.isEmpty()) {
                                return;
                            }
                            for (Note n : notes) {
                                n.setLocation(n.getLocation());
                            }
                        }
                    } catch (Throwable t) {
                    }
                }
            }
        }.start();
    }
}

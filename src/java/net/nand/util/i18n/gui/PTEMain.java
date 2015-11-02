/*
 * nand.net i18n utilities for Java: Property file editor for translators (side-by-side source and destination languages).
 * This file Copyright (C) 2013,2015 Jeremy D Monin <jeremy@nand.net>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The maintainer of this program can be reached at jeremy@nand.net
 */

package net.nand.util.i18n.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import net.nand.util.i18n.mgr.StringManager;

/**
 * Main startup for {@link PropertiesTranslatorEditor}.
 * Gives buttons with choice of new, open, open backup, exit.
 * Can browse to both dest + src files, or auto-pick src based on dest filename.
 *<P>
 * Work in progress.  See {@link PropertiesTranslatorEditor} for current limitations.
 *
 * @author Jeremy D Monin &lt;jeremy@nand.net&gt;
 */
@SuppressWarnings("serial")
public class PTEMain extends JFrame
    implements ActionListener, WindowListener
{
    /**
     * i18n text strings, taken from {@link PropertiesTranslatorEditor#strings};
     * if that's null, call {@link PropertiesTranslatorEditor#initStringManager()} to initialize.
     *<P>
     * Initialization is in PropertiesTranslatorEditor because the editor will always use that class
     * but not always use PTEMain.
     */
    private static StringManager strings;

    /** Editors we've opened; tracked here for unsaved changes before exit. */
    private final ArrayList<PropertiesTranslatorEditor> ptes;

    /**
     * Most recent time when user answered a "Save before exiting?" dialog with
     * Yes or No (not with Cancel exit), from {@link System#currentTimeMillis()}, or 0.
     * Set in {@link #askUnsaved(ArrayList)}, checked in {@link #windowClosing(WindowEvent)}.
     */
    private long askUnsavedAnsweredAt;

    private final JPanel btns;
    private JButton bNew, bOpenDest, bOpenDestSrc, bAbout, bExit;

    /** {@link Preferences} key in {@link #userPrefs} for directory of the most recently edited properties file. */
    private final static String LAST_EDITED_DIR = "lastEditedDir";

    /**
     * Persistently stored user preferences between runs, such as {@link #LAST_EDITED_DIR}.
     * Windows stores these in the registry under HKCU, OSX/Unix under the home directory.
     */
    private Preferences userPrefs;

    /**
     * 'Current' directory for open/save dialogs, from {@link #LAST_EDITED_DIR}, or null.
     * Tracked here because Java has no standard way to change the JVM's current directory.
     * Used and set in {@link #chooseFile(boolean, String)}.
     * Stored between runs within {@link #userPrefs}.
     */
    private File lastEditedDir;

    /**
     * Try to edit a destination file by finding its matching source file's name via the
     * {@link PropertiesTranslatorEditor#PropertiesTranslatorEditor(File) PropertiesTranslatorEditor(File)}
     * constructor.  If not found, or if it's a source file instead of a destination, displays an error message
     * and returns false.   Otherwise calls {@link PropertiesTranslatorEditor#init()} to show the pair for editing.
     *
     * @param dest  Destination .properties file
     * @param parent  Parent for any MessageDialog shown, or {@code null}
     * @return  True if found and shown for editing, false if error message shown instead
     */
    public boolean tryEditFromDestOnly(final File dest, final JFrame parent)
    {
        try {
            PropertiesTranslatorEditor pte = new PropertiesTranslatorEditor(dest);
            ptes.add(pte);
            pte.init();
            return true;
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog
                (parent, strings.get("dialog.open_dest.select_dest.text"),
                    /*
                     Please select the destination (more specific locale) .properties file, not the source file.
                     To open two specific files, use the 'Open Destination + Source' button.
                     */
                 strings.get("dialog.open_dest.select_dest.title"),  // "Select destination, not source"
                 JOptionPane.INFORMATION_MESSAGE);
        } catch (FileNotFoundException e) {
            // wrap error text in case dest is a long path
            JOptionPane.showMessageDialog
                (parent, strings.get("dialog.open_dest.no_src.text", dest),
                    /*
                     Could not find less-specific source locale .properties file on disk
                     to match {0}
                     To open two specific files, use the 'Open Destination + Source' button.
                     */
                 strings.get("dialog.open_dest.no_src.title"),  // "Source locale file not found"
                 JOptionPane.ERROR_MESSAGE);
        }

        return false;
    }

    /**
     * If there's 1 or 2 properties files on the command line, try to open it.
     * Otherwise, bring up the startup buttons.
     */
    public static void main(String[] args)
    {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        if (args.length >= 2)
        {
            new PropertiesTranslatorEditor(new File(args[0]), new File(args[1])).init();
        } else {
            PTEMain ptem = new PTEMain();

            if ((args.length == 1) && ptem.tryEditFromDestOnly(new File(args[0]), null))
                return;
                // if can't open args[0], shows error and falls through to PTEMain

            ptem.initAndShow();
        }
    }

    /**
     * Show the PropertiesTranslatorEditor "About" dialog.
     * Static to allow calls from any class.
     * @param parent  Parent for the dialog, as in {@link JOptionPane#showMessageDialog(java.awt.Component, Object)}
     */
    static void showAbout(final JFrame parent)
    {
        JOptionPane.showMessageDialog
            (parent,
             strings.get("dialog.about.text"),
             /*
             "PropertiesTranslatorEditor is a side-by-side editor for translators, showing\n" +
               "each key's value in the source and destination languages next to each other.\n" +
               "For more info, while editing click the Help button at the top of the editor.",
              */
             strings.get("dialog.about.title"),  // "About Properties Translator's Editor"
             JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Initialize layout and fields.  Does not pack or make visible; call {@link #initAndShow()} for that.
     */
    public PTEMain()
    {
        super("PropertiesTranslatorEditor");

        if (strings == null)
        {
            if (PropertiesTranslatorEditor.strings == null)
                PropertiesTranslatorEditor.initStringManager();
            strings = PropertiesTranslatorEditor.strings;
        }

        setTitle(strings.get("editor.window_title"));  // "Properties Translator's Editor"
        addWindowListener(this);  // windowClosing: save prefs and exit
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);  // check unsaved in windowClosing before dispose

        userPrefs = Preferences.userNodeForPackage(PTEMain.class);
        tryGetLastEditedDir();

        ptes = new ArrayList<PropertiesTranslatorEditor>();

        btns = new JPanel();
        btns.setLayout(new BoxLayout(btns, BoxLayout.PAGE_AXIS));
        btns.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));

        btns.add(new JLabel(strings.get("main.heading")));  // "Welcome to the Translator's Editor. Please choose:"
        bNew = addBtn(btns, this, strings.get("main.button.new"), KeyEvent.VK_N);      // "New..."
        bNew.setEnabled(false);  // TODO add this functionality
        bOpenDest = addBtn(btns, this, strings.get("main.button.open_dest"), KeyEvent.VK_O);  // "Open Destination..."
        bOpenDestSrc = addBtn(btns, this, strings.get("main.button.open_dest_src"), KeyEvent.VK_D);  // "Open Destination + Source..."
        bAbout = addBtn(btns, this, strings.get("main.button.about"), KeyEvent.VK_A);  // "About"
        bExit = addBtn(btns, this, strings.get("main.button.exit"), KeyEvent.VK_X);    // "Exit"

        getContentPane().add(btns);
        getRootPane().setDefaultButton(bOpenDest);
    }

    /**
     * Pack and make visible.
     */
    public void initAndShow()
    {
        pack();
        setVisible(true);
    }

    /**
     * Open these file(s) in a {@link PropertiesTranslatorEditor}.
     *
     * @param chooseFileSrc   Source properties file, or {@code null} to determine via the
     *                        {@link PropertiesTranslatorEditor#PropertiesTranslatorEditor(File) PropertiesTranslatorEditor(File)}
     *                        constructor.  If {@code null} and not found, displays an error message.
     * @param chooseFileDest  Destination properties file; can't be {@code null} or returns immediately
     * @param isNew           True if {@code chooseFileDest} should be created; not implemented yet.  Assumes
     *                        dest name follows properties naming standards for language and region.
     */
    public void openPropsEditor(File chooseFileSrc, File chooseFileDest, final boolean isNew)
    {
        if (chooseFileDest == null)
            return;

        // TODO handle isNew: create file?

        if (chooseFileSrc == null)
        {
            tryEditFromDestOnly(chooseFileDest, this);
                // calls new PropertiesTranslatorEditor(chooseFileDest).init() or shows error message
        } else {
            PropertiesTranslatorEditor pte = new PropertiesTranslatorEditor(chooseFileSrc, chooseFileDest);
            ptes.add(pte);
            pte.init();
        }
    }

    /**
     * If possible, reads from persistent {@link #userPrefs} and changes
     * 'current' directory field ({@link #lastEditedDir}) to that of
     * the most recently edited destination properties file.
     * @see #trySetDirMostRecent()
     */
    private void tryGetLastEditedDir()
    {
        lastEditedDir = null;

        try
        {
            if (userPrefs == null)
                return;  // unlikely, just in case

            final String dir = userPrefs.get(LAST_EDITED_DIR, null);
            if (dir == null)
                return;

            final File fdir = new File(dir);
            if (fdir.exists() && fdir.isDirectory())
                lastEditedDir = fdir;

        } catch (RuntimeException e) {
            // ignore SecurityException, IllegalStateException: don't change dir
        }
    }

    /**
     * Store 'current' directory {@link #lastEditedDir} to loaded {@link #userPrefs} preferences.
     * Catches and ignores {@link SecurityException}s, because this field is stored only for convenience.
     *<P>
     * <b>Note: Does not</b> persist to disk; to do so, call {@link #userPrefs}.{@link Preferences#flush() flush()}.
     * @see #tryGetLastEditedDir()
     */
    private void trySetDirMostRecent()
    {
        if ((lastEditedDir == null) || (userPrefs == null))
            return;

        try
        {
            userPrefs.put(LAST_EDITED_DIR, lastEditedDir.getAbsolutePath());
        } catch (SecurityException se) {}
    }

    /**
     * Add this button to the layout.
     * @param btns  Add to this button panel
     * @param lsnr  Add this action listener to the button
     * @param label Button's label
     * @param vkN  Shortcut mnemonic from {@link KeyEvent}
     * @return the new button
     */
    private static JButton addBtn(final Container btns, final ActionListener lsnr, final String label, final int vkN)
    {
        JButton b = new JButton(label);
        b.setMnemonic(vkN);
        btns.add(b);
        b.addActionListener(lsnr);
        Dimension size = b.getPreferredSize();
        size.width = Short.MAX_VALUE;
        b.setMaximumSize(size);
        return b;
    }

    /** Handle button clicks. */
    public void actionPerformed(ActionEvent e)
    {
        final Object src = e.getSource();
        if (src == bNew)
        {
            // TODO implement; need to enforce naming standards, or have dialog to ask src/dest lang+region
            System.err.println("Not implemented yet");
            // openPropsEditor(null, chooseFile(true, null), true);
        }
        else if (src == bOpenDest)
        {
            openPropsEditor(null, chooseFile(false, null), false);
        }
        else if (src == bOpenDestSrc)
        {
            clickedOpenDestSrc();
        }
        else if (src == bAbout)
        {
            showAbout(this);
        }
        else if (src == bExit)
        {
            windowClosing(null);
        }
    }

    /**
     * Handle a click on the "Open Destination + Source" button.
     * Browse to the destination file, then create and show the dialog to choose 2 property files.
     */
    private final void clickedOpenDestSrc()
    {
        final File dest = chooseFile(false, strings.get("dialog.open_dest_src.select_dest_file"));  // "Select destination file"
        if (dest == null)
            return;

        final File src1;
        try
        {
            src1 = PropertiesTranslatorEditor.makeParentFilename(dest.getPath());
            if (src1 == null)
            {
                // wrap error text in case dest is a long path
                JOptionPane.showMessageDialog
                    (this, strings.get("dialog.open_dest_src.no_src.text", dest),
                        // "Could not find less-specific source locale .properties file on disk\nto match {0}"
                     strings.get("dialog.open_dest.no_src.title"),
                        // "Source .properties file not found"
                     JOptionPane.WARNING_MESSAGE);
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog
                (this, strings.get("dialog.open_dest_src.select_dest.text"),
                    // "Please select the destination (more specific locale) .properties file, not the source file."
                 strings.get("dialog.open_dest.select_dest.title"),
                     // "Select destination, not source"
                 JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        final OpenDestSrcDialog dia = new OpenDestSrcDialog(dest, src1);
        dia.setVisible(true);  // modal, waits for selection
        if ((dia.dest == null) || (dia.src == null))
            return;

        openPropsEditor(dia.src, dia.dest, false);
    }

    /**
     * Choose a file to open or save.  Uses and updates {@link #lastEditedDir}.
     * For visual feedback, uses {@link Cursor#WAIT_CURSOR} during the delay before the file chooser appears.
     * @param forNew  If true, use Save dialog, otherwise Open dialog
     * @param title  Optional dialog title, or {@code null} for default
     * @return   the chosen file, or null if nothing was chosen
     */
    private File chooseFile(final boolean forNew, final String title)
    {
        // TODO filtering: setFileFilter, addChoosableFileFilter, etc
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        final JFileChooser fc = new JFileChooser();
        if ((lastEditedDir != null) && lastEditedDir.exists())
            fc.setCurrentDirectory(lastEditedDir);
        if (title != null)
            fc.setDialogTitle(title);

        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        int returnVal;
        if (forNew)
            returnVal = fc.showSaveDialog(this);
        else
            returnVal = fc.showOpenDialog(this);

        if (returnVal != JFileChooser.APPROVE_OPTION)
            return null;

        lastEditedDir = fc.getCurrentDirectory();
        trySetDirMostRecent();

        File file = fc.getSelectedFile();
        return file;
    }

    /**
     * Check all open editors' {@link PropertiesTranslatorEditor#hasUnsavedChanges()} and return any.
     * @return  Editors with unsaved changes to src or dest, or {@code null} if none
     * @see #askUnsaved(ArrayList)
     */
    private ArrayList<PropertiesTranslatorEditor> checkUnsaved()
    {
        ArrayList<PropertiesTranslatorEditor> unsaved = null;
        for (final PropertiesTranslatorEditor pte : ptes)
        {
            if (pte.hasUnsavedChanges())
            {
                if (unsaved == null)
                    unsaved = new ArrayList<PropertiesTranslatorEditor>();
                unsaved.add(pte);
            }
        }

        return unsaved;
    }

    /**
     * For any editors with unsaved changes, ask the user if they want to save, discard, or cancel exit.  If not
     * cancelled, sets {@link #askUnsavedAnsweredAt} and calls {@link #windowClosing(WindowEvent) windowClosing(null)}
     * to exit.
     * @param unsavedPTE  List of editors with unsaved changes, from {@link #checkUnsaved()}
     */
    private void askUnsaved(final ArrayList<PropertiesTranslatorEditor> unsavedPTE)
    {
        for (final PropertiesTranslatorEditor pte : unsavedPTE)
            if (! pte.checkUnsavedBeforeDispose())
                return;  // false == cancel exit

        askUnsavedAnsweredAt = System.currentTimeMillis();
        windowClosing(null);
    }

    /**
     * Check if any editor has unsaved changes.  If any, call {@link #askUnsaved(ArrayList)} instead of closing.
     * Save {@link #userPrefs} if possible, dispose of the main button window,
     * and call {@link System#exit(int) System.exit(0)}.
     * @param e  Event, ignored (null is okay)
     */
    public void windowClosing(WindowEvent e)
    {
        ArrayList<PropertiesTranslatorEditor> unsavedPTE = checkUnsaved();
        if (unsavedPTE != null)
        {
            // was user already asked?
            final long now = System.currentTimeMillis();
            if ((askUnsavedAnsweredAt == 0) || (now - askUnsavedAnsweredAt > 3000L))
            {
                askUnsaved(unsavedPTE);  // will call windowClosing after save or discard decision is made
                return;
            }
        }

        try
        {
            if (userPrefs != null)
                userPrefs.flush();
        } catch (BackingStoreException ex) {}  // is OK if we can't save last-opened-location pref

        dispose();
        System.exit(0);
    }

    public void windowActivated(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}

    /**
     * Modal dialog to choose a pair of destination and source locale files to edit.
     * To use the dialog, call {@link #setVisible(boolean) setVisible(true)} and when that returns,
     * check if {@link #src} != {@code null}.
     *<P>
     * If a destination file includes a region code {@code toClient_zz_rgn.properties}, the two source files offered are
     * {@code toClient_zz.properties} and {@code toClient.properties}.  If a destination file has no region,
     * {@code toClient_zz.properties}, the source file offered is {@code toClient.properties}.  An option of "other"
     * is always offered to select any file.
     *<P>
     * The dialog shows the full path to the destination file.  To reduce clutter, the source file choices show
     * only their filenames since they're in the same directory as the destination.  "Other" shows the full path.
     */
    private class OpenDestSrcDialog
        extends JDialog implements ActionListener
    {
        /** Destination file already chosen by user before this dialog; see {@link #src} */
        public final File dest;

        /** Source file chosen here by user, if any, or {@code null} if they cancelled; see {@link #dest} */
        public File src;

        private JButton bEdit, bCancel, bBrowseOther;

        /**
         * Source file choices or {@code null}.
         * {@code src1} is null if no matching source could be found on disk for {@link #dest}.
         * {@code src2} is null unless {@link #dest} has a language and region.
         */
        private File src1, src2, srcOther;

        /** Text field for {@link #srcOther} */
        private JTextField tfSrcOther;

        /**
         * Radio buttons to choose {@link #src1}, {@link #src2}, {@link #srcOther}.
         * {@link #src1} is null if no matching source could be found on disk for dest.
         * {@link #src2} is null unless {@link #dest} has a language and region.
         */
        private JRadioButton bSrc1, bSrc2, bSrcOther;

        private WindowAdapter wa;

        /**
         * Create a new dialog, not initially visible; see class javadoc.
         * @param dest  Destination file, not null
         * @param src1  First source file choice, as found by
         *     {@link PropertiesTranslatorEditor#makeParentFilename(String)}, or null if none
         * @throws IllegalArgumentException if {@code dest} is {@code null}
        */
        private OpenDestSrcDialog(final File dest, final File src1)
            throws IllegalArgumentException
        {
            super(PTEMain.this, strings.get("dialog.open_dest_src.select_src_dest_files"), true);
                // "Select source and destination files"
            if (dest == null)
                throw new IllegalArgumentException("null dest");

            this.dest = dest;
            this.src1 = src1;
            src = src1;  // default selection or null

            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            wa = new WindowAdapter()
            {
                public void windowClosing(WindowEvent e)
                {
                    src = null;
                }
            };
            addWindowListener(wa);

            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.gridwidth = GridBagConstraints.REMAINDER;  // Most components here are 1 per row
            gbc.fill = GridBagConstraints.HORIZONTAL;

            final JPanel p = new JPanel(gbl);

            p.setBorder(BorderFactory.createEmptyBorder(9, 9, 0, 9));  // button panel has a bottom margin, so 0 here

            addToGrid(p, gbl, gbc, new JLabel(strings.get("dialog.open_dest_src.select_to_edit")));
                // "Select the source and destination locale files to edit."

            addToGrid(p, gbl, gbc, Box.createRigidArea(new Dimension(0,15)));  // space above label
            addToGrid(p, gbl, gbc, new JLabel(strings.get("dialog.open_dest_src.src_label")));
                // "Source (less specific locale):"

            if (src1 != null)
            {
                bSrc1 = new JRadioButton(src1.getName());
                bSrc1.setSelected(true);
                bSrc1.addActionListener(this);
                addToGrid(p, gbl, gbc, bSrc1);

                try
                {
                    src2 = PropertiesTranslatorEditor.makeParentFilename(src1.getPath());
                } catch (IllegalArgumentException e) {}
                if (src2 != null)
                {
                    bSrc2 = new JRadioButton(src2.getName());
                    bSrc2.addActionListener(this);
                    addToGrid(p, gbl, gbc, bSrc2);
                }
            }

            // Other Source: radio, textfield, browse button on same row
            gbc.gridwidth = 1;
            bSrcOther = new JRadioButton("");
            bSrcOther.addActionListener(this);
            addToGrid(p, gbl, gbc, bSrcOther);
            // even if src1 == null, don't call bSrcOther.setSelected(true);
            // let the user click it to bring up the file chooser

            tfSrcOther = new JTextField();
            gbc.weightx = 1;  // expand tfSrcOther to fill available width
            addToGrid(p, gbl, gbc, tfSrcOther);
            gbc.weightx = 0;
            bBrowseOther = new JButton(strings.get("dialog.open_dest_src.other_"));  // "Other..."
            bBrowseOther.setMnemonic(KeyEvent.VK_O);
            bBrowseOther.addActionListener(this);
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            addToGrid(p, gbl, gbc, bBrowseOther);

            ButtonGroup radios = new ButtonGroup();
            if (bSrc1 != null)
                radios.add(bSrc1);
            if (bSrc2 != null)
                radios.add(bSrc2);
            radios.add(bSrcOther);

            addToGrid(p, gbl, gbc, Box.createRigidArea(new Dimension(0, 15)));  // space above label
            addToGrid(p, gbl, gbc, new JLabel(strings.get("dialog.open_dest_src.dest_label")));
                // "Destination (more specific locale):"
            addToGrid(p, gbl, gbc, new JLabel(dest.getPath()));  // show dest's entire path for clarity

            JPanel btns = new JPanel(new FlowLayout(FlowLayout.TRAILING, 3, 15));  // 15 for space above buttons
            bEdit = addBtn(btns, this, strings.get("base.edit"), KeyEvent.VK_E);
            bCancel = addBtn(btns, this, strings.get("base.cancel"), KeyEvent.VK_ESCAPE);
            addToGrid(p, gbl, gbc, btns);

            setContentPane(p);
            getRootPane().setDefaultButton(bEdit);
            getRootPane().registerKeyboardAction
                (new ActionListener()
                {
                    public void actionPerformed(ActionEvent arg0)
                    {
                        wa.windowClosing(null);  // set src to null
                        dispose();
                    }
                }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

            pack();

        }

        /** Add this component to a container which uses GridBagLayout. */
        private final void addToGrid
            (final Container ct, final GridBagLayout gbl, final GridBagConstraints gbc, final Component cmp)
        {
            gbl.setConstraints(cmp, gbc);
            ct.add(cmp);
        }

        /**
         * Handle button clicks and radio button selections:
         * Clicking a radio button sets {@link #src} from {@link #src1}, {@link #src2}, or {@link #srcOther}.
         * Edit button validates {@link #src} and may dispose the dialog.  Cancel also disposes here.
         */
        public void actionPerformed(final ActionEvent ae)
        {
            final Object s = ae.getSource();

            if (s == bCancel)
            {
                wa.windowClosing(null);  // set src to null
                dispose();
            }
            else if (s == bSrc1)
            {
                src = src1;
            }
            else if (s == bSrc2)
            {
                src = src2;
            }
            else if (s == bSrcOther)
            {
                src = srcOther;
                if ((srcOther == null) && (tfSrcOther.getText().trim().length() == 0))
                    chooseSrcOther();
            }
            else if (s == bBrowseOther)
            {
                if (chooseSrcOther())
                {
                    src = srcOther;
                    bSrcOther.setSelected(true);
                }
            }

            if (s != bEdit)
                return;

            // about to validate. If "Other" is selected, update the File objects in case text field contents changed.

            if (bSrcOther.isSelected())
            {
                final String other = tfSrcOther.getText().trim();
                if (other.length() == 0)
                    srcOther = null;
                else if ((srcOther == null) || ! other.equals(srcOther.getPath()))
                    srcOther = new File(other);

                src = srcOther;
            }

            // validate, dispose if OK

            if ((dest == null) || ! dest.exists())
            {
                JOptionPane.showMessageDialog
                    (this, strings.get("dialog.open_dest_src.no_dest.text"),  // "Destination locale file not found."
                     strings.get("dialog.open_dest_src.file_not_found"),      // "File not found",
                     JOptionPane.WARNING_MESSAGE);
            }
            else if ((src == null) || ! src.exists())
            {
                if ((src == null)
                    && ( (bSrc1 == null) || ! bSrc1.isSelected() )
                    && ( (bSrc2 == null) || ! bSrc2.isSelected() )
                    && ( tfSrcOther.getText().length() == 0 ))
                {
                    final String choose_src_file = strings.get("dialog.open_dest_src.choose_src_file");
                        // "Choose a source locale file."
                    JOptionPane.showMessageDialog
                        (this, choose_src_file,
                         choose_src_file, JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog
                        (this, strings.get("dialog.open_dest.no_src.title"),  // "Source locale file not found."
                         strings.get("dialog.open_dest_src.file_not_found"),  // "File not found",
                         JOptionPane.WARNING_MESSAGE);
                }
            } else {
                dispose();
            }
        }

        /**
         * Call {@link PTEMain#chooseFile(boolean, String)} for the "Other" source option.
         * If a file was selected and exists, update {@link #srcOther} and {@link #tfSrcOther}.
         * Make sure {@link #dest} isn't also selected as {@link #srcOther}.
         * @return  True if a file was selected and exists, false otherwise
         */
        private boolean chooseSrcOther()
        {
            File f = chooseFile(false, strings.get("dialog.open_dest_src.choose_src_file"));
                // "Choose a source locale file."
            if (f != null)
            {
                if (f.getAbsolutePath().equals(dest.getAbsolutePath()))
                {
                    JOptionPane.showMessageDialog
                        (this, strings.get("dialog.open_dest_src.src_is_dest.text"),
                             // "This file is the destination file, it cannot also be the source."
                         strings.get("dialog.open_dest_src.src_is_dest.title"),
                             // "Source is destination",
                         JOptionPane.WARNING_MESSAGE);
                    return false;
                }

                srcOther = f;
                tfSrcOther.setText(srcOther.getPath());
                return true;
            } else {
                return false;
            }
        }

    }  // inner class OpenDestSrcDialog

}

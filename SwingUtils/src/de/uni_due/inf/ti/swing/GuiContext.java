/* 
 * Copyright (c) 2010-2014, H.J. Sander Bruggink, University of Duisburg-Essen
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   - Neither the names of its copyright holder(s) nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package de.uni_due.inf.ti.swing;

import java.awt.Color;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;

import de.uni_due.inf.ti.gui.CommandTextInfo;

public class GuiContext
{    
    public static final String KEY_OK       = "SwingUtils.OK";
    public static final String KEY_ADD      = "SwingUtils.ADD";
    public static final String KEY_APPLY    = "SwingUtils.APPLY";
    public static final String KEY_CANCEL   = "SwingUtils.CANCEL";

    /** Resource bundle where the GUI strings are read from. */
    private static ResourceBundle guiStrings;
    
    /** Enum which contains the supported platforms. */
    public static enum Platform 
    {
        UNKNOWN ("UNK"), 
        WINDOWS ("WIN"), 
        MACOSX ("MAC"),
        LINUX ("UNX");
      
        private final String ident;
      
        Platform(String ident) 
        {
            this.ident = ident;
        }
      
        /**
         * Returns this platform's identification string.
         */
        public String getIdent() 
        {
            return ident;
        }
    }

    /** Cache for platform. */
    private static Platform platform = null;

    /**
     * Guess on which platform the program runs.
     */
    public static Platform getPlatform() 
    {
        if (platform == null) {
            platform = Platform.UNKNOWN;
            String osName = System.getProperty("os.name");
            if (osName != null) {
                if (osName.indexOf("Windows") >= 0)
                    platform = Platform.WINDOWS;
                else if (osName.indexOf("Mac OS X") >= 0) 
                    platform = Platform.MACOSX;
                else if (osName.indexOf("Linux") >= 0) 
                    platform = Platform.LINUX;
            }
        }
        return platform;
    }

    /** 
     * Initializes the SwingUtils library.
     * 
     * @param resourcePackage
     *      package where resource files are located
     * @param language
     *      ISO string of the interface language
     * @throws NullPointerException
     *      if {@code resourcePackage} is {@code null}
     * @throws MissingResourceException
     *      if not resource bundle was found
     */
    public static void initialize(String resourcePackage, String language)
    {
        if (resourcePackage == null) {
            throw new NullPointerException();
        }
        
        if (language == null) language = "";
        
        String pv = getPlatform().getIdent();
        Locale loc = new Locale(language, "", pv);
        guiStrings = ResourceBundle.getBundle(resourcePackage
                + ".ui_strings", loc);
    }
    
    /**
     * Creates a new menu. 
     * 
     * @param key
     *      key of the menu caption in the resource bundle
     * @return
     *      a new JMenu with a caption corresponding to the given key
     * @throws NullPointerException
     *      if {@code key} is {@code null}
     */
    public static JMenu createMenu(String key)
    {
        CommandTextInfo ti = CommandTextInfo.getCommandTextInfo(getGuiString(key));
        JMenu menu = new JMenu(ti.getText());
        if (ti.hasMnemonic()) {
            menu.setMnemonic(ti.getMnemonic());
            menu.setDisplayedMnemonicIndex(ti.getDisplayedMnemonicIndex());
        }
        return menu;
    }
    
    /**
     * Creates a new label.
     * 
     * @param key
     *      key of the label caption in the resource bundle
     * @return 
     *      new JLabel with a caption corresponding to the given key
     * @throws NullPointerException
     *      when {@code key} is {@code null}
     */
    public static JLabel createLabel(String key)
    {
        CommandTextInfo ti = CommandTextInfo.getCommandTextInfo(getGuiString(key));
        JLabel result = new JLabel(ti.getText());
        result.setOpaque(false);
        return result;
    }

    /**
     * Creates a new label.
     * 
     * @param key
     *      key of the label caption in the resource bundle
     * @param labelFor
     *      component for which receives focus when the label is activated
     * @return 
     *      new JLabel with a caption corresponding to the given key
     * @throws NullPointerException
     *      when {@code key} is {@code null}
     */
    public static JLabel createLabel(String key, JComponent labelFor)
    {
        CommandTextInfo ti = CommandTextInfo.getCommandTextInfo(getGuiString(key));
        JLabel result = new JLabel(ti.getText());
        result.setLabelFor(labelFor);
        result.setOpaque(false);
        return result;
    }
    
    /**
     * Creates a new check box.
     * 
     * @param key
     *      key of the check box caption in the resource bundle
     * @return 
     *      a new JCheckBox with a text corresponding to the given resource
     *      key
     * @throws NullPointerException
     *      when {@code key} is {@code null}
     */
    public static JCheckBox createCheckBox(String key)
    {
        CommandTextInfo ti = CommandTextInfo.getCommandTextInfo(getGuiString(key));
        JCheckBox result = new JCheckBox(ti.getText());
        result.setOpaque(false);
        result.setToolTipText(ti.getHint());
        result.setDisplayedMnemonicIndex(ti.getDisplayedMnemonicIndex());
        result.setMnemonic(ti.getMnemonic());
        return result;
    }
    
    /**
     * Creates a panel with a row of buttons.
     * 
     * @param buttons
     *      buttons to be placed on the panel
     * @return
     *      a panel with the given buttons on it
     * @throws NullPointerException
     *      when {@code buttons} is {@code null}
     */
    public static JPanel createButtonPanel(List<JButton> buttons) 
    {
        JPanel panel = new JPanel();
        boolean first = true;
        for (JButton but : buttons) {
            panel.add(but);
            if (!first) {
                panel.add(Box.createHorizontalStrut(10));
            }
            first = false;
        }
        panel.setBackground(new Color(0,0,0,0));
        return panel;
    }
    
    /**
     * Helper method for {@link getGuiString}. Do not call directly.
     */
    private static String _getGuiString(String key, int nesting)
    {
        if (nesting == 0) {
            System.err.println("Warning: gui string nesting too high.");
            return key;
        }
        try {
            String str = guiStrings.getString(key);
            if (str.length() == 0)
                return str;
            else {
                int startrd = str.indexOf("{@");
                if (startrd >= 0) {
                    int endrd = str.indexOf('}');
                    if (endrd <= startrd)
                        return str;
                    else {
                        String rep = _getGuiString(
                                str.substring(startrd + 2, endrd), nesting - 1);
                        return str.substring(0, startrd) + rep
                                + str.substring(endrd + 1);
                    }
                } else
                    return str;
            }
        } 
        catch (MissingResourceException e) {
            return key;
        }
    }
    
    /**
     * Get a string from the GUI resource bundle. If the key is not found in the
     * resource bundle, the key itself is returned.
     * 
     * @param key
     *      key of the required string
     * @return 
     *      string corresponding to the given key
     * @throws NullPointerException
     *      when {@code key} is {@code null}
     */
    public static String getGuiString(String key)
    {
        if (key == null) throw new NullPointerException();
        return _getGuiString(key, 4);
    }
    
    /**
     * Get a string from the GUI resource bundle and remove all "&"'s. This is 
     * a convenience method to be used for dialog titles, column headers, etc.
     * 
     * @param key
     *      key of the returned string
     * @return 
     *      string corresponding to the given key, with all "&"'s removed
     * @throws NullPointerException
     *      when {@code key} is {@code null}
     */
    public static String getSimpleGuiString(String key)
    {
        if (key == null) throw new NullPointerException();
        String raw = getGuiString(key);
        return CommandTextInfo.getCommandText(raw);
    }
}

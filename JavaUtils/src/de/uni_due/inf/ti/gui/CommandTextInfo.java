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

package de.uni_due.inf.ti.gui;

import java.awt.event.KeyEvent;

/**
 * This class represents information about command texts. It stores three
 * things: the command text itself, the key used as the mnemonic, and the index
 * within the command text which is displayed as the mnemonic. Furthermore, it
 * may additionally contain a "hint" text.
 * 
 * @author H. J. Sander Bruggink
 */
public class CommandTextInfo
{
    private int m;
    private int mIdx;
    private String text;
    private String hint;

    /**
     * Creates a new CommandTextInfo instance.
     * 
     * @param text
     *      displayed text
     * @param mnemonic
     *      mnemonic character of the command, or {@code -1} if it has no
     *      mnemonic
     * @param dispIdx
     *      index of the mnemonic character within the command, or
     *      {@code -1} if it has no mnemonic
     * @param hint
     *      hint of the command
     */
    public CommandTextInfo(String text, int mnemonic, int dispIdx, String hint)
    {
        this.text = text;
        this.m = mnemonic;
        this.mIdx = dispIdx;
        this.hint = hint;
    }

    
    /**
     * Creates a new CommandTextInfo instance without hint.
     * 
     * @param text
     *      displayed text
     * @param mnemonic
     *      mnemonic character of the command, or {@code -1} if it has no
     *      mnemonic
     * @param dispIdx
     *      index of the mnemonic character within the command, or
     *      {@code -1} if it has no mnemonic
     */
    public CommandTextInfo(String text, int mnemonic, int dispIdx)
    {
        this(text, mnemonic, dispIdx, null);
    }

    
    /**
     * Returns whether or not the caption represented by this CommandTextInfo
     * object has a mnemonic.
     */
    public boolean hasMnemonic()
    {
        return (m > 0);
    }

    
    /**
     * Returns the text of this CommandTextInfo.
     */
    public String getText()
    {
        return text;
    }

    
    /**
     * Returns the mnemonic of this CommandTextInfo.
     */
    public int getMnemonic()
    {
        return m;
    }

    
    /**
     * Returns the displayed mnemonic index of this CommandTextInfo.
     */
    public int getDisplayedMnemonicIndex()
    {
        return mIdx;
    }

    
    /**
     * Returns the hint string of this CommandTextInfo.
     */
    public String getHint()
    {
        return hint;
    }

    
    /**
     * Return the mnemonic code for a given character.
     * 
     * @param ch
     *            the character of which the mnemonic code must be returned
     * @return mnemonic code of {@code ch}, or -1 if {@code ch} cannot be used
     *         as a mnemonic
     */
    private static int getMnemonicFromChar(char ch)
    {
        if ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'Z')) {
            return (int) ch;
        } 
        else if (ch >= 'a' && ch <= 'z') {
            return KeyEvent.VK_A + ((int) ch - (int) 'a');
        } 
        else return -1;
    }

    
    /**
     * Returns a {@code CommandTextInfo} object which represents the information
     * in the string {@code txt}. The string is parsed as follows:
     * <ul>
     * <li>
     *   The part of the string before the first '|'-character is considered to
     *   be the command text, the rest an optional hint or title; (depending on
     *   the type of the text).
     * </li><li>
     *   The position of the first &amp; in the command text part is considered
     *   to be the displayed mnemonic index, and the next character is used as
     *   mnemonic. Furthermore, all &amp;'s in the command part are removed.
     * </li><li>
     *   All substrings of the form "%<i>n</i>", where <i>n</i> is a number
     *   from 1 to 9, are replaced by the given parameter strings, that is,
     *   "%1" is replaced by the first given parameter string, "%2" by the
     *   second, etc. If no parameter string is given for the index, the tag is
     *   left in the string unchanged.</li>
     * <li>
     * All 'active' characters can be escaped by preceding them with a backslash
     * ("\").</li>
     * </ul>
     * 
     * <strong>Note:</strong> Typically, the command text descriptions given as
     * arguments to this method are obtained from property resource bundles. In
     * this case, it is good to remember that the Java property file parsing
     * does some backslash parsing also. It may therefore in some case be
     * necessary to escape certain characters <em>twice</em>.
     * 
     * @param txt
     *      string representation of text and mnemonic index, as present
     *      in the resource files
     * @param param
     *      strings by which the variable placeholders "%1", "%2", etc,
     *      are to be replaced
     * @return 
     *      CommandTextInfo object containing the text, mnemonic, displayed
     *      mnemonic index and tooltip text represented by the description
     *      {@code txt}.
     */
    public static CommandTextInfo getCommandTextInfo(String txt,
            String... param)
    {
        return _getCommandTextInfo(txt, param);
    }

    
    /**
     * Returns a {@code CommandTextInfo} object which represents the information
     * in the string {@code txt}, without any argument replacements.
     * 
     * @see getCommandTextInfo(String, String...)
     */
    public static CommandTextInfo getCommandTextInfo(String txt)
    {
        return _getCommandTextInfo(txt, null);
    }

    
    /**
     * Parses a command text description and returns its text part. This method
     * is equivalent to {@code getCommandText(txt, param).getText()}.
     */
    public static String getCommandText(String txt, String... param)
    {
        return _getCommandTextInfo(txt, param).text;
    }

    
    /**
     * Parses a command text description and returns its text part. This method
     * is equivalent to {@code getCommandText(txt).getText()}.
     */
    public static String getCommandText(String txt)
    {
        return _getCommandTextInfo(txt, null).text;
    }

    
    /**
     * Private worker method. Do not call directly.
     */
    private static CommandTextInfo _getCommandTextInfo(String txt,
            String[] param)
    {
        StringBuilder txtb = new StringBuilder(txt);
        int pos = 0; // current position of the parser
        int divide = -1; // position of the "|", or -1 if "|" has not (yet) been
                         // encountered
        int mIdx = -1;
        int mnemonic = -1;
        while (pos < txtb.length()) {
            switch (txtb.charAt(pos)) {
                case '&':
                    if (divide < 0) {
                        if (mnemonic < 0 && pos < txtb.length() - 1) {
                            mnemonic = getMnemonicFromChar(txtb.charAt(pos + 1));
                            mIdx = pos;
                        }
                        txtb.deleteCharAt(pos); // delete & character
                        pos++; // and skip mnemonic character
                    } 
                    else
                        pos++; // skip & character
                    break;
                case '\\':
                    if (pos < txtb.length() - 1) {
                        char next = txtb.charAt(pos + 1);
                        switch (next) {
                            case 'n': // newline
                                txtb.replace(pos, pos + 2, "\n");
                                pos += "\n".length();
                                break;
                            case '|': // literal "command" character
                            case '\\':
                            case '&':
                                txtb.replace(pos, pos + 2,
                                        Character.toString(next));
                                pos++;
                                break;
                            default: // default: skip "\" character
                                pos++;
                        }
                    } 
                    else
                        pos++; // skip "\" character
                    break;
                case '%':
                    if (pos < txtb.length() - 1) {
                        char next = txtb.charAt(pos + 1);
                        if (Character.isDigit(next)) {
                            int idx = Character.digit(next, 10);
                            if (param != null && idx <= param.length) {
                                String rs = param[idx - 1];
                                txtb.replace(pos, pos + 2, rs);
                                pos += rs.length(); // do not parse replacement
                                                    // string
                            } 
                            else pos += 2; // too high parameter number,
                                           // skip entire parameter ("%n")
                        } 
                        else pos++; // not an index number, skip "%"
                    } 
                    else pos++; // end of stream: skip "%"
                    break;
                case '|':
                    if (divide < 0) {
                        divide = pos;
                    }
                    pos++;
                default:
                    pos++; // normal character encountered, skip it
            }
        }
        String command, hint;
        if (divide < 0) {
            command = txtb.toString();
            hint = null;
        } 
        else {
            command = txtb.substring(0, divide);
            hint = txtb.substring(divide + 1);
        }
        return new CommandTextInfo(command, mnemonic, mIdx, hint);
    }
}

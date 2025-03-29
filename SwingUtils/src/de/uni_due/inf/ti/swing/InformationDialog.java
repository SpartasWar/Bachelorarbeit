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

import java.awt.Dialog;
import java.awt.Frame;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;


/**
 * A dialog which presents information. The user can close the dialog but no
 * other user interaction is offered (unless additional buttons are added
 * through StandardDialog's 
 * {@link StandardDialog#addCustomAction(javax.swing.Action) addCustomAction} 
 * method) or a information component is used which has user interaction).
 * 
 * @author H.J. Sander Bruggink
 */
public class InformationDialog extends StandardDialog<Void>
{
    private static final long serialVersionUID = -262664487019798230L;
    
    private JComponent infoComponent;

    /**
     * Creates a new InformationDialog which shows a custom component containing
     * the information to be shown.
     * 
     * @param owner
     *      owner of the dialog
     * @param title
     *      title of the dialog
     * @param infoComponent
     *      component which displays the information
     */
    public InformationDialog(Dialog owner, String title, JComponent infoComponent)
    {
        super(owner, title, EnumSet.of(Button.CANCEL));
        if (infoComponent == null) {
            throw new NullPointerException();
        }
        setCancelKey("cmd.close");
        this.infoComponent = infoComponent;
    }
    
    
    /**
     * Creates a new InformationDialog which shows a text read from an
     * input stream.
     * 
     * @param owner
     *      owner of the dialog
     * @param title
     *      title of the dialog
     * @param stream
     *      input stream from which the displayed text is to be read
     */
    public InformationDialog(Dialog owner, String title, InputStream stream)
    {
        this(owner, title, createTextFromStream(stream));
    }
    

    /**
     * Creates a new InformationDialog which show a custom component containing
     * the information to be shown.
     * 
     * @param owner
     *      owner of the dialog
     * @param title
     *      title of the dialog
     * @param infoComponent
     *      component which displays the information
     */
    public InformationDialog(Frame owner, String title, JComponent infoComponent)
    {
        super(owner, title, EnumSet.of(Button.CANCEL));
        if (infoComponent == null) {
            throw new NullPointerException();
        }
        setCancelKey("cmd.close");
        this.infoComponent = infoComponent;
    }

    
    /**
     * Creates a new InformationDialog which shows a text read from an
     * input stream.
     * 
     * @param owner
     *      owner of the dialog
     * @param title
     *      title of the dialog
     * @param stream
     *      input stream from which the displayed text is to be read
     */
    public InformationDialog(Frame owner, String title, InputStream stream)
    {
        this(owner, title, createTextFromStream(stream));
    }
    

    /**
     * Sets the key for the Close button. Since, semantically, the Close button
     * is the same as the Cancel button, this method just calls
     * {@link StandardDialog#setCancelKey(String) setCancelKey(key)}
     * @param key
     */
    public void setCloseKey(String key)
    {
        setCancelKey(key);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    protected JComponent getDialogComponent()
    {
        JScrollPane scroll = new JScrollPane(infoComponent);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        return scroll;
    }
    
    
    /**
     * Creates a text area which contains the contents read from a stream.
     */
    private static JTextArea createTextFromStream(InputStream in)
    {
        JTextArea textArea = new JTextArea();
        TextAreaOutputStream out = new TextAreaOutputStream(textArea);
        
        try {
            byte[] buffer = new byte[1024];
            int len = in.read(buffer);
            while (len != -1) {
                out.write(buffer, 0, len);
                len = in.read(buffer);
            }
            out.close(); // no-op, included for completeness' sake
        }
        catch (IOException e) {
            textArea.append(String.format("%n%n%s%n", e.getMessage()));
        }
        
        textArea.setEditable(false);
            
        return textArea;
    }
}

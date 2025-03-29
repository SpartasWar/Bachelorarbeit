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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import de.uni_due.inf.ti.gui.BasicGridLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;


/**
 *  This class can be used to easily construct dialog boxes. 
 *  It layouts and handles the buttons in the bottom, and defines some
 *  standard behavior.
 *  
 *  @param <T>            
 *      the type of the information displayed and/or edited by this dialog
 */
public abstract class StandardDialog<T> extends JDialog 
{

    /** Serial version id of this class. */
    private static final long serialVersionUID = 0x0106197800010000L;
    
    /** Action string for escape. */
    private static final String ESCAPE_ACTION_STRING  = "ESCAPE";
    
    /**
     * The values of this enum represent the possible buttons which can occur
     * in a dialog.
     */
    public enum Button {
        ACCEPT,
        APPLY,
        CANCEL
    }
    
    /** Set of {@code Button.ACCEPT}. */
    public static EnumSet<Button> ACCEPT = EnumSet.of(Button.ACCEPT);
    
    /** Set of {@code Button.CANCEL}. */
    public static EnumSet<Button> CANCEL = EnumSet.of(Button.CANCEL);
    
    /** Set of {@code Button.ACCEPT} and {@code Button.CANCEL}. */
    public static EnumSet<Button> ACCEPT_CANCEL = 
            EnumSet.of(Button.ACCEPT, Button.CANCEL);
    
    /** Set of {@code Button.ACCEPT}, {@code Button.APPLY} and 
        {@code Button.CANCEL}. */
    public static EnumSet<Button> ACCEPT_APPLY_CANCEL = 
            EnumSet.of(Button.ACCEPT, Button.CANCEL, Button.APPLY);    
    
    // Resource keys for button texts.
    private String acceptKey    = "cmd.ok";
    private String applyKey     = "cmd.apply";
    private String cancelKey    = "cmd.cancel";
    private String errorKey     = "err.error";
   
    /** Flag which indicates whether the dialog is initialized. */ 
    private boolean initialized = false;
    
    /** The data maintained by this dialog. */
    private T options;

    /** Apply listeners registered with this dialog. */
    private ArrayList<ApplyListener<T>> listeners;
    
    /** Component which is displayed by this dialog. */
    private JComponent component;
    
    /** Buttons on this dialog. */
    private EnumSet<Button> buttons;
    
    /** List of custom actions for additional dialog buttons. */
    private List<Action> customActions = null;
    


    // ---------------------------------------------------------------------- //
    //                      Constructors and initialization                   //
    // ---------------------------------------------------------------------- //

    /**
     * Creates a new StandardDialog.
     *
     * @param owner          
     *      the frame from which the dialog is displayed
     * @param titleKey          
     *      the resource key of the string to display in the component's
     *      title bar
     * @param buttons           
     *      buttons showed by the dialog
     */
    protected StandardDialog(
            Frame owner, 
            String titleKey, 
            Collection<Button> buttons) 
    {
        super(owner, GuiContext.getSimpleGuiString(titleKey), true);
        this.buttons = EnumSet.copyOf(buttons);
        this.customActions = new ArrayList<>();
    }


    /**
     * Create a new StandardDialog.
     *
     * @param owner          
     *      the frame from which the dialog is displayed
     * @param titleKey
     *      the resource key of the string to display in the component's
     *      title bar
     * @param buttons
     *      buttons shown by the dialog
     */
    protected StandardDialog(
            Dialog owner, 
            String titleKey, 
            Collection<Button> buttons) 
    {
        super(owner, GuiContext.getSimpleGuiString(titleKey), true);
        this.buttons = EnumSet.copyOf(buttons);
        this.customActions = new ArrayList<>();
    }
    
    
    /**
     * Sets the resource key for the accept button.
     * Setting this resource key only has effect if called before the dialog is
     * initialized.
     * <p>
     * The default resource key is "cmd.ok".
     * 
     * @param key
     *      resource key for the accept button
     */
    public void setAcceptKey(String key)
    {
        acceptKey = key;
    }
    
    
    /**
     * Returns the resource key for the accept button.
     */
    public String getAcceptKey()
    {
        return acceptKey;
    }
    

    /**
     * Sets the resource key for the apply button.
     * Setting this resource key only has effect if called before the dialog is
     * initialized.
     * <p>
     * The default resource key is "cmd.apply".
     * 
     * @param key
     *      resource key for the apply button
     */
    public void setApplyKey(String key)
    {
        applyKey = key;
    }
    
    
    /**
     * Returns the resource key for the apply button.
     */
    public String getApplyKey()
    {
        return applyKey;
    }
    

    /**
     * Sets the resource key for the cancel button.
     * Setting this resource key only has effect if called before the dialog is
     * initialized.
     * <p>
     * The default resource key is "cmd.cancel". 
     * 
     * @param key
     *      resource key for the cancel button
     */
    public void setCancelKey(String key)
    {
        cancelKey = key;
    }
    
    
    /**
     * Returns the resource key for the cancel button.
     */
    public String getCancelKey()
    {
        return cancelKey;
    }
    
    
    /**
     * Sets the resource key for the title of error messages.
     * <p>
     * The default resource key is "err.error".
     * 
     * @param key
     *      the resource key for titles of error messages
     */
    public void setErrorKey(String key)
    {
        errorKey = key;
    }
    
    
    /**
     * Returns the resource key for the title of error messages.
     */
    public String getErrorKey()
    {
        return errorKey;
    }
    
    
    /**
     * Adds a button which performs a custom action to this dialog.
     * This method only has effect if called before the dialog is 
     * initialized.
     * 
     * @param action
     *      custom action which is performed when the button is pressed
     */
    public void addCustomAction(Action action)
    {
        if (customActions != null) {
            customActions.add(action);
        }
    }
    
    
    /**
     * This method creates components and adds them to the dialog.
     */
    private void layoutDialog() 
    {
        List<JButton> jbuts = new ArrayList<>(customActions.size() + buttons.size());
        for (Action action : customActions) {
            JButton b = new JButton(action);
            jbuts.add(b);
        }
        customActions = null; // not needed anymore
        
        if (buttons.contains(Button.ACCEPT)) {
            JButton b = new JButton(createAcceptAction(acceptKey));
            getRootPane().setDefaultButton(b);
            jbuts.add(b);
        }
        if (buttons.contains(Button.APPLY)) {
            JButton b = new JButton(createApplyAction(applyKey));
            jbuts.add(b);
        }
        if (buttons.contains(Button.CANCEL)) {
            Action cancelAction = createCancelAction(cancelKey);
            jbuts.add(new JButton(cancelAction));
            InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap actionMap = getRootPane().getActionMap();
            KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0); 
            inputMap.put(esc, ESCAPE_ACTION_STRING);
            actionMap.put(ESCAPE_ACTION_STRING, cancelAction);
        }

        getContentPane().add(GuiContext.createButtonPanel(jbuts), 
                BorderLayout.SOUTH);
        getContentPane().add(component, BorderLayout.CENTER);
        setResizable(false);
        pack();

        Rectangle ob = getOwner().getBounds();
        Dimension mySize = getSize();
        setLocation(ob.x + (ob.width - mySize.width) / 2,
                ob.y + (ob.height - mySize.height) / 2);
    }


    /**
     * Initialize this dialog.
     */
    public final void initialize() 
    {
        component = getDialogComponent();
        layoutDialog();
        initialized = true;    
    }


    // ---------------------------------------------------------------------- //
    //          Abstract methods that must be overridden by subclasses        //
    // ---------------------------------------------------------------------- //

    /**
     * Returns the main component of the dialog.
     * This method's task is to create the dialog's main component.
     * The main component is displayed in the middle of the dialog.
     * <p>
     * Subclasses must override this method.
     *  
     * @return        
     *      the main component of this dialog
     */
    protected abstract JComponent getDialogComponent();  


    /**
     * Parses the information currently input by the user.
     * Typically, subclasses should override this method. Only for the
     * most simple of dialog boxes (such as the about box) the default
     * implementation suffices.
     * <p> 
     * This default implementation simply returns {@code null}.
     *  
     * @return       
     *      the information currently being displayed by this dialog
     * @throws UserInputException 
     *      if the current value of the dialog's components are not of the
     *      correct form
     */
    protected T parseOptions() throws UserInputException 
    { 
        return null;
    }


    /**
     *  Set the options object.
     *  Typically, subclasses override this method to change the information
     *  which is currently being displayed by the dialog. 
     *  
     *  This default implementation does nothing.
     *  
     *  @param options     
     *       the options which must be modified by the dialog
     */
    public void setOptions(T options) { }



    // ---------------------------------------------------------------------- //
    //                         Apply event dispatching                        //
    // ---------------------------------------------------------------------- //

    /**
     * Register an ApplyListener to listen to this dialog's ApplyEvents.
     */
    public void addApplyListener(ApplyListener<T> l) 
    {
        if (listeners == null) listeners = new ArrayList<>();
        listeners.add(l);
    }


    /**
     * Stop an ApplyListener from listening to this dialog's ApplyEvents.
     */
    public void removeApplyListFrameener(ApplyListener<T> l) 
    {
        if (listeners == null) return;
        listeners.remove(l);
    }


    /**
     * Sends an ApplyEvent to all registered ApplyListeners.
     */
    private void fireApply(T options) 
    {
        if (listeners != null && !listeners.isEmpty()) {
            ApplyEvent<T> event = new ApplyEvent<T>(this, options);
            for (int i = listeners.size() - 1; i >= 0; i--) {
                listeners.get(i).optionsApplied(event);
            }
        }
    }


    // ---------------------------------------------------------------------- //
    //                          Displaying the dialog                         //
    // ---------------------------------------------------------------------- //

    /**
     *  Run this dialog on specified information.
     *  This dialog shows the dialog with the specified information and does
     *  not return until the user selected either the Ok or the Cancel button
     *  (or equivalent), or the user closed the dialog by some other means.
     *  If the user clicked Ok, the currently displayed information is returned,
     *  otherwise {@code null}.
     *  <p>
     *  <b>Note:</b> If the user clicks Ok, then an ApplyEvent is also
     *  generated. If one listens to ApplyEvents, the return value may
     *  in most cases be ignored.
     *  
     *  @param def        
     *      initial state of the displayed information
     *  @return           
     *      information edited by the user
     *  @throws IllegalStateException 
     *      if the dialog has not been properly initialized or is already
     *      running
     */
    public T runDialog(T def) 
    {
        if (!initialized) {
            throw new IllegalStateException("Dialog has not been properly " +
            		"initialized.");
        }
        
        if (def != null) { 
            setOptions(def);
        }
        
        setVisible(true);
        
        return options;
    }


    /**
     *  Run this dialog.
     *  This dialog shows the dialog with the specified information and does
     *  not return until the user selected either the Ok or the Cancel button
     *  (or equivalent), or the user closed the dialog by some other means.
     *  If the user clicked Ok, the currently displayed information is returned,
     *  otherwise {@code null}.
     *  <p>
     *  <b>Note:</b> If the user clicks Ok, then an ApplyEvent is also
     *  generated. 
     *  
     *  @return
     *      information edited by the user
     *  @throws IllegalStateException 
     *      if the dialog has not been properly initialized or is already
     *      running
     */
    public T runDialog() 
    {
        return runDialog(null);
    }


    /**
     * Convenience method which displays an error message in a dialog.
     */
    protected void showError(String msg) 
    {
        JOptionPane.showMessageDialog(
                this, 
                msg,
                GuiContext.getSimpleGuiString(errorKey), 
                JOptionPane.ERROR_MESSAGE);     
    }



    // ---------------------------------------------------------------------- //
    //                               Dialog actions                           //
    // ---------------------------------------------------------------------- //

    /**
     * Creates the action for the accept button.
     */
    private Action createAcceptAction(String key) 
    {
        return new StandardAction(key) {
            public void actionPerformed(ActionEvent e) {
                try {
                    options = parseOptions();
                    if (options != null) fireApply(options);
                    setVisible(false);
                }
                catch (UserInputException f) {
                    showError(f.getMessage());
                }
            }
        };
    }

    
    /** 
     * Creates the action for the apply button.
     */
    private Action createApplyAction(String key) 
    {
        return new StandardAction(key) {
            public void actionPerformed(ActionEvent e) {
                try {
                    options = parseOptions();
                    if (options != null) fireApply(options);
                }
                catch (UserInputException f) {
                    showError(f.getMessage());
                }
            }
        };
    }

    
    /**
     * Creates the action for the cancel button.
     */
    private Action createCancelAction(String key) 
    {
        return new StandardAction(key) {
            public void actionPerformed(ActionEvent e) {
                options = null;
                setVisible(false);
            }
        };
    }


    // ---------------------------------------------------------------------- //
    //                       Convenience utility methods                      //
    // ---------------------------------------------------------------------- //
    
    
    /**
     * Layout for the component list.
     */
    protected static class ComponentListLayout extends BasicGridLayout
    {
        private boolean adaptSize;
        
        /**
         * Creates a new ComponentListLayout.
         * 
         * @param rows
         *      number of rows in the layout
         * @param cols
         *      number of columns in the layout
         * @param hgap
         *      number of pixels between columns of the layout
         * @param vgap
         *      number of pixels between rows of the layout
         * @param adaptSize
         *      whether or not components are resized so that they have the
         *      same width as the widest component in a column
         */
        public ComponentListLayout(
                int rows, 
                int cols, 
                int hgap, 
                int vgap, 
                boolean adaptSize)
        {
            super(rows, cols, hgap, vgap);
            this.adaptSize = adaptSize;
        }
        
        
        /**
         * {@inheritDoc}
         */
        @Override 
        protected Alignment alignmentFor(Component c, int row, int col) {
            if (col == 0) 
                return Alignment.EAST;
            else 
                return Alignment.WEST;
        }
        
        
        /**
         * {@inheritDoc}
         */
        @Override
        protected Fill fillFor(Component c, int row, int col) {
            if (col >= 1 && adaptSize)
                return Fill.FILL_HORIZONTAL;
            else
                return Fill.FILL_NONE;
        }
    }
    
    
    /**
     * Creates a panel which contains a list of aligned components with 
     * corresponding labels.
     * 
     * @param panel
     *      the panel to which the components are to be added, or {@code null}
     *      if a new panel should be created
     * @param labels
     *      resource keys for the labels of the components
     * @param components
     *      the components
     * @param adaptSize
     *      whether or not all components are resized so that they have the
     *      width of the widest component
     * @return
     *      the created panel
     * @throws IllegalArgumentException
     *      if {@code labels} and {@code components} are of different length
     */
    public static JPanel createLabeledComponentList(
            JPanel panel, 
            String[] labels,
            JComponent[] components, 
            final boolean adaptSize) 
    {
        if (labels.length != components.length) {
            throw new IllegalArgumentException("Arrays must be of the same length.");
        }

        BasicGridLayout lo = new ComponentListLayout(0, 2, 4, 6, adaptSize); 
        if (panel == null) {
            panel = new JPanel(lo);
        }
        else {
            panel.setLayout(lo);
        }
        
        panel.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));

        for (int i = 0; i < labels.length; i++) {
            JLabel label = GuiContext.createLabel(labels[i], components[i]);
            panel.add(label);
            panel.add(components[i]);
        }

        return panel;
    }


    /**
     * Creates a panel which contains a list of aligned components with 
     * corresponding labels.
     * 
     * @param labels
     *      resources key for the captions of the labels
     * @param components
     *      the components
     * @param adaptSize
     *      whether or not all components are resized so that they have the
     *      width of the widest component
     * @return
     *      the created panel
     * @throws IllegalArgumentException
     *      if {@code labels} and {@code components} have a different length
     */
    public static JPanel createLabeledComponentList(
            String[] labels,
            JComponent[] components, 
            boolean adaptSize) 
    {
        return createLabeledComponentList(null, labels, components, adaptSize);
    }


    /**
     * Creates a panel which contains a list of aligned components with 
     * corresponding labels.
     * 
     * @param labels
     *      resource keys for the captions of the labels
     * @param components
     *      the components
     * @return
     *      the created panel
     * @throws IllegalArgumentException
     *      if {@code labels} and {@code components} have a different length
     */
    public static JPanel createLabeledComponentList(
            String[] labels,
            JComponent[] components) 
    {
        return createLabeledComponentList(null, labels, components, false);
    }
}

/* 
 * Copyright (c) 2013-2014, H.J. Sander Bruggink, University of Duisburg-Essen
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
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.uni_due.inf.ti.gui.CollectionListModel;

/**
 * Common dialog which edit a list of elements of a certain type.
 * 
 * @author H.J. Sander Bruggink
 *
 * @param <T>
 *      type of the elements edited by this dialog
 */
public class ListDialog<T> extends StandardDialog<List<T>>
{
    private class StringSyncList extends AbstractList<T>
    {
        private List<T> delegate;
        private List<String> stringList;
        
        public StringSyncList(List<T> delegate, List<String> stringList)
        {
            this.delegate = delegate;
            this.stringList = stringList;
            recreateStringList();
        }
        
        private void recreateStringList()
        {
            stringList.clear();
            for (T el : delegate) {
                stringList.add(ListDialog.this.toString(el));
            }
        }
        
        @Override
        public T get(int index) 
        {
            return delegate.get(index);
        }
        
        @Override
        public int size()
        {
            return delegate.size();
        }
        
        @Override
        public T set(int index, T el)
        {
            if (stringList.size() != delegate.size()) {
                recreateStringList();
            }
            stringList.set(index, ListDialog.this.toString(el));
            return delegate.set(index, el);
        }
        
        @Override
        public void add(int index, T el) 
        {
            if (stringList.size() != delegate.size()) {
                recreateStringList();
            }
            stringList.add(index, ListDialog.this.toString(el));
            delegate.add(index, el);
        }
        
        @Override
        public T remove(int index) 
        {
            if (stringList.size() != delegate.size()) {
                recreateStringList();
            }
            stringList.remove(index);
            return delegate.remove(index);
        }
        
        @Override
        public void clear()
        {
            delegate.clear();
            stringList.clear();
        }
    }
    
    
    /** Serial version UID. */
    private static final long serialVersionUID = -8364719485547554625L;

    public static enum Button {
        ADD, REMOVE, CLEAR, EDIT, UP, DOWN, CUSTOM
    }
    
    /** Default resource key for caption of add button. */
    public static final String DEFAULT_ADD_KEY      = "cmd.add";
    
    /** Default resource key for caption of remove button. */
    public static final String DEFAULT_REMOVE_KEY   = "cmd.remove";
    
    /** Default resource key for caption of clear button. */
    public static final String DEFAULT_CLEAR_KEY    = "cmd.clear";
    
    /** Default resource key for caption of edit button. */
    public static final String DEFAULT_EDIT_KEY     = "cmd.edit";
    
    /** Default resource key for caption of move up button. */
    public static final String DEFAULT_UP_KEY       = "cmd.moveUp";
    
    /** Default resource key for caption of move down button. */
    public static final String DEFAULT_DOWN_KEY     = "cmd.moveDown";
    
    private static final int HGAP           =  7;
    private static final int VGAP           =  7;
    private static final int BUTTON_GAP     = 10;
    private static final int BORDER_SIZE    = 10;
    
    private StandardDialog<T> itemDlg;
    private EnumSet<Button> buttons;
    
    private String addKey       = DEFAULT_ADD_KEY;
    private String removeKey    = DEFAULT_REMOVE_KEY;
    private String clearKey     = DEFAULT_CLEAR_KEY;
    private String editKey      = DEFAULT_EDIT_KEY;
    private String upKey        = DEFAULT_UP_KEY;
    private String downKey      = DEFAULT_DOWN_KEY;
    
    private Action addAction;
    private Action removeAction;
    private Action clearAction;
    private Action editAction;
    private Action upAction;
    private Action downAction;
    
    private boolean itemDialogMoved = false;
    
    /**
     * The list of elements currently displayed. Subclasses may modify this
     * list.
     */
    protected List<T> elements = null;
    
    /** The strings currently displayed. */
    private CollectionListModel<String> strings = null;
    
    /** The list component that displays the strings. */
    private JList<String> list;
    
    
    /**
     * Creates a new ListDialog.
     * 
     * @param owner
     *      owner of the dialog
     * @param title
     *      resource key of the title of the dialog
     * @param buttons
     *      the buttons which are to be displayed; if {@code Buttons.CUSTOM} is
     *      in this list, {@link #addCustomButtons(JComponent)} is called for
     *      custom buttons to be added to the dialog 
     */
    public ListDialog(Frame owner, String title, EnumSet<Button> buttons)
    {
        super(owner, title, EnumSet.of(StandardDialog.Button.ACCEPT, 
                StandardDialog.Button.CANCEL));
        this.buttons = EnumSet.copyOf(buttons);
    }

    
    /**
     * Creates a new ListDialog with all buttons.
     * 
     * @param owner
     *      owner of the dialog
     * @param title
     *      resource key of the title of the dialog
     */
    public ListDialog(Frame owner, String title)
    {
        this(owner, title, EnumSet.allOf(Button.class));
    }

    
    /**
     * Creates a new ListDialog.
     * 
     * @param owner
     *      owner of the dialog
     * @param title
     *      resource key of the title of the dialog
     * @param buttons
     *      the buttons which are to be displayed; if {@code Buttons.CUSTOM} is
     *      in this list, {@link #addCustomButtons(JComponent)} is called for
     *      custom buttons to be added to the dialog 
     */
    public ListDialog(Dialog owner, String title, EnumSet<Button> buttons)
    {
        super(owner, title, EnumSet.of(StandardDialog.Button.ACCEPT, 
                StandardDialog.Button.CANCEL));
        this.buttons = EnumSet.copyOf(buttons);
    }

    
    /**
     * Creates a new ListDialog with all buttons.
     * 
     * @param owner
     *      owner of the dialog
     * @param title
     *      resource key of the title of the dialog
     */
    public ListDialog(Dialog owner, String title)
    {
        this(owner, title, EnumSet.allOf(Button.class));
    }
    
    
    /**
     * Sets the dialog for editing the items of the list.
     */
    public void setItemDialog(StandardDialog<T> itemDlg)
    {
        this.itemDlg = itemDlg;
    }
    
    
    /**
     * Returns the dialog used for editing the items of the list.
     */
    public StandardDialog<T> getItemDialog()
    {
        return itemDlg;
    }
    

    /**
     * Sets the resource key for the "Add" button.
     * <p>
     * Setting resource keys only has effect before the 
     * {@link StandardDialog#initialize()} method has been called on this
     * dialog.
     * 
     * @param key
     *      the new resource key for the "Add" button; if {@code null}, the
     *      default resource key is used
     */
    public void setAddKey(String key)
    {
        addKey = key == null ? DEFAULT_ADD_KEY : key;
    }
    
    
    /**
     * Returns the resource key for the "Add" button.
     */
    public String getAddKey()
    {
        return addKey;
    }

    
    /**
     * Sets the resource key for the "Remove" button.
     * <p>
     * Setting resource keys only has effect before the 
     * {@link StandardDialog#initialize()} method has been called on this
     * dialog.
     * 
     * @param key
     *      the new resource key for the "Remove" button; if {@code null}, the
     *      default resource key is used
     */
    public void setRemoveKey(String key)
    {
        removeKey = key == null ? DEFAULT_REMOVE_KEY : key;
    }
    
    
    /**
     * Returns the resource key for the "Remove" button.
     */
    public String getRemoveKey()
    {
        return removeKey;
    }
    
 
    /**
     * Sets the resource key for the "Edit" button.
     * <p>
     * Setting resource keys only has effect before the 
     * {@link StandardDialog#initialize()} method has been called on this
     * dialog.
     * 
     * @param key
     *      the new resource key for the "Edit" button; if {@code null}, the
     *      default resource key is used
     */
    public void setEditKey(String key)
    {
        editKey = key == null ? DEFAULT_EDIT_KEY : key;
    }
    

    /**
     * Return the resource key for the "Edit" button.
     * @return
     */
    public String getEditKey()
    {
        return editKey;
    }
    
    
    /**
     * Sets the resource key for the "Clear" button.
     * <p>
     * Setting resource keys only has effect before the 
     * {@link StandardDialog#initialize()} method has been called on this
     * dialog.
     * 
     * @param key
     *      the new resource key for the "Clear" button; if {@code null}, the
     *      default resource key is used
     */
    public void setClearKey(String key)
    {
        clearKey = key == null ? DEFAULT_CLEAR_KEY : key;
    }
    
    
    /**
     * Returns the resource key for the "Clear" button.
     */
    public String getClearKey()
    {
        return clearKey;
    }
    
    
    /**
     * Sets the resource key for the "Up" button.
     * <p>
     * Setting resource keys only has effect before the 
     * {@link StandardDialog#initialize()} method has been called on this
     * dialog.
     * 
     * @param key
     *      the new resource key for the "Up" button; if {@code null}, the
     *      default resource key is used
     */
    public void setUpKey(String key)
    {
        upKey = key == null ? DEFAULT_UP_KEY : key;
    }
    
    
    /**
     * Returns the resource key for the "Up" button.
     */
    public String getUpKey()
    {
        return upKey;
    }
    

    /**
     * Sets the resource key for the "Down" button.
     * <p>
     * Setting resource keys only has effect before the 
     * {@link StandardDialog#initialize()} method has been called on this
     * dialog.
     * 
     * @param key
     *      the new resource key for the "Down" button; if {@code null}, the
     *      default resource key is used
     */
    public void setDownKey(String key)
    {
        downKey = key == null ? DEFAULT_DOWN_KEY : key;
    }
    
    
    /**
     * Returns the resource key for the "Down" button.
     */
    public String getDownKey()
    {
        return downKey;
    }
    
    
    /**
     * Change the enabled status of the buttons which depends on an item
     * being selected.
     */
    private void changeActionEnabled()
    {
        int index = list.getSelectedIndex();
        if (removeAction != null) removeAction.setEnabled(index >= 0);
        if (editAction != null) editAction.setEnabled(index >= 0);
        if (upAction != null) upAction.setEnabled(index >= 1);
        if (downAction != null) downAction.setEnabled(index >= 0 && index < elements.size() - 1);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected final JComponent getDialogComponent()
    {
        JPanel panel = new JPanel(new BorderLayout(HGAP, VGAP));
        panel.setBorder(BorderFactory.createEmptyBorder(BORDER_SIZE, 
                BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));
        
        strings = new CollectionListModel<String>();
        elements = new StringSyncList(new ArrayList<T>(), strings);
        list = new JList<>(strings);
        list.addListSelectionListener(new ListSelectionHandler());
        JScrollPane scroll = new JScrollPane(list);
        panel.add(scroll, BorderLayout.CENTER);
        
        if (!buttons.isEmpty()) {
            Dimension max = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
            JPanel buttonBox = new JPanel();
            BoxLayout layout = new BoxLayout(buttonBox, BoxLayout.PAGE_AXIS);
            buttonBox.setLayout(layout);
            ComponentAdder adder = new ComponentAdder(buttonBox);
            
            if (buttons.contains(Button.ADD)) {
                addAction = new AddAction();
                JButton addButton = new JButton(addAction);
                addButton.setMaximumSize(max);
                adder.addComponent(addButton);
            }
            if (buttons.contains(Button.REMOVE)) {
                removeAction = new RemoveAction();
                JButton removeButton = new JButton(removeAction);
                removeButton.setMaximumSize(max);
                adder.addComponent(removeButton);
            }
            if (buttons.contains(Button.CLEAR)) {
                clearAction = new ClearAction();
                JButton clearButton = new JButton(clearAction);
                clearButton.setMaximumSize(max);
                adder.addComponent(clearButton);
            }
            adder.addStrut();
            if (buttons.contains(Button.EDIT)) {
                editAction = new EditAction();
                JButton editButton = new JButton(editAction);
                editButton.setMaximumSize(max);
                adder.addComponent(editButton);
            }
            adder.addStrut();
            if (buttons.contains(Button.UP)) {
                upAction = new MoveAction(upKey, -1);
                JButton upButton = new JButton(upAction);
                upButton.setMaximumSize(max);
                adder.addComponent(upButton);
            }
            if (buttons.contains(Button.DOWN)) {
                downAction = new MoveAction(downKey, 1);
                JButton downButton = new JButton(downAction);
                downButton.setMaximumSize(max);
                adder.addComponent(downButton);
            }
            adder.addStrut();
            if (buttons.contains(Button.CUSTOM)) {
                addCustomButtons(adder);
            }
            panel.add(buttonBox, BorderLayout.EAST);
        }
        
        return panel;
    }
    
    
    /**
     * This method is called when {@code Buttons.CUSTOM} is in the displayed
     * button set. Subclasses which add {@code Buttons.CUSTOM} to the displayed
     * buttons may override this method to add custom controls the dialog
     * (usually buttons).
     * 
     * @param adder
     *      callback class to add buttons to the button box
     */
    protected void addCustomButtons(ComponentAdder adder)
    {
        // default implementation: do nothing
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setOptions(List<T> l)
    {
        elements.clear();
        elements.addAll(l);
        changeActionEnabled();
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected List<T> parseOptions()
    {
        return new ArrayList<T>(elements);
    }
    
    
    /**
     * Overrides the string representation of the elements in the list.
     * Normally, the strings displayed in the list are obtained by calling the
     * {@code toString()} method of the elements. Subclasses may override this
     * method to display different strings.
     * 
     * @param ob
     *      object to obtain a string representation of
     * @return
     *      string representation of {@code ob} to be displayed in the list
     */
    protected String toString(T ob)
    {
        return ob.toString();
    }
    
    
    /**
     * Returns a default object. The default object is used to provide default
     * values for new elements if the Add button is pressed.
     * <p>
     * This implementation just returns {@code null}.
     */
    protected T getDefaultObject()
    {
        return null;
    }
    
    
    /**
     * Centers the item dialog on this dialog.
     */
    private void centerItemDialog()
    {
        Dimension dim = itemDlg.getSize();
        Rectangle bounds = getBounds();
        itemDlg.setLocation(bounds.x + (bounds.width - dim.width) / 2,
                            bounds.y + (bounds.height - dim.height) / 2);
    }
    
    
    /**
     * Action for the "Add" button.
     */
    private class AddAction extends StandardAction 
    {
        AddAction()
        {
            super(addKey);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (!itemDialogMoved) centerItemDialog();
            T dflt = getDefaultObject();
            
            Point oldPos = itemDlg.getLocation();
            T newItem = itemDlg.runDialog(dflt);
            Point newPos = itemDlg.getLocation();
            if (!oldPos.equals(newPos)) itemDialogMoved = true;
            
            if (newItem != null) {
                elements.add(newItem);
                changeActionEnabled();
            }
        }             
    }


    /**
     * Action for the "Remove" button.
     */
    private class RemoveAction extends StandardAction 
    {
        RemoveAction()
        {
            super(removeKey);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            int index = list.getSelectedIndex();
            if (index >= 0 && index < elements.size()) {
                elements.remove(index);
                changeActionEnabled();
            }
        }             
    }
    
    
    /**
     * Action for the "Clear" button.
     */
    private class ClearAction extends StandardAction
    {
        ClearAction()
        {
            super(clearKey);
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            elements.clear();
            changeActionEnabled();
        }
    }


    /**
     * Action for the "Edit" button.
     */
    private class EditAction extends StandardAction 
    {
        EditAction()
        {
            super(editKey);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            int index = list.getSelectedIndex();
            if (index >= 0 && index < elements.size()) {
                if (!itemDialogMoved) centerItemDialog();
                T oldItem = elements.get(index);
                
                Point oldPos = itemDlg.getLocation();
                T newItem = itemDlg.runDialog(oldItem);
                Point newPos = itemDlg.getLocation();
                if (!oldPos.equals(newPos)) itemDialogMoved = true;
                
                if (newItem != null) {
                    elements.set(index, newItem);
                }
            }
        }             
    }


    /**
     * Action for move buttons, in particular "Move up" and "Move down".
     */
    private class MoveAction extends StandardAction 
    {
        private int direction;
        
        MoveAction(String key, int direction)
        {
            super(key);
            this.direction = direction;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            int index = list.getSelectedIndex();
            int newIndex = index + direction;
            if (index >= 0 && index < elements.size() 
                    && newIndex >= 0 && newIndex < elements.size()) {
                T tmp = elements.get(index);
                elements.set(index, elements.get(newIndex));
                elements.set(newIndex, tmp);
                list.setSelectedIndex(newIndex);
            }
        }             
    }
    

    /**
     * Event handler for list selection changes.
     */
    private class ListSelectionHandler implements ListSelectionListener
    {
        @Override
        public void valueChanged(ListSelectionEvent e)
        {
            changeActionEnabled();
        }
    }
    
    
    /**
     * Callback class to add buttons to the button box.
     */
    public static class ComponentAdder 
    {
        private JComponent container;
        private boolean strutNeeded;
        
        ComponentAdder(JComponent container)
        {
            if (container == null) throw new NullPointerException();
            this.container = container;
            this.strutNeeded = false;
        }

        /**
         * Adds a component to the button box.
         */
        public void addComponent(JComponent comp)
        {
            if (strutNeeded) {
                container.add(Box.createVerticalStrut(BUTTON_GAP));
                strutNeeded = false;
            }
            container.add(comp);
        }

        /**
         * Adds a vertical space to the button box.
         */
        public void addStrut() 
        {
            strutNeeded = true;
        }
    }

}

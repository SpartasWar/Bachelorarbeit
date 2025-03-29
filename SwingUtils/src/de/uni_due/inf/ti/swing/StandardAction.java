/* 
 * Copyright (c) 2010-2014, H.J. Sander Bruggink
 * Copyright (c) 2010-2014, University of Duisburg-Essen
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

import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;

import de.uni_due.inf.ti.gui.CommandTextInfo;

/**
 * This class represents a GUI action within the SwingUtils framework.
 * 
 * @author H.J. Sander Bruggink
 */
public abstract class StandardAction implements Action
{    
    /** Key for "enabled" property. */
    public static final String ENABLED_KEY = "enabled";

    /** Value for "enabled" property. */
    private boolean enabled = true;

    /** Property map which holds the properties of this action. */
    protected PropertyMap properties;
    
    /**
     * Creates a new StandardAction.
     * This is equivalent to calling {@code StandardAction(null, null)}.
     * <p>
     * This constructor can only be used by subclasses. Subclasses that use
     * this constructor, need to take care of the following initialization
     * procedures themselves:
     * <ul>
     * <li>set the action's text</li>
     * <li>set the action's action command</li>
     * <li>set the action's mnemonic character and displayed mnemonic index
     * (optional)</li>
     * <li>set the action's accelerator key (optional)</li>
     * </ul>
     */
    protected StandardAction()
    {
        this(null, null);
    }
    
    /**
     * Creates a new StandardAction with an accelerator key.
     * This is equivalent to calling {@code StandardAction(null, accel)}.
     * <p>
     * This constructor can only be used by subclasses. Subclasses that use
     * this constructor need to take care of the following initialization
     * procedures themselves:
     * <ul>
     * <li>set the action's text</li>
     * <li>set the action's action command</li>
     * <li>set the action's mnemonic character and displayed mnemonic index
     * (optional)</li>
     * </ul>
     * 
     * @param accel
     *        the action's accelerator key
     */
    protected StandardAction(KeyStroke accel)
    {
        this(null, accel);
    }
    
    /**
     * Creates a new StandardAction.
     * 
     * @param key
     *        Resource key of the text of the StandardAction.
     */
    public StandardAction(String key)
    {
        this(key, null);
    }

    /**
     * Creates a new StandardAction with an accelerator key.
     * 
     * @param key
     *      resource key of the text of the StandardAction
     * @param accel
     *      KeyStroke that serves as the accelerator key for this Action
     */
    public StandardAction(String key, KeyStroke accel)
    {
        properties = new PropertyMap(this);
        if (key != null) {
            String txt = GuiContext.getGuiString(key);
            CommandTextInfo ti = CommandTextInfo.getCommandTextInfo(txt);
            setFromCommandTextInfo(ti);
            setActionCommand(key);
        }
        if (accel != null) {
            setAccelerator(accel);
        }
    }
        
    /**
     * Sets the text, mnemonic and displayed mnemonic index to the value given
     * by a {@code CommandTextInfo} object.
     */
    public void setFromCommandTextInfo(CommandTextInfo ti) 
    {
        setText(ti.getText());
        setShortDescription(ti.getHint());
        if (ti.getMnemonic() >= 0) {
            setMnemonic(ti.getMnemonic());
            setDisplayedMnemonicIndex(ti.getDisplayedMnemonicIndex());
        }
    }
    
    /**
     * Sets the mnemonic of this Action.
     * 
     * @param mnemonic
     *        KeyEvent key code that represents the mnemonic for
     *        this action.
     */
    public void setMnemonic(int mnemonic)
    {
        properties.put(MNEMONIC_KEY, Integer.valueOf(mnemonic));
    }
    
    /**
     * Returns the mnemonic of this Action.
     */
    public int getMnemonic()
    {
        return ((Integer)properties.get(MNEMONIC_KEY)).intValue();
    }
    
    /**
     * Sets the displayed mnemonic index of this action.
     */
    public void setDisplayedMnemonicIndex(int idx)
    {
        properties.put(DISPLAYED_MNEMONIC_INDEX_KEY, Integer.valueOf(idx));
    }
    
    /**
     * Returns the displayed mnemonic index of this Action.
     */
    public int getDisplayedMnemonicIndex()
    {
        return ((Integer)properties.get(DISPLAYED_MNEMONIC_INDEX_KEY))
                .intValue();
    }

    /**
     * Sets the action command for this Action.
     */
    public void setActionCommand(String ac)
    {
        properties.put(ACTION_COMMAND_KEY, ac);
    }

    /**
     * Returns the action command of this Action.
     */
    public String getActionCommand()
    {
        return (String)properties.get(ACTION_COMMAND_KEY);
    }
    
    /**
     * Sets the text of this Action.
     */
    public void setText(String text)
    {
        properties.put(NAME, text);
    }
    
    /**
     * Returns the text of this Action.
     */
    public String getText()
    {
        return (String) properties.get(NAME);
    }
    
    /**
     * Sets the accelerator key stroke for this Action.
     */
    public void setAccelerator(KeyStroke accel)
    {
        properties.put(ACCELERATOR_KEY, accel);
    }
    
    /**
     * Returns the accelerator key stroke for this Action.
     */
    public KeyStroke getAccelerator()
    {
        return (KeyStroke)properties.get(ACCELERATOR_KEY);
    }
    
    /**
     * Sets the short description of this Action.
     * The short description is typically used as tooltip text.
     * 
     * @param hint
     *        new short description of this Action
     */
    public void setShortDescription(String hint)
    {
        properties.put(SHORT_DESCRIPTION, hint);
    }
    
    /**
     * Returns the short description of this Action.
     * The short description is typically used as tooltip text.
     * 
     * @return the short description of this Action
     */
    public String getShortDescription()
    {
        return (String)properties.get(SHORT_DESCRIPTION);
    }
    
    /**
     * Sets the small icon of this action.
     */
    public void setSmallIcon(Icon icon)
    {
        properties.put(SMALL_ICON, icon);
    }
    
    /**
     * Returns the small icon of this action.
     */
    public Icon getSmallIcon()
    {
        return (Icon)properties.get(SMALL_ICON);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnabled(boolean enabled)
    {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            properties.firePropertyChange(ENABLED_KEY,
                    Boolean.valueOf(!enabled), Boolean.valueOf(enabled));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Determines if this action is selected.
     */
    public boolean isSelected()
    {
        Object o = properties.get(SELECTED_KEY);
        if (o == null)
            return false;
        if (!(o instanceof Boolean))
            throw new ClassCastException();
        return (Boolean) o;
    }
    
    /**
     * Sets whether or not this action is selected.
     */
    public void setSelected(boolean selected)
    {
        properties.put(SELECTED_KEY, Boolean.valueOf(selected));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putValue(String key, Object value)
    {
        if (ENABLED_KEY.equals(key)) {
            if (!(value instanceof Boolean))
                throw new ClassCastException();
            boolean newValue = (Boolean) value;
            if (enabled != newValue) {
                enabled = newValue;
                properties.firePropertyChange(ENABLED_KEY,
                        Boolean.valueOf(!enabled), Boolean.valueOf(enabled));
            }
        } else
            properties.put(key, value);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(String key)
    {
        if (ENABLED_KEY.equals(key))
            return Boolean.valueOf(enabled);
        else
            return properties.get(key);
    }

    /**
     * Adds a property change listener.
     * 
     * @param list
     *        the property change listener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener list)
    {
        properties.addPropertyChangeListener(list);
    }
    
    /**
     * Removes a property change listener.
     * 
     * @param list
     *        the property change listener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener list)
    {
        properties.removePropertyChangeListener(list);
    }
    
    /**
     * Returns the keystroke for menu shortcuts.
     * This method uses
     * {@code Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()} to
     * obtain the menu shortcut key mast for the current platform.
     * It returns the keystroke corresponding to {@code key} and the menu
     * shortcut key being pressed at the same time.
     * <p>
     * For example, on Mac OS X, this method will return Cmd-{@code key}, while
     * on Windows it will return Ctrl-{@code key}.
     */
    public static KeyStroke getMenuShortcut(char key)
    {
        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        return KeyStroke.getKeyStroke(key, ctrl);
    }

    /**
     * Returns a keystroke for menu shortcuts augmented with shift.
     * This method uses
     * {@code Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() to
     * obtain the menu shortcut key mast for the current platform. It returns
     * the keystroke corresponding to {@code key}, the menu shortcut key
     * and shift being pressed at the same time.
     * <p>
     * For example, on Mac OS X, this method will return Cmd-Shift-{@code key},
     * while on Windows it will return Ctrl-Shift-{@code key}.
     */
    public static KeyStroke getShiftMenuShortcut(char key)
    {
        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        int shift = InputEvent.SHIFT_DOWN_MASK;
        return KeyStroke.getKeyStroke(key, ctrl | shift);
    }
}

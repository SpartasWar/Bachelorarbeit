/* 
 * Copyright (c) 2013-2015, H.J. Sander Bruggink
 * Copyright (c) 2013-2014, University of Duisburg-Essen
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

// 2014-??-?? - Created
// 2015-02-15 - Replaced Guava Function with Java 8 Function (old method marked deprecated)

package de.uni_due.inf.ti.gui;

import java.awt.Component;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * List cell renderer which renders string representations of items.
 * The advantage of this list cell renderer above the one supplied with Swing,
 * is that this ListCellRenderer offers some methods to customize the strings
 * displayed for some items.
 *  
 * @author H.J. Sander Bruggink
 */
public class StringListCellRenderer<T> extends JLabel implements ListCellRenderer<T>
{
    private static final long serialVersionUID = -6321606960558380167L;
    
    private Function<? super T,String> stringGen;
    
    /**
     * Creates a new StringListCellRenderer.
     */
    public StringListCellRenderer()
    {
        super();
        setOpaque(true);
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
    }
    
    /**
     * Sets the string generator for this StringListCellRenderer.
     * The string generator generates the string representations of the
     * rendered objects.
     *  
     * @param stringGen
     *      the new string generator of this StringListCellRenderer, or
     *      {@code null} to remove the current string generator
     * @deprecated
     *      use {@link setStringGenerator(Function)} instead
     */
    @Deprecated
    public void setStringGenerator(com.google.common.base.Function<? super T,String> stringGen)
    {
        this.stringGen = x -> stringGen.apply(x);
    }
    
    /**
     * Sets the string generator for this StringListCellRenderer.
     * The string generator generates the string representations of the
     * rendered objects.
     *  
     * @param stringGen
     *      the new string generator of this StringListCellRenderer, or
     *      {@code null} to remove the current string generator
     */
    public void setStringGenerator(Function<? super T,String> stringGen)
    {
        this.stringGen = stringGen;
    }
    
    /**
     * Sets the string generator for this StringListCellRenderer to
     * a generated which reads from a map.
     * 
     * @param stringMap
     *      the map which contains the string representations of some
     *      objects
     */
    public void setStringMap(Map<? super T,String> stringMap)
    {
        stringGen = x -> stringMap.get(x);
    }
    
    /**
     * Returns the string generator for this StringListCellRenderer.
     * 
     * @return
     *      the string generator for this StringListCellRenderer, or
     *      {@code null} if this StringListCellRenderer does not have
     *      a string generator
     */
    public Function<? super T,String> getStringGenerator()
    {
        return stringGen;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Component getListCellRendererComponent(
            JList<? extends T> list,
            T value, 
            int index, 
            boolean isSelected, 
            boolean hasFocus)
    {
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        }
        else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        
        String text = stringGen == null ? null : stringGen.apply(value);
        if (text == null) text = value.toString();
        setFont(list.getFont());
        setText(text);
        
        return this;
    }
}

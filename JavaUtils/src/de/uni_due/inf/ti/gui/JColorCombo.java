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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Combo box which displays a number of predefined colors, together with an
 * icon showing the color.
 * 
 * @author H.J. Sander Bruggink
 */
public class JColorCombo extends JComboBox<Object> {
  
  private static final long serialVersionUID = 7965052299414127817L;
  private static final int ICON_WIDTH = 20;
  private static final int ICON_HEIGHT = 10;
  
  private static class ColorItem implements Icon {
    Color color;
    String name;
    
    ColorItem(Color color, String name) {
      this.color = color;
      this.name = name;
    }
    
    public int getIconHeight() {
      return ICON_HEIGHT;
    }
    
    public int getIconWidth() {
      return ICON_WIDTH;
    }
    
    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.setColor(color);
      g.fillRect(x, y, ICON_WIDTH, ICON_HEIGHT);
      g.setColor(Color.BLACK);
      g.drawRect(x, y, ICON_WIDTH, ICON_HEIGHT);
    }   
  }
  

  private static class SchemeItem implements Icon {
    ColorScheme scheme;
    String name;
    
    SchemeItem(ColorScheme scheme, String name) {
      this.scheme = scheme;
      this.name = name;
    }
    
    public int getIconHeight() {
      return ICON_HEIGHT;
    }
    
    public int getIconWidth() {
      return ICON_WIDTH;
    }
    
    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.setColor(scheme.getBackground());
      g.fillRect(x, y, ICON_WIDTH, ICON_HEIGHT);
      g.setColor(scheme.getForeground());
      g.drawRect(x + 4, y + 4, ICON_WIDTH - 8, ICON_HEIGHT - 8);
      g.setColor(Color.BLACK);
      g.drawRect(x, y, ICON_WIDTH, ICON_HEIGHT);
    }   
  }

  /**
   * The renderer which renders items in the combo box. 
   * Typically, only one instance needs to be created per JColorCombo.
   */
  private class Renderer extends JLabel implements ListCellRenderer<Object> {
    
    private static final long serialVersionUID = 7749523891690735966L;

    public Renderer() {
      setOpaque(true);
      setHorizontalAlignment(LEFT);
      setVerticalAlignment(CENTER);
    }
    
    public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus
    ) {
      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      }
      else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
      if (value instanceof ColorItem) {
        ColorItem item = (ColorItem)value;
        setText(item.name);
        setIcon(item);
      }
      else if (value instanceof SchemeItem) {
        SchemeItem scheme = (SchemeItem)value;
        setText(scheme.name);
        setIcon(scheme);
      }
      else {
        setText(value.toString());
        setIcon(null);
      }
      return this;
    }
  }
  
  
  /**
   * Creates a new JColorCombo instance.
   */
  public JColorCombo() {
    setRenderer(new Renderer());
    setEditable(false);
  }
  
  
  /**
   * Adds a color as item to this JColorCombo.
   * 
   * @param color           the color to be added
   * @param name            the name of the color to be added
   */
  public void addItem(Color color, String name) {
    addItem(new ColorItem(color, name));
  }
  
  
  /**
   * Adds a ColorScheme as item to this JColorCombo.
   * 
   * @param scheme          the color scheme to be added
   * @param name            the name of the color scheme to be added
   */
  public void addItem(ColorScheme scheme, String name) {
    addItem(new SchemeItem(scheme, name));
  }
  
  
  /**
   * Returns the selected color.
   * 
   * @return                returns the color which is currently selected,
   *                        or {@code null} if no color is currently
   *                        selected
   */
  public Color getSelectedColor() {
    if (getSelectedIndex() < 0) return null;
    return ((ColorItem)getSelectedItem()).color;
  }
  

}

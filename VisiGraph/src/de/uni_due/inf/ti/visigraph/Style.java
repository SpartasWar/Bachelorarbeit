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

package de.uni_due.inf.ti.visigraph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Style of graphs and graph elements. 
 * All components (nodes, edge, boxes, etc) use the same style objects; not
 * all components use the same properties.
 * <p>
 * A style can have a parent style. If a property is not set for this style,
 * the value of the property is inherited from the parent style.
 * <p>
 * In addition, the {@code Style} class defines some static utility classes
 * and functions which are used to define and set attributes of certain types.
 * 
 * @author H.J. Sander Bruggink
 */
public final class Style
{
    /** Name of alignment property. Used for: VxBox. */
    public static final String PROP_ALIGNMENT       = "alignment";
    
    /** Name of back arrow head property. Used for: VxEdge. */
    public static final String PROP_BACK_ARROW      = "arrow.back";
    
    /** Name of box label position property. User for: VxBox. */
    public static final String PROP_BOX_LABEL_POS   = "boxlabel.pos";
    
    /** Name of double line property. Used for: VxNode, VxBox. */
    public static final String PROP_DOUBLE_LINE     = "double";
    
    /** Name of drawn property. Used for: VxNode, VxEdge, VxBox. */
    public static final String PROP_DRAW            = "draw";
    
    /** Name of filled property. Used for: VxNode, VxBox. */
    public static final String PROP_FILL            = "fill";
    
    /** Name of fill color property. Used for: VxNode, VxBox. */
    public static final String PROP_FILL_COLOR      = "color.fill";
    
    /** Name of flexible shape property. Used for: VxNode. */
    public static final String PROP_FLEXIBLE_SHAPE  = "flexible";
    
    /** Name of front arrow head property. Used for: VxEdge. */
    public static final String PROP_FRONT_ARROW     = "arrow.front";
    
    /** Name of inset property. Used for: VxBox. */
    public static final String PROP_INSET           = "inset";
    
    /** Name of label color property. Used for: VxNode, VxBox. */
    public static final String PROP_LABEL_COLOR     = "color.label";
    
    /** Name of label distance property. Used for: VxEdge. */
    public static final String PROP_LABEL_DISTANCE  = "label.distance";
    
    /** Name of label font property. Used for: VxNode, VxBox. */
    public static final String PROP_LABEL_FONT      = "font.label";
    
    /** Name of label position property. Used for: VxEdge. */
    public static final String PROP_LABEL_POS       = "label.pos";
    
    /** Name of line color property. Used for: VxNode, VxEdge, VxBox. */
    public static final String PROP_LINE_COLOR      = "color.line";
    
    /** Name of minimum width property. Used for: VxBox. */
    public static final String PROP_MIN_WIDTH       = "width.min";
    
    /** Name of minimum height property. Used for: VxBox. */
    public static final String PROP_MIN_HEIGHT      = "height.min";
    
    /** Name of shape property. Used for: VxNode. */
    public static final String PROP_SHAPE           = "shape";
    
    /** Name of stroke property. Used for: VxNode, VxEdge, VxBox. */
    public static final String PROP_STROKE          = "stroke";
    
    /**
     * Position of the label of a box.
     */
    public static enum Position {
        TOP_LEFT, TOP, TOP_RIGHT,
        LEFT, CENTER, RIGHT,
        BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT
    }

    /** 
     * Line width for default strokes. 
     */
    public static enum LineWidth { 
        THIN(0f), NORMAL(1f), MEDIUM(1.5f), THICK(2);

        /** Number of LineWidth constants. */
        public static final int length = 4;

        /** Value of this LineWidth. */
        private float value;

        /** Construct new LineWidth constant. */
        LineWidth(float value)
        {
            this.value = value;
        }
        
        /** Returns the value of this line width. */
        public float getValue()
        {
            return value;
        }
    }

    /** 
     * Dash pattern types for default strokes.
     */
    public static enum DashType { 
        SOLID, DASHED, DOTTED;
        
        /** Number of DashType constants. */
        public static final int length = 3;
    }
    
    /** Array to store default strokes. */
    private static BasicStroke[] defaultStrokes = null;
    
    /** Default label font */
    private static Font defaultLabelFont;
    
    /** Default shape */
    private static Shape defaultShape;
    

    /**
     * Returns a stroke with given properties. The strokes are cached, so that
     * for the same properties the same object is returned every call.
     * 
     * @param width
     *      width of the drawing line
     * @param dash
     *      dash pattern of the drawing line
     * @return
     *      a Stroke with the given properties
     */
    public static BasicStroke getDefaultStroke(LineWidth width, DashType dash)
    {
        if (defaultStrokes == null) {
            defaultStrokes = new BasicStroke[DashType.length * LineWidth.length];
        }
        
        int index = (width.ordinal() * DashType.length) + dash.ordinal();
        
        if (defaultStrokes[index] == null) {
            switch (dash) {
                case SOLID:
                    defaultStrokes[index] = new BasicStroke(width.getValue());
                    break;
                case DASHED: {
                    float[] data = { 10.0f } ;
                    defaultStrokes[index] = new BasicStroke(width.getValue(),
                            BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
                            data, 0f);
                    break;
                }
                case DOTTED: {
                    float[] data = { 2f, 2f } ;
                    defaultStrokes[index] = new BasicStroke(width.getValue(),
                            BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f,
                            data, 0f);
                }
            }
        }
        
        return defaultStrokes[index];
    }
    
    /** Parent style. */
    private Style parent;
    
    /** Property change listeners. */
    private List<PropertyChangeListener> listeners;
    
    /** Map containing the values for the various properties. */
    private Map<String,Object> properties;
    
    
    /**
     * Creates a new Style without a parent.
     * <p>
     * To create a child style of a style, use the {@link #createChild}
     * method.
     */
    public Style()
    {
        parent = null;
        properties = new HashMap<>();
        listeners = null;
    }
    
    /**
     * Returns the name of the selected variant of a given property.
     */
    private static String getSelectedName(String name)
    {
        return name + ".selected";
    }
    
    /**
     * Creates a new Style instance which is a copy of another one.
     * The copy has the same parent and the same property values as the 
     * original style. Property listeners are not transferred to the new
     * style.
     * 
     * @param orig
     *      the Style of which the new style will be a copy
     * @throws NullPointerException
     *      if {@code orig} is {@code null}
     */
    public Style(Style orig)
    {
        parent = orig.parent;
        properties = new HashMap<>(orig.properties);
        listeners = null;
    }
    
    /**
     * Returns the parent style of this style.
     */
    public final Style getParent()
    {
        return parent;
    }
    
    /**
     * Creates a child style of this style.
     * The new style is an empty style (none of the properties are set) whose
     * parent is this style.
     *  
     * @return
     *      a newly created child style of this style
     */
    public final Style createChild()
    {
        Style child = new Style();
        child.parent = this;
        return child;
    }

    /**
     * Returns a property from this style.
     * 
     * @param property
     *      name of the property
     * @param selected
     *      whether the property is to be retrieved for selected state
     * @param deflt
     *      default value for when the property is not set in this
     *      style
     * @return
     *      value for the given property
     */
    private final Object getProperty(String property, boolean selected, Object deflt) 
    {
        String name = selected ? getSelectedName(property) : property;
        Style style = this;
        while (style != null) {
            Object ob = style.properties.get(name);
            if (ob != null) return ob;
            style = style.parent;
        }
        if (selected) return getProperty(property, false, deflt);
                 else return deflt;
    }

    /**
     * Returns a boolean-valued property from this style.
     *
     * @param property
     *      name of the property
     * @param selected
     *      whether the property is to be retrieved for selected state
     * @param deflt
     *      default value for when the property is not set in this
     *      style
     * @return
     *      value for the given property
     */
    private final boolean getBoolProperty(String property, boolean selected, boolean deflt)
    {
        String name = selected ? getSelectedName(property) : property;
        Style style = this;
        while (style != null) {
            Object ob = style.properties.get(name);
            if (ob != null) return ((Boolean) ob).booleanValue();
            style = style.parent;
        }
        if (selected) return getBoolProperty(property, false, deflt);
                 else return deflt;
    }
    
    /**
     * Sets a property of this style.
     * 
     * @param property
     *      name of the property to set
     * @param selected
     *      whether to set the property for selected state or not
     * @param value
     *      new value of the property
     */
    private final void setProperty(String property, boolean selected, Object value)
    {
        String name = selected ? getSelectedName(property) : property;
        if (value == null) {
            Object oldValue = properties.remove(name);
            firePropertyChangeEvent(name, oldValue, null);
        }
        else {
            Object oldValue = properties.put(name, value);
            firePropertyChangeEvent(name, oldValue, value);
        }
    }
    
    /**
     * Returns the box alignment of this style.
     */
    public Position getAlignment()
    {
        return (Position)getProperty(PROP_ALIGNMENT, false, Position.CENTER);
    }

    /**
     * Sets the box alignment of this style.
     */
    public void setAlignment(boolean sel, Position a)
    {
        setProperty(PROP_ALIGNMENT, false, a);
    }
    
    /**
     * Returns the arrow head at the back.
     */
    public final ArrowHead getBackArrow()
    {
        ArrowHead head = (ArrowHead)getProperty(PROP_BACK_ARROW, false, null);
        return head == null ? ArrowHead.newEmptyArrowHead() : head;
    }
    
    /**
     * Sets the arrow head at the back.
     */
    public final void setBackArrow(ArrowHead backArrow)
    {
        setProperty(PROP_BACK_ARROW, false, backArrow);
    }
    
    /**
     * Returns the label position of boxes that use this style.
     */
    public Position getLabelPosition()
    {
        return (Position)getProperty(PROP_BOX_LABEL_POS, false, Position.TOP);
    }
    
    /**
     * Sets the label position of boxes that use this style.
     * 
     * @param position
     *      new box label position, or {@code null} to unset this property
     */
    public void setLabelPosition(Position position)
    {
        setProperty(PROP_BOX_LABEL_POS, false, position);
    }
    
    /**
     * Returns whether nodes are drawn with a double line in selected or 
     * unselected state.
     */
    public final boolean getDoubleLine(boolean sel)
    {
        return getBoolProperty(PROP_DOUBLE_LINE, sel, false);
    }
    
    /**
     * Returns whether nodes are drawn with a double line in unselected state.
     */
    public final boolean getDoubleLine()
    {
        return getDoubleLine(false);
    }
    
    /**
     * Sets whether nodes with this style are drawn with a double line.
     * 
     * @param sel
     *      if {@code true}, the double line property in selected state is set,
     *      otherwise the double line property in unselected state
     * @param dl 
     *      whether nodes are drawn with a double line
     */
    public final void setDoubleLine(boolean sel, boolean dl)
    {
        setProperty(PROP_DOUBLE_LINE, sel, dl);
    }
    
    /**
     * Sets whether nodes are drawn with a double line in unselected style.
     * 
     * @param dl 
     *      whether nodes are drawn with a double line
     */ 
    public final void setDoubleLine(boolean dl)
    {
        setDoubleLine(false, dl);
    }
    
    /**
     * Returns whether the line surrounding nodes in this style is drawn.
     * 
     * @param sel
     *      when {@code false}, the value for the unselected state is returned,
     *      otherwise the value for the selected state
     * @return
     *      {@code true} when the node is drawn
     */
    public final boolean isDrawn(boolean sel)
    {
        return getBoolProperty(PROP_DRAW, sel, true);
    }
    
    /**
     * Returns whether the line surrounding nodes in this style is drawn in
     * unselected state.
     * 
     * @return
     *      {@code true} when the node is drawn in unselected state
     */
    public final boolean isDrawn()
    {
        return isDrawn(false);
    }
    
    /**
     * Sets whether nodes in this style are drawn.
     * 
     * @param sel
     *      when {@code false}, the value for the unselected state is set,
     *      otherwise the value for the selected state
     * @param 
     *      whether or not nodes in this style are drawn
     */
    public final void setDrawn(boolean sel, boolean value)
    {
        setProperty(PROP_DRAW, sel, value);
    }
    
    /**
     * Sets whether nodes in this style are drawn in unselected state.
     * 
     * @param 
     *      whether or not nodes in this style are drawn
     */
    public final void setDrawn(boolean value)
    {
        setDrawn(false, value);
    }
    
    /**
     * Returns the fill color of this style in selected or unselected state.
     * 
     * @param sel
     *      if {@code true}, the fill color in selected state is returned,
     *      otherwise the fill color in unselected state
     * @return
     *      the fill color in selected or unselected state, depending on the
     *      value of {@code sel}
     */
    public final Color getFillColor(boolean sel)
    {
        return (Color)getProperty(PROP_FILL_COLOR, sel, Color.WHITE);
    }
    
    /**
     * Returns the fill color in unselected state.
     */
    public final Color getFillColor()
    {
        return getFillColor(false);
    }

    /**
     * Sets the fill color in selected or unselected state.
     * 
     * @param sel
     *      if {@code true}, the fill color in selected state is set, otherwise
     *      the fill color in unselected state
     * @param color
     *      the new fill color
     */
    public final void setFillColor(boolean sel, Color color)
    {
        setProperty(PROP_FILL_COLOR, sel, color);
    }
    
    /**
     * Sets the fill color in unselected state.
     */
    public final void setFillColor(Color color)
    {
        setFillColor(false, color);
    }

    /**
     * Returns whether nodes in this style are filled.
     * 
     * @param sel
     *      when {@code false}, the value for the unselected state is returned,
     *      otherwise the value for the selected state
     * @return
     *      {@code true} when the node is filled
     */
    public final boolean isFilled(boolean sel)
    {
        return getBoolProperty(PROP_FILL, sel, true);
    }
       
    /**
     * Returns whether nodes in this style are filled in unselected state.
     */
    public final boolean isFilled()
    {
        return isFilled(false);
    }
        
    /**
     * Sets whether nodes in this style are filled.
     * 
     * @param sel
     *      when {@code false}, the value for the unselected state is set,
     *      otherwise the value for the selected state
     * @param value
     *      whether nodes with this style are filled
     */
    public final void setFilled(boolean sel, boolean value)
    {
        setProperty(PROP_FILL, sel, value);
    }
      
    /**
     * Sets whether nodes in this style are filled in unselected state.
     * 
     * @param value
     *      whether nodes with this style are filled
     */
    public final void setFilled(boolean value)
    {
        setFilled(false, value);
    }
        
    /**
     * Returns whether nodes in this style have flexible shape. If yes, the
     * shape becomes larger if the nodes' label doesn't fit in it.
     * 
     * @return
     *      {@code true} when the node has a flexible shape
     */
    public final boolean hasFlexibleShape()
    {
        return getBoolProperty(PROP_FLEXIBLE_SHAPE, false, false);
    }
        
    /**
     * Sets whether nodes in this style have flexible shapes. If yes, the shape
     * of the node will grow to fit the label if the label doesn't fit in the
     * node.
     * 
     * @param value
     *      whether nodes with this style have a flexible shape
     */
    public final void setFlexibleShape(boolean value)
    {
        setProperty(PROP_FLEXIBLE_SHAPE, false, value);
    }
  
    /**
     * Returns the arrow head at the front.
     */
    public final ArrowHead getFrontArrow()
    {
        ArrowHead head = (ArrowHead)getProperty(PROP_FRONT_ARROW, false, null);
        return head == null ? ArrowHead.newEmptyArrowHead() : head;
    }   
    
    /**
     * Sets the arrow head at the front.
     */
    public final void setFrontArrow(ArrowHead frontArrow)
    {
        setProperty(PROP_FRONT_ARROW, false, frontArrow);
    }
      
    /**
     * Returns the inset of boxes that use this style.
     */
    public double getInset()
    {
        Double inset = (Double)getProperty(PROP_INSET, false, null);
        return inset == null ? 0.0 : inset;
    }
      
    /**
     * Sets the inset of boxes that use this style.
     * 
     * @param ins
     *      new value for the inset property
     */
    public void setInset(double ins)
    {
        setProperty(PROP_INSET, false, ins);
    }

	/**
	 * Unsets the inset of boxes that use this style. This causes
	 * the inherited value of the parent style, if any, to be used.
	 */
	public void unsetInset() {
		setProperty(PROP_INSET, false, null);
	}

    /**
     * Returns the label color of this style in selected or unselected state.
     * 
     * @param sel
     *      if {@code true}, the label color in selected state is returned,
     *      otherwise the label color in unselected state
     * @return
     *      the label color in selected or unselected state, depending on the
     *      value of {@code sel}
     */
    public final Color getLabelColor(boolean sel)
    {
        return (Color)getProperty(PROP_LABEL_COLOR, sel, Color.BLACK);
    }
      
    /**
     * Returns the label color of this style in unselected state.
     */
    public final Color getLabelColor()
    {
        return getLabelColor(false);
    } 
    
    /**
     * Sets the label color of this style in selected or unselected state.
     * 
     * @param sel
     *      if {@code true}, the label color in selected state is set,
     *      otherwise the label color in unselected state
     * @param color
     *      the new label color
     */
    public final void setLabelColor(boolean sel, Color color)
    {
        setProperty(PROP_LABEL_COLOR, sel, color);
    }   
    
    /**
     * Sets the label color in unselected state.
     */
    public final void setLabelColor(Color color)
    {
        setLabelColor(false, color);
    }
   
    /**
     * Returns the font of the label in this style.
     * 
     * @return
     *      the selected or unselected label font in this style
     */
    public final Font getLabelFont()
    {
        Font font = (Font)getProperty(PROP_LABEL_FONT, false, null);
        if (font == null) {
            if (defaultLabelFont == null) {
                defaultLabelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
            }
            font = defaultLabelFont;
        }
        return font;
    }
      
    /** 
     * Sets the font of the label in this style.
     * 
     * @param newFont
     *      the new selected or unselected label font; this parameter can be
     *      {@code null}, in which case 
     *      {@link #getLabelFont(boolean) getLabelFont(sel)} will return the 
     *      parent style's font
     */
    public final void setLabelFont(Font newFont)
    {
        setProperty(PROP_LABEL_FONT, false, newFont);
    }
       
    /**
     * Returns the line color of this style.
     * 
     * @param sel
     *      whether to return the selected or normal value of the line color.
     */
    public final Color getLineColor(boolean sel)
    {
        return (Color)getProperty(PROP_LINE_COLOR, sel, Color.BLACK);
    }
        
    /**
     * Returns the line color (in unselected state) of this style.
     */
    public final Color getLineColor()
    {
        return getLineColor(false);
    }

    /**
     * Sets the line color of this style.
     * 
     * @param sel
     *      determines whether the selected or unselected variant of this
     *      of the line color is set
     * @param color
     *      the new line color of this style
     */
    public final void setLineColor(boolean sel, Color color)
    {
        setProperty(PROP_LINE_COLOR, sel, color);
    }
        
    /**
     * Sets the unselected line color of this style.
     */
    public final void setLineColor(Color color)
    {
        setLineColor(false, color);
    }
    
    /**
     * Returns the minimum width of boxes that use this style. 
     */
    public double getMinimumWidth()
    {
        Double mw = (Double)getProperty(PROP_MIN_WIDTH, false, null);
        return mw == null ? 0.0 : mw;
    }
       
    /**
     * Sets the minimum width of boxes that use this style.
     *
     * @param width
     *      the new minimum width, a double >= 0
     * @throws IllegalArgumentException
     *      if {@code width} is smaller than 0.0
     */
    public void setMinimumWidth(double width)
    {
        if (width < 0.0) throw new IllegalArgumentException();
        setProperty(PROP_MIN_WIDTH, false, width);
    }

	/**
	 * Unsets the minimum width of boxes that use this style. This causes
	 * the inherited value of the parent style, if any, to be used.
	 */
	public void unsetMinimumWidth() {
		setProperty(PROP_MIN_WIDTH, false, null);
	}

    /**
     * Returns the minimum height of boxes that use this style.
     */
    public double getMinimumHeight()
    {
        Double mh = (Double)getProperty(PROP_MIN_HEIGHT, false, null);
        return mh == null ? 0.0 : mh;
    }  
    
    /**
     * Sets the minimum height of boxes that use this style.
     * 
     * @param height
     *      the new minimum height, a double >= 0
     * @throws IllegalArgumentException
     *      if {@code height} is smaller than 0.0
     */
    public void setMinimumHeight(double height)
    {
        if (height < 0.0) throw new IllegalArgumentException();
        setProperty(PROP_MIN_HEIGHT, false, height);
    }

	/**
	 * Unsets the minimum height of boxes that use this style. This causes
	 * the inherited value of the parent style, if any, to be used.
	 */
	public void unsetMinimumHeight() {
		setProperty(PROP_MIN_HEIGHT, false, null);
	}

    
    /** 
     * Returns the shape of nodes with this style.
     */
    public final Shape getShape()
    {
        Shape result = (Shape)getProperty(PROP_SHAPE, false, null);
        if (result == null) {
            if (defaultShape == null) {
                defaultShape = Shape.createCircle(15);
            }
            result = defaultShape;
        }
        return result;
    }    
    
    /**
     * Sets the shape of nodes with this style.
     */
    public final void setShape(Shape shape)
    {
        setProperty(PROP_SHAPE, false, shape);
    }   
        
    /**
     * Returns the stroke of this style. If the stroke is not set and
     * this style does not have a parent, a default value is returned.
     * 
     * @param sel
     *      whether to return the selected or normal value of the stroke.
     */
    public final BasicStroke getStroke(boolean sel)
    {
        BasicStroke stroke = (BasicStroke)getProperty(PROP_STROKE, sel, null);
        if (stroke == null) {
            stroke = getDefaultStroke(LineWidth.NORMAL, DashType.SOLID);
        }
        return stroke;
    } 
       
    /**
     * Returns the stroke (in unselected state) of this style.
     */
    public final BasicStroke getStroke()
    {
        return getStroke(false);
    }

    /**
     * Sets the stroke of this style.
     * 
     * @param sel
     *      determines whether the selected or unselected variant of this
     *      of the stroke is set
     * @param stroke
     *      the new stroke of this style
     */
    public final void setStroke(boolean sel, BasicStroke stroke)
    {
        setProperty(PROP_STROKE, sel, stroke);
    }   
    
    /**
     * Sets the unselected line color of this style.
     */
    public final void setStroke(BasicStroke stroke)
    {
        setStroke(false, stroke);
    }
 
    /**
     * Registers a property change listener to receive property change events
     * from this style.
     */
    public final void addPropertyChangeListener(PropertyChangeListener pcl)
    {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(pcl);
    }
    
    /**
     * Removes a property change listener from the list of listener which
     * receive property change events from this style.
     */
    public final void removePropertyChangeListener(PropertyChangeListener pcl)
    {
        if (listeners != null) {
            listeners.remove(pcl);
        }
    }
    
    /**
     * Sends a PropertyChangeEvent to all registered listeners.
     */
    private final void firePropertyChangeEvent(String propertyName, Object oldValue, Object newValue)
    {
        if (listeners == null || listeners.isEmpty()) return;
        PropertyChangeEvent e = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
        for (int i = listeners.size() - 1; i >= 0; i--) {
            listeners.get(i).propertyChange(e);
        }
    }
}

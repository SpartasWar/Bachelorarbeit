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

package de.uni_due.inf.ti.graph.io;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Object style.
 */
public class DotStyle {
    /**
     * Enum which specifies the color.
     */
    public static enum Color {
        DEFAULT, BLACK, WHITE, RED, GREEN, BLUE, YELLOW, PURPLE, CYAN
    }

    /**
     * Enum which specifies the line style. 
     */
    public static enum LineStyle {
        DEFAULT, NORMAL, DASHED, DOTTED
    }

    /**
     * Enum which specifies shapes.
     */
    public static enum Shape {
        DEFAULT, PLAINTEXT, RECTANGLE, ELLIPSE
    }

    /** Style of which all elements equal DEFAULT. */
    public static final DotStyle UNSET_STYLE = 
            new DotStyle(Shape.DEFAULT, Color.DEFAULT, LineStyle.DEFAULT);
    
    private Shape shape;
    private Color color;
    private LineStyle lineStyle;
    

    /**
     * Creates a new style object with a given shape, color and line style.
     * 
     * @param shape
     *      shape of the new style
     * @param color
     *      color of the new style
     * @param lineStyle
     *      line style of the new style
     * @throws NullPointerException
     *      if any of the arguments is {@code null}
     */
    public DotStyle(Shape shape, Color color, LineStyle lineStyle)
    {
        if (shape == null || color == null || lineStyle == null) {
            throw new NullPointerException();
        }
        
        this.shape = shape;
        this.color = color;
        this.lineStyle = lineStyle;
    }
    
    /**
     * Returns the shape of this style.
     */
    public Shape getShape()
    {
        return shape;
    }
    
    /**
     * Returns the color of this style.
     */
    public Color getColor() 
    {
        return color;
    }
    
    /**
     * Returns the line style of this style.
     */
    public LineStyle getLineStyle()
    {
        return lineStyle;
    }
    
    /**
     * Returns a style which is equal to this style, except for the color.
     * 
     * @param color
     *      color of the new style
     * @return
     *      style which has this style's shape and line style, and the
     *      specified color
     * @throws NullPointerException
     *      if {@code color} is {@code null}
     */
    public DotStyle getSubStyle(Color color)
    {
        return new DotStyle(shape, color, lineStyle);
    }
    
    /**
     * Returns a style which is equal to this style, except for the shape.
     * 
     * @param shape
     *      shape of the new style
     * @return
     *      style which has this style's color and line style, and the
     *      specified shape
     * @throws NullPointerException
     *      if {@code shape} is {@code null}
     */    
    public DotStyle getSubStyle(Shape shape)
    {
        return new DotStyle(shape, color, lineStyle);
    }

    /**
     * Returns a style which is equal to this style, except for the line style.
     * 
     * @param lineStyle
     *      line style of the new style
     * @return
     *      style which has this style's shape and color, and the
     *      specified line style
     * @throws NullPointerException
     *      if {@code lineStyle} is {@code null}
     */
    public DotStyle getSubStyle(LineStyle lineStyle)
    {
        return new DotStyle(shape, color, lineStyle);
    }
    
    /**
     * Returns a style which is equal to a given style, except that 
     * default values are changed to the values of this style. 
     * If {@code style == null}, this style is returned.
     * 
     * @param style
     *      style on which the new style is based
     * @return
     *      style which is equal to this style, except that values of 
     *      {@code style} are taken when possible
     */
    public DotStyle getSubStyle(@Nullable DotStyle style)
    {
        // determine if a new Style object must be created
        if (style == null || UNSET_STYLE.equals(style)) {
            return this;
        }
        else if (style.shape != Shape.DEFAULT &&
            style.color != Color.DEFAULT &&
            style.lineStyle != LineStyle.DEFAULT) {
            return style;
        }
        
        // if yes, create it
        Shape shape = style.shape;
        if (shape == Shape.DEFAULT) shape = this.shape;
        
        Color color = style.color;
        if (color == Color.DEFAULT) color = this.color;
        
        LineStyle lineStyle = style.lineStyle;
        if (lineStyle == LineStyle.DEFAULT) lineStyle = this.lineStyle;
        
        return new DotStyle(shape, color, lineStyle);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int hash = prime * shape.hashCode();
        hash = prime * hash + color.hashCode();
        hash = prime * hash + lineStyle.hashCode();
        return hash;
    }
    
    /**
     * Determined whether a given object is equal to this style. An object
     * is equal to this style if it is a style which has the same shape, color
     * and line style.
     * 
     * @param o
     *      the object of which it is to be determined whether or not it is
     *      equal to this style
     * @return
     *      {@code true} if {@code o} is equal to this style, {code false} if
     *      not
     */
    @Override
    public boolean equals(@Nullable Object o) 
    {
        if (!(o instanceof DotStyle)) return false;
        
        DotStyle so = (DotStyle)o;
        return (so.color == color && 
                so.shape == shape &&
                so.lineStyle == lineStyle);
    }

    /**
     * Returns a style which represents a certain color.
     * Shape and line style of the new style are not set.
     * 
     * @param color
     *      color of the new style
     * @return
     *      style which represents the given color
     * @throws NullPointerException
     *      when {@code color} is {@code null}
     */
    public static DotStyle getColorStyle(Color color)
    {
        return new DotStyle(Shape.DEFAULT, color, LineStyle.DEFAULT);
    }
    
    /**
     * Returns a style which represents a certain shape.
     * Color and line style of the new style are not set.
     * 
     * @param shape
     *      shape of the new style
     * @return
     *      style which represents the given shape
     * @throws NullPointerExceptin
     *      if {@code shape} is {@code null}
     */
    public static DotStyle getShapeStyle(Shape shape)
    {
        return new DotStyle(shape, Color.DEFAULT, LineStyle.DEFAULT);
    }
    
    /**
     * Returns a style which represents a given line style.
     * Color and shape of the new style are not set.
     * 
     * @param ls
     *      line style of the new style
     * @return
     *      style which represents the given line style
     * @throws NullPointerException
     *      if {@code ls} is {@code null}
     */
    public static DotStyle getLineStyle(LineStyle ls)
    {
        return new DotStyle(Shape.DEFAULT, Color.DEFAULT, ls);
    }
}
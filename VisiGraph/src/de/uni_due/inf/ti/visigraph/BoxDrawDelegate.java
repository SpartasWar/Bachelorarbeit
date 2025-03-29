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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

/**
 * Draw delegate for boxes.
 * 
 * @author H.J. Sander Bruggink
 */
class BoxDrawDelegate extends DrawDelegate<VxBox>
{
    private static final double BOX_LABEL_INSET = 2.0;
    
    /**
     * Creates a new BoxDrawDelegate instance.
     */
    public BoxDrawDelegate(VxBox box, GraphDrawer drawer)
    {
        super(box, drawer);
    }    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void draw(Graphics2D g, boolean selected)
    {
        VxBox box = getElement();
        Style style = getStyle();
        
        Color oldColor = g.getColor();
        Stroke oldStroke = g.getStroke();
        
        Rectangle2D boxRect = getBounds();
        g.setColor(style.getLineColor(selected));
        g.setStroke(style.getStroke(selected));
        g.draw(boxRect);
        g.setColor(style.getFillColor(selected));
        g.fill(boxRect);
        
        String label = box.getLabel();
        if (label != null) {
            Font font = style.getLabelFont();
            Font oldFont = g.getFont();
            g.setFont(font);
            Rectangle2D labelBds = font.getStringBounds(label, g.getFontRenderContext());
            Style.Position labelPos = style.getLabelPosition();

            double x = 0, y = 0;
            switch (labelPos) {
                case TOP_LEFT:
                case LEFT:
                case BOTTOM_LEFT:
                    x = boxRect.getX() + BOX_LABEL_INSET - labelBds.getWidth() / 2;
                    break;
                case TOP:
                case CENTER:
                case BOTTOM:
                    x = boxRect.getCenterX();
                    break;
                case TOP_RIGHT:
                case RIGHT:
                case BOTTOM_RIGHT:
                    x = boxRect.getMaxX() - BOX_LABEL_INSET - labelBds.getWidth() / 2;
                    break;
            }
            switch (labelPos) {
                case TOP_LEFT:
                case TOP:
                case TOP_RIGHT:
                    y = boxRect.getY() + BOX_LABEL_INSET;
                    break;
                case LEFT:
                case CENTER:
                case RIGHT:
                    y = boxRect.getCenterY();
                    break;
                case BOTTOM_LEFT:
                case BOTTOM:
                case BOTTOM_RIGHT:
                    y = boxRect.getMaxY() - BOX_LABEL_INSET - labelBds.getHeight();
                    break;
            }
            g.setColor(style.getLabelColor());
            Utils.drawLabel(g, x, y, label);
            
            g.setFont(oldFont);
        }
        
        g.setColor(oldColor);
        g.setStroke(oldStroke);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Rectangle2D getBounds(Rectangle2D buf)
    {
        VxBox box = getElement();
        buf = getDrawer().getBounds(box.getGroup(), buf);
        if (buf == null) buf = new Rectangle2D.Double();
        Rectangle2D.Double result = 
                buf instanceof Rectangle2D.Double ? 
                    (Rectangle2D.Double)buf : 
                    new Rectangle2D.Double();
        Style style = getStyle();
        
        double inset = style.getInset();
        double minWidth = style.getMinimumWidth();
        double minHeight = style.getMinimumHeight();
        Style.Position align = style.getAlignment();
        
        result.x -= inset;
        result.y -= inset;
        result.width += 2 * inset;
        result.height += 2 * inset;
        
        if (result.width < minWidth) {
            switch (align) {
                case TOP_LEFT:
                case LEFT:
                case BOTTOM_LEFT:
                    result.width = minWidth;
                    break;
                case TOP:
                case CENTER:
                case BOTTOM:
                    result.x -= (minWidth - result.width) / 2;
                    result.width = minWidth;                    
                    break;
                case TOP_RIGHT:
                case RIGHT:
                case BOTTOM_RIGHT:
                    result.x -= (minWidth - result.width);
                    result.width = minWidth;
                    break;
            }
        }
        if (result.height < minHeight) {
            switch (align) {
                case TOP_LEFT:
                case TOP:
                case TOP_RIGHT:
                    result.height = minHeight;
                    break;
                case LEFT:
                case CENTER:
                case RIGHT:
                    result.y -= (minHeight - result.height) / 2;
                    result.height = minHeight;                    
                    break;
                case BOTTOM_LEFT:
                case BOTTOM:
                case BOTTOM_RIGHT:
                    result.y -= (minHeight - result.height);
                    result.height = minWidth;
                    break;
            }
        }
        
        if (buf != result) buf.setRect(result);
        return buf;
    }
}

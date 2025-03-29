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

package de.uni_due.inf.ti.gui;
 
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

public class GraphicsUtils
{

    private static final String ELLIPSIS = "...";

    /**
     * Draws a string to a graphics context, where a given maximal width
     * may not be surpassed. If the string to draw is wider that the maximal
     * width, part of the string is ignored and an ellipsis is drawn.
     * 
     * @param g             graphics context to render the string to
     * @param x             x coordinate of the base line of the first
     *                      character of the string 
     * @param y             y coordinate of the base line of the string
     * @param maxWidth      maximum drawing width of the string
     * @param text          string to draw
     */
    public static void writeString(Graphics2D g, int x, int y, int maxWidth,
            String text)
    {
        Font font = g.getFont();
        FontRenderContext frc = g.getFontRenderContext();
        Rectangle2D tb = font.getStringBounds(text, frc);
        if (maxWidth >= 0 && tb.getWidth() > maxWidth) {
            Rectangle2D ell = font.getStringBounds(ELLIPSIS, frc);
            tb.setRect(x + tb.getX(), y + tb.getY(),
                    (double) maxWidth - (ell.getWidth() + 2), tb.getHeight());
            Shape oldClip = g.getClip();
            g.clip(tb);
            g.drawString(text, x, y);
            g.setClip(oldClip);
            g.drawString(ELLIPSIS, x + (int) tb.getWidth() + 2, y);
        } 
        else {
            g.drawString(text, x, y);
        }
    }

    
    /**
     * Normalizes an angle to the range 0-2PI.
     */
    public static double normalizeAngle(double angle) 
    {
        double norm = angle % (2 * Math.PI);
        if (norm < 0) norm += 2 * Math.PI;
        return norm;
    }
    
    
    /**
     * Normalizes an angle to the range of -PI-PI.
     */
    public static double normalizeRelativeAngle(double angle) 
    {
        angle = normalizeAngle(angle);
        if (angle > Math.PI) angle -= 2 * Math.PI;
        return angle;
    }
    
}

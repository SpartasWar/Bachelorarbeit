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

/**
 * This class represents a combination of a foreground and a
 * background color.
 * 
 * @author H.J.Sander Bruggink
 * 
 */
public class ColorScheme
{

    private Color foreground;
    private Color background;

    /**
     * Creates a new ColorScheme with a foreground and background
     * color.
     * 
     * @param foreground
     *        foreground color of the new ColorScheme
     * @param background
     *        background color of the new ColorScheme
     */
    public ColorScheme(Color foreground, Color background)
    {
        this.foreground = foreground;
        this.background = background;
    }

    /**
     * Returns the foreground color of this ColorScheme.
     */
    public Color getForeground()
    {
        return foreground;
    }

    /**
     * Sets the foreground color of this ColorScheme.
     */
    public void setForeground(Color foreground)
    {
        this.foreground = foreground;
    }

    /**
     * Returns the background color of this ColorScheme.
     */
    public Color getBackground()
    {
        return background;
    }

    /**
     * Sets the background color of this ColorScheme.
     */
    public void setBackground(Color background)
    {
        this.background = background;
    }

}

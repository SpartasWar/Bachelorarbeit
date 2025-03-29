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

package de.uni_due.inf.ti.util;

import java.awt.Font;
import java.util.SortedMap;
import java.util.TreeMap;

import de.uni_due.inf.ti.annotation.NonNull;
import de.uni_due.inf.ti.annotation.Nullable;

public class FontUtils {

    // Prohibit instantiation
    private FontUtils() {}


    /**
     * Creates a table of different sized fonts of the same family and style.
     * 
     * @param family          
     *      font family of the fonts in the table, e.g. {@code Font.MONOSPACED}
     * @param style
     *      style of the fonts in the table
     * @param sizes
     *      sizes of the fonts in the table
     * @return
     *      a map mapping the different sizes to a font object which represents
     *      the font of the given family and style of the same size
     */
    public static @NonNull SortedMap<Integer, Font> createFontTable(
            @NonNull String family, 
            int style, 
            int... sizes) 
    {
        SortedMap<Integer, Font> fonts = new TreeMap<Integer, Font>();
        for (int size : sizes) {
            Font font = new Font(family, style, size);
            fonts.put(size, font);
        }
        return fonts;
    }

    /**
     * Search a font in a font map.
     * This method returns the first font map which is smaller or equal to
     * the specified size. If the map contains no such font, {@code null} 
     * is returned.
     * 
     * @param fonts
     *      a map which maps font size to fonts
     * @param size
     *      the required font size
     * @return
     *      the largest font in {@code fonts} which is smaller or equal to the
     *      specified size, or {@code null} if such a font is not in the map
     */
    public static @Nullable Font getFont(
            @NonNull SortedMap<Integer, Font> fonts, 
            int size) 
    {
        SortedMap<Integer, Font> head = fonts.headMap(size + 1);
        if (head.isEmpty()) 
            return null;
        else 
            return head.get(head.lastKey());
    }
}

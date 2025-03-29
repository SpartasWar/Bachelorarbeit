/* 
 * Copyright (c) 2013-2014, H.J. Sander Bruggink
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

// 2013-??-?? - Generated
// 2014-09-09 - Extension, bug fixes to deal with null values in lists and maps

package de.uni_due.inf.ti.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import de.uni_due.inf.ti.annotation.Nullable;

/**
 * This class contains static methods which help constructing strings of 
 * various kinds.
 * 
 * @author H.J. Sander Bruggink
 */
public final class StringUtils
{
    // no instantiation
    private StringUtils() {}
    
    /**
     * Returns the string representation of an object. This method returns
     * "NULL" if the argument is {@code null}; otherwise it calls the
     * argument's {@link Object#toString()} method.
     * 
     * @param ob
     *      the object to return the string representation of
     * @return
     *      string representation of {@code ob}, or {@code "NULL"} if
     *      {@code ob == null}
     */
    private static String toString(Object ob)
    {
        return ob == null ? "NULL" : ob.toString();
    }
    
    /**
     * Returns a string representation of an Iterable.
     * The string representation consists of an opening string, then the string
     * representation of each element of the Iterable, in the order returned by
     * its Iterator, separated by a user-defined separator string, and finally
     * a closing string.
     *  
     * @param list          
     *      the collection to return a string representation of
     * @param open
     *      opening string, or {@code null} if no opening string should be used
     * @param sep
     *      the separator string to put between each element, or {@code null}
     *      if no separator string is used
     * @param close
     *      closing string, or {@code null} if no closing string is used
     * @return
     *      a string representation of the collection
     * @throws NullPointerException
     *      if {@code list} is {@code null}
     */
    public static <T> String listString(
            Iterable<T> list, 
            @Nullable String open, 
            @Nullable String sep, 
            @Nullable String close)
    {
        Stream<T> stream = StreamSupport.stream(list.spliterator(), false);
        return listString(stream, open, sep, close);
    }

    /**
     * Returns a string representation of a stream.
     * The string representation consists of an opening string, then the string
     * representation of each element of the stream, in the order returned by
     * its Iterator, separated by a user-defined separator string, and finally
     * a closing string.
     *  
     * @param stream
     *      the stream to return a string representation of
     * @param open
     *      opening string, or {@code null} if no opening string should be used
     * @param sep
     *      the separator string to put between each element, or {@code null}
     *      if no separator string is used
     * @param close
     *      closing string, or {@code null} if no closing string is used
     * @return
     *      a string representation of the collection
     * @throws NullPointerException
     *      if {@code list} is {@code null}
     */
    public static <T> String listString(
            Stream<T> stream, 
            @Nullable String open, 
            @Nullable String sep, 
            @Nullable String close)
    {
        StringBuilder buf = new StringBuilder();
        if (open != null) buf.append(open);
        
        boolean[] first = new boolean[1];
        first[0] = true;
        
        stream.forEachOrdered((T object) -> {
            if (!first[0] && sep != null) buf.append(sep);
            first[0] = false;
            buf.append(toString(object));
        });
        
        if (close != null) buf.append(close);
        
        return buf.toString();
    }

    /**
     * Returns a string representation of an Iterable.
     * The string representation consists of the string representation of each
     * element of the Iterable, in the order returned by its Iterator, 
     * separated by a user-defined separator string.
     *  
     * @param list          
     *      the collection to return a string representation of
     * @param sep
     *      the separator string to put between each element.
     * @return
     *      a string representation of the collection
     * @throws NullPointerException
     *      if {@code list} is {@code null}
     */
    public static String listString(
            Iterable<?> list, 
            @Nullable String sep)
    {
        return listString(list, null, sep, null);
    }

    /**
     * Returns a string representation of a stream.
     * The string representation consists of the string representation of each
     * element of the stream, in the order returned by its Iterator, 
     * separated by a user-defined separator string.
     *  
     * @param stream
     *      the stream to return a string representation of
     * @param sep
     *      the separator string to put between each element.
     * @return
     *      a string representation of the collection
     * @throws NullPointerException
     *      if {@code list} is {@code null}
     */
    public static String listString(
            Stream<?> stream, 
            @Nullable String sep)
    {
        return listString(stream, null, sep, null);
    }

    /**
     * Returns a string representation of a Map.
     * 
     * @param map
     *      the map to return a string representation of
     * @param ar
     *      the string to put between key and value
     * @param sep 
     *      the separator key to put between key-value pairs
     * @return
     *      a string representation of {@code map}
     * @throws NullPointerException
     *      if {@code map}, {@code ar} or {@code sep} is {@code null}
     */
    public static String mapString(Map<?,?> map, String ar, String sep)
    {
        if (map == null || ar == null || sep == null)
            throw new NullPointerException("Arguments must be non-null.");
        
        StringBuilder buf = new StringBuilder();
        
        boolean first = true;
        for (Map.Entry<?,?> entry : map.entrySet()) {
            if (!first) buf.append(sep);
            first = false;
            buf.append(toString(entry.getKey()));
            buf.append(ar);
            buf.append(toString(entry.getValue()));
        }
        
        return buf.toString();
    }
    
    /**
     * Returns a string which consists of a given number of spaces.
     * 
     * @param num
     *      number of spaces in the string
     * @return
     *      a string which consists of {@code num} spaces
     * @throws IllegalArgumentException
     *      if {@code num < 0}
     */
    public static String spaces(int num) 
    {
        if (num < 0) throw new IllegalArgumentException("Argument must be > 0");
        
        switch (num) { // pre-calculated
            case 0: 
                return "";
            case 1:
                return " ";
            case 2:
                return "  ";
            case 3:
                return "   ";
            case 4:
                return "    ";
            case 5:
                return "     ";
            case 6: 
                return "      ";
            case 7:
                return "       ";
            case 8:
                return "        ";
            case 9:
                return "         ";
            case 10:
                return "          ";
            default:
                StringBuilder buf = new StringBuilder();
                for (int i = 0; i < num; i++) {
                    buf.append(' ');
                }
                return buf.toString();
        }
    }
    
    /**
     * Returns a string which puts the string representations of a number of
     * objects in a column-by-column fashion. 
     * <p>
     * The user can specify an opening string, separator string and closing
     * string. Each of these strings can be {@code null}, which has the same
     * effect as the string being empty.
     * 
     * @param open        
     *      string which is inserted at the start of each line
     * @param sep
     *      string which is inserted between the columns of each line
     * @param close
     *      string which is inserted at the end of each line
     * @param strings
     *      objects whose string representations are put in the columns
     * @return
     *      column-by-column representation of {@code objects}
     */
    public static String columnize(String open, String sep, String close, Object... strings)
    {
        List<String[]> columns = new ArrayList<String[]>(strings.length);
        List<Integer> widths = new ArrayList<Integer>(strings.length);
        int numOfLines = 0;
        
        for (Object ob : strings) {
            String[] ss = toString(ob).split("\n");
            columns.add(ss);
            if (ss.length > numOfLines) {
                numOfLines = ss.length;
            }
            int width = 0;
            for (String s : ss) {
                if (s.length() > width) width = s.length();
            }
            widths.add(width);
        }
        
        StringBuilder buf = new StringBuilder();
        
        for (int i = 0; i < numOfLines; i++) {
            if (open != null) buf.append(open);
            for (int c = 0; c < columns.size(); c++) {
                if (c > 0 && sep != null) buf.append(sep);
                String[] component = columns.get(c);
                if (i < component.length) {
                    String l = component[i];
                    buf.append(l);
                    buf.append(spaces(widths.get(c) - l.length()));
                }
                else {
                    buf.append(spaces(widths.get(c)));
                }
            }
            if (close != null) buf.append(close);
            buf.append(System.lineSeparator());
        }
        
        return buf.toString();
    }
    
    /**
     * Returns a string which puts the string representations of a number of
     * objects in a column-by-column fashion.
     * 
     * @param indent        
     *      string which is inserted at the start of each line
     * @param sep
     *      number of spaces between each column
     * @param strings
     *      objects whose string representations are put in the columns
     * @return
     *      column-by-column representation of {@code objects}
     */
    public static String columnize(String indent, int sep, Object... strings)
    {
        return columnize(indent, spaces(sep), null, strings);
    }
    
    /**
     * Returns a string which puts the string representations of a number of
     * objects in a column-by-column fashion.
     * 
     * @param sep
     *      number of spaces between each column
     * @param strings
     *      objects whose string representations are put in the columns
     * @return
     *      column-by-column representation of {@code objects}
     */
    public static String columnize(int sep, Object... strings)
    {
        return columnize(null, spaces(sep), null, strings);
    }
    
    /**
     * Replaces all newlines in a string by a given replacement string, in
     * a platform-independent way.
     * 
     * @param string
     *      the string in which all newlines are to be replaced
     * @param replaceWith
     *      string to replace the newlines with
     * @return
     *      the string {@code string} in which all newlines have been replaced
     *      by {@code replaceWith}
     * @throws NullPointerException
     *      if either {@code string} or {@code replaceWith} is {@code null}
     */
    public static String replaceNewlines(String string, String replaceWith)
    {
        return string.replace(System.lineSeparator(), replaceWith);
    }
}

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

package de.uni_due.inf.ti.util;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.uni_due.inf.ti.annotation.NonNull;

public class Utilities
{

    /**
     * Private constructor in order to prevent instantiation.
     * (Class only contains static utility methods.)
     */
    private Utilities() {}

    /**
     * Determines whether a map is injective. A map is injective, when
     * different key are mapped to different values.
     * 
     * @param map
     *        the map of which injectivity is determined
     * @return {@code true} if {@code map} is injective, {@code false} otherwise
     */
    public static <S, T> boolean isInjective(Map<S, T> map)
    {
        Set<T> seen = new HashSet<T>(map.size());
        for (Map.Entry<S, T> entry : map.entrySet()) {
            T value = entry.getValue();
            if (seen.contains(value)) return false;
            seen.add(value);
        }
        return true;
    }

    /**
     * Lexicographically compares two lists of comparable objects.
     * <p>
     * List {@code a} is lexicographically smaller than list {@code b}, if there
     * is an index {@code k} such that {@code a[i].compareTo(b[i]) == 0}, for
     * all {@code i < k}, and {@code a[k].compareTo(b[k]) < 0}, or, if no such
     * index exists, {@code a.length < b.length}.
     * <p>
     * This method returns -1 if {@code a} is lexicographically smaller than
     * {@code b}, 1 if {@code b} is lexicographically smaller than {@code a},
     * and {@code 0} if neither is lexicographically smaller than the other.
     * 
     * @param <T>
     *        type of the objects in the list
     * @param a
     *        the first list to compare
     * @param b
     *        the second list to compare
     * @return -1, 0 or 1 if {@code a} is lexicographically smaller, equal
     *         or greater than {@code b}
     */
    public static <T extends Comparable<T>> int lexCompareTo(T[] a, T[] b)
    {
        int limit = Math.min(a.length, b.length);
        for (int i = 0; i < limit; i++) {
            int c = a[i].compareTo(b[i]);
            if (c != 0) return c;
        }
        if (a.length < b.length)
            return -1;
        else if (a.length > b.length)
            return 1;
        else
            return 0;
    }

    /**
     * Replaces the extension of a path string by a new extension.
     * The extension is considered to be the substring which starts at the
     * last point in the final component of the path, and ends at the end
     * of the path.
     * 
     * @param path
     *        path string to change the extension of
     * @param ext
     *        new extension of the path string
     * @return the path string in which the extension is
     *         replaced
     */
    public static String changeExtension(String path, String ext)
    {
        if (ext.charAt(0) != '.') ext = "." + ext;
        int extPos = path.lastIndexOf('.');
        if (extPos == -1) {
            return path + ext;
        }
        else {
            if (path.lastIndexOf(File.separatorChar) > extPos)
                return path + ext;
            else
                return path.substring(0, extPos) + ext;
        }
    }

    /**
     * Replaces the extension of a file by a new extension.
     * The extension is considered to be the substring which starts at the
     * last point in the final component of the file, and ends at the end
     * of the file name.
     * 
     * @param file
     *        file to change the extension of
     * @param ext
     *        new extension of the file
     * @return the file in which the extension is
     *         replaced
     */
    public static File changeExtension(File file, String ext)
    {
        String path = file.getPath();
        path = changeExtension(path, ext);
        return new File(path);
    }

    /**
     * Calculates the proper mathematical modulus of two {@code int}s.
     */
    public static int mod(int a, int b)
    {
        return ((a % b) + b) % b;
    }
    
    /**
     * Determines whether the intersection of an iterable and a collection is 
     * empty.
     */
    public static <T> boolean intersectionEmpty(Iterable<T> col1, Collection<T> col2)
    {
        for (T item : col1) {
            if (col2.contains(item)) 
                return false;
        }
        return true;
    }

	/**
	 * Asserts that the given value is not null and returns it.
	 * @apiNote Useful when using the results of other libraries without
	 * nullness annotations.
	 * @param <T> value type
	 * @param value value to check and return
	 * @return the value
	 */
	public static <T> @NonNull T assertNonNull(T value) {
		assert value != null;
		return value;
	}
}

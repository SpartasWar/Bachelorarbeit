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

class BasicGridDirection implements GridDirection
{
    int columnDirection;
    int rowDirection;
    boolean vertical;

    public BasicGridDirection(int cd, int rd, boolean v)
    {
        columnDirection = cd;
        rowDirection = rd;
        vertical = v;
    }

    public void nextPosition(GridPosition pos, int rows, int cols)
    {
        if (vertical) {
            pos.row += rowDirection;
            if (pos.row < 0 || pos.row >= rows) onColumnEnd(pos, rows, cols);
        }
        else {
            pos.col += columnDirection;
            if (pos.col < 0 || pos.col >= cols) onRowEnd(pos, rows, cols);
        }
    }

    void onColumnEnd(GridPosition pos, int rows, int cols)
    {
        if (rowDirection < 0) {
            pos.row = rows;
        }
        else {
            pos.row = 0;
        }
        pos.col += columnDirection;
    }

    void onRowEnd(GridPosition pos, int rows, int cols)
    {
        if (columnDirection < 0) {
            pos.col = cols;
        }
        else {
            pos.col = 0;
        }
        pos.row += rowDirection;
    }

}

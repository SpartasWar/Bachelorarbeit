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

/**
 * This exception is thrown by SgfBuilder if it encounters a semantic error.
 * 
 * @author H. J. Sander Bruggink
 */
public class SgfBuildException extends Exception
{    
    private static final long serialVersionUID = 7692387644085152886L;

    private int lineNo;
    private int colNo;

    /**
     * Creates a new SgfBuildException.
     * 
     * @param lineNo
     *            The line in which the error occurred.
     * @param colNo
     *            The column in which the error occurred.
     * @param msg
     *            The error message.
     */
    public SgfBuildException(int lineNo, int colNo, String msg)
    {
        super(msg);
        this.lineNo = lineNo;
        this.colNo = colNo;
    }

    /**
     * Creates a new SgfBuildException.
     * 
     * @param lineNo        the line in which the error occurred
     * @param msg           the detailed error message
     */
    public SgfBuildException(int lineNo, String msg)
    {
        this(lineNo, -1, msg);
    }
    
    /**
     * Creates a new SgfBuildException.
     * 
     * @param lineNo        the in which the error occurred
     * @param colNo         the column in which the error occurred
     * @param msg           the detailed error message
     */
    public SgfBuildException(int lineNo, int colNo, String msg, Throwable cause) 
    {
        super(msg, cause);
        this.lineNo = lineNo;
        this.colNo = colNo;
    }
    
    /**
     * Creates a new SgfBuildException.
     * 
     * @param lineNo        the in which the error occurred
     * @param msg           the detailed error message
     */
    public SgfBuildException(int lineNo, String msg, Throwable cause)
    {
        this(lineNo, -1, msg, cause);
    }

    /**
     * {@inheritDoc}
     */
    public String getMessage()
    {
        if (lineNo < 0)
            return super.getMessage();
        else if (colNo < 0)
            return String.format("%s (Line: %d)", super.getMessage(), lineNo);
        else
            return String.format("%s (Line: %d, Col: %d)", super.getMessage(), 
                    lineNo, colNo);
    }
}

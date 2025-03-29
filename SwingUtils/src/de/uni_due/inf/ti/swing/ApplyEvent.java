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

package de.uni_due.inf.ti.swing;

import java.util.EventObject;

/**
 * An event which is sent when the user presses Ok or Apply (or equivalent) on
 * a dialog.
 * 
 * @author H.J. Sander Bruggink
 *
 * @param <T>
 *      type of the contents of the dialog from which the event is sent
 */
class ApplyEvent<T> extends EventObject 
{
    /** Serial version UID */
    private static final long serialVersionUID = -7745457552772233235L;

    /** Information associated with the event. */
    private T options;

    /**
     * Creates a new ApplyEvent.
     * 
     * @param source          
     *      object from which the event originated
     */
    public ApplyEvent(Object source) 
    {
        this(source, null);
    }

    /**
     * Constructs a new ApplyEvent.
     * 
     * @param source          
     *      object from which the event originated
     * @param options         
     *      information associated with the event
     */
    public ApplyEvent(Object source, T options) 
    {
        super(source);
        this.options = options;
    }

    /**
     * Returns the information associated with this ApplyEvent.
     */
    public T getOptions() 
    {
        return options;
    }
}

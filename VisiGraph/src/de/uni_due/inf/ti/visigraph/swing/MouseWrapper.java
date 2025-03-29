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

package de.uni_due.inf.ti.visigraph.swing;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.EventListener;

import javax.swing.event.MouseInputListener;

/**
 * A MouseInputListener which wraps around another MouseInputListener.
 */
public class MouseWrapper implements MouseInputListener 
{
    private MouseListener mouseDelegate;
    private MouseMotionListener motionDelegate;

    /**
     * Creates a new MouseWrapper.
     * 
     * @param delegate
     *      delegate of the new MouseWrapper
     * @throws NullPointerException
     *      if {@code delegate} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code delegate} does not implement either the
     *      {@link MouseListener} or the {@link MouseMotionListener} interface
     *      (or both)
     */
    public MouseWrapper(EventListener delegate) 
    {
        if (delegate == null) {
            throw new NullPointerException();
        }
        if (delegate instanceof MouseListener) {
            mouseDelegate = (MouseListener)mouseDelegate; // FIXME should this be delegate
        }
        else {
            mouseDelegate = null;
        }
        if (delegate instanceof MouseMotionListener) {
            motionDelegate = (MouseMotionListener)mouseDelegate; // FIXME delegate?
        }
        else {
            motionDelegate = null;
        }
        if (mouseDelegate == null && motionDelegate == null) {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * Returns the delegate of this MouseWrapper.
     * The returned object will either implement the {@link MouseListener} 
     * interface or the {@link MouseMotionListener} interface (or both).
     * 
     * @return
     *      the delegate of this MouseWrapper
     */
    public EventListener getDelegate()
    {
        if (mouseDelegate != null) {
            return mouseDelegate;
        }
        else {
            return motionDelegate;
        }
    }

    /**
     * Sets the delegate of this MouseWrapper.
     * 
     * @param delegate
     *      the new delegate of this MouseWrapper
     * @throws NullPointerException
     *      if {@code delegate} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code delegate} does not implement either the
     *      {@link MouseListener} or the {@link MouseMotionListener} interface
     *      (or both)
     */
    public void setDelegate(EventListener delegate) 
    {
        if (delegate == null) {
            throw new NullPointerException();
        }
        if (delegate instanceof MouseListener) {
            mouseDelegate = (MouseListener)mouseDelegate; // FIXME should this be "delegate"
        }
        else {
            mouseDelegate = null;
        }
        if (delegate instanceof MouseMotionListener) {
            motionDelegate = (MouseMotionListener)mouseDelegate;
        }
        else {
            motionDelegate = null;
        }
        if (mouseDelegate == null && motionDelegate == null) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        if (mouseDelegate != null) mouseDelegate.mouseClicked(e);
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        if (mouseDelegate != null) mouseDelegate.mouseEntered(e);
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        if (mouseDelegate != null) mouseDelegate.mouseExited(e);
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        if (mouseDelegate!= null) mouseDelegate.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        if (mouseDelegate != null) mouseDelegate.mouseReleased(e);
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        if (motionDelegate != null) motionDelegate.mouseDragged(e);
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        if (motionDelegate != null) motionDelegate.mouseMoved(e);
    }        
}
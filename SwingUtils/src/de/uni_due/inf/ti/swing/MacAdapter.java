/* 
 * Copyright (c) 2015, H.J. Sander Bruggink
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

// 2015-02-16 - Initial version

package de.uni_due.inf.ti.swing;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * PlatformAdapter subclass which makes Mac OS X extensions available to the application.
 * 
 * @author H.J. Sander Bruggink
 */
public class MacAdapter extends PlatformAdapter
{
	/** The {@link com.apple.eawt.Application} instance for this application. */
    private Object eawtApplication = null;
    
    /**
     * Initializes the Mac OS X platform.
     */
    public void initialize()
    {
    	System.setProperty("apple.laf.useScreenMenuBar", "true");
    	String appName = getApplicationName();
    	if (appName != null) {
    		System.setProperty( "com.apple.mrj.application.apple.menu.about.name", appName);
    	}
    }
    
    /**
     * Returns the Application instance for this application. 
     */
    private Object getEawtApplication() throws Exception 
    {
        if (eawtApplication == null) {
            Class<?> clazz = Class.forName("com.apple.eawt.Application");
            Method factoryMethod = clazz.getDeclaredMethod("getApplication");
            eawtApplication = factoryMethod.invoke(null);
        }
        return eawtApplication;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAboutHandler(AboutHandler handler)
    {
        try {
            Object app = getEawtApplication();
            Class<?> aboutHandlerClass = Class.forName("com.apple.eawt.AboutHandler");
            Method setAboutHandlerMethod = app.getClass().getDeclaredMethod("setAboutHandler", aboutHandlerClass);
            if (handler == null) {
                setAboutHandlerMethod.invoke(app, new Object[] { null });
            }
            else {
                InvocationHandler ih = (Object p, Method m, Object[] as) -> {
                    handler.invoke();
                    return null;                
                };
                Object proxy = Proxy.newProxyInstance(MacAdapter.class.getClassLoader(), new Class<?>[] { aboutHandlerClass }, ih);
                setAboutHandlerMethod.invoke(app, new Object[] { proxy });
            }
        }
        catch (Exception e) {
            System.err.println("Installing about handler failed.");
            e.printStackTrace(System.err);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPreferencesHandler(PreferencesHandler handler)
    {
        try {
            Object app = getEawtApplication();
            Class<?> handlerClass = Class.forName("com.apple.eawt.PreferencesHandler");
            Method setHandlerMethod = app.getClass().getDeclaredMethod("setPreferencesHandler", handlerClass);
            if (handler == null) {
                setHandlerMethod.invoke(app, new Object[] { null });
            }
            else {
                InvocationHandler ih = (Object p, Method m, Object[] as) -> {
                    handler.invoke();
                    return null;                
                };
                Object proxy = Proxy.newProxyInstance(MacAdapter.class.getClassLoader(), new Class<?>[] { handlerClass }, ih);
                setHandlerMethod.invoke(app, new Object[] { proxy });
            }
        }
        catch (Exception e) {
            System.err.println("Installing preferences handler failed.");
            e.printStackTrace(System.err);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setQuitHandler(QuitHandler handler)
    {
        try {
            Object app = getEawtApplication();
            Class<?> handlerClass = Class.forName("com.apple.eawt.QuitHandler");
            Method setHandlerMethod = app.getClass().getDeclaredMethod("setQuitHandler", handlerClass);
            if (handler == null) {
                setHandlerMethod.invoke(app, new Object[] { null });
            }
            else {
                InvocationHandler ih = (Object p, Method m, Object[] as) -> {
                    assert as.length >= 2;
                    Class<?> responseClass = Class.forName("com.apple.eawt.QuitResponse");
                    assert responseClass.isInstance(as[1]);
                    
                    boolean canQuit = handler.invoke();
                    if (canQuit) {
                    	Method qm = responseClass.getDeclaredMethod("performQuit");
                    	qm.invoke(as[1]);
                    }
                    else {
                    	Method qm = responseClass.getDeclaredMethod("cancelQuit");
                    	qm.invoke(as[1]);
                    }
                    
                    return null;                
                };
                Object proxy = Proxy.newProxyInstance(MacAdapter.class.getClassLoader(), new Class<?>[] { handlerClass }, ih);
                setHandlerMethod.invoke(app, proxy);
            }
        }
        catch (Exception e) {
            System.err.println("Installing preferences handler failed.");
            e.printStackTrace(System.err);
        }            
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Object getDefaultProperty(PlatformProperty property) 
    {
        switch (property) {
            case MENU_CONTAINS_ABOUT:
            case MENU_CONTAINS_PREFERENCES:
            case MENU_CONTAINS_QUIT:
            case SHOW_MNEMONICS:
                return Boolean.FALSE;
            default:
                return super.getDefaultProperty(property);
        }
    }
 }

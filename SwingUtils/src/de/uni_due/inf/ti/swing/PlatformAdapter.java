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

// 2015-02-15 - Initial version
// 2015-02-16 - Added Javadocs and possibility to register Actions as handlers

package de.uni_due.inf.ti.swing;

import java.awt.event.ActionEvent;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.function.Predicate;

import javax.swing.Action;

import de.uni_due.inf.ti.annotation.Nullable;
import de.uni_due.inf.ti.util.Pair;

/**
 * This class facilitates integrating the application with the host platform. 
 * It accomplishes this in two ways:
 * <ol>
 * <li>it provides an abstract interface to react to platform-specific events; 
 *     and
 * <li>it holds properties which control the way SwingUtils and the client
 *     application build up the GUI, in order to provide a more native-like
 *     user interface (these properties can be explicitly set by the user
 *     to override the system defaults).
 * </ol>
 * By default, a PlatformAdapter for the Mac OS X platform is provided, but 
 * users can register custom PlatformAdapters with the
 * {@link #registerPlatformAdapter(Predicate, PlatformAdapter)} method.
 * 
 * @author H.J. Sander Bruggink
 */
public class PlatformAdapter
{
    /**
     * Names of the properties for which values are stored in PlatformAdapters.
     */
    protected enum PlatformProperty
    {
        MENU_CONTAINS_ABOUT, 
        MENU_CONTAINS_PREFERENCES, 
        MENU_CONTAINS_QUIT,
        SHOW_MNEMONICS
    }

    /**
     * Handler for about requests.
     */
    @FunctionalInterface
    public static interface AboutHandler
    {
        /**
         * Called by the system when the user requests the about box through the
         * operating system.
         */
        public void invoke();
    }

    /**
     * Handler for preferences requests.
     */
    @FunctionalInterface
    public static interface PreferencesHandler
    {
        /**
         * Called by the system when the user requests the preferences box
         * through the operating system.
         */
        public void invoke();
    }

    /**
     * Handler for quit requests.
     */
    @FunctionalInterface
    public static interface QuitHandler
    {
        /**
         * Called by the system when the user requests the application to quit.
         * It should return {@code true} if the application can quit, and
         * {@code false} otherwise.
         * 
         * @return {@code true} if the application can quit, {@code false}
         *         otherwise
         */
        public boolean invoke();
    }

    private static PlatformAdapter instance = null;
    private static LinkedList<Pair<Predicate<String>,PlatformAdapter>> adapters;
    static {
        adapters = new LinkedList<>();
        registerPlatformAdapter(x -> x.toLowerCase().contains("os x"), new MacAdapter());
    }

    private String appName = null;
    private EnumMap<PlatformProperty, Object> properties = new EnumMap<>(PlatformProperty.class);

    /**
     * Registers a user-defined PlatformAdapter instance. 
     * All PlatformAdapters must have been registered before the first call
     * to {@link #getInstance()} or {@link #getInstance(String)}. 
     * All calls to this method after that have no effect.
     * 
     * @param pred
     *      predicate which determines when the registered PlatformAdapter
     *      can be used
     * @param adapter
     *      the PlatformAdapter to be registered
     */
    public static void registerPlatformAdapter(Predicate<String> pred, PlatformAdapter adapter)
    {
        if (adapters != null) {
            adapters.addFirst(Pair.create(pred, adapter));
        }
    }
    
    /**
     * Creates a new singleton PlatformAdapter instance for this application.
     * After calling this method, the {@code instance} class variable is
     * non-null.
     * <p>
     * This method can only be called once during the lifetime of the 
     * application.
     * 
     * @param platformString
     *      platform string for which to create the instance
     * @throws NullPointerException
     *      when {@code adapters} is {@code null} (when this happens, this
     *      usually means that the method is called a second time) 
     */
    private static void createNewInstance(String platformString) 
    {
        for (Pair<Predicate<String>,PlatformAdapter> pair : adapters) {
            if (pair.getFirst().test(platformString)) {
                instance = pair.getSecond();
                adapters = null; // make null so that it can be garbage collected
                return;
            }
        }
        
        instance = new PlatformAdapter();
    }
    
    /**
     * Returns the PlatformAdapter instance for this application.
     * <p> 
     * If no instance exists yet, a PlatformAdapter for the current
     * platform is selected in the following way:
     * <ol>
     * <li>The method iterates through the registered PlatformAdapters
     *     (the ones registered last are checked first).
     * <li>As soon as one predicate returns true, the associated 
     *     PlatformAdapter is selected.
     * <li>If none of the predicates returned true, a default 
     *     PlatformAdapter is selected.
     * </ol> 
     * 
     * @return
     *      the PlatformAdapter instance for this application.
     */
    public static PlatformAdapter getInstance()
    {
        if (instance == null) {
            createNewInstance(System.getProperty("os.name").toLowerCase());
        }
        return instance;
    }

    /**
     * Returns the singleton instance of the PlatformAdapter; if no instance
     * exists yet, initialize the instance for the given platform string.
     * For information how a PlatformAdapter is selected for a platform string,
     * see the {@link #getInstance()} documentation.
     * <p>
     * This method exists for testing purposes only. By calling it before any
     * calls to {@link #getInstance()}, it can be used to replace the Platform
     * adapter for the real platform by another one.
     * 
     * @param platform
     *      hint for the platform for which to return the PlatformAdapter
     * @return 
     *      the PlatformAdapter instance for this application
     */
    public static PlatformAdapter getInstance(String platform)
    {
        if (instance == null) {
            createNewInstance(platform);
        }
        return instance;
    }

    /**
     * Initializes the platform. This method should be called after the
     * properties of the PlatformAdapter have been set.
     */
    public void initialize()
    {
        // This default implementation does nothing.
    }

    /**
     * Sets the name of the application.
     */
    public void setApplicationName(String name)
    {
        appName = name;
    }

    /**
     * Returns the application name, or {@code null} of no application name has
     * been set.
     */
    public @Nullable String getApplicationName()
    {
        return appName;
    }

    /**
     * Registers a handler which is called when the user selects the about box
     * provided by the operating system. The new handler replaces a handler set
     * previously. If {@code null}, the old handler is removed.
     * <p>
     * The {@code AboutHandler} interface is a functional interface containing a
     * single, no-parameter, void method {@code invoke()}. When the use
     * activates the about box by platform-specific means, this method is
     * called.
     * 
     * @param handler
     *      handler for about box requests
     */
    public void setAboutHandler(AboutHandler handler)
    {
        // default implementation does nothing
    }

    /**
     * Registers an about handler in the form of a Swing Action. The new handler
     * replaces a handler set previously. If {@code null}, the old handler is
     * removed.
     * 
     * @param act
     *      new handler for about box requests
     */
    public final void setAboutHandler(Action act)
    {
        if (act == null) {
            setAboutHandler((AboutHandler) null);
        } else {
            setAboutHandler(() -> {
                ActionEvent e = new ActionEvent(this, 0, "about");
                act.actionPerformed(e);
            });
        }
    }

    /**
     * Registers a handler which is called when the user selects the
     * platform-dependent way of entering the program preferences. The new
     * handler replaces a handler set previously. If {@code null}, the old
     * handler is removed.
     * <p>
     * The {@code AboutHandler} interface is a functional interface containing a
     * single, no-parameter, void method {@code invoke()}. When the user
     * activates the preferences box by platform-specific means, this method is
     * called.
     * 
     * @param handler
     *      handler for preferences requests
     */
    public void setPreferencesHandler(PreferencesHandler handler)
    {
        // default implementation does nothing
    }

    /**
     * Registers a preferences handler in the form of a Swing Action. The new
     * handler replaces a handler set previously. If {@code null}, the old
     * handler is removed.
     * 
     * @param act
     *      new handler for preferences requests
     */
    public final void setPreferencesHandler(Action act)
    {
        if (act == null) {
            setPreferencesHandler((PreferencesHandler) null);
        } else {
            setPreferencesHandler(() -> {
                ActionEvent e = new ActionEvent(this, 0, "prefs");
                act.actionPerformed(e);
            });
        }
    }

    /**
     * Registers a handler which is called when the user selects the
     * platform-dependent ways of quitting the application. The new handler
     * replaces a handler set previously. If {@code null}, the old handler is
     * removed.
     * <p>
     * The {@code AboutHandler} interface is a functional interface containing a
     * single, no-parameter method {@code invoke()}. When the user request the
     * application to quit by platform-specific means, this method is called. It
     * should return {@code true} if the application can quit, and {@code false}
     * if the application cannot quit.
     * 
     * @param handler
     *      handler for quit requests
     */
    public void setQuitHandler(QuitHandler handler)
    {
        // default implementation does nothing
    }

    /**
     * Sets whether or not the about box menu item should be shown in the menu.
     * Setting this property to {@code null} for the platform default. This
     * method exists mainly for testing purposes; in most circumstances using
     * the platform default is recommended.
     * <p>
     * Note that this method only stores a value, and affects the return value
     * of the {@link #menuContainsAboutBox()} method. This class does not
     * generate the menu itself.
     * 
     * @param value
     *      whether or not the about box menu item should be added to the
     *      menu; or {@code null} for platform default
     */
    public final void setMenuContainsAboutBox(Boolean value)
    {
        properties.put(PlatformProperty.MENU_CONTAINS_ABOUT, value);
    }

    /**
     * Returns whether the menu should contain a menu item for displaying the
     * about box.
     */
    public final boolean menuContainsAboutBox()
    {
        return getProperty(PlatformProperty.MENU_CONTAINS_ABOUT, Boolean.class);
    }

    /**
     * Sets whether or not the preferences menu item should be shown in the
     * menu. Setting this property to {@code null} for the platform default.
     * This method exists mainly for testing purposes; in most circumstances
     * using the platform default is recommended.
     * <p>
     * Note that this method only stores a value, and affects the return value
     * of the {@link #menuContainsPreferences()} method. This class does not
     * generate the menu itself.
     * 
     * @param value
     *      whether or not the preferences menu item should be added to
     *      the menu; or {@code null} for platform default
     */
    public final void setMenuContainsPreferences(Boolean value)
    {
        properties.put(PlatformProperty.MENU_CONTAINS_PREFERENCES, value);
    }

    /**
     * Returns whether the menu should contain a menu item for opening the
     * preferences.
     */
    public final boolean menuContainsPreferences()
    {
        return getProperty(PlatformProperty.MENU_CONTAINS_PREFERENCES,
                Boolean.class);
    }

    /**
     * Sets whether or not the quit menu item should be shown in the menu.
     * Setting this property to {@code null} for the platform default. This
     * method exists mainly for testing purposes; in most circumstances using
     * the platform default is recommended.
     * <p>
     * Note that this method only stores a value, and affects the return value
     * of the {@link #menuContainsQuit()} method. This class does not generate
     * the menu itself.
     * 
     * @param value
     *      whether or not the quit menu item should be added to the menu;
     *      or {@code null} for platform default
     */
    public final void setMenuContainsQuit(Boolean value)
    {
        properties.put(PlatformProperty.MENU_CONTAINS_QUIT, value);
    }

    /**
     * Returns whether the menu should contain a menu item for quitting the
     * application.
     */
    public final boolean menuContainsQuit()
    {
        return getProperty(PlatformProperty.MENU_CONTAINS_QUIT, Boolean.class);
    }
    
    /**
     * Sets whether or not mnemonics are shown in buttons and menu items.
     * Setting this property to {@code null} for the platform default. This
     * method exists mainly for testing purposes; in most circumstances using
     * the platform default is recommended.
     * <p>
     * Note that this method only stores a value, and affects the return value
     * of the {@link #menuContainsQuit()} method. This class does not generate
     * the menu itself.
     * 
     * @param value
     *      whether or mnemonics are shown in buttons and menu items;
     *      or {@code null} for platform default
     */
    public final void setShowMnemonics(Boolean value)
    {
        setProperty(PlatformProperty.SHOW_MNEMONICS, value);
    }

    /**
     * Returns the default value of a property for the platform described by
     * this PlatformAdapter.
     * <p>
     * Subclasses that override this method must make sure that this method
     * should always return an object of the correct type. In particular, this
     * method never returns {@code null}. It is recommended to call this
     * implementation (by {@code super.getDefaultProperty(property)}) to obtain
     * a default value for properties where you do not want to set a different
     * value explicitly.
     * 
     * @param property
     *      name of the property to obtain the platform default value of
     * @return 
     *      default value of the given property for the platform described by
     *      this PlatformAdapter
     */
    protected Object getDefaultProperty(PlatformProperty property)
    {
        switch (property) {
            case MENU_CONTAINS_ABOUT:
            case MENU_CONTAINS_PREFERENCES:
            case MENU_CONTAINS_QUIT:
            case SHOW_MNEMONICS:
                return Boolean.TRUE;
        }
        assert false; // unreachable  
        return Boolean.FALSE;
    }

    /**
     * Returns the value of a platform property. This method first determines,
     * whether the property has been explicitly set by the user. When not, the
     * method returns the platform default for the property.
     * 
     * @param property
     *      property to return the value of
     * @return 
     *      value of {@code property}
     */
    @SuppressWarnings("unchecked")
    private <T> T getProperty(PlatformProperty property, Class<T> type)
    {
        Object val = properties.get(property);
        if (val == null || !type.isInstance(val)) {
            val = getDefaultProperty(property);
            assert val != null;
            assert type.isInstance(val);
        }
        return (T) val;
    }
    
    /** 
     * Sets the value of a property. 
     * When the {@code value} arguments is {@code null}, the old value of
     * the property is removed and the value of the property defaults to the
     * system default value.
     * 
     * @param property
     *      property to set the value of
     * @param value
     *      new value of the property
     */
    private void setProperty(PlatformProperty property, Object value) 
    {
        if (value == null) {
            properties.remove(property);
        }
        else {
            properties.put(property, value);
        }
    }
}

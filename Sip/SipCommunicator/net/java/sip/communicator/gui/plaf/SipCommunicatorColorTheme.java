/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * Portions of this software are based upon public domain software
 * originally written at the National Center for Supercomputing Applications,
 * University of Illinois, Urbana-Champaign.
 */
package net.java.sip.communicator.gui.plaf;

import javax.swing.plaf.metal.*;
import javax.swing.plaf.ColorUIResource;
import net.java.sip.communicator.gui.GuiManager;
import java.awt.Color;
import javax.swing.*;
import net.java.sip.communicator.common.*;

/**
 * SipCommunicator color settings
 * @todo load from XML
 * @author Emil Ivov <emcho@dev.java.net>
 * @version 1.0
 */

public class SipCommunicatorColorTheme
    extends DefaultMetalTheme
{
    private static final Console console = Console.getConsole(SipCommunicatorColorTheme.class);

    //Set default colors
    //ColorUIResource defaultForeground = new ColorUIResource(122, 150, 233);
    ColorUIResource defaultForeground = new ColorUIResource(48, 63, 112);
    ColorUIResource menuForeground    = defaultForeground;

    ColorUIResource textHighlightColor = new ColorUIResource(212, 208, 200);
    //ColorUIResource textColor          = new ColorUIResource(21, 49, 129);
    ColorUIResource textColor          = new ColorUIResource(48, 63, 112);
       ColorUIResource controlTextColor   = textColor;
    ColorUIResource inactiveControlTextColor = textHighlightColor;
    ColorUIResource userTextColor      = textColor;
    ColorUIResource highlightedTextColor = textColor;

    ColorUIResource defaultBackground = new ColorUIResource(255, 255, 255);
    ColorUIResource menuBackground    = defaultBackground;

    ColorUIResource scSecondaryThree = new ColorUIResource(242, 242, 242);
    ColorUIResource scPrimaryOne     = new ColorUIResource(212, 208, 200);
    ColorUIResource scPrimaryThree   = scSecondaryThree;

    //-----------------------
    public static ColorUIResource NOT_REGISTERED = new ColorUIResource(224, 101, 131);
    public static ColorUIResource REGISTERING    = new ColorUIResource(134, 124, 176);
    public static ColorUIResource REGISTERED     = new ColorUIResource(85, 182, 127);
    //--------
//    public static ColorUIResource CONTACT_GROUP    = new ColorUIResource(134, 124, 176);
    public static ColorUIResource CONTACT_GROUP    = new ColorUIResource(213, 105, 131);
//    public static ColorUIResource CONTACT_GROUP    = new ColorUIResource(236, 46, 94);
//    public static ColorUIResource ON_LINE_CONTACT  = new ColorUIResource(48, 63, 112);
    public static ColorUIResource ON_LINE_CONTACT  = new ColorUIResource(53, 40, 120);
    public static ColorUIResource OFF_LINE_CONTACT = new ColorUIResource(Color.gray);
//    public static ColorUIResource OFF_LINE_CONTACT = new ColorUIResource(49, 49, 49);

    public String getName()
    {
        return "SipCommunicatorColorTheme";
    }
//--------------------------- MENUS --------------------------------------------
    public ColorUIResource getMenuForeground()
    {
        //return defaultForeground;
        return textColor;
    }

    public ColorUIResource getTextHighlightColor()
    {
        return textHighlightColor;
    }


    public ColorUIResource getMenuBackground()
    {
        return defaultBackground;
    }

    public ColorUIResource getMenuSelectedForeground()
    {
        return highlightedTextColor;
    }

    public ColorUIResource getMenuSelectedBackground()
    {
        return textHighlightColor;
    }

//---------------------------- PRIMARY, SECONDARY ... --------------------------
    protected ColorUIResource getPrimary1()
    {
        return defaultForeground;
    }


    protected ColorUIResource getPrimary3()
    {
        return scPrimaryThree;
    }

    protected ColorUIResource getPrimary2()
    {
        return scPrimaryOne;
    }

    protected ColorUIResource getSecondary1()
    {
        return defaultForeground;
    }
    /*
    protected ColorUIResource getSecondary2()
    {
       return defaultBackground;
    }
*/
    protected ColorUIResource getSecondary3()
    {
       return scSecondaryThree;
    }

    protected ColorUIResource getBlack()
    {
//       return defaultForeground;
        return textColor;
    }

//    public ColorUIResource getHighlightedTextColor()
//    {
//       return highlightedTextColor;
//    }
/*
    public ColorUIResource getControlTextColor()
    {
       return highlightedTextColor;
    }
*/
//------------------------------------------------------------------------------
    public ColorUIResource getInactiveControlTextColor()
    {
        return inactiveControlTextColor;

    }

    public ColorUIResource getControlTextColor()
    {
        return controlTextColor;
    }

    public ColorUIResource getUserTextColor()
    {
        return userTextColor;
    }

    /**
     * Helper method to use when running separate sip-communicator modules
     */
    public static void initLookAndFeel()
    {
        MetalLookAndFeel mlf = new MetalLookAndFeel();
        mlf.setCurrentTheme( new SipCommunicatorColorTheme());

        try {
            UIManager.setLookAndFeel(mlf);
        }
        catch (UnsupportedLookAndFeelException ex) {
            console.error("Failed to set custom look and feel", ex);
        }
    }

}

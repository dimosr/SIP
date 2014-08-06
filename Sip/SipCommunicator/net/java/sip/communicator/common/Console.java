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
package net.java.sip.communicator.common;

import java.io.*;
import java.util.*;
import javax.swing.*;
import org.apache.log4j.*;
import org.apache.log4j.Level;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.1
 */
public class Console
{
    public static boolean debugMode = true;
//-------------------- Messages --------------------------
    public static void showMsg(String message)
    {
        showMsg("Message", message);
    }

    public static void showMsg(String title, String message)
    {
        JOptionPane.showMessageDialog(null, message,
                                      title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showDetailedMsg(String message, String details)
    {
        ConsoleUI.showMsg("Message", message, details, ConsoleUI.MESSAGE_ICON);
    }

    public static void showDetailedMsg(String title, String message,
                                       String details)
    {
        ConsoleUI.showMsg(title, message, details, ConsoleUI.MESSAGE_ICON);
    }

//------------------- Errors -----------------------------
    public static void showError(String msg)
    {
        showError("Error!", msg);
    }

    public static void showError(String title, String msg)
    {
        showError(title, msg, msg);
    }

    public static void showError(String title, String msg, String detailedMsg)
    {
//        JOptionPane.showMessageDialog(null, msg + "\nDetails:\n" + detailedMsg,
//                                      title,
//                                      JOptionPane.ERROR_MESSAGE);
        ConsoleUI.showMsg(title, msg, detailedMsg, ConsoleUI.ERROR_ICON);
    }

//------------------- Exceptions --------------------------
    public static void showException(Throwable exc)
    {
        showException("The following exception occurred:\n"
                      + exc.getMessage(),
                      exc);
    }

    public static void showException(String msg, Throwable exc)
    {
        showException(exc.getClass().getName(), msg, exc);
    }

    public static void showException(String title, String msg, Throwable exc)
    {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        exc.printStackTrace(printWriter);
        if (debugMode) {
            exc.printStackTrace();
        }
        showError(title, msg, writer.toString());
        printWriter.close();
        try {
            writer.close();
        }
        catch (IOException ex) {}
    }

    public static void showNonFatalException(String msg, Throwable exc)
    {
        showNonFatalException(exc.getClass().getName(), msg, exc);
    }

    public static void showNonFatalException(String title,
                                             String msg,
                                             Throwable exc)
    {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        exc.printStackTrace(printWriter);
        if (debugMode) {
            exc.printStackTrace(System.out);
        }
        ConsoleUI.showMsg(title, msg, writer.toString(), ConsoleUI.ERROR_ICON);
        printWriter.close();
        try {
            writer.close();
        }
        catch (IOException ex) {}
    }

//---------------- Printing Debug Info -----------------------------------
    /**
     * Prints the string representation of obj
     * @param obj the object to print
     *
     * @deprecated Please use debug, info, error and fatal instead
     */
    public static void println(Object obj)
    {
        System.out.println(obj);
    }

    /**
     * Prints the string representation of obj
     * @param obj the object to print
     *
     * @deprecated Please use debug, info, error and fatal instead
     */
    public static void print(Object obj)
    {
        System.out.print(obj);
    }

//--------------- LOGGER ENCAPSULATION -----------------------------------
    // ------------------------------------------------------------- Attributes
    private static boolean initialized = false;
    private static String LAYOUT = "%r [%t] %p %c{2} %x - %m%n";
    private Logger logger = null;
    // ------------------------------------------------------------ Constructor
    /**
     * Base constructor
     */
    private Console(Logger logger)
    {
        this.logger = logger;
        if (!initialized) {
            initialize();
        }
    }

    public static Console getConsole(Class clazz)
    {
        return new Console(Logger.getLogger(clazz));
    }

    public static Console getConsole(String name)
    {
        return new Console(Logger.getLogger(name));
    }

    // ---------------------------------------------------------- Implmentation
    private void initialize()
    {
        Category root = Category.getRoot();
        Enumeration appenders = root.getAllAppenders();
        if (appenders == null || !appenders.hasMoreElements()) {
            // No config, set some defaults ( consistent with
            // commons-logging patterns ).
            ConsoleAppender app = new ConsoleAppender(new PatternLayout(LAYOUT),
                ConsoleAppender.SYSTEM_OUT);
            app.setName("SIP COMMUNICATOR");
            root.addAppender(app);
            root.setLevel(TraceLevel.TRACE);
        }
        initialized = true;
    }

    /**
     * Logs an entry in the calling method
     */
    public void logEntry()
    {
        if (logger.isEnabledFor(TraceLevel.TRACE)) {
            StackTraceElement caller = new Throwable().getStackTrace()[1];
            logger.log(TraceLevel.TRACE, "[entry] " + caller.getMethodName());
        }
    }

    /**
     * Logs exiting the calling method
     */
    public void logExit()
    {
        if (logger.isEnabledFor(TraceLevel.TRACE)) {
            StackTraceElement caller = new Throwable().getStackTrace()[1];
            logger.log(TraceLevel.TRACE, "[exit] " + caller.getMethodName());
        }
    }

    /**
     * Log a message to the Log4j Category with <code>TRACE</code> priority.
     * Currently logs to <code>DEBUG</code> level in Log4J.
     */
    public void trace(Object message)
    {
        logger.log(TraceLevel.TRACE, message);
    }

    /**
     * Log an error to the Log4j Category with <code>TRACE</code> priority.
     * Currently logs to <code>DEBUG</code> level in Log4J.
     */
    public void trace(Object message, Throwable t)
    {
            logger.log(TraceLevel.TRACE, message, t);
    }

    public void debug(Object message, Throwable t)
    {
        logger.debug(message, t);
    }

    public void debug(Object message)
    {
        logger.debug(message);
    }

    public void info(Object message, Throwable t)
    {
        logger.info(message, t);
    }

    public void info(Object message)
    {
        logger.info(message);
    }

    public void warn(Object message, Throwable t)
    {
        logger.warn(message, t);
    }

    public void warn(Object message)
    {
        logger.warn(message);
    }

    public void error(Object message, Throwable t)
    {
        logger.error(message, t);
    }

    public void error(Object message)
    {
        logger.error(message);
    }

    public void fatal(Object message, Throwable t)
    {
        logger.fatal(message, t);
    }

    public void fatal(Object message)
    {
        logger.fatal(message);
        logger.isDebugEnabled();
    }

    public boolean isTraceEnabled()
    {
        return logger.getLevel() ==null || logger.getLevel().equals(TraceLevel.TRACE);
    }

    public void setToTraceLevel()
    {
        logger.setLevel(TraceLevel.TRACE);
    }


    public boolean isDebugEnabled()
    {
        return logger.isDebugEnabled();
    }

    public void setToDebugLevel()
    {
        logger.setLevel(Level.DEBUG);
    }

    public boolean isInfoEnabled()
    {
        return logger.isInfoEnabled();
    }

    public void setToInfoLevel()
    {
        logger.setLevel(Level.INFO);
    }

    public boolean isWarnEnabled()
    {
        return logger.isEnabledFor(Priority.WARN);
    }

    public void setToWarnLevel()
    {
        logger.setLevel(Level.WARN);
    }


    //------------------------------- TEST --------------------------------
    public static void main(String[] args)
    {
        Console console = Console.getConsole(Console.class);
        console.debug("Debug");
        try {
            Thread.sleep(500);
        }
        catch (InterruptedException ex) {
        }
        console.info("Info");
        console.warn("Warn");
        console.error("Error");
        console.fatal("fatal");
    }

    public static class TraceLevel extends Level
    {
        public final static int TRACE_INT  = 5000;
        /**
         * The <code>TRACE</code> level designates very severe error
         * events that will presumably lead the application to abort.
         */
        final static public Level TRACE = new TraceLevel();

        public TraceLevel()
        {
            super(TRACE_INT, "TRACE", 8);
        }

        /**
         * Convert an integer passed as argument to a level. If the
         * conversion fails, then this method returns.
         */
        public static Level toLevel(String sArg)
        {
            return (Level) toLevel(sArg, TRACE);
        }

        /**
          Convert an integer passed as argument to a level. If the
          conversion fails, then this method returns.
         */
        public
            static
            Level toLevel(int val)
        {
            return (Level) toLevel(val, TRACE);
        }
    }
}

/*
 * DebugIM.java
 *
 * Created on October 9, 2002, 4:16 PM
 */

package gov.nist.sip.instantmessaging;

import java.io.*;
import java.util.*;
import java.net.*;
import java.lang.*;
/**
 *
 * @author  olivier
 * @version 1.0
 */
public class DebugIM {

    private static String debugFile;
    
    public static void setDebugFile(String file) {
            debugFile=file;
    }
    
    public static void writeFile(String inFile,String outFile, String text, boolean sep) {
        // we read this file to obtain the options
        try{
            FileWriter fileWriter = new FileWriter(outFile,true);
            PrintWriter pw = new PrintWriter(fileWriter,false);
            
            if (text==null) {
                pw.println();
            }
            else
            if (sep) {
                 pw.print(text);
            }
            else {
                 pw.println(text);
            }
           
            pw.close();
            fileWriter.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void println(String text){
        if (debugFile!=null)
            writeFile(debugFile,debugFile,text,false);
        else
            System.out.println(text);
    }
    
    public static void println(){
        if (debugFile!=null)
            writeFile(debugFile,debugFile,null,false);
        else
            System.out.println();
    }
    
    public static void print(String text){
        if (debugFile!=null)
            writeFile(debugFile,debugFile,text,true);
        else
            System.out.print(text);
    }
    
    
}

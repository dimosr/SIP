package gov.nist.sip.proxy;

import java.io.*;
import java.util.*;
import java.net.*;
import java.lang.*;

/** Debugging println.
*
*@version  JAIN-SIP-1.1
*
*@author Olivier Deruelle <deruelle@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class ProxyDebug {

    public static  boolean debug = false;
    private static String proxyOutput;
    private static PrintStream stream=System.out;
    
    
    public static void setProxyOutputFile(String proxyOut) {
       proxyOutput=proxyOut;
    }
    
    public static void writeFile(String inFile,String outFile, 
	String text, boolean sep) {
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
    
    public static void logException(Exception ex) {
	   if (debug) {
	       ex.printStackTrace(stream);
	    }
	}
    
    public static void println(String text){
        if (debug) {
            if (proxyOutput!=null)
                writeFile(proxyOutput,proxyOutput,text,false);
            else 
                stream.println(text);
        }
    }
    
    public static void println(){
        if (debug) {
            if (proxyOutput!=null)
                writeFile(proxyOutput,proxyOutput,null,false);
            else stream.println();
        }
    }
    
    public static void print(String text){
        if (debug) {
            if (proxyOutput!=null)
                writeFile(proxyOutput,proxyOutput,text,true);
            else stream.print(text);
        }
    }
    
}

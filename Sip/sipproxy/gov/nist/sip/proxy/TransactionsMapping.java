package gov.nist.sip.proxy;

import java.util.*;
import javax.sip.*;
import javax.sip.message.*; 
import javax.sip.header.*;
import javax.sip.address.*;

import gov.nist.sip.proxy.registrar.*;

//ifdef SIMULATION
/*
import sim.java.net.*;
//endif
*/

/** A class that manages mapping of client to server transactions.
*
*@version  JAIN-SIP-1.1
*
*@author Olivier Deruelle <deruelle@nist.gov>  <br/>
*M. Ranganathan <mranga@nist.gov>  <br/>
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

class TransactionsMapping {
    private  Hashtable table;
    /** Creates new TransactionsTable */
    protected TransactionsMapping( ServerTransaction serverTransaction) {
	Dialog serverDialog = serverTransaction.getDialog();
        table= new Hashtable();
	serverDialog.setApplicationData(this);
    }


   protected Dialog getPeerDialog(ServerTransaction serverTransaction) {
	if (table.containsKey(serverTransaction)) {
		ClientTransaction ct = getClientTransaction(serverTransaction);
		if (ct != null) return ct.getDialog();
	} else {
	      Transaction transaction = 
			serverTransaction.getDialog().getFirstTransaction();
		if (transaction instanceof ServerTransaction) {
		    ClientTransaction ct = getClientTransaction
				((ServerTransaction) transaction);
		    if (ct != null) return ct.getDialog();
		} else {
		    ServerTransaction st = getServerTransaction
				((ClientTransaction) transaction);
		    if (st != null) return st.getDialog();
		}
	}
	return null;

  }

    
    protected ServerTransaction
    getServerTransaction(ClientTransaction clientTransaction) {
            // Retrieve the good value:
            Enumeration e=table.keys();
            
            while( e.hasMoreElements() ) {
                ServerTransaction serverTransaction=
                (ServerTransaction)e.nextElement();
                Vector vector=(Vector)table.get(serverTransaction);
                
                for (Enumeration en = vector.elements(); en.hasMoreElements();){
                    ClientTransaction ct = (ClientTransaction)en.nextElement();
                    if (ct == clientTransaction) {
                        return serverTransaction;
                    }
                }
            }
            return null;
    }
    
    protected  ClientTransaction
    getClientTransaction(ServerTransaction serverTransaction) {
            Vector vector=(Vector)table.get(serverTransaction);
            if (vector == null) return null;
            else {
                for (Enumeration e = vector.elements() ;
                e.hasMoreElements(); ) {
                    ClientTransaction ct =
                    (ClientTransaction) e.nextElement();
                    Dialog d = ct.getDialog();
                    if ( d.getState() != null &&
                    d.getState().equals(DialogState.CONFIRMED))
                        return ct;
                    else
                        if ( d.getState() == null) {
                            ProxyDebug.println
			     ("TransactionsMapping, getClientTransaction(),"+
                            " the dialog state is null.");
                        } else {
                            ProxyDebug.println
				("TransactionsMapping, getClientTransaction(),"+
                            " the dialog state is:"+d.getState().toString());
                        }
                }
                return null;
            }
    }

    protected  boolean hasMapping(ServerTransaction st) {
            if (! table.containsKey(st)) return false;
            else {
                // retrieve the mapping from the table and check if is empty.
                Vector vector = (Vector) table.get(st);
                return ! vector.isEmpty();
            }
    }
    
    protected Vector getClientTransactions
    (ServerTransaction serverTransaction) {
        if (serverTransaction==null) return null;
            return (Vector)table.get(serverTransaction);
    }

    protected  void
    addMapping
    (ServerTransaction serverTransaction,
    ClientTransaction clientTransaction) {
	if (    clientTransaction == null ||
		clientTransaction.getDialog() == null)  {
		return;
	}
        Vector clients=getClientTransactions(serverTransaction);
        Dialog dialog = serverTransaction.getDialog();
        TransactionsMapping map = 
		(TransactionsMapping) dialog.getApplicationData();
        Dialog clientDialog  = clientTransaction.getDialog();
        clientDialog.setApplicationData(map);
        if (clients==null) {
	     clients=new Vector();
             table.put(serverTransaction,clients);
             clients.addElement(clientTransaction);
	 } else {
             for ( Enumeration e = clients.elements() ; 
			e.hasMoreElements(); ) {
			// already exists so bail out.
			if (clientTransaction == e.nextElement()) return;
	       }
              clients.addElement(clientTransaction);
	      if (ProxyDebug.debug) printTransactionsMapping();
            }
    }
    
    protected  void removeMapping
    (ServerTransaction serverTransaction) {
         table.remove(serverTransaction);
        
    }

    protected  void removeMapping
    (ClientTransaction clientTransaction) {
            ServerTransaction serverTransaction =
            getServerTransaction(clientTransaction);
            Vector clientTransactions = 
		getClientTransactions(serverTransaction);
	   
           if (   clientTransactions != null &&
		  clientTransactions.isEmpty()) {
	          clientTransactions.removeElement(clientTransaction);
                  table.remove(serverTransaction);
           }
    }

    
    protected void printTransactionsMapping(){
        ProxyDebug.println
	("***********************************************");
	ProxyDebug.println("this = " + this);
        ProxyDebug.println("TRANSACTIONS TABLE: ");
        synchronized (table) {
            Enumeration e=table.keys();
            
            while( e.hasMoreElements() ) {
                ServerTransaction serverTransaction=
                (ServerTransaction)e.nextElement();
                ProxyDebug.println("- serverTransaction: "
                +serverTransaction+"\n"+
                "   - state: "+serverTransaction.getState()+"\n"+
                "   - dialog: "+serverTransaction.getDialog());
                if (serverTransaction.getDialog()!=null)
                    ProxyDebug.println ("   - dialog state: "
			+serverTransaction.getDialog().getState());
                
                Vector vector=(Vector)table.get(serverTransaction);
		if (vector != null) {
                 for (int i=0;i<vector.size();i++){
                    ClientTransaction clientTransac=
                    (ClientTransaction)vector.elementAt(i);
                    ProxyDebug.println
                    ("   - its clientTransaction: "+clientTransac+", state: "
			+serverTransaction.getState());
                 }
		}
            }
            ProxyDebug.println("********************************************");
            if (ProxyDebug.debug) ProxyDebug.println();
        }
    }
    
}

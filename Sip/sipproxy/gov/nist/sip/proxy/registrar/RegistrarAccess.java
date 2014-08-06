
/** An interface to fetch the registry contents as an XML formatted string
* from the proxy.
*/
package gov.nist.sip.proxy.registrar;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

public interface RegistrarAccess extends Remote {
    
        public String getRegistryXMLTags() throws RemoteException;
	// public String getRegistryContents() throws RemoteException;
	public Vector getRegistryBindings() throws RemoteException;
	public int getRegistrySize() throws RemoteException;
	// More query functions to be added.
}

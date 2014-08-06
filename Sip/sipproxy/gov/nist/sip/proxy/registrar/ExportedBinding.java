
package gov.nist.sip.proxy.registrar;

import java.util.*;
/** Exported binding.
* You can use RMI to get the bindings as an array of such values.
*/
public class ExportedBinding implements java.io.Serializable {
	protected String requestURI;
	protected String contactAddress;
	protected String fromAddress;
	protected String toAddress;
	protected long   expiryTime;
	protected String key;

	public String getRequestURI() { return this.requestURI; }
	public String getFromAddress() { return this.fromAddress; }
	public String getToAddress() { return this.toAddress; }
	public String getContactAddress() { return this.contactAddress; }
	public long   getExpiryTime() { return this.expiryTime; }
	public String getKey() { return this.key; }



}

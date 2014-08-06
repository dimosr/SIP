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

import net.java.stun4j.*;
import net.java.stun4j.client.*;
import java.net.InetAddress;

/**
 * The class is used to detect and output the type of the firewall and/or NAT
 * that the running host is behind (if any). It is only used for debugging
 * currently but in the future would help optimise network behavior.
 *
 * <p>Organisation: Network Research Team (LSIIT @ ULP)</p>
 * @author Emil Ivov
 * @version 0.1
 */
class NetworkDiagnostics
    extends Thread
{
    private Console console = Console.getConsole(NetworkDiagnostics.class);

    public NetworkDiagnostics()
    {
        setName("NetworkDiagnosticsThread");
    }

    public void run()
    {
        try
        {
            StunAddress localAddr = null;
            StunAddress serverAddr = null;
            localAddr = new StunAddress(InetAddress.getLocalHost(), 5678);
            serverAddr = new StunAddress("stun01bak.sipphone.com.", 3479);
            NetworkConfigurationDiscoveryProcess addressDiscovery =
                new NetworkConfigurationDiscoveryProcess(localAddr, serverAddr);
            addressDiscovery.start();
            StunDiscoveryReport report = addressDiscovery.determineAddress();
            if(console.isDebugEnabled())
            	console.debug("Result of NetworkDiagnosticts:\n"+report);
        }
        catch(Throwable exc)
        //catch everything
		//this is a diagnostic kit only so we don't want it
        //to spoil our actual application
        {
			console.error("The network diagnostics process has failed with "
                          +"the following exception", exc);
        }
    }

}

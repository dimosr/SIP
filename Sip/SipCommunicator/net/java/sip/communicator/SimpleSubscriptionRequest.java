package net.java.sip.communicator;

import net.java.sip.communicator.gui.imp.SubscriptionRequestUIModel;
import java.util.Enumeration;

/**
 * <p>Title: SIP Communicator</p>
 * <p>Description: A SIP UA</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Network Research Team, Louis Pasteur University</p>
 * @author Emil Ivov
 * @version 1.0
 */

public class SimpleSubscriptionRequest
    implements SubscriptionRequestUIModel
{

    private String   displayName       = null;
    private String   address           = null;
    private String   message           = null;
    private String[] acceptedResponses = null;

    public SimpleSubscriptionRequest(String   displayName,
                               String   address,
                               String   reasonPhrase,
                               String[] acceptedResponses )
    {
        this.displayName = displayName;
        this.address = address;
        this.message = reasonPhrase;
        this.acceptedResponses = acceptedResponses;
    }

    /**
     * Returns an Enumeration of Strings accepted as a response to this request.
     *
     * @return a java.util.Enumeration of String objects accepted as responses
     *   to this request.
     * @todo Implement this
     *   net.java.sip.communicator.gui.imp.SubscriptionRequestUIModel method
     */
    public String[] getAcceptedResponses()
    {
        return acceptedResponses;
    }

    /**
     * Returns a display name (alias) for the remote party requesting the
     * subscription authorization.
     *
     * @return a String object containing a descriptive (display) name for the
     *   entity requestion the authorization
     * @todo Implement this
     *   net.java.sip.communicator.gui.imp.SubscriptionRequestUIModel method
     */
    public String getRequestingPartyDisplayName()
    {
        return displayName;
    }

    /**
     * Returns the address of the party that has requested authorization.
     *
     * @return a String object containing the address of the entity that has
     *   requested subscription authorization.
     * @todo Implement this
     *   net.java.sip.communicator.gui.imp.SubscriptionRequestUIModel method
     */
    public String getRequestingPartyAddress()
    {
        return address;
    }

    /**
     * @return a String object containing a human readable message giving the
     * reason for the request.
     *
     * @return a String object containing a human readable message giving the
     *   reason for the request.
     * @todo Implement this
     *   net.java.sip.communicator.gui.imp.SubscriptionRequestUIModel method
     */
    public String getReasonPhrase()
    {
        return message;
    }

}

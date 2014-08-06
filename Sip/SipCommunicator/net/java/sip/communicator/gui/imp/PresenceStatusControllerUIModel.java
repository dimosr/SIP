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
package net.java.sip.communicator.gui.imp;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import javax.swing.event.*;
import java.awt.Component;
import net.java.sip.communicator.common.*;

/**
 * <p>Title: SIP Communicator</p>
 * <p>Description: A SIP UA</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Network Research Team, Louis Pasteur University</p>
 * @author Emil Ivov
 * @version 1.0
 */

public abstract class PresenceStatusControllerUIModel
    implements ComboBoxModel, ListCellRenderer
{
    protected EventListenerList listenerList = new EventListenerList();
    protected ListCellRenderer  listCellRenderer = new DefaultListCellRenderer();

    public PresenceStatusControllerUIModel()
    {
    }

    /**
     * Returns the value at the specified index.
     *
     * @param index the requested index
     * @return the value at <code>index</code>
     */
    public Object getElementAt(int index)
    {
        return getStatusAt(index);
    }

    /**
     * Returns the value of the presence status corresponding to the specified
     * index.
     *
     * @param index the requested index
     * @return the value at <code>index</code>
     */
    public abstract Object getStatusAt(int index);

    /**
     * Returns the length of the list.
     *
     * @return the length of the list
     */
    public int getSize()
    {
        return getStatusCount();
    }

    /**
     * Returns the size of the supported stuatus set.
     *
     * @return the size of the supported stuatus set
     */
    public abstract int getStatusCount();

    /**
     * Returns the selected item or <code>null</code> if there is no selection
     * @return The selected item or <code>null</code> if there is no selection
     */
    public Object getSelectedItem()
    {
        return getCurrentPresenceStatus();
    }

    /**
     * Returns the currently active Presence Status of SipCommunicator
     * @return the currently active Presence Status of SipCommunicator
     */
    public abstract Object getCurrentPresenceStatus();

    /**
     * Set the selected item.
     *
     * @param anItem the list object to select or <code>null</code> to clear the
     *   selection
     */
    public void setSelectedItem(Object anItem)
    {
        requestStatusChange(anItem.toString());
    }

    /**
     * Requests the underlying presence stack to change the current status
     * to <code>newStatus</code>.
     *
     * @param anItem the list object to select or <code>null</code> to clear the
     *   selection
     */
    public abstract void requestStatusChange(String newStatus);




    //--------------------- LISTENERS -----------------------------
    /**
     * <code>AbstractListModel</code> subclasses must call this method
     * <b>after</b>
     * one or more elements of the list change.  The changed elements
     * are specified by the closed interval index0, index1 -- the endpoints
     * are included.  Note that
     * index0 need not be less than or equal to index1.
     *
     * @param source the <code>ListModel</code> that changed, typically "this"
     * @param index0 one end of the new interval
     * @param index1 the other end of the new interval
     * @see EventListenerList
     * @see DefaultListModel
     */
    protected void fireContentsChanged(Object source, int index0, int index1)
    {
        Object[] listeners = listenerList.getListenerList();
        ListDataEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ListDataListener.class) {
                if (e == null) {
                    e = new ListDataEvent(source,
                                          ListDataEvent.CONTENTS_CHANGED,
                                          index0, index1);
                }
                ( (ListDataListener) listeners[i + 1]).contentsChanged(e);
            }
        }
    }

    /**
     * <code>AbstractListModel</code> subclasses must call this method
     * <b>after</b>
     * one or more elements are added to the model.  The new elements
     * are specified by a closed interval index0, index1 -- the enpoints
     * are included.  Note that
     * index0 need not be less than or equal to index1.
     *
     * @param source the <code>ListModel</code> that changed, typically "this"
     * @param index0 one end of the new interval
     * @param index1 the other end of the new interval
     * @see EventListenerList
     * @see DefaultListModel
     */
    protected void fireIntervalAdded(Object source, int index0, int index1)
    {
        Object[] listeners = listenerList.getListenerList();
        ListDataEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ListDataListener.class) {
                if (e == null) {
                    e = new ListDataEvent(source, ListDataEvent.INTERVAL_ADDED,
                                          index0, index1);
                }
                ( (ListDataListener) listeners[i + 1]).intervalAdded(e);
            }
        }
    }

    /**
     * <code>AbstractListModel</code> subclasses must call this method
     * <b>after</b> one or more elements are removed from the model.
     * The new elements
     * are specified by a closed interval index0, index1, i.e. the
     * range that includes both index0 and index1.  Note that
     * index0 need not be less than or equal to index1.
     *
     * @param source the ListModel that changed, typically "this"
     * @param index0 one end of the new interval
     * @param index1 the other end of the new interval
     * @see EventListenerList
     * @see DefaultListModel
     */
    protected void fireIntervalRemoved(Object source, int index0, int index1)
    {
        Object[] listeners = listenerList.getListenerList();
        ListDataEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ListDataListener.class) {
                if (e == null) {
                    e = new ListDataEvent(source,
                                          ListDataEvent.INTERVAL_REMOVED,
                                          index0, index1);
                }
                ( (ListDataListener) listeners[i + 1]).intervalRemoved(e);
            }
        }
    }

    /**
     * Adds a listener to the list that's notified each time a change
     * to the data model occurs.
     *
     * @param l the <code>ListDataListener</code> to be added
     */
    public void addListDataListener(ListDataListener l)
    {
        listenerList.add(ListDataListener.class, l);
    }

    /**
     * Removes a listener from the list that's notified each time a
     * change to the data model occurs.
     *
     * @param l the <code>ListDataListener</code> to be removed
     */
    public void removeListDataListener(ListDataListener l)
    {
        listenerList.remove(ListDataListener.class, l);
    }


    //-------------------- List Cell Renderer ----------------------------------
    /**
     * Return a component that has been configured to display the specified value.
     *
     * @param list The JList we're painting.
     * @param value The value returned by list.getModel().getElementAt(index).
     * @param index The cells index.
     * @param isSelected True if the specified cell was selected.
     * @param cellHasFocus True if the specified cell has the focus.
     * @return A component whose paint() method will render the specified value.
     */
    public Component getListCellRendererComponent(JList list,  Object  value,
                                                  int   index, boolean isSelected,
                                                  boolean cellHasFocus)
    {
        ((JLabel)listCellRenderer).setText(value.toString());
        ((JLabel)listCellRenderer).setIcon(new ImageIcon(Utils.getResource("sip-communicator-16x16.jpg")));
        //Get the selected index. (The index param isn't
           //always valid, so just use the value.)
        //int selectedIndex = ((Integer)value).intValue();

       if (isSelected) {
           ((JLabel)listCellRenderer).setBackground(list.getSelectionBackground());
           ((JLabel)listCellRenderer).setForeground(list.getSelectionForeground());
       } else {
           ((JLabel)listCellRenderer).setBackground(list.getBackground());
           ((JLabel)listCellRenderer).setForeground(list.getForeground());
       }

        return (Component)listCellRenderer;
    }



}

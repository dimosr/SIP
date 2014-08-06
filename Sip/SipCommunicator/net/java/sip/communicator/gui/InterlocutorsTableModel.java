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
package net.java.sip.communicator.gui;

import java.util.*;
import javax.swing.table.*;

/**
 * <p>Title: SIP COMMUNICATOR</p>
 * <p>Description:JAIN-SIP Audio/Video phone application</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: LSIIT laboratory (http://lsiit.u-strasbg.fr) </p>
 * <p>Network Research Team (http://www-r2.u-strasbg.fr))</p>
 * <p>Louis Pasteur University - Strasbourg - France</p>
 * @author Emil Ivov (http://www.emcho.com)
 * @version 1.1
 *
 */
class InterlocutorsTableModel
    extends AbstractTableModel
{
    private static final int NAME_COLUMN_INDEX = 0;
    private static final int ADDRESS_COLUMN_INDEX = 1;
    private static final int CALL_STATUS_COLUMN_INDEX = 2;
    private final String[] columnNames = {
        "Name",
        "Address",
        "Call Status"};
    private Vector interlocutors = new Vector();
    public int getColumnCount()
    {
        return columnNames.length;
    }

    public int getRowCount()
    {
        return interlocutors.size();
    }

    public String getColumnName(int col)
    {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col)
    {
        Object res;
        if (row >= interlocutors.size()) {
            return "";
        }
        InterlocutorUI interlocutor = (InterlocutorUI) interlocutors.get(row);
        switch (col) {
            case NAME_COLUMN_INDEX:
                res = interlocutor.getName();
                break;
            case ADDRESS_COLUMN_INDEX:
                res = interlocutor.getAddress();
                break;
            case CALL_STATUS_COLUMN_INDEX:
                res = interlocutor.getCallState();
                break;
            default:
                throw new IndexOutOfBoundsException("There is no column " +
                    new Integer(col).toString());
        }
        return res == null ? "" : res;
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c)
    {
        return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col)
    {
        return false;
    }

    private void printDebugData()
    {
        int numRows = getRowCount();
        int numCols = getColumnCount();
        for (int i = 0; i < numRows; i++) {
            System.out.print("    row " + i + ":");
            for (int j = 0; j < numCols; j++) {
                System.out.print("  " + getValueAt(i, j));
            }
            System.out.println();
        }
        System.out.println("--------------------------");
    }

    void addInterlocutor(InterlocutorUI interlocutor)
    {
        interlocutors.addElement(interlocutor);
        fireTableRowsInserted(interlocutors.size() - 1,
                              interlocutors.size() - 1);
    }

    void removeInterlocutor(int id)
    {
        for (int i = 0; i < interlocutors.size(); i++) {
            if ( ( (InterlocutorUI) interlocutors.get(i)).getID() == id) {
                interlocutors.removeElementAt(i);
                fireTableRowsDeleted(i, i);
            }
        }
    }

    InterlocutorUI getInterlocutorAt(int row)
    {
        return ( (InterlocutorUI) interlocutors.get(row));
    }

    private void updateInterlocutorStatus(int interID)
    {
        int index = findIndex(interID);
        fireTableRowsUpdated(index, index);
    }

    int findIndex(int id)
    {
        for (int i = 0; i < interlocutors.size(); i++) {
            if ( ( (InterlocutorUI) interlocutors.get(i)).getID() == id) {
                return i;
            }
        }
        return -1;
    }

//----------------------------- GUI Callback ---------------------------
    public void update(InterlocutorUI interlocutorUI)
    {
        updateInterlocutorStatus(interlocutorUI.getID());
    }

    public void remove(InterlocutorUI interlocutorUI)
    {
        removeInterlocutor(interlocutorUI.getID());
    }
}
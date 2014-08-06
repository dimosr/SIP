package net.java.sip.communicator.plugin.setup;

import java.util.*;

/**
 * <p>Title: SIP Communicator</p>
 * <p>Description: A SIP UA</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Network Research Team, Louis Pasteur University</p>
 * @author Emil Ivov
 * @version 1.0
 */

public class WizardPropertySet
{
    private ArrayList propertyList = new ArrayList();


    public class WizardProperty
    {
        String propertyHrName = null;
        String propertyName = null;
        String propertyValue = null;
    }

    WizardProperty findProperty(String propertyName)
    {
        for(int i =0; i < propertyList.size(); i++)
        {
            WizardProperty prop = (WizardProperty)propertyList.get(i);
            if(prop != null && prop.propertyName.equals(propertyName))
                return prop;
        }

        return null;
    }

    void setProperty(String propertyName,
                     String humanReadableName,
                     String value)
    {
        WizardProperty prop = findProperty(propertyName);

        if(prop == null)
        {
            prop = new WizardProperty();
            propertyList.add(prop);
        }

        prop.propertyName = propertyName;
        prop.propertyHrName = humanReadableName;
        prop.propertyValue = value;
    }

    WizardProperty getPropertyAt(int index)
    {
        return (WizardProperty)propertyList.get(index);
    }

    int getPropertyCount()
    {
        return propertyList.size();
    }

}

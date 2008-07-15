/**
 * <p>Title: CellSpecimen Class>
 * <p>Description:  A biospecimen composed of�purified single cells not in the 
 * context of a tissue or other biospecimen fluid.</p>
 * Copyright:    Copyright (c) year
 * Company: Washington University, School of Medicine, St. Louis.
 * @author Gautam Shetty
 * @version 1.00
 */

package edu.wustl.catissuecore.domain;

import java.io.Serializable;

import edu.wustl.common.actionForm.AbstractActionForm;
import edu.wustl.common.actionForm.IValueObject;
import edu.wustl.common.util.logger.Logger;

/**
 * A biospecimen composed of�purified single cells not in the 
 * context of a tissue or other biospecimen fluid.
 * @hibernate.subclass name="CellSpecimen" discriminator-value = "Cell"
 */
public class CellSpecimen extends Specimen implements Serializable
{
    private static final long serialVersionUID = 1234567890L;

    public CellSpecimen()
    {
    	
    }
    
    public CellSpecimen(AbstractActionForm form)
    {
    	setAllValues(form);
    }

    /**
     * This function Copies the data from an NewSpecimenForm object to a CellSpecimen object.
     * @param siteForm An SiteForm object containing the information about the site.  
     * */
    public void setAllValues(IValueObject abstractForm)
    {
        try
        {
        	super.setAllValues(abstractForm);
        }
        catch (Exception excp)
        {
            Logger.out.error(excp.getMessage(),excp);
        }
    }
    
    public CellSpecimen(SpecimenRequirement cellReqSpecimen)
    {
    	super(cellReqSpecimen);
    }
}
package edu.wustl.catissuecore.actionForm;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import edu.wustl.catissuecore.bean.CollectionProtocolEventBean;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.actionForm.AbstractActionForm;
import edu.wustl.common.cde.CDEManager;
import edu.wustl.common.domain.AbstractDomainObject;
import edu.wustl.common.util.dbManager.DAOException;
import edu.wustl.common.util.global.ApplicationProperties;
import edu.wustl.common.util.global.Validator;
import edu.wustl.common.util.logger.Logger;

 
 
public class ProtocolEventDetailsForm extends AbstractActionForm
{
	
	private String clinicalDiagnosis;
    
	private String clinicalStatus;
	
	/**
	 * Defines the required collectionPointLabel.
	 */
	protected String collectionPointLabel;

	/**
	 * Defines the relative time point in days, with respect to the registration date of participant on this protocol, when the specimen should be collected from participant.
	 */
	protected Double studyCalendarEventPoint=1D;
	
	protected String collectionProtocolEventkey;
	/**
	 * Event Attributes
	 */
	private long collectionEventId;																											// Mandar : CollectionEvent 10-July-06
	private long collectionEventSpecimenId;
	private long collectionEventUserId;
	private String collectionEventCollectionProcedure;
	private String collectionEventContainer;
	private String collectionEventComments = "";
	
	
	private long receivedEventId;
	private long receivedEventSpecimenId;
	private long receivedEventUserId;
	private String receivedEventReceivedQuality;
	private String receivedEventComments = "";
	@Override
	public int getFormId()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void reset()
	{
		// TODO Auto-generated method stub
		
	}

	public void setAllValues(AbstractDomainObject arg0)
	{
		// TODO Auto-generated method stub
		
	}

	
	public String getClinicalDiagnosis()
	{
		return clinicalDiagnosis;
	}

	
	public void setClinicalDiagnosis(String clinicalDiagnosis)
	{
		this.clinicalDiagnosis = clinicalDiagnosis;
	}

	
	public String getClinicalStatus()
	{
		return clinicalStatus;
	}

	
	public void setClinicalStatus(String clinicalStatus)
	{
		this.clinicalStatus = clinicalStatus;
	}

	
	public String getCollectionPointLabel()
	{
		return collectionPointLabel;
	}

	
	public void setCollectionPointLabel(String collectionPointLabel)
	{
		this.collectionPointLabel = collectionPointLabel;
	}

	
	public Double getStudyCalendarEventPoint()
	{
		return studyCalendarEventPoint;
	}

	
	public void setStudyCalendarEventPoint(Double studyCalendarEventPoint)
	{
		this.studyCalendarEventPoint = studyCalendarEventPoint;
	}

	
	public long getCollectionEventId()
	{
		return collectionEventId;
	}

	
	public void setCollectionEventId(long collectionEventId)
	{
		this.collectionEventId = collectionEventId;
	}

	
	public long getCollectionEventSpecimenId()
	{
		return collectionEventSpecimenId;
	}

	
	public void setCollectionEventSpecimenId(long collectionEventSpecimenId)
	{
		this.collectionEventSpecimenId = collectionEventSpecimenId;
	}

	
	public long getCollectionEventUserId()
	{
		return collectionEventUserId;
	}

	
	public void setCollectionEventUserId(long collectionEventUserId)
	{
		this.collectionEventUserId = collectionEventUserId;
	}

	
	public String getCollectionEventCollectionProcedure()
	{
		return collectionEventCollectionProcedure;
	}

	
	public void setCollectionEventCollectionProcedure(String collectionEventCollectionProcedure)
	{
		this.collectionEventCollectionProcedure = collectionEventCollectionProcedure;
	}

	
	public String getCollectionEventContainer()
	{
		return collectionEventContainer;
	}

	
	public void setCollectionEventContainer(String collectionEventContainer)
	{
		this.collectionEventContainer = collectionEventContainer;
	}

	
	public String getCollectionEventComments()
	{
		return collectionEventComments;
	}

	
	public void setCollectionEventComments(String collectionEventComments)
	{
		this.collectionEventComments = collectionEventComments;
	}

	
	public long getReceivedEventId()
	{
		return receivedEventId;
	}

	
	public void setReceivedEventId(long receivedEventId)
	{
		this.receivedEventId = receivedEventId;
	}

	
	public long getReceivedEventSpecimenId()
	{
		return receivedEventSpecimenId;
	}

	
	public void setReceivedEventSpecimenId(long receivedEventSpecimenId)
	{
		this.receivedEventSpecimenId = receivedEventSpecimenId;
	}

	
	public long getReceivedEventUserId()
	{
		return receivedEventUserId;
	}

	
	public void setReceivedEventUserId(long receivedEventUserId)
	{
		this.receivedEventUserId = receivedEventUserId;
	}

	
	public String getReceivedEventReceivedQuality()
	{
		return receivedEventReceivedQuality;
	}

	
	public void setReceivedEventReceivedQuality(String receivedEventReceivedQuality)
	{
		this.receivedEventReceivedQuality = receivedEventReceivedQuality;
	}

	
	public String getReceivedEventComments()
	{
		return receivedEventComments;
	}

	
	public void setReceivedEventComments(String receivedEventComments)
	{
		this.receivedEventComments = receivedEventComments;
	}
	
	public String getCollectionProtocolEventkey()
	{
		return collectionProtocolEventkey;
	}

	
	public void setCollectionProtocolEventkey(String collectionProtocolEventkey)
	{
		this.collectionProtocolEventkey = collectionProtocolEventkey;
	}
	
	/**
	 * Overrides the validate method of ActionForm.
	 * @return error ActionErrors instance
	 * @param mapping Actionmapping instance
	 * @param request HttpServletRequest instance
	 */
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request)
	{
		ActionErrors errors = new ActionErrors();
		Validator validator = new Validator();
		try
		{
				setRedirectValue(validator );
				HttpSession session = request.getSession();
				Map collectionProtocolEventMap = (Map)session.getAttribute("collectionProtocolEventMap");
				if(collectionProtocolEventMap!=null)
				{
					Collection collectionProtocolEventBeanCollection = (Collection)collectionProtocolEventMap.values();
					Iterator collectionProtocolEventBeanCollectionItr = collectionProtocolEventBeanCollection.iterator();
					while(collectionProtocolEventBeanCollectionItr.hasNext())
					{
						CollectionProtocolEventBean collectionProtocolEventBean = (CollectionProtocolEventBean)collectionProtocolEventBeanCollectionItr.next();
						String collectionPointLabel = collectionProtocolEventBean.getCollectionPointLabel();
						if(!collectionProtocolEventBean.getUniqueIdentifier().equals(this.collectionProtocolEventkey))
						{
								if(this.getCollectionPointLabel().equals(collectionPointLabel))
								{
									errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.unique",
											ApplicationProperties.getValue("collectionprotocol.collectionpointlabel")));
									break;
								}
						}
					}
				}
				 
				 double dblValue = Double.parseDouble(this.studyCalendarEventPoint.toString());
				 if (Double.isNaN(dblValue)) 
		         {
					 errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.invalid",
								ApplicationProperties.getValue("collectionprotocol.studycalendartitle")));
		         }
				if(validator.isEmpty(this.collectionPointLabel.toString()))
				{
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",
									ApplicationProperties.getValue("collectionprotocol.collectionpointlabel")));
				}
				
				// Mandatory Field : clinical Diagnosis
				if(!validator.isValidOption(this.clinicalDiagnosis))
				{
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.selected",
									ApplicationProperties.getValue("specimenCollectionGroup.clinicalDiagnosis")));
				}
				
				// Mandatory Field : clinical Status
				if(!validator.isValidOption(clinicalStatus))
				{
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.selected",
									ApplicationProperties.getValue("specimenCollectionGroup.clinicalStatus")));
				}
				/*
				 Commented by Virender
				if ((collectionEventUserId) == -1L)
		        {
		       		errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required","Collection Event's user"));
		        }
				
				// checks the collectionProcedure
				if (!validator.isValidOption(this.getCollectionEventCollectionProcedure()))
				{
					String message = ApplicationProperties.getValue("collectioneventparameters.collectionprocedure");
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",message));
				}			
				
				List procedureList = CDEManager.getCDEManager().getPermissibleValueList(Constants.CDE_NAME_COLLECTION_PROCEDURE, null);
				if (!Validator.isEnumeratedValue(procedureList, this.getCollectionEventCollectionProcedure()))
				{
					String message = ApplicationProperties.getValue("cpbasedentry.collectionprocedure");
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.invalid",message));
				}
				//Container validation
				if (!validator.isValidOption(this.getCollectionEventContainer()))
				{
					String message = ApplicationProperties.getValue("collectioneventparameters.container");
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",message));
				}
				List containerList = CDEManager.getCDEManager().getPermissibleValueList(Constants.CDE_NAME_CONTAINER, null);
				if (!Validator.isEnumeratedValue(containerList, this.getCollectionEventContainer()))
				{
					String message = ApplicationProperties.getValue("collectioneventparameters.container");
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.invalid",message));
				}
				if ((receivedEventUserId) == -1L)
		        {
		       		errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required","Received Event's user"));
		        }			
				List qualityList = CDEManager.getCDEManager().getPermissibleValueList(Constants.CDE_NAME_RECEIVED_QUALITY, null);
				if (!Validator.isEnumeratedValue(qualityList, this.receivedEventReceivedQuality))
				{
					String message = ApplicationProperties.getValue("cpbasedentry.receivedquality");
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.invalid",message));
					
				}*/
		
		}
		catch (Exception excp)
		{
	    	// use of logger as per bug 79
	    	Logger.out.error(excp.getMessage(),excp); 
			errors = new ActionErrors();
		}
		return errors;
	}

}

/**
 * <p>Title: SpecimenCollectionGroupForm Class>
 * <p>Description:  SpecimenCollectionGroupForm Class is used to encapsulate 
 * all the request parameters passed from New SpecimenCollectionGroup webpage. </p>
 * Copyright:    Copyright (c) year
 * Company: Washington University, School of Medicine, St. Louis.
 * @author Ajay Sharma
 * @version 1.00
 */

package edu.wustl.catissuecore.actionForm;	  
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import edu.wustl.catissuecore.domain.CollectionEventParameters;
import edu.wustl.catissuecore.domain.Participant;
import edu.wustl.catissuecore.domain.ReceivedEventParameters;
import edu.wustl.catissuecore.domain.Site;
import edu.wustl.catissuecore.domain.SpecimenCollectionGroup;
import edu.wustl.catissuecore.domain.User;
import edu.wustl.catissuecore.util.EventsUtil;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.catissuecore.util.global.DefaultValueManager;
import edu.wustl.catissuecore.util.global.Utility;
import edu.wustl.common.actionForm.AbstractActionForm;
import edu.wustl.common.domain.AbstractDomainObject;
import edu.wustl.common.util.global.ApplicationProperties;
import edu.wustl.common.util.global.Validator;
import edu.wustl.common.util.logger.Logger;

/**
 * SpecimenCollectionGroupForm Class is used to encapsulate 
 * all the request parameters passed from New SpecimenCollectionGroup webpage.
 * @author ajay_sharma
 */
public class SpecimenCollectionGroupForm extends AbstractActionForm implements ConsentTierData
{
	/**
     * Name : Virender Mehta
     * Reviewer: Sachin Lale
     * Bug ID: defaultValueConfiguration_BugID
     * Patch ID:defaultValueConfiguration_BugID_7
     * Description: Configuration for default value for clinicalDiagnosis and clinicalStatus
     *
     */
	private String clinicalDiagnosis = (String)DefaultValueManager.getDefaultValue(Constants.DEFAULT_CLINICAL_DIAGNOSIS);
    
	private String clinicalStatus = (String)DefaultValueManager.getDefaultValue(Constants.DEFAULT_CLINICAL_STATUS);
	
	private String surgicalPathologyNumber;
	
	private long participantsMedicalIdentifierId;
	
	   /**
	 * Name : Ashish Gupta
	 * Reviewer Name : Sachin Lale 
	 * Bug ID: 2741
	 * Patch ID: 2741_9	 
	 * Description: Event Attributes
	*/
	private long collectionEventId;																											// Mandar : CollectionEvent 10-July-06
	private long collectionEventSpecimenId;
	private long collectionEventUserId;
	private String collectionEventdateOfEvent;
	private String collectionEventTimeInHours;
	private String collectionEventTimeInMinutes;
	private String collectionEventCollectionProcedure;
	private String collectionEventContainer;
	private String collectionEventComments = "";
	
	
	private long receivedEventId;
	private long receivedEventSpecimenId;
	private long receivedEventUserId;
	private String receivedEventDateOfEvent;
	private String receivedEventTimeInHours;
	private String receivedEventTimeInMinutes;
	private String receivedEventReceivedQuality;
	private String receivedEventComments = "";
	

	
	/**
	 * An id which refers to the site of the container if it is parent container.
	 */
	private long siteId;
	
	private long  collectionProtocolId;
	
	private long collectionProtocolEventId;
		
	/**
	 * Nmae: Vijay Pande
	 * Reviewer Name: Aarti Sharma
	 * Name of the variable changed from checkedButton to radionButton since this name was conflicting with the same name used on specimen page and creating problem (Wrong value was set) in CP based view
	 * Please check all the references of the variable radioButtonForParticipant
	 */
	/**
     * Radio button to choose participantName/participantNumber.
     */
    private int radioButtonForParticipant = 1;
//Consent Tracking Module Virender Mehta
	/**
	 * Map for Storing responses for Consent Tiers.
	 */
	protected Map consentResponseForScgValues = new HashMap();
	/**
	 * No of Consent Tier
	 */
	private int consentTierCounter=0;
	/**
	 * Signed Consent URL
	 */
	protected String signedConsentUrl="";
	/**
	 * Witness name that may be PI
	 */
	protected String witnessName;

	/**
	 * Consent Date, Date on which Consent is Signed
	 */
	protected String consentDate="";
	
	/**
	 * This will be set in case of withdrawl popup
	 */
	protected String withdrawlButtonStatus = Constants.WITHDRAW_RESPONSE_NOACTION;
	/**
	 * This will be set in case if there is any change in response.
	 */
	protected String applyChangesTo= Constants.APPLY_NONE;
	/**
	 * If user changes the response after submiting response then this string will have 
	 * responseKeys for which response is changed .
	 */
	protected String stringOfResponseKeys="";
	
//Consent Tracking Module Virender Mehta 

    
	/**
     * unique name for Specimen Collection Group 
     */
    private String name ;

	/**For Migration Start**/	
    /**
	 * participantName 
     */
    private String participantName;
    
	private long participantId;
	
	private String protocolParticipantIdentifier;
	
	//For AddNew functionality
	private long collectionProtocolRegistrationId;
	/**
	 * Name : Ashish Gupta
	 * Reviewer Name : Sachin Lale 
	 * Bug ID: 2741
	 * Patch ID: 2741_4	 
	 * Description: Attribute to set events in specimens associated with this scg
	*/
	private boolean applyEventsToSpecimens = false;
	/**
	 * Name : Falguni Sachde
	 *  
	 * 
	 * 	 
	 * Description: Attribute to set Collection Protocol Short name to associated with this scg
	*/
    private String collectionProtocolName;
    /**
	 * Name : Falguni Sachde
	 *  
	 * 
	 * 	 
	 * Description: Attribute to set Participant Name concatenated with Participant Identifier.
	*/
    private String participantNameWithProtocolId;
	
    /**
     * Comments given by user.
     */
    
    /**
     * Name: Shital Lawhale
     * Reviewer Name : Sachin Lale 
     * Bug ID: 3052
     * Patch ID: 3052_1_2
     * See also: 1_1 to 1_5
     * Description : A comment field at the Specimen Collection Group.
     */
    private String comment;
        
	/**
	 * Name : Ashish Gupta
	 * Reviewer Name : Sachin Lale 
	 * Bug ID: Multiple Specimen Bug
	 * Patch ID: Multiple Specimen Bug_3 
	 * See also: 1-8
	 * Description: number of specimens field on scg form
	*/
	private int numberOfSpecimens;
	
	/**
	 * Name: Chetan Patil
	 * Reviewer: Sachin Lale
	 * Bug ID: Bug#4227
	 * Patch ID: Bug#4227_1
	 * Description: buttonType stores the id of the button only if button for Add Multiple Specimen
	 * is clicked. If the value of this varaiable is null then the validation of number of specimen
	 * against actual number of specimen requirements is skipped. 
	 */
	private String buttonType;
	
	private String collectionStatus;
	/**
	 * @return the buttonType
	 */
	public String getButtonType()
	{
		return buttonType;
	}



	/**
	 * @param buttonType the buttonType to set
	 */
	public void setButtonType(String buttonType) 
	{
		this.buttonType = buttonType;
	}



	/**
	 * @return the numberOfSpecimens
	 */
	public int getNumberOfSpecimens()
	{
		return numberOfSpecimens;
	}


	
	/**
	 * @param numberOfSpecimens the numberOfSpecimens to set
	 */
	public void setNumberOfSpecimens(int numberOfSpecimens)
	{
		this.numberOfSpecimens = numberOfSpecimens;
	}


	/**
	 * No argument constructor for SpecimenCollectionGroupForm class 
	 */
	public SpecimenCollectionGroupForm()
	{
		reset();
	}
	
   
    /**
     * @return Returns the clinicalDiagnosis.
     */
    public String getClinicalDiagnosis()
    {
        return clinicalDiagnosis;
    }
    /**
     * @param cinicalDiagnosis The clinicalDiagnosis to set.
     */
    public void setClinicalDiagnosis(String cinicalDiagnosis)
    {
        this.clinicalDiagnosis = cinicalDiagnosis;
    }
           
	/**
	 * @return Returns the name.
	 */
	public String getName() 
	{
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name)
	{
		this.name = name;
	}
    /**
     * @return Returns the surgicalPathologyNumber.
     */
    public String getSurgicalPathologyNumber()
    {
        return surgicalPathologyNumber;
    }
    /**
     * @param surgicalPathologyNumber The surgicalPathologyNumber to set.
     */
    public void setSurgicalPathologyNumber(String surgicalPathologyNumber)
    {
        this.surgicalPathologyNumber = surgicalPathologyNumber;
    }
  
		
	/**
	 * @return participantId
	 */
	public long getParticipantId() 
	{
		return participantId;
	}

	/**
	 * @param participantId Setting participant id
	 */
	public void setParticipantId(long participantId) 
	{
		this.participantId = participantId;
	}

	/**
	 * For AddNew functionality
	 * @return collectionProtocolRegistrationId
	 */
	public long getCollectionProtocolRegistrationId()
	{
	    return this.collectionProtocolRegistrationId;
	}
	/**
	 * @param collectionProtocolRegistrationId Setting Collection Prot reg id
	 */
	public void setCollectionProtocolRegistrationId(long collectionProtocolRegistrationId)
	{
	    this.collectionProtocolRegistrationId = collectionProtocolRegistrationId;
	}
	
	/**
	 * @return Returns the radioButtonForParticipant value.
	 */
	public int getRadioButtonForParticipant()
	{
		return radioButtonForParticipant;
	}

	/**
	 * @param radioButton The radioButtonForParticipant to set.
	 */
	public void setRadioButtonForParticipant(int radioButton)
	{
			if(isMutable())
			{
				this.radioButtonForParticipant = radioButton;
			}
	}

	/**
	 * Resets the values of all the fields.
	 * Is called by the overridden reset method defined in ActionForm.  
	 * */
	protected void reset()
	{
//		this.clinicalDiagnosis = null;
//	    
//		this.clinicalStatus = null;;
//		
//		this.surgicalPathologyNumber = null;
//		
//		this.protocolParticipantIdentifier =  null;
//		radioButtonForParticipant = 1;
	}
	/**
	   * This function Copies the data from an storage type object to a StorageTypeForm object.
	   * @param abstractDomain A StorageType object containing the information about storage type of the container.  
	   */
	public void setAllValues(AbstractDomainObject abstractDomain)
	{
//		if(operation.equals("add" ) )
//			setMutable(true );
			
		SpecimenCollectionGroup specimenCollectionGroup = (SpecimenCollectionGroup) abstractDomain;
			
		id = specimenCollectionGroup.getId().longValue();
		name =  specimenCollectionGroup.getName();    
		Logger.out.debug("specimenCollectionGroup.getClinicalDiagnosis() "+specimenCollectionGroup.getClinicalDiagnosis());
		clinicalDiagnosis = Utility.toString(specimenCollectionGroup.getClinicalDiagnosis());
		clinicalStatus = Utility.toString(specimenCollectionGroup.getClinicalStatus());
		activityStatus = Utility.toString(specimenCollectionGroup.getActivityStatus());
		collectionStatus = Utility.toString(specimenCollectionGroup.getCollectionStatus());
		surgicalPathologyNumber = Utility.toString(specimenCollectionGroup.getSurgicalPathologyNumber());
         /**
         * Name: Shital Lawhale
         * Reviewer Name : Sachin Lale 
         * Bug ID: 3052
         * Patch ID: 3052_1_4
         * See also: 1_1 to 1_5
         * Description : Get comment field from database and set it to form bean.
         */  
        comment = Utility.toString(specimenCollectionGroup.getComment());
			
////		ClinicalReport clinicalReport = specimenCollectionGroup.getClinicalReport();
//		surgicalPathologyNumber = Utility.toString(clinicalReport.getSurgicalPathologyNumber());
//		
//		if(clinicalReport.getParticipantMedicalIdentifier()!=null)
//		{
//			participantsMedicalIdentifierId = clinicalReport.getParticipantMedicalIdentifier().getId().longValue();
//		}
			
		collectionProtocolId = specimenCollectionGroup.getCollectionProtocolRegistration().getCollectionProtocol().getId().longValue();
		collectionProtocolEventId = specimenCollectionGroup.getCollectionProtocolEvent().getId().longValue();
		
		Participant participant = specimenCollectionGroup.getCollectionProtocolRegistration().getParticipant();
		/**For Migration Start**/	
		
		participantId=participant.getId();
		/**For Migration End**/	
		Logger.out.debug("SCgForm --------- Participant : -- "+ participant.toString());
		//if(participant!=null)
		String firstName = null;
		String lastName = null;
		String birthDate = null;
		String ssn = null;
		
		if(participant.getFirstName()==null)
		{
			firstName ="";
		}
		else
		{
			firstName = participant.getFirstName();
		}
		
		if(participant.getLastName()==null)
		{
			lastName ="";
		}
		else
		{
			lastName = participant.getLastName();
		}
		
		participantName=lastName+", "+firstName;
		
		if(participant.getBirthDate()==null)
		{
			birthDate ="";
		}
		else
		{
			birthDate = participant.getBirthDate().toString();
		}
		
		if(participant.getSocialSecurityNumber()==null)
		{
			ssn ="";
		}
		else
		{
			ssn = participant.getSocialSecurityNumber();
		}
		
	
		if(firstName.length()>0 || lastName.length()>0 || birthDate.length()>0 || ssn.length()>0)
		{
				participantId = participant.getId().longValue();
				radioButtonForParticipant = 1;
		}
		else
		{
			protocolParticipantIdentifier =  Utility.toString(specimenCollectionGroup.getCollectionProtocolRegistration().getProtocolParticipantIdentifier());
			radioButtonForParticipant = 2;
		}
		
		Logger.out.debug("participantId.................................."+participantId);
		Logger.out.debug("protocolParticipantIdentifier........................."+protocolParticipantIdentifier);
		Logger.out.debug("SCgForm --------- checkButton : -- " + radioButtonForParticipant );
		
		//Abhishek Mehta If site is null
		Site site = specimenCollectionGroup.getSpecimenCollectionSite();
		if(null != site)
		{
			siteId = site.getId().longValue();
		}
		
        /**
	  	 * For Consent tracking setting UI attributes
	  	 */
			User witness= specimenCollectionGroup.getCollectionProtocolRegistration().getConsentWitness();
			if(witness==null||witness.getFirstName()==null)
			{
				this.witnessName="";
			}
			else
			{
				this.witnessName=Utility.toString(witness.getFirstName());
			}
			this.signedConsentUrl=Utility.toString(specimenCollectionGroup.getCollectionProtocolRegistration().getSignedConsentDocumentURL());
			this.consentDate=Utility.parseDateToString(specimenCollectionGroup.getCollectionProtocolRegistration().getConsentSignatureDate(), Constants.DATE_PATTERN_MM_DD_YYYY);
		/**
	 * Name : Ashish Gupta
	 * Reviewer Name : Sachin Lale 
	 * Bug ID: 2741
	 * Patch ID: 2741_10	 
	 * Description: Method to populate Events in SCG form
	*/
		//Populating the events
		setSCGEvents(specimenCollectionGroup);
	}
	/**
	 * @param specimenCollectionGroup Settign Sp Coll Group 
	 */
	private void setSCGEvents(SpecimenCollectionGroup specimenCollectionGroup)
	{
		Collection eventsParametersColl = specimenCollectionGroup.getSpecimenEventParametersCollection();
		if(eventsParametersColl != null && !eventsParametersColl.isEmpty())
		{
			Iterator iter = eventsParametersColl.iterator();
			while(iter.hasNext())
			{
				Object tempObj = iter.next();
				Calendar calender = Calendar.getInstance();
				if(tempObj instanceof CollectionEventParameters)
				{
					CollectionEventParameters collectionEventParameters = (CollectionEventParameters)tempObj;
					this.collectionEventId = collectionEventParameters.getId().longValue();																											// Mandar : CollectionEvent 10-July-06
					//this.collectionEventSpecimenId = collectionEventParameters.getSpecimen().getId().longValue();
					this.collectionEventUserId = collectionEventParameters.getUser().getId().longValue();					
				
				 	calender.setTime(collectionEventParameters.getTimestamp());
					this.collectionEventdateOfEvent = Utility.parseDateToString(collectionEventParameters.getTimestamp(),Constants.DATE_PATTERN_MM_DD_YYYY);
					this.collectionEventTimeInHours = Utility.toString(Integer.toString(calender.get(Calendar.HOUR_OF_DAY)));
					this.collectionEventTimeInMinutes  = Utility.toString(Integer.toString(calender.get(Calendar.MINUTE)));
					this.collectionEventCollectionProcedure = collectionEventParameters.getCollectionProcedure();
					this.collectionEventContainer = collectionEventParameters.getContainer();
					this.collectionEventComments = Utility.toString(collectionEventParameters.getComment());
				}
				else if(tempObj instanceof ReceivedEventParameters)
				{
					ReceivedEventParameters receivedEventParameters = (ReceivedEventParameters)tempObj;
					
					calender.setTime(receivedEventParameters.getTimestamp());
					this.receivedEventId = receivedEventParameters.getId().longValue();
				//	this.receivedEventSpecimenId = receivedEventParameters.getSpecimen().getId().longValue();
					this.receivedEventUserId = receivedEventParameters.getUser().getId().longValue();
					this.receivedEventDateOfEvent = Utility.parseDateToString(receivedEventParameters.getTimestamp(),Constants.DATE_PATTERN_MM_DD_YYYY);
					this.receivedEventTimeInHours = Utility.toString(Integer.toString(calender.get(Calendar.HOUR_OF_DAY)));
					this.receivedEventTimeInMinutes = Utility.toString(Integer.toString(calender.get(Calendar.MINUTE)));
					this.receivedEventReceivedQuality = receivedEventParameters.getReceivedQuality();
					this.receivedEventComments = Utility.toString(receivedEventParameters.getComment());
				}
			}
		}
	}
	  
	/**
	 * @see edu.wustl.catissuecore.actionForm.AbstractActionForm#getFormId()
	 * @return SPECIMEN_COLLECTION_GROUP_FORM_ID
	 */
	public int getFormId() 
	{
		return Constants.SPECIMEN_COLLECTION_GROUP_FORM_ID;
	}
	/**
	 * @return siteId
	 */
	public long getSiteId() 
	{
		return siteId;
	}

	/**
	 * @param siteId Setting Site id
	 */
	public void setSiteId(long siteId) 
	{
		this.siteId = siteId;
	}

	/**
	 * @return clinicalStatus
	 */
	public String getClinicalStatus()
	{
		return clinicalStatus;
	}

	/**
	 * @param clinicalStatus Settign clinicalStatus
	 */
	public void setClinicalStatus(String clinicalStatus) 
	{
		this.clinicalStatus = clinicalStatus;
	}

	/**
	 * @return collectionProtocolEventId
	 */
	public long getCollectionProtocolEventId() 
	{
		return collectionProtocolEventId;
	}

	/**
	 * @param collectionProtocolEventId Setting collectionProtocolEventId
	 */
	public void setCollectionProtocolEventId(long collectionProtocolEventId) 
	{
		this.collectionProtocolEventId = collectionProtocolEventId;
	}

	/**
	 * @return collectionProtocolId
	 */
	public long getCollectionProtocolId() 
	{
		return collectionProtocolId;
	}

	/**
	 * @param collectionProtocolId Setting collectionProtocolId
	 */
	public void setCollectionProtocolId(long collectionProtocolId) 
	{
		this.collectionProtocolId = collectionProtocolId;
	}

	/**
	 * @return participantsMedicalIdentifierId
	 */
	public long getParticipantsMedicalIdentifierId() 
	{
		return participantsMedicalIdentifierId;
	}

	/**
	 * @param participantsMedicalIdentifierId Setting  participantsMedicalIdentifierId
	 */
	public void setParticipantsMedicalIdentifierId(long participantsMedicalIdentifierId) 
	{
		this.participantsMedicalIdentifierId = participantsMedicalIdentifierId;
	}

	/**
	 * @return protocolParticipantIdentifier
	 */
	public String getProtocolParticipantIdentifier() 
	{
		return protocolParticipantIdentifier;
	}

	/**
	 * @param protocolParticipantIdentifier Setting protocolParticipantIdentifier
	 */
	public void setProtocolParticipantIdentifier(String protocolParticipantIdentifier)
	{
		this.protocolParticipantIdentifier = protocolParticipantIdentifier;
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
			if(this.collectionProtocolId == -1)
			{
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.selected",
								ApplicationProperties.getValue("specimenCollectionGroup.protocolTitle")));
			}
			
			if(this.siteId == -1)
			{
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.selected",
								ApplicationProperties.getValue("specimenCollectionGroup.site")));
			}
			
			/**
			 * Name: Vijay Pande
			 * Reviewer Name: Aarti Sharma
			 * Validation for participant name and participantProtocolIdentifier added
			 */
			// Check what user has selected Participant Name / Participant Number

			if(this.radioButtonForParticipant == 1)
			{   
				//if participant name field is checked.
				/**For Migration Start**/	
				if(this.participantName==null || validator.isEmpty(this.participantName))  // || Utility.this.participantName.trim().equals(""))
				{
					if(this.participantNameWithProtocolId==null||validator.isEmpty(this.participantNameWithProtocolId))
					{
						errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",ApplicationProperties.getValue("specimenCollectionGroup.collectedByParticipant")));
					}	
				}
			/**For Migration End**/	
			}
			else
			{
				if (validator.isEmpty(this.protocolParticipantIdentifier))
				{
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required", ApplicationProperties.getValue("specimenCollectionGroup.collectedByProtocolParticipantNumber")));	
				}
			}
			if(this.name.equals(""))
			{
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",
								ApplicationProperties.getValue("specimenCollectionGroup.groupName")));
			}
			
            // Mandatory Field : Study Calendar event point
			if(this.collectionProtocolEventId == -1)
			{
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.selected",
								ApplicationProperties.getValue("specimenCollectionGroup.studyCalendarEventPoint")));
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
			
			//Condition for medical Record Number.
			if(this.radioButtonForParticipant == 1)
			{   
				//if participant name field is checked.
				// here medical record number field should be enabled and must have some value selected.
//					if(this.participantsMedicalIdentifierId == -1){
//						errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.selected",
//										ApplicationProperties.getValue("specimenCollectionGroup.medicalRecordNumber")));
//					}
							
			}
			else
			{
				// here this field will be alltogether disabled
				// No need of any condition.
			}
			/**
	 * Name : Ashish Gupta
	 * Reviewer Name : Sachin Lale 
	 * Bug ID: 2741
	 * Patch ID: 2741_4	 
	 * Description: Methods for validation of events in scg
	*/
			//Time validation
			String collectionTime = this.collectionEventTimeInHours+":"+this.collectionEventTimeInMinutes+":00";
			String receivedTime = this.receivedEventTimeInHours+":"+this.receivedEventTimeInMinutes+":00";
//			CollectionEvent validation.
    		EventsUtil.validateCollectionEvent(errors,validator,collectionEventUserId,collectionEventdateOfEvent,collectionEventCollectionProcedure,collectionTime);
    		//ReceivedEvent validation
    		EventsUtil.validateReceivedEvent(errors,validator,receivedEventUserId,receivedEventDateOfEvent,receivedEventReceivedQuality,receivedTime );	
    		    		
			//Added by Ashish for Multiple Specimens
			/**
			 * Name : Ashish Gupta
			 * Reviewer Name : Sachin Lale 
			 * Bug ID: Multiple Specimen Bug
			 * Patch ID: Multiple Specimen Bug_4 
			 * See also: 1-8
			 * Description: Remove the page on which number of multiple specimens are entered while going to multiple specimen page.
			*/
			String buttonName = request.getParameter("button");

			if(buttonName != null && !buttonName.equals(""))
			{
				if(numberOfSpecimens < 1)
		        {	
					setNumberOfSpecimens(1);
		        	errors.add(ActionErrors.GLOBAL_ERROR,new ActionError("errors.multiplespecimen.minimumspecimen"));		        	
		        }
			}
			request.getSession().setAttribute("scgForm", this);
			//For setting whether to set specimen 
			String applyToString = request.getParameter("applyToSpecimenValue");
			if(applyToString != null && applyToString.equals("true") )
			{
				applyEventsToSpecimens = true;
			}
		}
		catch (Exception excp)
		{
	    	// use of logger as per bug 79
	    	Logger.out.error(excp.getMessage(),excp); 
			errors = new ActionErrors();
		}
		return errors;
	}
	
	/**
     * This method sets Identifier of Objects inserted by AddNew activity in Form-Bean which initialized AddNew action
     * @param addNewFor - FormBean ID of the object inserted
     *  @param addObjectIdentifier - Identifier of the Object inserted 
     */
	public void setAddNewObjectIdentifier(String addNewFor, Long addObjectIdentifier)
    {
        if(addNewFor.equals("collectionProtocol"))
        {
            setCollectionProtocolId(addObjectIdentifier.longValue());
        }
        else if(addNewFor.equals("site"))
        {
            setSiteId(addObjectIdentifier.longValue());
        }
        else if(addNewFor.equals("participant"))
        {
//            setParticipantId(addObjectIdentifier.longValue());
            setCollectionProtocolRegistrationId(addObjectIdentifier.longValue());
            setRadioButtonForParticipant(1);
        }
        else if(addNewFor.equals("protocolParticipantIdentifier"))
        {
            setCollectionProtocolRegistrationId(addObjectIdentifier.longValue());
//            setProtocolParticipantIdentifier(addObjectIdentifier.toString());
            setRadioButtonForParticipant(2);
        }
    }
	
	//Consent Tracking Module Virender Mehta
	/**
	 * @return consentResponseForScgValues  The comments associated with Response at Specimen Collection Group level
	 */	
	public Map getConsentResponseForScgValues() 
	{
		return consentResponseForScgValues;
	}
	
	/**
	 * @param consentResponseForScgValues  The comments associated with Response at Specimen Collection Group level
	 */	
	public void setConsentResponseForScgValues(Map consentResponseForScgValues)
	{
		this.consentResponseForScgValues = consentResponseForScgValues;
	}
	
	/**
     * @param key Key prepared for saving data.
     * @param value Values correspponding to key
     */
    public void setConsentResponseForScgValue(String key, Object value) 
    {
   	 if (isMutable())
   		consentResponseForScgValues.put(key, value);
    }

    /**
     * @param key Key prepared for saving data.
     * @return consentResponseForScgValues.get(key)
     */
    public Object getConsentResponseForScgValue(String key) 
    {
        return consentResponseForScgValues.get(key);
    }
    
	/**
	 * @return values in map consentResponseForScgValues
	 */
	public Collection getAllConsentResponseForScgValue() 
	{
		return consentResponseForScgValues.values();
	}

	/**
	 *@return consentTierCounter  This will keep track of count of Consent Tier
	 */
	public int getConsentTierCounter()
	{
		return consentTierCounter;
	}

	/**
	 *@param consentTierCounter  This will keep track of count of Consent Tier
	 */
	public void setConsentTierCounter(int consentTierCounter)
	{
		this.consentTierCounter = consentTierCounter;
	}

	/**
	 * @return consentDate The Date on Which Consent is Signed
	 */	
	public String getConsentDate()
	{
		return consentDate;
	}

	/**
	 * @param consentDate The Date on Which Consent is Signed
	 */
	public void setConsentDate(String consentDate)
	{
		this.consentDate = consentDate;
	}
	
	/**
	 * @return signedConsentUrl The reference to the electric signed document(eg PDF file)
	 */	
	public String getSignedConsentUrl()
	{
		return signedConsentUrl;
	}
	
	/**
	 * @param signedConsentUrl The reference to the electric signed document(eg PDF file)
	 */	
	public void setSignedConsentUrl(String signedConsentUrl)
	{
		this.signedConsentUrl = signedConsentUrl;
	}
	
	/**
	 * @return witnessName The name of the witness to the consent Signature(PI or coordinator of the Collection Protocol)
	 */	
	public String getWitnessName()
	{
		return witnessName;
	}
	
	/**
	 * @param witnessName The name of the witness to the consent Signature(PI or coordinator of the Collection Protocol)
	 */	
	public void setWitnessName(String witnessName)
	{
		this.witnessName = witnessName;
	}
	
	/**
	 * It returns status of button(return,discard,reset)
	 * @return withdrawlButtonStatus
	 */
	public String getWithdrawlButtonStatus()
	{
		return withdrawlButtonStatus;
	}

	/**
	 * It returns status of button(return,discard,reset)
	 * @param withdrawlButtonStatus return,discard,reset
	 */
	public void setWithdrawlButtonStatus(String withdrawlButtonStatus)
	{
		this.withdrawlButtonStatus = withdrawlButtonStatus;
	}
	
	/**
	 * @return applyChangesTo
	 */
	public String getApplyChangesTo()
	{
		return applyChangesTo;
	}

	/**
	 * @param applyChangesTo 
	 */
	public void setApplyChangesTo(String applyChangesTo)
	{
		this.applyChangesTo = applyChangesTo;
	}
	
	/**
	 * 
	 * @return stringOfResponseKeys
	 */
	public String getStringOfResponseKeys()
	{
		return stringOfResponseKeys;
	}
	
	/**
	 * 
	 * @param stringOfResponseKeys
	 */
	public void setStringOfResponseKeys(String stringOfResponseKeys)
	{
		this.stringOfResponseKeys = stringOfResponseKeys;
	}
	
	/**
	 * This function creates Array of String of keys and add them into the consentTiersList.
	 * @return consentTiersList
	 */
	public Collection getConsentTiers()
	{
		Collection consentTiersList=new ArrayList();
		String [] strArray = null;
		int noOfConsents =this.getConsentTierCounter();
		for(int counter=0;counter<noOfConsents;counter++)
		{	
			strArray = new String[6];
			strArray[0]="consentResponseForScgValues(ConsentBean:"+counter+"_consentTierID)";
			strArray[1]="consentResponseForScgValues(ConsentBean:"+counter+"_statement)";
			strArray[2]="consentResponseForScgValues(ConsentBean:"+counter+"_participantResponse)";
			strArray[3]="consentResponseForScgValues(ConsentBean:"+counter+"_participantResponseID)";
			strArray[4]="consentResponseForScgValues(ConsentBean:"+counter+"_specimenCollectionGroupLevelResponse)";
			strArray[5]="consentResponseForScgValues(ConsentBean:"+counter+"_specimenCollectionGroupLevelResponseID)";
			consentTiersList.add(strArray);
		}
		return consentTiersList;
	}

	/**
	 * This function returns the format of Key
	 * @return consentResponseForScgValues(ConsentBean:`_specimenCollectionGroupLevelResponse) 
	 */
	public String getConsentTierMap()
	{
		return "consentResponseForScgValues(ConsentBean:`_specimenCollectionGroupLevelResponse)";
	}
//Consent Tracking Module Virender Mehta
	/**
	 * @return the collectionEventCollectionProcedure
	 */
	public String getCollectionEventCollectionProcedure()
	{
		return collectionEventCollectionProcedure;
	}

	/**
	 * @param collectionEventCollectionProcedure the collectionEventCollectionProcedure to set
	 */
	public void setCollectionEventCollectionProcedure(String collectionEventCollectionProcedure)
	{
		this.collectionEventCollectionProcedure = collectionEventCollectionProcedure;
	}

	/**
	 * @return the collectionEventComments
	 */
	public String getCollectionEventComments()
	{
		return collectionEventComments;
	}

	/**
	 * @param collectionEventComments the collectionEventComments to set
	 */
	public void setCollectionEventComments(String collectionEventComments)
	{
		this.collectionEventComments = collectionEventComments;
	}

	/**
	 * @return the collectionEventContainer
	 */
	public String getCollectionEventContainer()
	{
		return collectionEventContainer;
	}
	
	/**
	 * @param collectionEventContainer the collectionEventContainer to set
	 */
	public void setCollectionEventContainer(String collectionEventContainer)
	{
		this.collectionEventContainer = collectionEventContainer;
	}
	
	/**
	 * @return the collectionEventdateOfEvent
	 */
	public String getCollectionEventdateOfEvent()
	{
		return collectionEventdateOfEvent;
	}
	
	/**
	 * @param collectionEventdateOfEvent the collectionEventdateOfEvent to set
	 */
	public void setCollectionEventdateOfEvent(String collectionEventdateOfEvent)
	{
		this.collectionEventdateOfEvent = collectionEventdateOfEvent;
	}

	/**
	 * @return the collectionEventId
	 */
	public long getCollectionEventId()
	{
		return collectionEventId;
	}

	/**
	 * @param collectionEventId the collectionEventId to set
	 */
	public void setCollectionEventId(long collectionEventId)
	{
		this.collectionEventId = collectionEventId;
	}
	
	/**
	 * @return the collectionEventSpecimenId
	 */
	public long getCollectionEventSpecimenId()
	{
		return collectionEventSpecimenId;
	}
	
	/**
	 * @param collectionEventSpecimenId the collectionEventSpecimenId to set
	 */
	public void setCollectionEventSpecimenId(long collectionEventSpecimenId)
	{
		this.collectionEventSpecimenId = collectionEventSpecimenId;
	}
	
	/**
	 * @return the collectionEventTimeInHours
	 */
	public String getCollectionEventTimeInHours()
	{
		return collectionEventTimeInHours;
	}
	
	/**
	 * @param collectionEventTimeInHours the collectionEventTimeInHours to set
	 */
	public void setCollectionEventTimeInHours(String collectionEventTimeInHours)
	{
		this.collectionEventTimeInHours = collectionEventTimeInHours;
	}
	
	/**
	 * @return the collectionEventTimeInMinutes
	 */
	public String getCollectionEventTimeInMinutes()
	{
		return collectionEventTimeInMinutes;
	}
	
	/**
	 * @param collectionEventTimeInMinutes the collectionEventTimeInMinutes to set
	 */
	public void setCollectionEventTimeInMinutes(String collectionEventTimeInMinutes)
	{
		this.collectionEventTimeInMinutes = collectionEventTimeInMinutes;
	}

	/**
	 * @return the collectionEventUserId
	 */
	public long getCollectionEventUserId()
	{
		return collectionEventUserId;
	}
	
	/**
	 * @param collectionEventUserId the collectionEventUserId to set
	 */
	public void setCollectionEventUserId(long collectionEventUserId)
	{
		this.collectionEventUserId = collectionEventUserId;
	}
	
	/**
	 * @return the receivedEventComments
	 */
	public String getReceivedEventComments()
	{
		return receivedEventComments;
	}
	
	/**
	 * @param receivedEventComments the receivedEventComments to set
	 */
	public void setReceivedEventComments(String receivedEventComments)
	{
		this.receivedEventComments = receivedEventComments;
	}
	
	/**
	 * @return the receivedEventDateOfEvent
	 */
	public String getReceivedEventDateOfEvent()
	{
		return receivedEventDateOfEvent;
	}
	
	/**
	 * @param receivedEventDateOfEvent the receivedEventDateOfEvent to set
	 */
	public void setReceivedEventDateOfEvent(String receivedEventDateOfEvent)
	{
		this.receivedEventDateOfEvent = receivedEventDateOfEvent;
	}
	
	/**
	 * @return the receivedEventId
	 */
	public long getReceivedEventId()
	{
		return receivedEventId;
	}
	
	/**
	 * @param receivedEventId the receivedEventId to set
	 */
	public void setReceivedEventId(long receivedEventId)
	{
		this.receivedEventId = receivedEventId;
	}
	
	/**
	 * @return the receivedEventReceivedQuality
	 */
	public String getReceivedEventReceivedQuality()
	{
		return receivedEventReceivedQuality;
	}
	
	/**
	 * @param receivedEventReceivedQuality the receivedEventReceivedQuality to set
	 */
	public void setReceivedEventReceivedQuality(String receivedEventReceivedQuality)
	{
		this.receivedEventReceivedQuality = receivedEventReceivedQuality;
	}
	
	/**
	 * @return the receivedEventSpecimenId
	 */
	public long getReceivedEventSpecimenId()
	{
		return receivedEventSpecimenId;
	}
	
	/**
	 * @param receivedEventSpecimenId the receivedEventSpecimenId to set
	 */
	public void setReceivedEventSpecimenId(long receivedEventSpecimenId)
	{
		this.receivedEventSpecimenId = receivedEventSpecimenId;
	}
	
	/**
	 * @return the receivedEventTimeInHours
	 */
	public String getReceivedEventTimeInHours()
	{
		return receivedEventTimeInHours;
	}
	
	/**
	 * @param receivedEventTimeInHours the receivedEventTimeInHours to set
	 */
	public void setReceivedEventTimeInHours(String receivedEventTimeInHours)
	{
		this.receivedEventTimeInHours = receivedEventTimeInHours;
	}
	
	/**
	 * @return the receivedEventTimeInMinutes
	 */
	public String getReceivedEventTimeInMinutes()
	{
		return receivedEventTimeInMinutes;
	}
	
	/**
	 * @param receivedEventTimeInMinutes the receivedEventTimeInMinutes to set
	 */
	public void setReceivedEventTimeInMinutes(String receivedEventTimeInMinutes)
	{
		this.receivedEventTimeInMinutes = receivedEventTimeInMinutes;
	}
	
	/**
	 * @return the receivedEventUserId
	 */
	public long getReceivedEventUserId()
	{
		return receivedEventUserId;
	}
	
	/**
	 * @param receivedEventUserId the receivedEventUserId to set
	 */
	public void setReceivedEventUserId(long receivedEventUserId)
	{
		this.receivedEventUserId = receivedEventUserId;
	}
	
	/**
	 * @return the applyEventsToSpecimens
	 */
	public boolean isApplyEventsToSpecimens()
	{
		return applyEventsToSpecimens;
	}
	
	/**
	 * @param applyEventsToSpecimens the applyEventsToSpecimens to set
	 */
	public void setApplyEventsToSpecimens(boolean applyEventsToSpecimens)
	{
		this.applyEventsToSpecimens = applyEventsToSpecimens;
	}

    /**
     * Name: Shital Lawhale
     * Reviewer Name : Sachin Lale 
     * Bug ID: 3052
     * Patch ID: 3052_1_3
     * See also: 1_1 to 1_5
     * Description : A comment field at the Specimen Collection Group.
     */    
    
    /**
     * @returns comment
     */
    public String getComment()
    {
        return comment;
    }

    /**
     * @param comment : user comment to set
     */
    
    public void setComment(String comment)
    {
        this.comment = comment;
    }

	/**
	 * Patch ID: Bug#3184_7
	 */
    private String restrictSCGCheckbox;
	
	/**
	 * This method returns the value of the checkbox
	 * @return the restrictSCGCheckbox
	 */
	public String getRestrictSCGCheckbox() 
	{
		return restrictSCGCheckbox;
	}

	/**
	 * This method sets the value of Checkbox
	 * @param restrictSCGCheckbox the restrictSCGCheckbox to set
	 */
	public void setRestrictSCGCheckbox(String restrictSCGCheckbox) 
	{
		this.restrictSCGCheckbox = restrictSCGCheckbox;
	}
	/**For Migration Start**/

	/**
	 * This method returns the value of the participantName
	 * @return the participantName
	 */
	public String getParticipantName() 
	{
		return participantName;
	}

	/**
	 * This method sets the participantName
	 * @param participantName the participantName to set
	 */
	public void setParticipantName(String participantName) 
	{
		this.participantName = participantName;
	}



	public String getCollectionProtocolName() {
		return collectionProtocolName;
	}



	public void setCollectionProtocolName(String collectionProtocolName) {
		this.collectionProtocolName = collectionProtocolName;
	}

	public String getParticipantNameWithProtocolId() {
		participantNameWithProtocolId = "";
		if(participantName!=null)
			participantNameWithProtocolId = participantName;
		else
			participantNameWithProtocolId ="N/A";  
			
		if(protocolParticipantIdentifier!=null && protocolParticipantIdentifier.length() > 0)	
			participantNameWithProtocolId = participantNameWithProtocolId + '('+ protocolParticipantIdentifier + ')';
		else
			participantNameWithProtocolId = participantNameWithProtocolId+'('+"N/A" + ')';
			
		return participantNameWithProtocolId;
	}



	public void setParticipantNameWithProtocolId(String participantNameProtocolId) {
		this.participantNameWithProtocolId = participantNameProtocolId;
	}



	public String getCollectionStatus() {
		return collectionStatus;
	}



	public void setCollectionStatus(String collectionStatus) {
		this.collectionStatus = collectionStatus;
	}

}
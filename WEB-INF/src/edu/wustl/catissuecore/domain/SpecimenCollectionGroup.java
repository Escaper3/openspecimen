/**
 * <p>Title: SpecimenCollectionGroup Class>
 * <p>Description: An event that results in the collection 
 * of one or more specimen from a participant.</p>
 * Copyright:    Copyright (c) year
 * Company: Washington University, School of Medicine, St. Louis.
 * @author Gautam Shetty
 * @version 1.00
 */

package edu.wustl.catissuecore.domain;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import edu.wustl.catissuecore.actionForm.SpecimenCollectionGroupForm;
import edu.wustl.catissuecore.bean.ConsentBean;
import edu.wustl.catissuecore.domain.pathology.DeidentifiedSurgicalPathologyReport;
import edu.wustl.catissuecore.domain.pathology.IdentifiedSurgicalPathologyReport;
import edu.wustl.catissuecore.util.EventsUtil;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.actionForm.AbstractActionForm;
import edu.wustl.common.actionForm.IValueObject;
import edu.wustl.common.domain.AbstractDomainObject;
import edu.wustl.common.exception.AssignDataException;
import edu.wustl.common.util.MapDataParser;
import edu.wustl.common.util.logger.Logger;

/**
 * An event that results in the collection 
 * of one or more specimen from a participant.
 * @hibernate.class table="CATISSUE_SPECIMEN_COLL_GROUP"
 * @author gautam_shetty
 */
public class SpecimenCollectionGroup extends AbstractDomainObject implements Serializable
{
    private static final long serialVersionUID = 1234567890L;
/**
	 * Name : Ashish Gupta
	 * Reviewer Name : Sachin Lale 
	 * Bug ID: 2741
	 * Patch ID: 2741_1	 
	 * Description: Condition indicating whether to propagate collection events and received events to specimens under this scg
	*/
    /**
     * Condition indicating whether to propagate collection events and received events to specimens under this scg
     */
    protected transient boolean applyEventsToSpecimens = false;
     /**
     * System generated unique id.
     */
    protected Long id;
    
    /**
     * name assigned to Specimen Collection Group
     */
    protected String name;
    /**
     * Participant's clinical diagnosis at 
     * this collection event (e.g. Prostate Adenocarcinoma).
     */
    protected String clinicalDiagnosis;

    /**
     * The clinical status of the participant at the time of specimen collection. 
     * (e.g. New DX, pre-RX, pre-OP, post-OP, remission, relapse)
     */
    protected String clinicalStatus;
     
    /**
     * Defines whether this  record can be queried (Active) 
     * or not queried (Inactive) by any actor.
     */
    protected String activityStatus;
    
    /**
     * A physical location associated with biospecimen collection, 
     * storage, processing, or utilization.
     */
	protected Site specimenCollectionSite;

    /**
     * A required specimen collection event associated with a Collection Protocol.
     */
    protected CollectionProtocolEvent collectionProtocolEvent;

    /**
     * The Specimens in this SpecimenCollectionGroup.
     */
    protected Collection specimenCollection = new HashSet();

    /**
     * Name: Sachin Lale 
     * Bug ID: 3052
     * Patch ID: 3052_1
     * See also: 1-4 
     * Description : A comment field at the Specimen Collection Group level.
     */
    protected String comment;

    /**
     * A registration of a Participant to a Collection Protocol.
     */
    protected CollectionProtocolRegistration collectionProtocolRegistration;
	   //----For Consent Tracking. Ashish 22/11/06
    /**
     * The consent tier status by multiple participants for a particular specimen collection group.
     */
    protected Collection consentTierStatusCollection;
    
	//Mandar 15-jan-07 
	/*
	 * To perform operation based on withdraw button clicked.
	 * Default No Action to allow normal behaviour. 
	 */
	protected String consentWithdrawalOption=Constants.WITHDRAW_RESPONSE_NOACTION;
	
	//Mandar 19-jan-07 
	/*
	 * To apply changes to specimen based on consent status changes.
	 * Default Apply none to allow normal behaviour. 
	 */
	protected String applyChangesTo=Constants.APPLY_NONE;

	//Mandar 22-jan-07 
	/*
	 * To apply changes to specimen based on consent status changes.
	 * Default empty. 
	 */
	
	protected String stringOfResponseKeys="";
	
	/**
	 * Surgical Pathology Number of the associated pathology report, erlier was Present in Clinical Report
	 */
	protected String surgicalPathologyNumber;
	/**
    * An identified surgical pathology report associated with 
    * current specimen collection group  
    */
	protected IdentifiedSurgicalPathologyReport identifiedSurgicalPathologyReport;
   /**
    * A deidentified surgical pathology report associated with 
    * current specimen collection group  
    */
   
	protected DeidentifiedSurgicalPathologyReport deIdentifiedSurgicalPathologyReport;
	/**
	 * @return the consentTierStatusCollection
	 * @hibernate.collection-one-to-many class="edu.wustl.catissuecore.domain.ConsentTierStatus" lazy="true" cascade="save-update"
	 * @hibernate.set table="CATISSUE_CONSENT_TIER_STATUS" name="consentTierStatusCollection"
	 * @hibernate.collection-key column="SPECIMEN_COLL_GROUP_ID"
	 */
	public Collection getConsentTierStatusCollection()
	{
		return consentTierStatusCollection;
	}
	
	/**
	 * @param consentTierStatusCollection the consentTierStatusCollection to set
	 */
	public void setConsentTierStatusCollection(Collection consentTierStatusCollection)
	{
		this.consentTierStatusCollection = consentTierStatusCollection;
	}
    //----Consent Tracking End
    /**
	 * Name : Ashish Gupta
	 * Reviewer Name : Sachin Lale 
	 * Bug ID: 2741
	 * Patch ID: 2741_2	 
	 * Description: 1 to many Association between SCG and SpecimenEventParameters
	*/
    /**
     * Collection and Received events associated with this SCG
     */
    protected Collection specimenEventParametersCollection = new HashSet();
	    
	/**
	 * @return the specimenEventParametersCollection
	 * @hibernate.set cascade="save-update" inverse="true" table="CATISSUE_SPECIMEN_EVENT_PARAM" lazy="false"
	 * @hibernate.collection-one-to-many class="edu.wustl.catissuecore.domain.SpecimenEventParameters"  
	 * @hibernate.collection-key column="SPECIMEN_COLL_GRP_ID" 
	 */
	public Collection getSpecimenEventParametersCollection()
	{
		return specimenEventParametersCollection;
	}

	
	/**
	 * @param specimenEventParametersCollection the specimenEventParametersCollection to set
	 */
	public void setSpecimenEventParametersCollection(Collection specimenEventParametersCollection)
	{
		this.specimenEventParametersCollection = specimenEventParametersCollection;
	}

	public SpecimenCollectionGroup()
    {
    
    }
    
	public SpecimenCollectionGroup(AbstractActionForm form) throws AssignDataException
	{
		Logger.out.debug("<<< Before setting Values >>>");
		setAllValues(form);
	}

	/**
	 * Returns the system generated unique id.
	 * @hibernate.id name="id" column="IDENTIFIER" type="long" length="30"
	 * unsaved-value="null" generator-class="native"
	 * @hibernate.generator-param name="sequence" value="CATISSUE_SPECIMEN_COLL_GRP_SEQ"
	 * @return the system generated unique id.
	 * @see #setId(Long)
	 */
	public Long getId() 
	{
		return id;
	}


	/**
	 * @param id
	 */
	public void setId(Long id) 
	{
		this.id = id;
	}
	/**
	 * Returns the system generated unique Specimen Collection Group name.
	 * @hibernate.property name="name" column="NAME" type="string" length="255"
	 * @return the system generated unique name.
	 * @see #setName(String)
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
    /**
     * Returns the participant's clinical diagnosis at 
     * this collection event (e.g. Prostate Adenocarcinoma).
     * @hibernate.property name="clinicalDiagnosis" type="string" 
     * column="CLINICAL_DIAGNOSIS" length="150"
     * @return the participant's clinical diagnosis at 
     * this collection event (e.g. Prostate Adenocarcinoma).
     * @see #setClinicalDiagnosis(String)
     */
    public String getClinicalDiagnosis()
    {
        return clinicalDiagnosis;
    }

    /**
     * Sets the participant's clinical diagnosis at 
     * this collection event (e.g. Prostate Adenocarcinoma).
     * @param clinicalDiagnosis the participant's clinical diagnosis at 
     * this collection event (e.g. Prostate Adenocarcinoma).
     * @see #getClinicalDiagnosis()
     */
    public void setClinicalDiagnosis(String clinicalDiagnosis)
    {
        this.clinicalDiagnosis = clinicalDiagnosis;
    }

    /**
     * Returns the clinical status of the participant at the time of specimen collection. 
     * (e.g. New DX, pre-RX, pre-OP, post-OP, remission, relapse)
     * @hibernate.property name="clinicalStatus" type="string" 
     * column="CLINICAL_STATUS" length="50"
     * @return clinical status of the participant at the time of specimen collection.
     * @see #setClinicalStatus(String)
     */
    public String getClinicalStatus()
    {
        return clinicalStatus;
    }

    /**
     * Sets the clinical status of the participant at the time of specimen collection. 
     * (e.g. New DX, pre-RX, pre-OP, post-OP, remission, relapse)
     * @param clinicalStatus the clinical status of the participant at the time of specimen collection.
     * @see #getClinicalStatus()
     */
    public void setClinicalStatus(String clinicalStatus)
    {
        this.clinicalStatus = clinicalStatus;
    }

    /**
     * Returns whether this  record can be queried (Active) 
     * or not queried (Inactive) by any actor.
     * @hibernate.property name="activityStatus" type="string" 
     * column="ACTIVITY_STATUS" length="50"
     * @return Active if this record can be queried else returns InActive.
     * @see #setActivityStatus(String)
     */
    public String getActivityStatus()
    {
        return activityStatus;
    }

    /**
     * Sets whether this  record can be queried (Active) 
     * or not queried (Inactive) by any actor.
     * @param activityStatus Active if this record can be queried else returns InActive.
     * @see #getActivityStatus()
     */
    public void setActivityStatus(String activityStatus)
    {
        this.activityStatus = activityStatus;
    }

    /**
     * Returns the physical location associated with biospecimen collection, 
     * storage, processing, or utilization.
     * @hibernate.many-to-one column="SITE_ID" 
     * class="edu.wustl.catissuecore.domain.Site" constrained="true"
     * @return the physical location associated with biospecimen collection, 
     * storage, processing, or utilization.
     * @see #setSpecimenCollectionSite(Site)
     */
    public Site getSpecimenCollectionSite()
    {
        return specimenCollectionSite;
    }

    /**
     * Sets the physical location associated with biospecimen collection, 
     * storage, processing, or utilization.
     * @param site physical location associated with biospecimen collection, 
     * storage, processing, or utilization.
     * @see #getSpecimenCollectionSite()
     */
    public void setSpecimenCollectionSite(Site specimenCollectionSite)
    {
        this.specimenCollectionSite = specimenCollectionSite;
    }

    /**
     * Returns the required specimen collection event 
     * associated with a Collection Protocol.
     * @hibernate.many-to-one column="COLLECTION_PROTOCOL_EVENT_ID" 
     * class="edu.wustl.catissuecore.domain.CollectionProtocolEvent" constrained="true"
     * @return the required specimen collection event 
     * associated with a Collection Protocol.
     * @see #setCollectionProtocolEvent(CollectionProtocolEvent)
     */
    public CollectionProtocolEvent getCollectionProtocolEvent()
    {
        return collectionProtocolEvent;
    }

    /**
     * Sets the required specimen collection event 
     * associated with a Collection Protocol.
     * @param collectionProtocolEvent the required specimen collection event 
     * associated with a Collection Protocol.
     * @see #getCollectionProtocolEvent()
     */
    public void setCollectionProtocolEvent(CollectionProtocolEvent collectionProtocolEvent)
    {
        this.collectionProtocolEvent = collectionProtocolEvent;
    }

    /**
     * Returns the collection Specimens in this SpecimenCollectionGroup.
     * @hibernate.set name="specimenCollection" table="CATISSUE_SPECIMEN"
	 * cascade="none" inverse="true" lazy="false"
	 * @hibernate.collection-key column="SPECIMEN_COLLECTION_GROUP_ID"
	 * @hibernate.collection-one-to-many class="edu.wustl.catissuecore.domain.Specimen"
     * @return the collection Specimens in this SpecimenCollectionGroup.
     * @see #setSpecimenCollection(Collection)
     */
    public Collection getSpecimenCollection()
    {
        return specimenCollection;
    }

    /**
     * Sets the collection Specimens in this SpecimenCollectionGroup.
     * @param specimenCollection the collection Specimens in this SpecimenCollectionGroup.
     * @see #getSpecimenCollection()
     */
    public void setSpecimenCollection(Collection specimenCollection)
    {
        this.specimenCollection = specimenCollection;
    }

    /**
     * Returns the registration of a Participant to a Collection Protocol.
     * @hibernate.many-to-one column="COLLECTION_PROTOCOL_REG_ID" 
     * class="edu.wustl.catissuecore.domain.CollectionProtocolRegistration" constrained="true"
     * @return the registration of a Participant to a Collection Protocol.
     * @see #setCollectionProtocolRegistration(CollectionProtocolRegistration)
     */
    public CollectionProtocolRegistration getCollectionProtocolRegistration()
    {
        return collectionProtocolRegistration;
    }

    /**
     * Sets the registration of a Participant to a Collection Protocol.
     * @param collectionProtocolRegistration the registration of a Participant 
     * to a Collection Protocol.
     * @see #getCollectionProtocolRegistration()
     */
    public void setCollectionProtocolRegistration(
            CollectionProtocolRegistration collectionProtocolRegistration)
    {
        this.collectionProtocolRegistration = collectionProtocolRegistration;
    }

	/* (non-Javadoc)
	 * @see edu.wustl.catissuecore.domain.AbstractDomainObject#setAllValues(edu.wustl.catissuecore.actionForm.AbstractActionForm)
	 */
	public void setAllValues(IValueObject valueObject) throws AssignDataException 
	{
		AbstractActionForm abstractForm = (AbstractActionForm)valueObject;
		SpecimenCollectionGroupForm form = (SpecimenCollectionGroupForm)abstractForm;
		try
		{
			this.setClinicalDiagnosis(form.getClinicalDiagnosis());
	        this.setClinicalStatus(form.getClinicalStatus());
	        this.setActivityStatus(form.getActivityStatus());
			this.setName(form.getName());
			specimenCollectionSite = new Site();
			specimenCollectionSite.setId(new Long(form.getSiteId()));
			
			/**
             * Name: Sachin Lale
             * Bug ID: 3052
             * Patch ID: 3052_1
             * See also: 1_1 to 1_5
             * Description : A comment field is set from form bean to domain object.
             */  
            this.setComment(form.getComment());
            
			collectionProtocolEvent= new CollectionProtocolEvent();
			collectionProtocolEvent.setId(new Long(form.getCollectionProtocolEventId()));
			
			Logger.out.debug("form.getParticipantsMedicalIdentifierId() "+form.getParticipantsMedicalIdentifierId());
			
			this.setSurgicalPathologyNumber(form.getSurgicalPathologyNumber());

			collectionProtocolRegistration = new CollectionProtocolRegistration();
			/**
			* Name: Vijay Pande
			* Reviewer Name: Aarti Sharma
			* Variable checkedButton name is changed to radioButton hence its getter method name is changed
			*/
			if(form.getRadioButtonForParticipant() == 1)
			{    
				//value of radio button is 2 when participant name is selected
				Participant participant = new Participant();
				/**For Migration Start**/
//				form.setParticipantId(Utility.getParticipantId(form.getParticipantName()));
				/**For Migration End**/
				participant.setId(new Long(form.getParticipantId()));
				collectionProtocolRegistration.setParticipant(participant);
				collectionProtocolRegistration.setProtocolParticipantIdentifier(null);
				
				ParticipantMedicalIdentifier participantMedicalIdentifier = new ParticipantMedicalIdentifier();
				participantMedicalIdentifier.setId(new Long(form.getParticipantsMedicalIdentifierId()));
			}
			else
			{
				collectionProtocolRegistration.setProtocolParticipantIdentifier(form.getProtocolParticipantIdentifier());
				collectionProtocolRegistration.setParticipant(null);
			}
			
			CollectionProtocol collectionProtocol = new CollectionProtocol();
			collectionProtocol.setId(new Long(form.getCollectionProtocolId()));
			collectionProtocolRegistration.setCollectionProtocol(collectionProtocol);
			
			/**
			 * Setting the consentTier responses for SCG Level. 
			 * Virender Mehta
			 */
			this.consentTierStatusCollection = prepareParticipantResponseCollection(form);
			
			// ----------- Mandar --15-Jan-07
			this.consentWithdrawalOption = form.getWithdrawlButtonStatus();  
			//Mandar: 19-Jan-07 :- For applying changes to specimen
			this.applyChangesTo = form.getApplyChangesTo(); 
			this.stringOfResponseKeys = form.getStringOfResponseKeys();
			
   /**
	 * Name : Ashish Gupta
	 * Reviewer Name : Sachin Lale 
	 * Bug ID: 2741
	 * Patch ID: 2741_3	 
	 * Description: Populating events in SCG
	*/			
			//Adding Events
			setEventsFromForm(form,form.getOperation());
			//Adding events to Specimens
			if(form.isApplyEventsToSpecimens())
			{
				applyEventsToSpecimens = true;
			}
		}
		catch(Exception e)
		{
			Logger.out.error(e.getMessage(),e);
			throw new AssignDataException();
		}
	}   

    /**
	* For Consent Tracking
	* Setting the Domain Object 
	* @param  form CollectionProtocolRegistrationForm
	* @return consentResponseColl
	*/
	private Collection prepareParticipantResponseCollection(SpecimenCollectionGroupForm form) 
	{
		MapDataParser mapdataParser = new MapDataParser("edu.wustl.catissuecore.bean");
        Collection beanObjColl=null;
		try
		{
			beanObjColl = mapdataParser.generateData(form.getConsentResponseForScgValues());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
        Iterator iter = beanObjColl.iterator();
        Collection consentResponseColl = new HashSet();
        while(iter.hasNext())
        {
        	ConsentBean consentBean = (ConsentBean)iter.next();
        	ConsentTierStatus consentTierstatus = new ConsentTierStatus();
        	//Setting response
        	consentTierstatus.setStatus(consentBean.getSpecimenCollectionGroupLevelResponse());
        	if(consentBean.getSpecimenCollectionGroupLevelResponseID()!=null&&consentBean.getSpecimenCollectionGroupLevelResponseID().trim().length()>0)
        	{
        		consentTierstatus.setId(Long.parseLong(consentBean.getSpecimenCollectionGroupLevelResponseID()));
        	}
        	//Setting consent tier
        	ConsentTier consentTier = new ConsentTier();
        	consentTier.setId(Long.parseLong(consentBean.getConsentTierID()));
        	consentTierstatus.setConsentTier(consentTier);	        	
        	consentResponseColl.add(consentTierstatus);
        }
        return consentResponseColl;
	}
	

	/**
	 * Name : Ashish Gupta
	 * Reviewer Name : Sachin Lale 
	 * Bug ID: 2741
	 * Patch ID: 2741_4	 
	 * Description: Method to populate Events in SCG
	*/
	/**
	 * @param form
	 * This function populates all events for the given scg
	 */
	private void setEventsFromForm(SpecimenCollectionGroupForm form,String operation)
	{
		CollectionEventParameters collectionEventParameters = null;
		ReceivedEventParameters receivedEventParameters = null;
		Collection tempColl = new HashSet();
	
		//Collection Events
		if(operation.equals(Constants.ADD))
		{
			collectionEventParameters = new CollectionEventParameters();
			receivedEventParameters = new ReceivedEventParameters();
		}
		else
		{
			Iterator iter = specimenEventParametersCollection.iterator();
			while(iter.hasNext())
			{
				Object temp = iter.next();
				if(temp instanceof CollectionEventParameters)
				{
					collectionEventParameters = (CollectionEventParameters)temp;
				}
				else if(temp instanceof ReceivedEventParameters)
				{
					receivedEventParameters = (ReceivedEventParameters)temp;
				}
			}
			if(form.getCollectionEventId() != 0)
			{
				collectionEventParameters.setId(new Long(form.getCollectionEventId()));
				receivedEventParameters.setId(new Long(form.getReceivedEventId()));
			}
		}
		//creating new events when there are no events associated with the scg
		if(collectionEventParameters == null && receivedEventParameters == null)
		{
			collectionEventParameters = new CollectionEventParameters();
			receivedEventParameters = new ReceivedEventParameters();
		}
		setEventParameters(collectionEventParameters,receivedEventParameters,form);				
		
		tempColl.add(collectionEventParameters);
		tempColl.add(receivedEventParameters);
		if(operation.equals(Constants.ADD))
		{
			this.specimenEventParametersCollection.add(collectionEventParameters);
			this.specimenEventParametersCollection.add(receivedEventParameters);
		}
		else
		{
			this.specimenEventParametersCollection = tempColl;
		}		
	}
	/**
	 * @param collectionEventParameters
	 * @param receivedEventParameters
	 * @param form
	 */
	private void setEventParameters(CollectionEventParameters collectionEventParameters,ReceivedEventParameters receivedEventParameters,SpecimenCollectionGroupForm form)
	{
		collectionEventParameters.setCollectionProcedure(form.getCollectionEventCollectionProcedure());
		collectionEventParameters.setComments(form.getCollectionEventComments());
		collectionEventParameters.setContainer(form.getCollectionEventContainer());		
		Date timestamp = EventsUtil.setTimeStamp(form.getCollectionEventdateOfEvent(),form.getCollectionEventTimeInHours(),form.getCollectionEventTimeInMinutes());
		collectionEventParameters.setTimestamp(timestamp);
		User user = new User();
		user.setId(new Long(form.getCollectionEventUserId()));
		collectionEventParameters.setUser(user);	
		collectionEventParameters.setSpecimenCollectionGroup(this);	
		
		//Received Events		
		receivedEventParameters.setComments(form.getReceivedEventComments());
		User receivedUser = new User();
		receivedUser.setId(new Long(form.getReceivedEventUserId()));
		receivedEventParameters.setUser(receivedUser);
		receivedEventParameters.setReceivedQuality(form.getReceivedEventReceivedQuality());		
		Date receivedTimestamp = EventsUtil.setTimeStamp(form.getReceivedEventDateOfEvent(),form.getReceivedEventTimeInHours(),form.getReceivedEventTimeInMinutes());
		receivedEventParameters.setTimestamp(receivedTimestamp);		
		receivedEventParameters.setSpecimenCollectionGroup(this);
	}
	
	 /**
     * Returns message label to display on success add or edit
     * @return String
     */
	public String getMessageLabel() {		
		return this.name;
	}
	
	/**
	 * Name: Sachin Lale 
     * Bug ID: 3052
     * Patch ID: 3052_2
     * Seea also: 1-4 and 1_1 to 1_5
	 * Returns the Specimen Collection Group comment .
	 * @hibernate.property name="comment" type="string" column="COMMENTS" length="2000"
	 * @return comment.
	 * @see #setComment(String)
	 */
	public String getComment() {
		return comment;
	}
	/**
	 * @param name The name to set.
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}	
		//----------------------------Mandar 15-jan-07
	public String getConsentWithdrawalOption() {
		return consentWithdrawalOption;
	}

	public void setConsentWithdrawalOption(String consentWithdrawalOption) {
		this.consentWithdrawalOption = consentWithdrawalOption;
	}
	
	//------------------------- Mandar 19-Jan-07
	public String getApplyChangesTo() {
		return applyChangesTo;
	}
	public void setApplyChangesTo(String applyChangesTo) {
		this.applyChangesTo = applyChangesTo;
	}
	// ------------------- Mandar : - 22-Jan-07

	public String getStringOfResponseKeys() {
		return stringOfResponseKeys;
	}

	public void setStringOfResponseKeys(String stringOfResponseKeys) {
		this.stringOfResponseKeys = stringOfResponseKeys;
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
     * Returns the surgicalPathologyNumber of the report at the time of specimen collection. 
     * @hibernate.property name="surgicalPathologyNumber" type="string" 
     * column="SURGICAL_PATHOLOGY_NUMBER" length="50"
     * @return surgical pathology number of the report at the time of specimen collection.
     * @see #setSurgicalPathologyNumber(String)
     */
	public String getSurgicalPathologyNumber() 
	{
		return surgicalPathologyNumber;
	}

	/**
     * Sets the surgical pathology number of the report at the time of specimen collection. 
     * @param surgicalPathologyNumber the surgical pathology report of the report at the time of specimen collection.
     * @see #getSurgicalPathologyNumber()
     */
	public void setSurgicalPathologyNumber(String surgicalPathologyNumber) 
	{
		this.surgicalPathologyNumber = surgicalPathologyNumber;
	} 

	/**
	 * Returns deidentified surgical pathology report of the current specimen collection group
	 * @hibernate.one-to-one  name="deIdentifiedSurgicalPathologyReport"
	 * class="edu.wustl.catissuecore.domain.pathology.DeidentifiedSurgicalPathologyReport"
	 * property-ref="specimenCollectionGroup" not-null="false" cascade="save-update"
	 */
    public DeidentifiedSurgicalPathologyReport getDeIdentifiedSurgicalPathologyReport() 
    {
		return deIdentifiedSurgicalPathologyReport;
	}

    /**
     * Sets the deidentified surgical pathology report associated with the specimen collection group
     * @param deIdentifiedSurgicalPathologyReport deidentified report object
     */
	public void setDeIdentifiedSurgicalPathologyReport(DeidentifiedSurgicalPathologyReport deIdentifiedSurgicalPathologyReport) 
	{
		this.deIdentifiedSurgicalPathologyReport = deIdentifiedSurgicalPathologyReport;
	}	
	/**
	 * Returns deidentified surgical pathology report of the current specimen collection group
	 * @hibernate.one-to-one  name="identifiedSurgicalPathologyReport"
	 * class="edu.wustl.catissuecore.domain.pathology.IdentifiedSurgicalPathologyReport"
	 * propertyref="specimenCollectionGroup" not-null="false" cascade="save-update"
	 */
	public IdentifiedSurgicalPathologyReport getIdentifiedSurgicalPathologyReport() 
	{
		return identifiedSurgicalPathologyReport;
	}
	
	/**
	 *  Sets the identified surgical pathology report associated with the specimen collection group
	 * @param identifiedSurgicalPathologyReport identified report object
	 */
	public void setIdentifiedSurgicalPathologyReport(IdentifiedSurgicalPathologyReport identifiedSurgicalPathologyReport) 
	{
		this.identifiedSurgicalPathologyReport = identifiedSurgicalPathologyReport;
	}
}
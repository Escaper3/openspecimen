package edu.wustl.catissuecore.domain.pathology;


import java.sql.Clob;
import java.util.Collection;
import java.util.Date;

import edu.wustl.catissuecore.domain.SpecimenCollectionGroup;
import edu.wustl.common.actionForm.IValueObject;
import edu.wustl.common.domain.AbstractDomainObject;
import edu.wustl.common.exception.AssignDataException;


/**
 * Represents different logical sections of surgical pathology report.
 * @hibernate.class
 * table="CATISSUE_REPORT_QUEUE"
 */
public class ReportLoaderQueue extends AbstractDomainObject
{

	protected Long id;
	protected Clob reportText;
	protected Collection participantCollection;
	protected String status;
	protected SpecimenCollectionGroup specimenCollectionGroup;
	protected String surgicalPathologyNumber;
	protected String participantName;
	protected Date reportLoadedDate;
	protected String siteName;
	
	/**
	 * @return status information. 
     * @hibernate.property name="status"
     * type="string" column="STATUS" 
     * length="10"
     */
	public String getStatus()
	{
		return status;
	}

	
	/**
	 * Set the status of the queue record
	 * @param status status of the record of queue
	 */
	public void setStatus(String status)
	{
		this.status = status;
	}

	/**
	 * default Constructor
	 */
	public ReportLoaderQueue()
	{
		
	}
	
	/**
	 * Constructor with text as input
	 * @param text report text
	 * 
	 */
	public ReportLoaderQueue(Clob text)
	{
		//this.id=new Long(4);
		this.reportText=text;
	}
	
	
	/**
	 * @return system generated id
	 * @hibernate.id name="id" column="IDENTIFIER" type="long" length="30"
	 * unsaved-value="null" generator-class="native" 
	 * @hibernate.generator-param name="sequence" value="CATISSUE_REPORT_QUEUE_SEQ"
	 */
	public Long getId()
	{
		return id;
	}
	
	/**
	 * Set id of the object
	 * @param id of the object
	 */
	public void setId(Long id)
	{
		this.id = id;
	}
	
	/**
	 * Returns the collection of Studies for this Protocol.
	 * @hibernate.set name="participantCollection" table="CATISSUE_REPORT_PARTICIP_REL" 
	 * cascade="save-update" inverse="false" lazy="false"
	 * @hibernate.collection-key column="REPORT_ID"
	 * @hibernate.collection-many-to-many class="edu.wustl.catissuecore.domain.Participant" column="PARTICIPANT_ID"
	 * @return Returns the collection of Studies for this Protocol.
	 */
	public Collection getParticipantCollection()
	{
		return participantCollection;
	}
	
	/**
	 * @param collection
	 * Assign set of participants to current object
	 */
	public void setParticipantCollection(Collection collection)
	{
		this.participantCollection = collection;
	}
	/**
	 * @return reportText information. 
     * @hibernate.property name="reportText"
     * type="java.sql.Clob" column="REPORT_TEXT" 
     * length="4000"
     */	
	public Clob getReportText()
	{
		return reportText;
	}
	
	/**
	 * Set report text
	 * @param reportText report text 
	 */
	public void setReportText(Clob reportText)
	{
		this.reportText = reportText;
	}

	public void setAllValues(IValueObject abstractForm) throws AssignDataException
	{		
		
	}
	
	/**
	 * @return the specimenCollectionGroup
	 * @hibernate.many-to-one class="edu.wustl.catissuecore.domain.SpecimenCollectionGroup"  column="SPECIMEN_COLL_GRP_ID" cascade="save-update"
	 *
	 */
	public SpecimenCollectionGroup getSpecimenCollectionGroup()
	{
		return specimenCollectionGroup;
	}
	
	/**
	 * @param specimenCollectionGroup the specimenCollectionGroup to set
	 */
	public void setSpecimenCollectionGroup(SpecimenCollectionGroup specimenCollectionGroup)
	{
		this.specimenCollectionGroup = specimenCollectionGroup;
	}
	
	/**
	 * @return surgical pathology number  
     * @hibernate.property name="surgicalPathologyNumber"
     * type="string" column="SURGICAL_PATHOLOGY_NUMBER" 
     * length="255"
     */
	public String getSurgicalPathologyNumber() 
	{
		return surgicalPathologyNumber;
	}


	/**
	 * @param surgicalPathologyNumber
	 */
	public void setSurgicalPathologyNumber(String accessionNumber) 
	{
		this.surgicalPathologyNumber = accessionNumber;
	}


	/**
	 * @return participant name 
     * @hibernate.property name="participantName"
     * type="string" column="PARTICIPANT_NAME" 
     * length="255"
     */
	public String getParticipantName() 
	{
		return participantName;
	}

	/**
	 * @param participantName
	 */
	public void setParticipantName(String participantName) 
	{
		this.participantName = participantName;
	}


	/**
	 * @return report Loaded Date 
     * @hibernate.property name="reportLoadedDate"
     * type="date" column="REPORT_LOADED_DATE" 
     */
	public Date getReportLoadedDate() 
	{
		return reportLoadedDate;
	}


	/**
	 * @param reportLoadedDate
	 */
	public void setReportLoadedDate(Date reportLoadedDate) 
	{
		this.reportLoadedDate = reportLoadedDate;
	}

	/**
	 * @return site name 
     * @hibernate.property name="siteName"
     * type="string" column="SITE_NAME" 
     * length="255"
     */
	public String getSiteName() 
	{
		return siteName;
	}

	/**
	 * @param siteName
	 */
	public void setSiteName(String siteName) 
	{
		this.siteName = siteName;
	}
}

/**
 * This class is designed to contain common methods for the Consent Withdraw process.
 * This class will be used in CollectionProtocolRegistration, SpecimenCollectionGroup and Specimen Bizlogic classes.
 * 
 * @author mandar_deshmukh
 *  
 */
package edu.wustl.catissuecore.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.wustl.catissuecore.bean.ConsentBean;
import edu.wustl.catissuecore.bizlogic.BizLogicFactory;
import edu.wustl.catissuecore.bizlogic.CollectionProtocolBizLogic;
import edu.wustl.catissuecore.bizlogic.NewSpecimenBizLogic;
import edu.wustl.catissuecore.domain.CollectionProtocol;
import edu.wustl.catissuecore.domain.CollectionProtocolRegistration;
import edu.wustl.catissuecore.domain.ConsentTier;
import edu.wustl.catissuecore.domain.ConsentTierResponse;
import edu.wustl.catissuecore.domain.ConsentTierStatus;
import edu.wustl.catissuecore.domain.DisposalEventParameters;
import edu.wustl.catissuecore.domain.ReturnEventParameters;
import edu.wustl.catissuecore.domain.Specimen;
import edu.wustl.catissuecore.domain.SpecimenCollectionGroup;
import edu.wustl.catissuecore.domain.SpecimenPosition;
import edu.wustl.catissuecore.domain.StorageContainer;
import edu.wustl.catissuecore.domain.User;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.beans.NameValueBean;
import edu.wustl.common.beans.SessionDataBean;
import edu.wustl.common.bizlogic.IBizLogic;
import edu.wustl.common.dao.DAO;
import edu.wustl.common.util.dbManager.DAOException;

/**
 * This class is designed to contain common methods for the Consent Withdraw process.
 * This class will be used in CollectionProtocolRegistration, SpecimenCollectionGroup and Specimen Bizlogic classes.
 * 
 * @author mandar_deshmukh
 *  
 */

public class ConsentUtil
{
	/**
	 * This method updates the SpecimenCollectionGroup instance by setting all the consent tierstatus to with 
	 * @param scg Instance of SpecimenCollectionGroup to be updated.
	 * @param oldScg Instance of OldSpecimenCollectionGroup to be updated.
	 * @param consentTierID Identifier of ConsentTier to be withdrawn.
	 * @param withdrawOption Action to be performed on the withdrawn collectiongroup.
	 * @param dao DAO instance. Used for inserting disposal event. 
	 * @param sessionDataBean SessionDataBean instance. Used for inserting disposal event.
	 * @throws DAOException 
	 */
	public static void updateSCG(SpecimenCollectionGroup scg, SpecimenCollectionGroup oldscg, long consentTierID, String withdrawOption,  DAO dao, SessionDataBean sessionDataBean) throws DAOException
	{
		Collection newScgStatusCollection = new HashSet();
		Collection consentTierStatusCollection =scg.getConsentTierStatusCollection();
		Iterator itr = consentTierStatusCollection.iterator() ;
		while(itr.hasNext() )
		{
			ConsentTierStatus consentTierstatus = (ConsentTierStatus)itr.next();
			//compare consent tier id of scg with cpr consent tier of response
			if(consentTierstatus.getConsentTier().getId().longValue() == consentTierID)
			{
				consentTierstatus.setStatus(Constants.WITHDRAWN);
				updateSpecimensInSCG(scg,oldscg,consentTierID,withdrawOption , dao, sessionDataBean );
			}
			newScgStatusCollection.add(consentTierstatus );	// set updated consenttierstatus in scg
		}
		scg.setConsentTierStatusCollection( newScgStatusCollection);
		if(!(withdrawOption.equals(Constants.WITHDRAW_RESPONSE_RESET)))
		{	
			scg.setActivityStatus(Constants.ACTIVITY_STATUS_DISABLED);
		}
	}
	
	/**
	 * This method updates the SpecimenCollectionGroup instance by setting all the consent tierstatus to with 
	 * @param scg Instance of SpecimenCollectionGroup to be updated.
	 * @param consentTierID Identifier of ConsentTier to be withdrawn.
	 * @param withdrawOption Action to be performed on the withdrawn collectiongroup.
	 * @param dao DAO instance. Used for inserting disposal event. 
	 * @param sessionDataBean SessionDataBean instance. Used for inserting disposal event.
	 *  
	 */
	public static void updateSCG(SpecimenCollectionGroup scg, long consentTierID, String withdrawOption,  DAO dao, SessionDataBean sessionDataBean) throws DAOException
	{
		updateSCG(scg, scg, consentTierID,withdrawOption,dao, sessionDataBean);
	}
	
	
	/*
	 * This method updates the specimens for the given SCG and sets the consent status to withdraw.
	 */
	private static void updateSpecimensInSCG(SpecimenCollectionGroup scg, SpecimenCollectionGroup oldscg, long consentTierID, String consentWithdrawalOption,  DAO dao, SessionDataBean sessionDataBean) throws DAOException
	{
		Collection specimenCollection =(Collection)dao.retrieveAttribute(SpecimenCollectionGroup.class.getName(),scg.getId(),"elements(specimenCollection)"); 
		Collection updatedSpecimenCollection = new HashSet();
		Iterator specimenItr = specimenCollection.iterator() ;
		while(specimenItr.hasNext())
		{
			Specimen specimen = (Specimen)specimenItr.next();
			updateSpecimenStatus(specimen, consentWithdrawalOption, consentTierID, dao, sessionDataBean);
			updatedSpecimenCollection.add(specimen );
		}
		scg.setSpecimenCollection(updatedSpecimenCollection);
	}
	
	/**
	 * This method updates the Specimen instance by setting all the consenttierstatus to withdraw. 
	 * @param specimen  Instance of Specimen to be updated. 
	 * @param consentWithdrawalOption Action to be performed on the withdrawn specimen.
	 * @param consentTierID Identifier of ConsentTier to be withdrawn.
	 * @param dao DAO instance. Used for inserting disposal event. 
	 * @param sessionDataBean SessionDataBean instance. Used for inserting disposal event.
	 * @throws DAOException 
	 */
	public static void updateSpecimenStatus(Specimen specimen, String consentWithdrawalOption, long consentTierID,  DAO dao, SessionDataBean sessionDataBean) throws DAOException
	{
		
		Collection consentTierStatusCollection = specimen.getConsentTierStatusCollection();
		Collection updatedSpecimenStatusCollection = new HashSet();
		Iterator specimenStatusItr = consentTierStatusCollection.iterator() ;
		while(specimenStatusItr.hasNext() )
		{
			ConsentTierStatus consentTierstatus = (ConsentTierStatus)specimenStatusItr.next() ;
			if(consentTierstatus.getConsentTier().getId().longValue() == consentTierID)
			{
				if(consentWithdrawalOption != null)
				{					
					consentTierstatus.setStatus(Constants.WITHDRAWN );
					withdrawResponse(specimen, consentWithdrawalOption,   dao,  sessionDataBean);
				}
			}
			updatedSpecimenStatusCollection.add(consentTierstatus );
		}
		specimen.setConsentTierStatusCollection( updatedSpecimenStatusCollection);
		updateChildSpecimens(specimen, consentWithdrawalOption, consentTierID, dao, sessionDataBean);
	}

	/*
	 * This method performs an action on specimen based on user response.
	 */
	private static void withdrawResponse(Specimen specimen, String consentWithdrawalOption,  DAO dao, SessionDataBean sessionDataBean)
	{
		if(consentWithdrawalOption.equalsIgnoreCase(Constants.WITHDRAW_RESPONSE_DISCARD))
		{
			addDisposalEvent(specimen, dao, sessionDataBean);
		}
		else if(consentWithdrawalOption.equalsIgnoreCase(Constants.WITHDRAW_RESPONSE_RETURN))
		{
			addReturnEvent(specimen, dao, sessionDataBean);
		}
		//only if consentWithdrawalOption is not reset or noaction.
		if(!consentWithdrawalOption.equalsIgnoreCase(Constants.WITHDRAW_RESPONSE_RESET) && !consentWithdrawalOption.equalsIgnoreCase(Constants.WITHDRAW_RESPONSE_NOACTION) )
		{
			specimen.setActivityStatus(Constants.ACTIVITY_STATUS_DISABLED);
			specimen.setAvailable(new Boolean(false) );

			if(specimen.getSpecimenPosition() != null && specimen.getSpecimenPosition().getStorageContainer() !=null)		// locations cleared
			{
				Map containerMap = null;
				try
				{
					containerMap = StorageContainerUtil.getContainerMapFromCache();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				StorageContainerUtil.insertSinglePositionInContainerMap(specimen.getSpecimenPosition().getStorageContainer(),containerMap,specimen.getSpecimenPosition().getPositionDimensionOne().intValue(), specimen.getSpecimenPosition().getPositionDimensionTwo().intValue()    );
			}
			specimen.setSpecimenPosition(null);
//			specimen.setPositionDimensionOne(null);
//			specimen.setPositionDimensionTwo(null);
//			specimen.setStorageContainer(null);
			specimen.setAvailableQuantity(null);
			specimen.setInitialQuantity(null);
		}
	}
	
	private static void addReturnEvent(Specimen specimen, DAO dao, SessionDataBean sessionDataBean)
	{
		try
		{
			Collection eventCollection = specimen.getSpecimenEventCollection();
			if(!isEventAdded(eventCollection, "ReturnEventParameters"))
			{
				ReturnEventParameters returnEvent = new ReturnEventParameters();
				returnEvent.setSpecimen(specimen );
				dao.insert(returnEvent,sessionDataBean,true,true) ;
				
				eventCollection.add(returnEvent);
				specimen.setSpecimenEventCollection(eventCollection);
			}
		}
		catch(Exception excp)
		{
			excp.printStackTrace(); 
		}
	}

	/*
	 * This method adds a disposal event to the specimen.
	 */
	private static void addDisposalEvent(Specimen specimen, DAO dao, SessionDataBean sessionDataBean)
	{
		try
		{
			Collection eventCollection = specimen.getSpecimenEventCollection();
			if(!isEventAdded(eventCollection, "DisposalEventParameters"))
			{
				new NewSpecimenBizLogic().disposeSpecimen(sessionDataBean, specimen, dao);
			}
		}
		catch(Exception excp)
		{
			excp.printStackTrace(); 
		}
	}
	
	private static void updateChildSpecimens(Specimen specimen, String consentWithdrawalOption, long consentTierID, DAO dao, SessionDataBean sessionDataBean) throws DAOException
	{
		Long specimenId = (Long)specimen.getId();	
		Collection childSpecimens = (Collection)dao.retrieveAttribute(Specimen.class.getName(),specimenId,"elements(childrenSpecimen)");
		//Collection childSpecimens = specimen.getChildrenSpecimen();
		if(childSpecimens!=null)
		{	
			Iterator childItr = childSpecimens.iterator();  
			while(childItr.hasNext() )
			{
				Specimen childSpecimen = (Specimen)childItr.next();
				consentWithdrawForchildSpecimens(childSpecimen , dao,  sessionDataBean, consentWithdrawalOption, consentTierID);
			}
		}	
	}
	
	private static void consentWithdrawForchildSpecimens(Specimen specimen, DAO dao, SessionDataBean sessionDataBean, String consentWithdrawalOption, long consentTierID) throws DAOException
	{
		if(specimen!=null)
		{
			updateSpecimenStatus(specimen,  consentWithdrawalOption, consentTierID, dao, sessionDataBean);
			Collection childSpecimens = specimen.getChildrenSpecimen();
			Iterator itr = childSpecimens.iterator();  
			while(itr.hasNext() )
			{
				Specimen childSpecimen = (Specimen)itr.next();
				consentWithdrawForchildSpecimens(childSpecimen, dao, sessionDataBean, consentWithdrawalOption, consentTierID);
			}
		}
	}

	/**
	 * This method is used to copy the consents from parent specimen to the child specimen.
	 * 
	 * @param specimen Instance of specimen. It is the child specimen to which the consents will be set.
	 * @param parentSpecimen Instance of specimen. It is the parent specimen from which the consents will be copied.
	 * @throws DAOException 
	 */
	public static void setConsentsFromParent(Specimen specimen, Specimen parentSpecimen, DAO dao) throws DAOException
	{
		Collection consentTierStatusCollection = new HashSet();
		//Lazy Resolved ----  parentSpecimen.getConsentTierStatusCollection();
		Collection parentStatusCollection = (Collection)dao.retrieveAttribute(Specimen.class.getName(), parentSpecimen.getId(),"elements(consentTierStatusCollection)"); 
		Iterator parentStatusCollectionIterator = parentStatusCollection.iterator();
		while(parentStatusCollectionIterator.hasNext() )
		{
			ConsentTierStatus cts = (ConsentTierStatus)parentStatusCollectionIterator.next();
			ConsentTierStatus newCts = new ConsentTierStatus();
			newCts.setStatus(cts.getStatus());
			newCts.setConsentTier(cts.getConsentTier());
			consentTierStatusCollection.add(newCts);
		}
		specimen.setConsentTierStatusCollection( consentTierStatusCollection);
	}
	
	/*
	 * This method checks if the given event is already added to the specimen.
	 */
	private static boolean isEventAdded(Collection eventCollection, String eventType)
	{
		boolean result = false;
		Iterator eventCollectionIterator = eventCollection.iterator();
		while(eventCollectionIterator.hasNext() )
		{
			Object event = eventCollectionIterator.next();
			if(event.getClass().getSimpleName().equals(eventType))
			{
				result= true;
				break;
			}
		}
		return result;
	}
	// ----------------WITHDRAW functionality end
	//--------Mandar : - 24-Jan-07 ------------------ApplyChanges Functionality start
	 
	/**
	 * This method updates the specimens status for the given SCG.
	 * @param specimenCollectionGroup
	 * @param oldSpecimenCollectionGroup
	 * @param dao
	 * @param sessionDataBean
	 * @throws DAOException 
	 */
	public static void updateSpecimenStatusInSCG(SpecimenCollectionGroup specimenCollectionGroup,SpecimenCollectionGroup oldSpecimenCollectionGroup, DAO dao) throws DAOException
	{
		Collection newConsentTierStatusCollection = specimenCollectionGroup.getConsentTierStatusCollection();
		Collection oldConsentTierStatusCollection =  oldSpecimenCollectionGroup.getConsentTierStatusCollection();
		Iterator itr = newConsentTierStatusCollection.iterator() ;
		while(itr.hasNext() )
		{
			ConsentTierStatus consentTierStatus = (ConsentTierStatus)itr.next();
			String statusValue = consentTierStatus.getStatus();
			long consentTierID = consentTierStatus.getConsentTier().getId().longValue();
			updateSCGSpecimenCollection(specimenCollectionGroup, oldSpecimenCollectionGroup, consentTierID, statusValue, newConsentTierStatusCollection, oldConsentTierStatusCollection,dao);	
		}
	}

	/*
	 * This method updates the specimen consent status. 
	 */
	private static void updateSCGSpecimenCollection(SpecimenCollectionGroup specimenCollectionGroup, SpecimenCollectionGroup oldSpecimenCollectionGroup, long consentTierID, String  statusValue, Collection newSCGConsentCollection, Collection oldSCGConsentCollection,DAO dao) throws DAOException
	{
		Collection specimenCollection = (Collection)dao.retrieveAttribute(SpecimenCollectionGroup.class.getName(), specimenCollectionGroup.getId(),"elements(specimenCollection)");  
		//oldSpecimenCollectionGroup.getSpecimenCollection();
		Collection updatedSpecimenCollection = new HashSet();
		String applyChangesTo =  specimenCollectionGroup.getApplyChangesTo(); 
		Iterator specimenItr = specimenCollection.iterator() ;
		while(specimenItr.hasNext() )
		{
			Specimen specimen = (Specimen)specimenItr.next();
			updateSpecimenConsentStatus(specimen, applyChangesTo, consentTierID, statusValue, newSCGConsentCollection, oldSCGConsentCollection, dao );
			updatedSpecimenCollection.add(specimen );
		}
		specimenCollectionGroup.setSpecimenCollection(updatedSpecimenCollection );
	}
	
	public static void updateSpecimenConsentStatus(Specimen specimen, String applyChangesTo, long consentTierID, String  statusValue, Collection newConsentCollection, Collection oldConsentCollection,DAO dao) throws DAOException
	{
		if(applyChangesTo.equalsIgnoreCase(Constants.APPLY_ALL))
			updateSpecimenConsentStatus(specimen, consentTierID, statusValue, applyChangesTo, dao);
		else if(applyChangesTo.equalsIgnoreCase(Constants.APPLY))
		{
			//To pass both collections
			checkConflictingConsents(newConsentCollection, oldConsentCollection, specimen, dao);
		}
	}
	
	/**
	 * This method updates the Specimen instance by setting all the consenttierstatus to withdraw. 
	 * @param specimen  Instance of Specimen to be updated. 
	 * @param consentWithdrawalOption Action to be performed on the withdrawn specimen.
	 * @param consentTierID Identifier of ConsentTier to be withdrawn.
	 * @throws DAOException 
	 */
	private static void updateSpecimenConsentStatus(Specimen specimen, long consentTierID, String statusValue, String applyChangesTo, DAO dao) throws DAOException
	{
		Collection consentTierStatusCollection = specimen.getConsentTierStatusCollection();
		Collection updatedSpecimenStatusCollection = new HashSet();
		Iterator specimenStatusItr = consentTierStatusCollection.iterator() ;
		while(specimenStatusItr.hasNext() )
		{
			ConsentTierStatus consentTierstatus = (ConsentTierStatus)specimenStatusItr.next() ;
			if(consentTierstatus.getConsentTier().getId().longValue() == consentTierID)
			{
				consentTierstatus.setStatus(statusValue);
			}
			updatedSpecimenStatusCollection.add(consentTierstatus);
		}
		specimen.setConsentTierStatusCollection(updatedSpecimenStatusCollection);
		
		//to update child specimens
		Collection childSpecimens = specimen.getChildrenSpecimen();
		Iterator childItr = childSpecimens.iterator();  
		while(childItr.hasNext() )
		{
			Specimen childSpecimen = (Specimen)childItr.next();
			consentStatusUpdateForchildSpecimens(childSpecimen , consentTierID, statusValue ,applyChangesTo, dao);
		}
	}

	private static void consentStatusUpdateForchildSpecimens(Specimen specimen, long consentTierID, String statusValue, String applyChangesTo, DAO dao) throws DAOException
	{
		if(specimen!=null)
		{
			updateSpecimenConsentStatus(specimen, consentTierID, statusValue, applyChangesTo, dao);
			Collection childSpecimens = specimen.getChildrenSpecimen();
			Iterator itr = childSpecimens.iterator();  
			while(itr.hasNext() )
			{
				Specimen childSpecimen = (Specimen)itr.next();
				consentStatusUpdateForchildSpecimens(childSpecimen, consentTierID, statusValue, applyChangesTo, dao);
			}
		}
	}

	/*
	 * This method verifies the consents of SCG and specimen for any conflicts.
	 */
	private static void checkConflictingConsents(Collection newConsentCollection, Collection oldConsentCollection, Specimen specimen, DAO dao ) throws DAOException
	{
/*		 if oldSCG.c1 == S.c1 then update specimen with new SCG.c1
 * 			OR
 *		 if oldS.c1 == cS.c1 then update child specimen with new S.c1
 */		
		Iterator oldConsentItr = oldConsentCollection.iterator();
		while(oldConsentItr.hasNext() )
		{
			ConsentTierStatus oldConsentStatus = (ConsentTierStatus)oldConsentItr.next() ;
			Collection specimenConsentStatusCollection = specimen.getConsentTierStatusCollection();
			Iterator specimenConsentStatusItr = specimenConsentStatusCollection.iterator() ;
			Collection updatedSpecimenConsentStatusCollection = new HashSet();
			while(specimenConsentStatusItr.hasNext() )
			{
				ConsentTierStatus specimenConsentStatus = (ConsentTierStatus)specimenConsentStatusItr.next() ;
				if(oldConsentStatus.getConsentTier().getId().longValue() == specimenConsentStatus.getConsentTier().getId().longValue() )
				{
					if(oldConsentStatus.getStatus().equals(specimenConsentStatus.getStatus()))
					{
						Iterator newConsentItr = newConsentCollection.iterator();
						while(newConsentItr.hasNext() )
						{
							ConsentTierStatus newConsentStatus = (ConsentTierStatus)newConsentItr.next() ;
							if(newConsentStatus.getConsentTier().getId().longValue() == specimenConsentStatus.getConsentTier().getId().longValue() )
							{
								specimenConsentStatus.setStatus(newConsentStatus.getStatus()); 
							}
						}
					}
				}
				updatedSpecimenConsentStatusCollection.add(specimenConsentStatus);
			}
			specimen.setConsentTierStatusCollection(updatedSpecimenConsentStatusCollection);
		}

		//to update child specimens
		Collection childSpecimens = specimen.getChildrenSpecimen();
		Iterator childItr = childSpecimens.iterator();  
		while(childItr.hasNext() )
		{
			Specimen childSpecimen = (Specimen)childItr.next();
			consentStatusUpdateForchildSpecimens(childSpecimen , newConsentCollection, oldConsentCollection, dao);
		}
	}
	
	private static void consentStatusUpdateForchildSpecimens(Specimen specimen, Collection newConsentCollection, Collection oldConsentCollection, DAO dao) throws DAOException
	{
		if(specimen!=null)
		{
			checkConflictingConsents(newConsentCollection, oldConsentCollection, specimen, dao);
			Collection childSpecimens = specimen.getChildrenSpecimen();
			Iterator itr = childSpecimens.iterator();  
			while(itr.hasNext() )
			{
				Specimen childSpecimen = (Specimen)itr.next();
				consentStatusUpdateForchildSpecimens(childSpecimen, newConsentCollection, oldConsentCollection, dao);
			}
		}
	}
	// ------------------------ Mandar : 24-Jan-07 Apply changes --------- end
	/**
	 * This function is used for retriving Specimen collection group  from Collection protocol registration Object
	 * @param specimenObj
	 * @param finalDataList
	 * @throws DAOException 
	 */
	public static void getSpecimenDetails(CollectionProtocolRegistration collectionProtocolRegistration, List finalDataList) throws DAOException
	{
		IBizLogic bizLogic = BizLogicFactory.getInstance().getBizLogic(Constants.DEFAULT_BIZ_LOGIC);
		Collection specimencollectionGroup = (Collection)bizLogic.retrieveAttribute(CollectionProtocolRegistration.class.getName(),collectionProtocolRegistration.getId(), "elements(specimenCollectionGroupCollection)");
		//Collection specimencollectionGroup = collectionProtocolRegistration.getSpecimenCollectionGroupCollection();
		Iterator specimenCollGroupIterator = specimencollectionGroup.iterator();
		while(specimenCollGroupIterator.hasNext())
		{
			SpecimenCollectionGroup specimenCollectionGroupObj =(SpecimenCollectionGroup)specimenCollGroupIterator.next(); 
			getDetailsOfSpecimen(specimenCollectionGroupObj, finalDataList);
		}		
	}
	/**
	 * This function is used for retriving specimen and sub specimen's attributes.
	 * @param specimenObj
	 * @param finalDataList
	 * @throws DAOException 
	 */
	private static void getDetailsOfSpecimen(SpecimenCollectionGroup specimenCollGroupObj, List finalDataList) throws DAOException
	{
		// lazy Resolved specimenCollGroupObj.getSpecimenCollection();
		IBizLogic bizLogic = BizLogicFactory.getInstance().getBizLogic(Constants.DEFAULT_BIZ_LOGIC);
		Collection specimenCollection = (Collection)bizLogic.retrieveAttribute(SpecimenCollectionGroup.class.getName(), specimenCollGroupObj.getId(), "elements(specimenCollection)");
		Iterator specimenIterator = specimenCollection.iterator();
		while(specimenIterator.hasNext())
		{
			Specimen specimenObj =(Specimen)specimenIterator.next();
			List specimenDetailList=new ArrayList();
			if(specimenObj.getActivityStatus().equals(Constants.ACTIVITY_STATUS_ACTIVE))
			{
				specimenDetailList.add(specimenObj.getLabel());
				specimenDetailList.add(specimenObj.getType());
				if(specimenObj.getSpecimenPosition()==null)
				{
					specimenDetailList.add(Constants.VIRTUALLY_LOCATED);
				}
				else
				{
					SpecimenPosition position=	(SpecimenPosition)bizLogic.retrieveAttribute(Specimen.class.getName(), specimenObj.getId(),"specimenPosition");
					String storageLocation=position.getStorageContainer().getName()+": X-Axis-"+position.getPositionDimensionOne()+", Y-Axis-"+position.getPositionDimensionTwo();
					specimenDetailList.add(storageLocation);
				}
				specimenDetailList.add(specimenObj.getClassName());
				finalDataList.add(specimenDetailList);
			}
		}
		
	}
	/**
	 * Adding name,value pair in NameValueBean for Witness Name
	 * @param collProtId Get Witness List for this ID
	 * @return consentWitnessList
	 */ 
	public static List witnessNameList(String collProtId) throws DAOException
	{		
		IBizLogic bizLogic = BizLogicFactory.getInstance().getBizLogic(Constants.DEFAULT_BIZ_LOGIC);
		
		Object object = bizLogic.retrieve(CollectionProtocol.class.getName(), new Long(collProtId));		
		CollectionProtocol collectionProtocol = (CollectionProtocol) object;
		//Setting the consent witness
		String witnessFullName="";
		List consentWitnessList = new ArrayList();
		consentWitnessList.add(new NameValueBean(Constants.SELECT_OPTION,"-1"));
		Collection userCollection = null;
		if(collectionProtocol.getId()!= null)
		{ 
			userCollection = (Collection)bizLogic.retrieveAttribute(CollectionProtocol.class.getName(),collectionProtocol.getId(), "elements(coordinatorCollection)");
		}
		
		Iterator iter = userCollection.iterator();
		while(iter.hasNext())
		{
			User user = (User)iter.next();
			witnessFullName = user.getLastName()+", "+user.getFirstName();
			consentWitnessList.add(new NameValueBean(witnessFullName,user.getId()));
		}
		//Setting the PI
		User principalInvestigator = (User)bizLogic.retrieveAttribute(CollectionProtocol.class.getName(),collectionProtocol.getId(), "principalInvestigator");
		String piFullName=principalInvestigator.getLastName()+", "+principalInvestigator.getFirstName();
		consentWitnessList.add(new NameValueBean(piFullName,principalInvestigator.getId()));
		return consentWitnessList;
	}	
	/**
	 * This function adds the columns to the List
	 * @return columnList 
	 */
	public static List<String> columnNames()
	{
		List<String> columnList = new ArrayList<String>();
		columnList.add(Constants.LABLE);
		columnList.add(Constants.TYPE);
		columnList.add(Constants.STORAGE_CONTAINER_LOCATION);
		columnList.add(Constants.CLASS_NAME);
		return columnList; 
	}
	/**
	 * Adding name,value pair in NameValueBean for Witness Name
	 * @param collProtId Get Witness List for this ID
	 * @return consentWitnessList
	 */ 
	public static Collection getConsentList(String collectionProtocolID) throws DAOException
    {   	
    	CollectionProtocolBizLogic collectionProtocolBizLogic = (CollectionProtocolBizLogic)BizLogicFactory.getInstance().getBizLogic(Constants.COLLECTION_PROTOCOL_FORM_ID);
    	Object object  = collectionProtocolBizLogic.retrieve(CollectionProtocol.class.getName(), new Long(collectionProtocolID));		
		CollectionProtocol collectionProtocol = (CollectionProtocol) object;
		Collection consentTierCollection = (Collection)collectionProtocolBizLogic.retrieveAttribute(CollectionProtocol.class.getName(), collectionProtocol.getId(), "elements(consentTierCollection)");
		return consentTierCollection;
    }
	
	/**
	* For ConsentTracking Preparing consentResponseForScgValues for populating Dynamic contents on the UI  
	* @param partiResponseCollection This Containes the collection of ConsentTier Response at CPR level
	* @param statusResponseCollection This Containes the collection of ConsentTier Response at Specimen level 
	* @return tempMap
	*/
    public static Map prepareSCGResponseMap(Collection statusResponseCollection,
    		Collection partiResponseCollection,String statusResponse, String statusResponseId)
	   {
	    	Map tempMap = new HashMap();
	    	Long consentTierID;
			Long consentID;
			if(partiResponseCollection!=null ||statusResponseCollection!=null)
			{
				int i = 0;
				Iterator statusResponsIter = statusResponseCollection.iterator();			
				while(statusResponsIter.hasNext())
				{
					ConsentTierStatus consentTierstatus=(ConsentTierStatus)statusResponsIter.next();
					consentTierID=consentTierstatus.getConsentTier().getId();
					Iterator participantResponseIter = partiResponseCollection.iterator();
					while(participantResponseIter.hasNext())
					{
						ConsentTierResponse consentTierResponse=(ConsentTierResponse)participantResponseIter.next();
						consentID=consentTierResponse.getConsentTier().getId();
						if(consentTierID.longValue()==consentID.longValue())						
						{
							ConsentTier consent = consentTierResponse.getConsentTier();
							String idKey="ConsentBean:"+i+"_consentTierID";
							String statementKey="ConsentBean:"+i+"_statement";
							String participantResponsekey = "ConsentBean:"+i+"_participantResponse";
							String participantResponceIdKey="ConsentBean:"+i+"_participantResponseID";
							String statusResponsekey  = "ConsentBean:"+i+statusResponse;
							String statusResponseIDkey ="ConsentBean:"+i+statusResponseId;
							
							tempMap.put(idKey, consent.getId());
							tempMap.put(statementKey,consent.getStatement());
							tempMap.put(participantResponsekey, consentTierResponse.getResponse());
							tempMap.put(participantResponceIdKey, consentTierResponse.getId());
							tempMap.put(statusResponsekey, consentTierstatus.getStatus());
							tempMap.put(statusResponseIDkey, consentTierstatus.getId());
							i++;
							break;
						}
					}
				}
				return tempMap;
			}		
			else
			{
				return null;
			}
	   }
    
    /**
	 * @param consentTierResponseCollection
	 * @param iter
	 */
	public static void createConsentResponseColl(Collection consentTierResponseCollection, Iterator iter)
	{
		while(iter.hasNext())
		{
			ConsentBean consentBean = (ConsentBean)iter.next();
			ConsentTierResponse consentTierResponse = new ConsentTierResponse();
			//Setting response
			consentTierResponse.setResponse(consentBean.getParticipantResponse());
			if(consentBean.getParticipantResponseID()!=null&&consentBean.getParticipantResponseID().trim().length()>0)
			{
				consentTierResponse.setId(Long.parseLong(consentBean.getParticipantResponseID()));
			}
			//Setting consent tier
			ConsentTier consentTier = new ConsentTier();
			consentTier.setId(Long.parseLong(consentBean.getConsentTierID()));
			consentTier.setStatement(consentBean.getStatement());
			consentTierResponse.setConsentTier(consentTier);
			consentTierResponseCollection.add(consentTierResponse);
		}
	}
}

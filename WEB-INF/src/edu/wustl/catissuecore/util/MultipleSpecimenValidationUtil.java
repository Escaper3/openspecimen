package edu.wustl.catissuecore.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;

import edu.wustl.catissuecore.bizlogic.SpecimenCollectionGroupBizLogic;
import edu.wustl.catissuecore.domain.AbstractSpecimenCollectionGroup;
import edu.wustl.catissuecore.domain.Biohazard;
import edu.wustl.catissuecore.domain.CollectionEventParameters;
import edu.wustl.catissuecore.domain.CollectionProtocol;
import edu.wustl.catissuecore.domain.CollectionProtocolRegistration;
import edu.wustl.catissuecore.domain.ExternalIdentifier;
import edu.wustl.catissuecore.domain.ReceivedEventParameters;
import edu.wustl.catissuecore.domain.Specimen;
import edu.wustl.catissuecore.domain.SpecimenCharacteristics;
import edu.wustl.catissuecore.domain.SpecimenCollectionGroup;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.catissuecore.util.global.Utility;
import edu.wustl.catissuecore.util.global.Variables;
import edu.wustl.common.cde.CDEManager;
import edu.wustl.common.dao.DAO;
import edu.wustl.common.util.dbManager.DAOException;
import edu.wustl.common.util.global.ApplicationProperties;
import edu.wustl.common.util.global.Validator;

/**
 * <p>This class initializes the fields of MultipleSpecimenValidationUtil.java</p>
 * @author Ashwin Gupta
 * @version 1.1
 */
public final class MultipleSpecimenValidationUtil
{

	//Abhishek Mehta : Performance related Changes
	/**
	 * validate multiple specimens.
	 * @param specimenMap
	 * @param dao
	 * @param operation
	 * @return
	 * @throws DAOException
	 */
	public static boolean validateMultipleSpecimen(LinkedHashSet specimenMap, DAO dao, String operation) throws DAOException
	{ 
		boolean result = true;
		
		setSCGinSpecimen(specimenMap,dao);
		Iterator specimenIterator = specimenMap.iterator();
		int count = 0;
		while (specimenIterator.hasNext() && result == true)
		{
			Specimen specimen = (Specimen) specimenIterator.next();
			count++;
			// TODO uncomment code for label, performance
			try
			{
				result = validateSingleSpecimen(specimen, dao, operation, true);
			}
			catch (DAOException daoException)
			{
				String message = daoException.getMessage();
				message += " (This message is for Specimen number " + count + ")";
				daoException.setMessage(message);
				throw daoException;
			}

			Collection derivedSpecimens = specimen.getChildrenSpecimen();

			if (derivedSpecimens == null)
			{
				continue;
			}

			Iterator it = derivedSpecimens.iterator();
			//validate derived specimens
			int i = 0;
			while(it.hasNext())
			{
				Specimen derivedSpecimen = (Specimen)it.next();
				derivedSpecimen.setSpecimenCharacteristics(specimen.getSpecimenCharacteristics());
				derivedSpecimen.setSpecimenCollectionGroup(specimen.getSpecimenCollectionGroup());
				derivedSpecimen.setPathologicalStatus(specimen.getPathologicalStatus());
				derivedSpecimen.getParentSpecimen().setId(specimen.getId());
				try
				{
					result = validateSingleSpecimen(derivedSpecimen, dao, operation, false);
				}
				catch (DAOException daoException)
				{
					int j = i + 1;
					String message = daoException.getMessage();
					message += " (This message is for Derived Specimen " + j + " of Parent Specimen number " + count + ")";
					daoException.setMessage(message);
					throw daoException;
				}

				if (!result)
				{
					break;
				}
				i++;
			}

		}
		System.out.println("End Inside validateMultipleSpecimen() " + result);
		return result;
	}
	
	
	/**
	 * Sets SCG in Specimen.
	 * @param specimenMap map
	 * @param dao dao
	 * @throws DAOException dao exception
	 */
	public static void setSCGinSpecimen(LinkedHashSet specimenMap, DAO dao) throws DAOException
	{
		Iterator specimenIterator = specimenMap.iterator();
		while (specimenIterator.hasNext())
		{
			Specimen specimen = (Specimen) specimenIterator.next();
			//validate single specimen
			if (specimen.getSpecimenCollectionGroup() != null)
			{
				String[] selectColumnName = {"id","collectionProtocolRegistration.id","collectionProtocolRegistration.collectionProtocol.id"};
				String[] whereColumnName = {Constants.NAME};
				String[] whereColumnCondition = {"="};
				String[] whereColumnValue = {specimen.getSpecimenCollectionGroup().getGroupName()};
				List spCollGroupList = dao.retrieve(SpecimenCollectionGroup.class.getName(), selectColumnName, whereColumnName, whereColumnCondition,
						whereColumnValue, null);
				// TODO saperate calls for SCG - ID and cpid
				// SCG - ID will be needed before populateStorageLocations
				
				// TODO test
				if (!spCollGroupList.isEmpty())
				{
					Object idList[] = (Object[]) spCollGroupList.get(0); // Move up + here
					//Long scgId  = (Long) spCollGroupList.get(0);
					//Long scgId = (Long) idList[0];
					Long scgId = (Long) idList[0]; // Move up 
					long cprId = ((Long) idList[1]).longValue();//here
					long cpId = ((Long) idList[2]).longValue();//here
					specimen.getSpecimenCollectionGroup().setId(scgId);
					//TODO instantiate associated objects(CPR & CP) & set IDs
					CollectionProtocol cp = new CollectionProtocol();
					cp.setId(new Long(cpId));
					CollectionProtocolRegistration cpr = new CollectionProtocolRegistration();
					cpr.setId(new Long(cprId));
					cpr.setCollectionProtocol(cp);
					((SpecimenCollectionGroup) specimen.getSpecimenCollectionGroup()).setCollectionProtocolRegistration(cpr);
				}
			}
		}
	}
	/**
	 * validates single specimen.
	 * @param specimen
	 * @param dao
	 * @param operation
	 * @param partOfMulipleSpecimen
	 * @return
	 * @throws DAOException
	 */
	public static boolean validateSingleSpecimen(Specimen specimen, DAO dao, String operation, boolean partOfMulipleSpecimen) throws DAOException
	{
		//Added by Ashish		
		//Logger.out.debug("Start-Inside validate method of specimen bizlogic");
		System.out.println("Inside validateSingleSpecimen() ");
		if (specimen == null)
		{
			throw new DAOException(ApplicationProperties.getValue("domain.object.null.err.msg", "Specimen"));
		}

		Validator validator = new Validator();
		if(operation.equals(Constants.ADD))
		{

			if (specimen.getSpecimenCollectionGroup() == null || specimen.getSpecimenCollectionGroup().getId() == null
					|| specimen.getSpecimenCollectionGroup().getId().longValue()==-1)
			{
				String message = ApplicationProperties.getValue("specimen.specimenCollectionGroup");
				throw new DAOException(ApplicationProperties.getValue("errors.item.required", message));
			}
		}
		if(!Variables.isSpecimenLabelGeneratorAvl){
			if (validator.isEmpty(specimen.getLabel()))
			{
				String message = ApplicationProperties.getValue("specimen.label");
				throw new DAOException(ApplicationProperties.getValue("errors.item.required", message));
			}
		}
		if (validator.isEmpty(specimen.getClassName()))
		{
			String message = ApplicationProperties.getValue("specimen.type");
			throw new DAOException(ApplicationProperties.getValue("errors.item.required", message));
		}

		if (validator.isEmpty(specimen.getSpecimenType()))
		{
			String message = ApplicationProperties.getValue("specimen.subType");
			throw new DAOException(ApplicationProperties.getValue("errors.item.required", message));
		}

		/*
		// commented as storage container has been removed from multiple specimen page - Ashwin 
		if (specimen.getStorageContainer() != null && specimen.getStorageContainer().getId() == null)
		{
			String message = ApplicationProperties.getValue("specimen.subType");
			throw new DAOException(ApplicationProperties.getValue("errors.invalid", message));
		}
		*/
		
		//validations for external identifiers
		Collection extIdentifierCollection = specimen.getExternalIdentifierCollection();
		ExternalIdentifier extIdentifier = null;
		if (extIdentifierCollection != null && !extIdentifierCollection.isEmpty())
		{
			Iterator itr = extIdentifierCollection.iterator();
			while (itr.hasNext())
			{
				extIdentifier = (ExternalIdentifier) itr.next();
				if (validator.isEmpty(extIdentifier.getName()))
				{
					String message = ApplicationProperties.getValue("specimen.msg");
					extIdentifierCollection.remove(extIdentifier);
					continue;
					//throw new DAOException(ApplicationProperties.getValue("errors.specimen.externalIdentifier.missing", message));
				}
				if (validator.isEmpty(extIdentifier.getValue()))
				{
					String message = ApplicationProperties.getValue("specimen.msg");
					throw new DAOException(ApplicationProperties.getValue("errors.specimen.externalIdentifier.missing", message));
				}
			}
		}
		//End Ashish

		if (Constants.ALIQUOT.equals(specimen.getLineage()))
		{
			//return true;
		}

		validateFields(specimen, dao, operation, partOfMulipleSpecimen);

		List specimenClassList = CDEManager.getCDEManager().getPermissibleValueList(Constants.CDE_NAME_SPECIMEN_CLASS, null);
		String specimenClass = Utility.getSpecimenClassName(specimen);

		if (!Validator.isEnumeratedValue(specimenClassList, specimenClass))
		{
			throw new DAOException(ApplicationProperties.getValue("protocol.class.errMsg"));
		}

		if (!Validator.isEnumeratedValue(Utility.getSpecimenTypes(specimenClass), specimen.getSpecimenType()))
		{
			throw new DAOException(ApplicationProperties.getValue("protocol.type.errMsg"));
		}

		
		if(specimen.getParentSpecimen()!=null)
		{
			if (specimen.getSpecimenEventCollection() != null)
			{
				Iterator specimenEventCollectionIterator = specimen.getSpecimenEventCollection().iterator();
				while (specimenEventCollectionIterator.hasNext())
				{
					//CollectionEvent validation.
					Object eventObject = specimenEventCollectionIterator.next();
					EventsUtil.validateEventsObject(eventObject, validator);
				}
			}
			//Validations for Biohazard Add-More Block
			Collection bioHazardCollection = specimen.getBiohazardCollection();
			Biohazard biohazard = null;
			if (bioHazardCollection != null && !bioHazardCollection.isEmpty())
			{
				Iterator itr = bioHazardCollection.iterator();
				while (itr.hasNext())
				{
					biohazard = (Biohazard) itr.next();
					if (!validator.isValidOption(biohazard.getType()))
					{
						String message = ApplicationProperties.getValue("newSpecimen.msg");
						throw new DAOException(ApplicationProperties.getValue("errors.newSpecimen.biohazard.missing", message));
					}
					if (biohazard.getId() == null || biohazard.getId().toString().equals("-1"))
					{
						String message = ApplicationProperties.getValue("newSpecimen.msg");
						throw new DAOException(ApplicationProperties.getValue("errors.newSpecimen.biohazard.missing", message));
					}
				}
			}
			SpecimenCharacteristics characters = specimen.getSpecimenCharacteristics();
			if(specimen.getParentSpecimen() == null)
			{
				if (characters == null)
				{
					throw new DAOException(ApplicationProperties.getValue("specimen.characteristics.errMsg"));
				}
				else
				{
					if (specimen.getSpecimenCollectionGroup() != null)
					{
						//				NameValueBean undefinedVal = new NameValueBean(Constants.UNDEFINED,Constants.UNDEFINED);
						List tissueSiteList = CDEManager.getCDEManager().getPermissibleValueList(Constants.CDE_NAME_TISSUE_SITE, null);
		
						if (!Validator.isEnumeratedValue(tissueSiteList, characters.getTissueSite()))
						{
							throw new DAOException(ApplicationProperties.getValue("protocol.tissueSite.errMsg"));
						}
		
						//		    	NameValueBean unknownVal = new NameValueBean(Constants.UNKNOWN,Constants.UNKNOWN);
						List tissueSideList = CDEManager.getCDEManager().getPermissibleValueList(Constants.CDE_NAME_TISSUE_SIDE, null);
		
						if (!Validator.isEnumeratedValue(tissueSideList, characters.getTissueSide()))
						{
							throw new DAOException(ApplicationProperties.getValue("specimen.tissueSide.errMsg"));
						}
		
						List pathologicalStatusList = CDEManager.getCDEManager().getPermissibleValueList(Constants.CDE_NAME_PATHOLOGICAL_STATUS, null);
		
						if (!Validator.isEnumeratedValue(pathologicalStatusList, specimen.getPathologicalStatus()))
						{
							throw new DAOException(ApplicationProperties.getValue("protocol.pathologyStatus.errMsg"));
						}
					}
				}
			}
		}
		
		if (operation.equals(Constants.ADD))
		{
			if (!specimen.getAvailable().booleanValue())
			{
				throw new DAOException(ApplicationProperties.getValue("specimen.available.errMsg"));
			}

			if (!Constants.ACTIVITY_STATUS_ACTIVE.equals(specimen.getActivityStatus()))
			{
				throw new DAOException(ApplicationProperties.getValue("activityStatus.active.errMsg"));
			}
		}
		else
		{
			if (!Validator.isEnumeratedValue(Constants.ACTIVITY_STATUS_VALUES, specimen.getActivityStatus()))
			{
				throw new DAOException(ApplicationProperties.getValue("activityStatus.errMsg"));
			}
		}
		//Logger.out.debug("End-Inside validate method of specimen bizlogic");
		return true;
	}

	/**
	 * validate fields
	 * @param specimen specimen
	 * @param dao 
	 * @param operation string operation
	 * @param partOfMulipleSpecimen 
	 * @throws DAOException
	 */
	private static void validateFields(Specimen specimen, DAO dao, String operation, boolean partOfMulipleSpecimen) throws DAOException
	{
		Validator validator = new Validator();

		if (partOfMulipleSpecimen)
		{
			AbstractSpecimenCollectionGroup scg = specimen.getSpecimenCollectionGroup();
			if (scg == null || validator.isEmpty(scg.getGroupName()))
			{
				if(scg.getId()== null)
				{
					String quantityString = ApplicationProperties.getValue("specimen.specimenCollectionGroup");
					throw new DAOException(ApplicationProperties.getValue("errors.item.required", quantityString));
				}
			}

		}
		if  (!Variables.isSpecimenLabelGeneratorAvl)
		{
			if (validator.isEmpty(specimen.getLabel()))
			{
				String labelString = ApplicationProperties.getValue("specimen.label");
				throw new DAOException(ApplicationProperties.getValue("errors.item.required", labelString));
			}
		}
		if (specimen.getInitialQuantity() == null || specimen.getInitialQuantity() == null)
		{
			String quantityString = ApplicationProperties.getValue("specimen.quantity");
			throw new DAOException(ApplicationProperties.getValue("errors.item.required", quantityString));
		}

		/**
		 *  If specimen is virtually located, then in that case storage container is being set null explicitly in Specimen
		 *  domain object.Hence to avoid NullPointerException here check null of container is required.
		 *  @author jitendra_agrawal  
		 */
		/*
		// commented as storage container has been removed from multiple specimen page - Ashwin 
		if (specimen.getStorageContainer() != null)
		{
			Long storageContainerId = specimen.getStorageContainer().getId();
			Integer xPos = specimen.getPositionDimensionOne();
			Integer yPos = specimen.getPositionDimensionTwo();

			if (storageContainerId == null || xPos == null || yPos == null || xPos.intValue() < 0 || yPos.intValue() < 0)
			{
				throw new DAOException(ApplicationProperties.getValue("errors.item.format", ApplicationProperties
						.getValue("specimen.positionInStorageContainer")));
			}
		}
		*/
	}

	public static void setMultipleSpecimensInSession(
			HttpServletRequest request,  Long scgId)
			throws DAOException
	{
		Map specimenMap = new SpecimenCollectionGroupBizLogic()
								.getSpecimenList(scgId);
		HttpSession session = request.getSession();
		session.setAttribute(Constants.SPECIMEN_LIST_SESSION_MAP, specimenMap);
	}
	
	/**
	 * @param errors
	 * @param validator
	 */
	public static void validateDate(ActionErrors errors, Validator validator, long userId, String dateOfEvent,
			String timeInHours, String timeInMinutes)
	{
		// checks the userid
		if ((userId) == -1L)
		{
			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",ApplicationProperties.getValue("eventparameters.user")));
		}
		if (!validator.checkDate(dateOfEvent))
		{
			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",ApplicationProperties.getValue("eventparameters.dateofevent")));
		}
		
		if (!validator.isNumeric( timeInMinutes,0))
		{
			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.invalid",ApplicationProperties.getValue("eventparameters.timeinminutes")));
		}
		
		if (!validator.isNumeric( timeInHours,0))
		{
			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.invalid",ApplicationProperties.getValue("eventparameters.timeinhours")));
		}
	}
}


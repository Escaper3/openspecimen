/**
 * <p>Title: NewSpecimenAction Class>
 * <p>Description:	NewSpecimenAction initializes the fields in the New Specimen page.</p>
 * Copyright:    Copyright (c) year
 * Company: Washington University, School of Medicine, St. Louis.
 * @author Aniruddha Phadnis
 * @version 1.00
 */

package edu.wustl.catissuecore.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.wustl.catissuecore.action.annotations.AnnotationConstants;
import edu.wustl.catissuecore.actionForm.NewSpecimenForm;
import edu.wustl.catissuecore.actionForm.SpecimenCollectionGroupForm;
import edu.wustl.catissuecore.bizlogic.AnnotationUtil;
import edu.wustl.catissuecore.bizlogic.BizLogicFactory;
import edu.wustl.catissuecore.bizlogic.NewSpecimenBizLogic;
import edu.wustl.catissuecore.bizlogic.SpecimenCollectionGroupBizLogic;
import edu.wustl.catissuecore.bizlogic.StorageContainerBizLogic;
import edu.wustl.catissuecore.bizlogic.UserBizLogic;
import edu.wustl.catissuecore.client.CaCoreAppServicesDelegator;
import edu.wustl.catissuecore.domain.Biohazard;
import edu.wustl.catissuecore.domain.CollectionProtocolEvent;
import edu.wustl.catissuecore.domain.CollectionProtocolRegistration;
import edu.wustl.catissuecore.domain.ConsentTier;
import edu.wustl.catissuecore.domain.ConsentTierResponse;
import edu.wustl.catissuecore.domain.ConsentTierStatus;
import edu.wustl.catissuecore.domain.Specimen;
import edu.wustl.catissuecore.domain.SpecimenCollectionGroup;
import edu.wustl.catissuecore.domain.SpecimenRequirement;
import edu.wustl.catissuecore.domain.StorageContainer;
import edu.wustl.catissuecore.domain.User;
import edu.wustl.catissuecore.util.CatissueCoreCacheManager;
import edu.wustl.catissuecore.util.EventsUtil;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.catissuecore.util.global.DefaultValueManager;
import edu.wustl.catissuecore.util.global.Utility;
import edu.wustl.common.action.SecureAction;
import edu.wustl.common.beans.NameValueBean;
import edu.wustl.common.beans.SessionDataBean;
import edu.wustl.common.bizlogic.DefaultBizLogic;
import edu.wustl.common.bizlogic.IBizLogic;
import edu.wustl.common.cde.CDE;
import edu.wustl.common.cde.CDEManager;
import edu.wustl.common.cde.PermissibleValue;
import edu.wustl.common.util.MapDataParser;
import edu.wustl.common.util.dbManager.DAOException;
import edu.wustl.common.util.logger.Logger;

/**
 * NewSpecimenAction initializes the fields in the New Specimen page.
 * @author aniruddha_phadnis
 */
public class NewSpecimenAction extends SecureAction 
{

	/**
	 * Overrides the execute method of Action class.
	 * @param mapping object of ActionMapping
	 * @param form object of ActionForm
	 * @param request object of HttpServletRequest
	 * @param response object of HttpServletResponse
	 * @throws Exception generic exception
	 */
	public ActionForward executeSecureAction(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		//Logger.out.debug("NewSpecimenAction start@@@@@@@@@");
		NewSpecimenForm specimenForm = (NewSpecimenForm) form;
		String pageOf = request.getParameter(Constants.PAGEOF);
		String forwardPage=specimenForm.getForwardTo();
        if(forwardPage.equals(Constants.PAGEOF_SPECIMEN_COLLECTION_REQUIREMENT_GROUP))
	    {
        	pageOf=forwardPage;
        	request.setAttribute(Constants.STATUS_MESSAGE_KEY, "errors.specimencollectionrequirementgroupedit");
        	return mapping.findForward(pageOf);
	    }
		IBizLogic bizLogicObj = BizLogicFactory.getInstance().getBizLogic(Constants.DEFAULT_BIZ_LOGIC);
		
		String treeRefresh = request.getParameter("refresh");
		request.setAttribute("refresh",treeRefresh);
		
        //Gets the value of the operation parameter.
		String operation = (String) request.getParameter(Constants.OPERATION);
		//boolean to indicate whether the suitable containers to be shown in dropdown 
		//is exceeding the max limit.
		String exceedingMaxLimit = new String();

		//Sets the operation attribute to be used in the Edit/View Specimen Page in Advance Search Object View. 
		request.setAttribute(Constants.OPERATION, operation);

		if (operation != null && operation.equalsIgnoreCase(Constants.ADD))
		{
			specimenForm.setId(0);
		}
		String virtuallyLocated = request.getParameter("virtualLocated");
		if (virtuallyLocated != null && virtuallyLocated.equals("true"))
		{
			specimenForm.setVirtuallyLocated(true);
		}

		//Name of button clicked
		String button = request.getParameter("button");
		Map map = null;

		if (button != null)
		{
			if (button.equals("deleteExId"))
			{
				List key = new ArrayList();
				key.add("ExternalIdentifier:i_name");
				key.add("ExternalIdentifier:i_value");

				//Gets the map from ActionForm
				map = specimenForm.getExternalIdentifier();
				MapDataParser.deleteRow(key, map, request.getParameter("status"));
			}
			else
			{
				List key = new ArrayList();
				key.add("Biohazard:i_type");
				key.add("Biohazard:i_id");

				//Gets the map from ActionForm
				map = specimenForm.getBiohazard();
				MapDataParser.deleteRow(key, map, request.getParameter("status"));
			}
		}
		
//		*************  ForwardTo implementation *************
		HashMap forwardToHashMap = (HashMap) request.getAttribute("forwardToHashMap");
		String specimenCollectionGroupName = "";
		if (forwardToHashMap != null)
		{
			String specimenCollectionGroupId = (String) forwardToHashMap.get("specimenCollectionGroupId");
			/**For Migration Start**/
			
			specimenCollectionGroupName = (String) forwardToHashMap.get("specimenCollectionGroupName");
			Logger.out.debug("specimenCollectionGroupName found in forwardToHashMap========>>>>>>" + specimenCollectionGroupName);
			specimenForm.setSpecimenCollectionGroupName(specimenCollectionGroupName);
			/**For Migration End**/
			Logger.out.debug("SpecimenCollectionGroupId found in forwardToHashMap========>>>>>>" + specimenCollectionGroupId);

			if (specimenCollectionGroupId != null)
			{
				/**
				 *  Retaining properties of specimen when more is clicked.
				 *  Bug no -- 2623
				 */
				//Populating the specimen collection group name in the specimen page
				setFormValues(specimenForm, specimenCollectionGroupId,specimenCollectionGroupName);
			}
		}
		
		//*************  ForwardTo implementation *************

		//Consent Tracking (Virender Mehta)     - Start
		//Adding name,value pair in NameValueBean
			String tabSelected = request.getParameter(Constants.SELECTED_TAB);
			if(tabSelected!=null)
			{
				request.setAttribute(Constants.SELECTED_TAB,tabSelected);
			}
			String scg_id=String.valueOf(specimenForm.getSpecimenCollectionGroupId());
			SpecimenCollectionGroup specimenCollectionGroup= getSCGObj(scg_id);
			//PHI Data
			String initialURLValue="";
			String initialWitnessValue="";
			String initialSignedConsentDateValue="";
			//Lazy - specimenCollectionGroup.getCollectionProtocolRegistration()
			CollectionProtocolRegistration collectionProtocolRegistration = getCPRObj(scg_id);
			if(collectionProtocolRegistration ==null  || collectionProtocolRegistration.getSignedConsentDocumentURL()==null)
			{
				initialURLValue=Constants.NULL;
			}
			User consentWitness=null;
			if(collectionProtocolRegistration.getId()!= null)
			{
				consentWitness = (User)bizLogicObj.retrieveAttribute(CollectionProtocolRegistration.class.getName(),collectionProtocolRegistration.getId(), "consentWitness");
			}
			//User consentWitness= collectionProtocolRegistration.getConsentWitness();
			//Resolved lazy
			if(consentWitness==null)
			{
				initialWitnessValue=Constants.NULL;
			}
			if(collectionProtocolRegistration.getConsentSignatureDate()==null)
			{
				initialSignedConsentDateValue=Constants.NULL;
			}
			List cprObjectList=new ArrayList();
			cprObjectList.add(collectionProtocolRegistration);
			SessionDataBean sessionDataBean=(SessionDataBean)request.getSession().getAttribute(Constants.SESSION_DATA);
			CaCoreAppServicesDelegator caCoreAppServicesDelegator = new CaCoreAppServicesDelegator();
			String userName = Utility.toString(sessionDataBean.getUserName());	
			List collProtObject = caCoreAppServicesDelegator.delegateSearchFilter(userName,cprObjectList);
			CollectionProtocolRegistration cprObject =null;
			cprObject = (CollectionProtocolRegistration)collProtObject.get(0);
			
			String witnessName="";
			String getConsentDate="";
			String getSignedConsentURL="";
			User witness = cprObject.getConsentWitness();
			if(witness==null)
			{
				if(initialWitnessValue.equals(Constants.NULL))
				{
					witnessName="";
				}
				else
				{
					witnessName=Constants.HASHED_OUT;
				}
				specimenForm.setWitnessName(witnessName);
			}
			else
			{
				witness = (User)bizLogicObj.retrieveAttribute(CollectionProtocolRegistration.class.getName(),cprObject.getId(), "consentWitness");
				String witnessFullName = witness.getLastName()+", "+witness.getFirstName();
				specimenForm.setWitnessName(witnessFullName);
			}
			if(cprObject.getConsentSignatureDate()==null)
			{
				if(initialSignedConsentDateValue.equals(Constants.NULL))
				{
					getConsentDate="";
				}
				else
				{
					getConsentDate=Constants.HASHED_OUT;
				}
			}
			else
			{
				getConsentDate=Utility.parseDateToString(cprObject.getConsentSignatureDate(), Constants.DATE_PATTERN_MM_DD_YYYY);
			}
			
			if(cprObject.getSignedConsentDocumentURL()==null)
			{
				if(initialURLValue.equals(Constants.NULL))
				{
					getSignedConsentURL="";
				}
				else
				{
					getSignedConsentURL=Constants.HASHED_OUT;
				}
			}
			else
			{
				getSignedConsentURL=Utility.toString(cprObject.getSignedConsentDocumentURL());
			}
			specimenForm.setConsentDate(getConsentDate);
			specimenForm.setSignedConsentUrl(getSignedConsentURL);
			Collection consentTierRespCollection = (Collection)bizLogicObj.retrieveAttribute(CollectionProtocolRegistration.class.getName(),cprObject.getId(), "elements(consentTierResponseCollection)" );
			//Lazy Resolved --- cprObject.getConsentTierResponseCollection() 
			Set participantResponseSet =(Set)consentTierRespCollection;
			
			List participantResponseList;
			
			if (participantResponseSet != null)
			{
				participantResponseList = new ArrayList(participantResponseSet);
			}
			else
			{
				participantResponseList = new ArrayList();
			}				
			
			if(operation.equalsIgnoreCase(Constants.ADD))
			{
				ActionErrors errors = (ActionErrors) request.getAttribute(Globals.ERROR_KEY);
				if(errors == null)
				{ 
					String scgDropDown = request.getParameter(Constants.SCG_DROPDOWN);
					if(scgDropDown==null||scgDropDown.equalsIgnoreCase(Constants.TRUE))
					{
						//Resolved lazy ------- = specimenCollectionGroup.getConsentTierStatusCollection();
						Collection consentResponseStatuslevel=(Collection)bizLogicObj.retrieveAttribute(SpecimenCollectionGroup.class.getName(),specimenCollectionGroup.getId(), "elements(consentTierStatusCollection)");
						Map tempMap=prepareConsentMap(participantResponseList,consentResponseStatuslevel);
						specimenForm.setConsentResponseForSpecimenValues(tempMap);
					}
				}
				specimenForm.setConsentTierCounter(participantResponseList.size());
			}
			else
			{
				String specimenID = null;
				specimenID = String.valueOf(specimenForm.getId());
				Specimen specimenObject = getSpecimenObj(specimenID);
				//Added by Falguni=To set Specimen label in Form.
				specimenForm.setLabel(specimenObject.getLabel());
				//List added for grid
				List specimenDetails= new ArrayList();
				getSpecimenDetails(specimenObject,specimenDetails);
				List columnList=columnNames();
				//Resolved lazy
				//specimenObject.getSpecimenCollectionGroup().getCollectionProtocolRegistration().getConsentTierResponseCollection()
				//specimenObject.getConsentTierStatusCollection();
				//Collection consentResponse = (Collection)bizLogicObj.retrieveAttribute(Specimen.class.getName(),specimenObject.getId(), "elements(specimenCollectionGroup.collectionProtocolRegistration.consentTierResponseCollection)");
				
				String consentResponseHql ="select elements(scg.collectionProtocolRegistration.consentTierResponseCollection)"+ 
				" from edu.wustl.catissuecore.domain.SpecimenCollectionGroup as scg," +
				" edu.wustl.catissuecore.domain.Specimen as spec " +
				" where spec.specimenCollectionGroup.id=scg.id and spec.id="+ specimenObject.getId();
				Collection consentResponse = Utility.executeQuery(consentResponseHql);
				Collection consentResponseStatuslevel=(Collection)bizLogicObj.retrieveAttribute(Specimen.class.getName(),specimenObject.getId(), "elements(consentTierStatusCollection)"); 
				Map tempMap=prepareSCGResponseMap(consentResponseStatuslevel, consentResponse);
				specimenForm.setConsentResponseForSpecimenValues(tempMap);
				specimenForm.setConsentTierCounter(participantResponseList.size()) ;
				HttpSession session =request.getSession();
				session.setAttribute(Constants.SPECIMEN_LIST,specimenDetails);
				session.setAttribute(Constants.COLUMNLIST,columnList);
			}
			List specimenResponseList = new ArrayList();
			specimenResponseList=Utility.responceList(operation);
			request.setAttribute(Constants.SPECIMEN_RESPONSELIST, specimenResponseList);
		//Consent Tracking (Virender Mehta)		- Stop 		
     		
		pageOf = request.getParameter(Constants.PAGEOF);
		request.setAttribute(Constants.PAGEOF, pageOf);

		//Sets the activityStatusList attribute to be used in the Site Add/Edit Page.
		request.setAttribute(Constants.ACTIVITYSTATUSLIST, Constants.ACTIVITY_STATUS_VALUES);
		//Sets the collectionStatusList attribute to be used in the Site Add/Edit Page.
		request.setAttribute(Constants.COLLECTIONSTATUSLIST, Constants.SPECIMEN_COLLECTION_STATUS_VALUES);

		NewSpecimenBizLogic bizLogic = (NewSpecimenBizLogic) BizLogicFactory.getInstance().getBizLogic(Constants.NEW_SPECIMEN_FORM_ID);

		if (specimenForm.isParentPresent())//If parent specimen is present then
		{
//			String[] fields = {Constants.SYSTEM_LABEL};
//			List parentSpecimenList = bizLogic.getList(Specimen.class.getName(), fields, Constants.SYSTEM_IDENTIFIER, true);
//			request.setAttribute(Constants.PARENT_SPECIMEN_ID_LIST, parentSpecimenList);
		}

		String[] bhIdArray = {"-1"};
		String[] bhTypeArray = {Constants.SELECT_OPTION};
		String[] bhNameArray = {Constants.SELECT_OPTION};

		String[] selectColNames = {Constants.SYSTEM_IDENTIFIER, "name", "type"};
		List biohazardList = bizLogic.retrieve(Biohazard.class.getName(), selectColNames);
		Iterator iterator = biohazardList.iterator();

		//Creating & setting the biohazard name, id & type list
		if (biohazardList != null && !biohazardList.isEmpty())
		{
			bhIdArray = new String[biohazardList.size() + 1];
			bhTypeArray = new String[biohazardList.size() + 1];
			bhNameArray = new String[biohazardList.size() + 1];

			bhIdArray[0] = "-1";
			bhTypeArray[0] = "";
			bhNameArray[0] = Constants.SELECT_OPTION;

			int i = 1;

			while (iterator.hasNext())
			{
				Object[] hazard = (Object[]) iterator.next();
				bhIdArray[i] = String.valueOf(hazard[0]);
				bhNameArray[i] = (String) hazard[1];
				bhTypeArray[i] = (String) hazard[2];
				i++;
			}
		}

		request.setAttribute(Constants.BIOHAZARD_NAME_LIST, bhNameArray);
		request.setAttribute(Constants.BIOHAZARD_ID_LIST, bhIdArray);
		request.setAttribute(Constants.BIOHAZARD_TYPES_LIST, bhTypeArray);
		
		/**
		 * Name: Chetan Patil
		 * Reviewer: Sachin Lale
		 * Bug ID: Bug#3184
		 * Patch ID: Bug#3184_1
		 * Also See: 2-6
		 * Description: Here the older code has been integrated again inorder to restrict the specimen values based on
		 * requirements of study calendar event point. Two method are written to separate the code. Method populateAllRestrictedLists()
		 * will populate the values for the lists form Specimen Requirements whereas, method populateAllLists() will populate the values 
		 * of the list form the system.
		 */
		//Setting Secimen Collection Group
		/**For Migration Start**/
//		initializeAndSetSpecimenCollectionGroupIdList(bizLogic,request);
		/**For Migration Start**/
		/**
		 * Patch ID: Bug#4245_2
		 * 
		 */
		List<NameValueBean> specimenClassList = new ArrayList<NameValueBean>();
		List<NameValueBean> specimenTypeList = new ArrayList<NameValueBean>();
		List<NameValueBean> tissueSiteList = new ArrayList<NameValueBean>();
		List<NameValueBean> tissueSideList = new ArrayList<NameValueBean>();
		List<NameValueBean> pathologicalStatusList = new ArrayList<NameValueBean>();
		Map<String, List<NameValueBean>> subTypeMap = new HashMap<String, List<NameValueBean>>();
		
		String restrictSCGCheckbox = specimenForm.getRestrictSCGCheckbox();
		if(restrictSCGCheckbox == null) 
		{//If not form the form, get the value form request. Case of Technician accessing SCG to add specimen.
			restrictSCGCheckbox = (String)request.getParameter(Constants.RESTRICT_SCG_CHECKBOX);
		}
		
		String specimenCollectionGroupId = specimenForm.getSpecimenCollectionGroupId();
		if (specimenCollectionGroupId != null && !specimenCollectionGroupId.equals("")) 
		{	// If specimen is being added form specimen collection group page or a specimen is being edited.
			
			if (Constants.ALIQUOT.equals(specimenForm.getLineage()))
			{
				populateListBoxes(specimenForm, request);
			}
			else if(restrictSCGCheckbox != null && restrictSCGCheckbox.equals(Constants.TRUE))
            {
				//Patch ID: Bug#3184_25
				//Populate restricted values into the lists to be displayed. Also set the values selected in the list, if there is only
				//one specimen requirement associated with the Collection Protocol Event; otherwise do not set any value as selected.
				int numberOfSpecimens = populateAllRestrictedLists(request, specimenForm, specimenCollectionGroupId,specimenClassList, 
						specimenTypeList, tissueSiteList, tissueSideList, pathologicalStatusList, subTypeMap);
				//TODO create constants and importantly test for all cases
				String onCollOrClassChange = request.getParameter(Constants.ON_COLL_OR_CLASSCHANGE);
				if(((onCollOrClassChange == null) || !(onCollOrClassChange.equalsIgnoreCase(Constants.TRUE))) && (numberOfSpecimens > 1))
				{
					specimenForm.setClassName("");
					specimenForm.setType("");
					specimenForm.setTissueSite("");
					specimenForm.setPathologicalStatus("");
				}
            }
			else
			{
				populateAllLists(specimenForm, specimenClassList, specimenTypeList, tissueSiteList, tissueSideList,	
						pathologicalStatusList, subTypeMap);
			}
		}
		else
		{	// On adding a new specimen independently.
			populateAllLists(specimenForm, specimenClassList, specimenTypeList, tissueSiteList, tissueSideList,	
					pathologicalStatusList, subTypeMap);
		}
		
		/**
         * Name : Virender Mehta
         * Reviewer: Sachin Lale
         * Bug ID: defaultValueConfiguration_BugID
         * Patch ID:defaultValueConfiguration_BugID_10
         * See also:defaultValueConfiguration_BugID_9,11
         * Description: Configuration of default value for TissueSite, TissueSite, PathologicalStatus
         * 
         * Note by Chetan: Value setting for TissueSite and PathologicalStatus has been moved into the
         * method populateAllLists().
         */
		// Setting the default values
		if (specimenForm.getTissueSide() == null || specimenForm.getTissueSide().equals("-1")) 
		{
			specimenForm.setTissueSide((String)DefaultValueManager.getDefaultValue(Constants.DEFAULT_TISSUE_SIDE));
		}
				
		// sets the Specimen Class list
		request.setAttribute(Constants.SPECIMEN_CLASS_LIST, specimenClassList);
		// sets the Specimen Type list
		request.setAttribute(Constants.SPECIMEN_TYPE_LIST, specimenTypeList);
		// sets the Tissue Site list
		request.setAttribute(Constants.TISSUE_SITE_LIST, tissueSiteList);
		// sets the PathologicalStatus list
		request.setAttribute(Constants.PATHOLOGICAL_STATUS_LIST, pathologicalStatusList);
		// sets the Side list
		request.setAttribute(Constants.TISSUE_SIDE_LIST, tissueSideList);
		// set the map of subtype
		request.setAttribute(Constants.SPECIMEN_TYPE_MAP, subTypeMap);

		//Setting biohazard list
		biohazardList = CDEManager.getCDEManager().getPermissibleValueList(Constants.CDE_NAME_BIOHAZARD, null);
		request.setAttribute(Constants.BIOHAZARD_TYPE_LIST, biohazardList);
		
		/**
	 * Name : Ashish Gupta
	 * Reviewer Name : Sachin Lale 
	 * Bug ID: 2741
	 * Patch ID: 2741_12	 
	 * Description: Propagating events from scg to multiple specimens
	*/
		Object scgForm = request.getAttribute("scgForm");
		if(scgForm != null)
		{
			SpecimenCollectionGroupForm specimenCollectionGroupForm = (SpecimenCollectionGroupForm)scgForm;
			populateEventsFromScg(specimenCollectionGroupForm,specimenForm);
		}
		else
		{
			//Mandar : 10-July-06 AutoEvents : CollectionEvent
			setCollectionEventRequestParameters(request, specimenForm);
	
			//Mandar : 11-July-06 AutoEvents : ReceivedEvent
			setReceivedEventRequestParameters(request, specimenForm);
	
			//Mandar : set default date and time too event fields
			setDateParameters(specimenForm);
		}
		//    	 ---- chetan 15-06-06 ----
		StorageContainerBizLogic scbizLogic = (StorageContainerBizLogic) BizLogicFactory.getInstance().getBizLogic(
				Constants.STORAGE_CONTAINER_FORM_ID);
		TreeMap containerMap = new TreeMap();
		Vector initialValues = null;
		if (operation.equals(Constants.ADD))
		{
			SessionDataBean sessionData = (SessionDataBean) request.getSession().getAttribute(Constants.SESSION_DATA); 
			if(specimenForm.getLabel()==null || specimenForm.getLabel().equals(""))
			{
				//int totalNoOfSpecimen = bizLogic.totalNoOfSpecimen(sessionData)+1;
				/**
	        	 * Name : Virender Mehta
	             * Reviewer: Sachin Lale
	             * Description: By getting instance of AbstractSpecimenGenerator abstract class current label retrived and set.
	        	 */
//				SpecimenLabelGenerator spLblGenerator  = SpecimenLabelGeneratorFactory.getInstance();
//				Map inputMap = new HashMap();
//				inputMap.put(Constants.SCG_NAME_KEY, specimenCollectionGroupName);
//				String specimenLabel= spLblGenerator.getNextAvailableSpecimenlabel(inputMap);
//				specimenForm.setLabel(specimenLabel);
			}
			
			if (specimenForm.getSpecimenCollectionGroupName() != null && !specimenForm.getSpecimenCollectionGroupName().equals("")
					&& specimenForm.getClassName() != null && !specimenForm.getClassName().equals("") && !specimenForm.getClassName().equals("-1"))
			{
				//Logger.out.debug("before retrieval of spCollGroupList inside specimen action ^^^^^^^^^^^");
				String[] selectColumnName = {"collectionProtocolRegistration.collectionProtocol.id"};
				String[] whereColumnName = {"name"};
				String[] whereColumnCondition = {"="};
				String[] whereColumnValue = {specimenForm.getSpecimenCollectionGroupName()};
				List spCollGroupList = bizLogic.retrieve(SpecimenCollectionGroup.class.getName(), selectColumnName, whereColumnName,
						whereColumnCondition, whereColumnValue, null);
				//Logger.out.debug("after retrieval of spCollGroupList inside specimen action ^^^^^^^^^^^");
				if (spCollGroupList!=null && !spCollGroupList.isEmpty())
				{
					//					Object []spCollGroup = (Object[]) spCollGroupList
					//							.get(0);
					long cpId = ((Long) spCollGroupList.get(0)).longValue();
					String spClass = specimenForm.getClassName();
					Logger.out.info("cpId :" + cpId + "spClass:" + spClass);
					request.setAttribute(Constants.COLLECTION_PROTOCOL_ID, cpId + "");
					if (virtuallyLocated != null && virtuallyLocated.equals("false"))
					{
						specimenForm.setVirtuallyLocated(false);
					}
					
					if(specimenForm.getStContSelection() == 2)
					{
					//Logger.out.debug("calling getAllocatedContaienrMapForSpecimen() function from NewSpecimenAction---");
					 sessionData = (SessionDataBean) request.getSession().getAttribute(Constants.SESSION_DATA);
					containerMap = scbizLogic.getAllocatedContaienrMapForSpecimen(cpId, spClass, 0, exceedingMaxLimit, sessionData, true);
					//Logger.out.debug("exceedingMaxLimit in action for Boolean:"+exceedingMaxLimit);
					Logger.out.debug("finish ---calling getAllocatedContaienrMapForSpecimen() function from NewSpecimenAction---");
					ActionErrors errors = (ActionErrors) request.getAttribute(Globals.ERROR_KEY);
					if (containerMap.isEmpty()) 
					{
						
						if (errors == null || errors.size() == 0)
						{
							errors = new ActionErrors();
						}
						errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("storageposition.not.available"));
						saveErrors(request, errors);
					}
					Logger.out.debug("calling checkForInitialValues() function from NewSpecimenAction---");
					if (errors == null || errors.size() == 0)
					{
						initialValues = checkForInitialValues(containerMap);
					}
					else
					{
						String[] startingPoints = new String[3];
						startingPoints[0] = specimenForm.getStorageContainer();
						startingPoints[1] = specimenForm.getPositionDimensionOne();
						startingPoints[2] = specimenForm.getPositionDimensionTwo() ;
						initialValues = new Vector();
						initialValues.add(startingPoints);
					}
					Logger.out.debug("finish ---calling checkForInitialValues() function from NewSpecimenAction---");
				}
				}
			}
		}
		else
		{
			containerMap = new TreeMap();
			String[] startingPoints = new String[]{"-1", "-1", "-1"};

			Logger.out.info("--------------container:" + specimenForm.getStorageContainer());
			Logger.out.info("--------------pos1:" + specimenForm.getPositionDimensionOne());
			Logger.out.info("--------------pos2:" + specimenForm.getPositionDimensionTwo());

			if (specimenForm.getStorageContainer() != null && !specimenForm.getStorageContainer().equals(""))
			{
				Integer id = new Integer(specimenForm.getStorageContainer());
				String parentContainerName = "";
				String valueField1 = "id";
				List list = bizLogic.retrieve(StorageContainer.class.getName(), valueField1, new Long(specimenForm.getStorageContainer()));
				if (!list.isEmpty())
				{
					StorageContainer container = (StorageContainer) list.get(0);
					parentContainerName = container.getName();

				}
				Integer pos1 = new Integer(specimenForm.getPositionDimensionOne());
				Integer pos2 = new Integer(specimenForm.getPositionDimensionTwo());

				List pos2List = new ArrayList();
				pos2List.add(new NameValueBean(pos2, pos2));

				Map pos1Map = new TreeMap();
				pos1Map.put(new NameValueBean(pos1, pos1), pos2List);
				containerMap.put(new NameValueBean(parentContainerName, id), pos1Map);

				if (specimenForm.getStorageContainer() != null && !specimenForm.getStorageContainer().equals("-1"))
				{
					startingPoints[0] = specimenForm.getStorageContainer();

				}
				if (specimenForm.getPositionDimensionOne() != null && !specimenForm.getPositionDimensionOne().equals("-1"))
				{
					startingPoints[1] = specimenForm.getPositionDimensionOne();
				}
				if (specimenForm.getPositionDimensionTwo() != null && !specimenForm.getPositionDimensionTwo().equals("-1"))
				{
					startingPoints[2] = specimenForm.getPositionDimensionTwo();
				}
			}
			initialValues = new Vector();
			Logger.out.info("Starting points[0]" + startingPoints[0]);
			Logger.out.info("Starting points[1]" + startingPoints[1]);
			Logger.out.info("Starting points[2]" + startingPoints[2]);
			initialValues.add(startingPoints);

		}
		request.setAttribute("initValues", initialValues);
		request.setAttribute(Constants.EXCEEDS_MAX_LIMIT, exceedingMaxLimit);
		request.setAttribute(Constants.AVAILABLE_CONTAINER_MAP, containerMap);
		// -------------------------
		//Falguni:Performance Enhancement.
		Long specimenEntityId = null;
		if (CatissueCoreCacheManager.getInstance().getObjectFromCache("specimenEntityId") != null)
		{
			specimenEntityId = (Long) CatissueCoreCacheManager.getInstance().getObjectFromCache("specimenEntityId");
		}
		else
		{
			specimenEntityId = AnnotationUtil.getEntityId(AnnotationConstants.ENTITY_NAME_SPECIMEN);
			CatissueCoreCacheManager.getInstance().addObjectToCache("specimenEntityId",specimenEntityId);		
		}	
		
		request.setAttribute("specimenEntityId",specimenEntityId);
		
		if (specimenForm.isVirtuallyLocated())
		{
			request.setAttribute("disabled", "true");
		}
		
		// set associated identified report id
		Long reportId=getAssociatedIdentifiedReportId(specimenForm.getId());
		if(reportId==null)
		{
			reportId=new Long(-1);
		}
		else if(Utility.isQuarantined(reportId))
		{
			reportId=new Long(-2);
		}
		HttpSession session =request.getSession();
		session.setAttribute(Constants.IDENTIFIED_REPORT_ID, reportId);
		//Logger.out.debug("End of specimen action");

		return mapping.findForward(pageOf);
	}
	
	/**
	 * This method populates the list boxes for type, tissue site, tissue side
	 * and pathological status if this specimen is an aliquot.
	 * @param specimenForm object of NewSpecimenForm
	 * @param request object of HttpServletRequest
	 */
	private void populateListBoxes(NewSpecimenForm specimenForm, HttpServletRequest request)
	{
		//Setting the specimen type list
		NameValueBean bean = new NameValueBean(specimenForm.getType(), specimenForm.getType());
		List specimenTypeList = new ArrayList();
		specimenTypeList.add(bean);
		request.setAttribute(Constants.SPECIMEN_TYPE_LIST, specimenTypeList);

		//Setting tissue site list
		bean = new NameValueBean(specimenForm.getTissueSite(), specimenForm.getTissueSite());
		List tissueSiteList = new ArrayList();
		tissueSiteList.add(bean);
		request.setAttribute(Constants.TISSUE_SITE_LIST, tissueSiteList);

		//Setting tissue side list
		bean = new NameValueBean(specimenForm.getTissueSide(), specimenForm.getTissueSide());
		List tissueSideList = new ArrayList();
		tissueSideList.add(bean);
		request.setAttribute(Constants.TISSUE_SIDE_LIST, tissueSideList);

		//Setting pathological status list
		bean = new NameValueBean(specimenForm.getPathologicalStatus(), specimenForm.getPathologicalStatus());
		List pathologicalStatusList = new ArrayList();
		pathologicalStatusList.add(bean);
		request.setAttribute(Constants.PATHOLOGICAL_STATUS_LIST, pathologicalStatusList);
	}

	// Mandar AutoEvents CollectionEvent start
	/**
	 * This method sets all the collection event parameters for the SpecimenEventParameter pages
	 * @param request HttpServletRequest instance in which the data will be set. 
	 * @param specimenForm NewSpecimenForm instance
	 * @throws Exception Throws Exception. Helps in handling exceptions at one common point.
	 */
	private void setCollectionEventRequestParameters(HttpServletRequest request, NewSpecimenForm specimenForm) throws Exception
	{
		//Gets the value of the operation parameter.
		String operation = request.getParameter(Constants.OPERATION);

		//Sets the operation attribute to be used in the Add/Edit FrozenEventParameters Page. 
		request.setAttribute(Constants.OPERATION, operation);

		//Sets the minutesList attribute to be used in the Add/Edit FrozenEventParameters Page.
		request.setAttribute(Constants.MINUTES_LIST, Constants.MINUTES_ARRAY);

		//Sets the hourList attribute to be used in the Add/Edit FrozenEventParameters Page.
		request.setAttribute(Constants.HOUR_LIST, Constants.HOUR_ARRAY);

		//        //The id of specimen of this event.
		//        String specimenId = request.getParameter(Constants.SPECIMEN_ID); 
		//        request.setAttribute(Constants.SPECIMEN_ID, specimenId);
		//        Logger.out.debug("\t\t SpecimenEventParametersAction************************************ : "+specimenId );
		//        
		UserBizLogic userBizLogic = (UserBizLogic) BizLogicFactory.getInstance().getBizLogic(Constants.USER_FORM_ID);
		Collection userCollection = userBizLogic.getUsers(operation);

		request.setAttribute(Constants.USERLIST, userCollection);

		SessionDataBean sessionData = getSessionData(request);
		if (sessionData != null)
		{
			String user = sessionData.getLastName() + ", " + sessionData.getFirstName();
			long collectionEventUserId = EventsUtil.getIdFromCollection(userCollection, user);
			
			if(specimenForm.getCollectionEventUserId() == 0)
			{
			 specimenForm.setCollectionEventUserId(collectionEventUserId);
			}
			if(specimenForm.getReceivedEventUserId() == 0)
			{
			  specimenForm.setReceivedEventUserId(collectionEventUserId);
			}
		}

		/**
         * Name : Virender Mehta
         * Reviewer: Sachin Lale
         * Bug ID: defaultValueConfiguration_BugID
         * Patch ID:defaultValueConfiguration_BugID_11
         * See also:defaultValueConfiguration_BugID_9,10,11
         * Description: Configuration for default value for Collection Procedure, Container and Quality
         */
		
		// set the procedure lists
		List procedureList = CDEManager.getCDEManager().getPermissibleValueList(Constants.CDE_NAME_COLLECTION_PROCEDURE, null);
		request.setAttribute(Constants.PROCEDURE_LIST, procedureList);
		//Bug- setting the default collection event procedure
		if (specimenForm.getCollectionEventCollectionProcedure() == null)
		{
			specimenForm.setCollectionEventCollectionProcedure((String)DefaultValueManager.getDefaultValue(Constants.DEFAULT_COLLECTION_PROCEDURE));
		}

		// set the container lists
		List containerList = CDEManager.getCDEManager().getPermissibleValueList(Constants.CDE_NAME_CONTAINER, null);
		request.setAttribute(Constants.CONTAINER_LIST, containerList);
		//Bug- setting the default collection event container
		if (specimenForm.getCollectionEventContainer() == null)
		{
			specimenForm.setCollectionEventContainer((String)DefaultValueManager.getDefaultValue(Constants.DEFAULT_CONTAINER));
		}

	}

	// Mandar AutoEvents CollectionEvent end

	// Mandar Autoevents ReceivedEvent start
	/**
	 * This method sets all the received event parameters for the SpecimenEventParameter pages
	 * @param request HttpServletRequest instance in which the data will be set.
	 * @param specimenForm NewSpecimenForm instance
	 * @throws Exception Throws Exception. Helps in handling exceptions at one common point.
	 */
	private void setReceivedEventRequestParameters(HttpServletRequest request, NewSpecimenForm specimenForm) throws Exception
	{

		List qualityList = CDEManager.getCDEManager().getPermissibleValueList(Constants.CDE_NAME_RECEIVED_QUALITY, null);
		request.setAttribute(Constants.RECEIVED_QUALITY_LIST, qualityList);
		//Bug- setting the default recieved event quality
		if (specimenForm.getReceivedEventReceivedQuality() == null)
		{
			specimenForm.setReceivedEventReceivedQuality((String)DefaultValueManager.getDefaultValue(Constants.DEFAULT_RECEIVED_QUALITY));
		}
	}

	/**
	 * @param specimenForm instance of NewSpecimenForm
	 */
	private void setDateParameters(NewSpecimenForm specimenForm)
	{
		// set the current Date and Time for the event.
		Calendar cal = Calendar.getInstance();
		//Collection Event fields
		if (specimenForm.getCollectionEventdateOfEvent() == null)
		{
			specimenForm.setCollectionEventdateOfEvent(Utility.parseDateToString(cal.getTime(), Constants.DATE_PATTERN_MM_DD_YYYY));
		}
		if (specimenForm.getCollectionEventTimeInHours() == null)
		{
			specimenForm.setCollectionEventTimeInHours(Integer.toString(cal.get(Calendar.HOUR_OF_DAY)));
		}
		if (specimenForm.getCollectionEventTimeInMinutes() == null)
		{
			specimenForm.setCollectionEventTimeInMinutes(Integer.toString(cal.get(Calendar.MINUTE)));
		}

		//ReceivedEvent Fields
		if (specimenForm.getReceivedEventDateOfEvent() == null)
		{
			specimenForm.setReceivedEventDateOfEvent(Utility.parseDateToString(cal.getTime(), Constants.DATE_PATTERN_MM_DD_YYYY));
		}
		if (specimenForm.getReceivedEventTimeInHours() == null)
		{
			specimenForm.setReceivedEventTimeInHours(Integer.toString(cal.get(Calendar.HOUR_OF_DAY)));
		}
		if (specimenForm.getReceivedEventTimeInMinutes() == null)
		{
			specimenForm.setReceivedEventTimeInMinutes(Integer.toString(cal.get(Calendar.MINUTE)));
		}

	}

	/**
	 * @param specimenForm instance of NewSpecimenForm
	 */
	private void clearCollectionEvent(NewSpecimenForm specimenForm)
	{
		specimenForm.setCollectionEventCollectionProcedure("");
		specimenForm.setCollectionEventComments("");
		specimenForm.setCollectionEventContainer("");
		specimenForm.setCollectionEventdateOfEvent("");
		specimenForm.setCollectionEventTimeInHours("");
		specimenForm.setCollectionEventTimeInMinutes("");
		specimenForm.setCollectionEventUserId(-1);
	}

	/**
	 * @param specimenForm instance of NewSpecimenForm
	 */
	private void clearReceivedEvent(NewSpecimenForm specimenForm)
	{
		specimenForm.setReceivedEventComments("");
		specimenForm.setReceivedEventDateOfEvent("");
		specimenForm.setReceivedEventReceivedQuality("");
		specimenForm.setReceivedEventTimeInHours("");
		specimenForm.setReceivedEventTimeInMinutes("");
		specimenForm.setReceivedEventUserId(-1);
	}

	Vector checkForInitialValues(TreeMap containerMap)
	{
		Vector initialValues = null;

		if (containerMap.size() > 0)
		{
			String[] startingPoints = new String[3];

			Set keySet = containerMap.keySet();
			Iterator itr = keySet.iterator();
			NameValueBean nvb = (NameValueBean) itr.next();
			startingPoints[0] = nvb.getValue();

			Map map1 = (Map) containerMap.get(nvb);
			keySet = map1.keySet();
			itr = keySet.iterator();
			nvb = (NameValueBean) itr.next();
			startingPoints[1] = nvb.getValue();

			List list = (List) map1.get(nvb);
			nvb = (NameValueBean) list.get(0);
			startingPoints[2] = nvb.getValue();

			Logger.out.info("Starting points[0]" + startingPoints[0]);
			Logger.out.info("Starting points[1]" + startingPoints[1]);
			Logger.out.info("Starting points[2]" + startingPoints[2]);
			initialValues = new Vector();
			initialValues.add(startingPoints);

		}
		return initialValues;

		//request.setAttribute("initValues", initialValues);
	}

     	//Consent Tracking (Virender Mehta)	
	/**
	 * This function will return SpecimenCollectionGroup object 
	 * @param scg_id Selected SpecimenCollectionGroup ID
	 * @return specimenCollectionGroupObject
	 */
	private SpecimenCollectionGroup getSCGObj(String scg_id) throws DAOException
	{
		SpecimenCollectionGroupBizLogic specimenCollectionBizLogic = (SpecimenCollectionGroupBizLogic)BizLogicFactory.getInstance().getBizLogic(Constants.SPECIMEN_COLLECTION_GROUP_FORM_ID);
		String colName = "id";			
		List getSCGIdFromDB = specimenCollectionBizLogic.retrieve(SpecimenCollectionGroup.class.getName(), colName, scg_id);
		SpecimenCollectionGroup specimenCollectionGroupObject = null;
		if(getSCGIdFromDB!=null && !getSCGIdFromDB.isEmpty())
		{
			specimenCollectionGroupObject = (SpecimenCollectionGroup)getSCGIdFromDB.get(0);
		}
		return specimenCollectionGroupObject;
	}
	
	/**
	 * This function will return SpecimenCollectionGroup object 
	 * @param scg_id Selected SpecimenCollectionGroup ID
	 * @return collectionProtocolRegistrationObject
	 */
	private CollectionProtocolRegistration getCPRObj(String scg_id) throws DAOException
	{
		IBizLogic bizLogic = BizLogicFactory.getInstance().getBizLogic(Constants.DEFAULT_BIZ_LOGIC);
		String colName = "collectionProtocolRegistration";			
		CollectionProtocolRegistration collectionProtocolRegistrationObject =(CollectionProtocolRegistration)bizLogic.retrieveAttribute(SpecimenCollectionGroup.class.getName(),new Long(scg_id) ,colName);
		return collectionProtocolRegistrationObject;
	}
	/**
	* For ConsentTracking Preparing consentResponseForScgValues for populating Dynamic contents on the UI  
	* @param partiResponseCollection This Containes the collection of ConsentTier Response at CPR level
	* @param statusResponseCollection This Containes the collection of ConsentTier Response at Specimen level 
	* @return tempMap
	*/
    private Map prepareSCGResponseMap(Collection statusResponseCollection, Collection partiResponseCollection)
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
							String specimenResponsekey  = "ConsentBean:"+i+"_specimenLevelResponse";
							String specimenResponseIDkey ="ConsentBean:"+i+"_specimenLevelResponseID";
							
							tempMap.put(idKey, consent.getId());
							tempMap.put(statementKey,consent.getStatement());
							tempMap.put(participantResponsekey, consentTierResponse.getResponse());
							tempMap.put(participantResponceIdKey, consentTierResponse.getId());
							tempMap.put(specimenResponsekey, consentTierstatus.getStatus());
							tempMap.put(specimenResponseIDkey, consentTierstatus.getId());
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
	 * Prepare Map for Consent tiers
	 * @param participantResponseList This list will be iterated to map to populate participant Response status.
	 * @return tempMap
	 */
	private Map prepareConsentMap(List participantResponseList,Collection consentResponse)
	{
		Map tempMap = new HashMap();
		Long consentTierID;
		Long consentID;
		if(participantResponseList!=null||consentResponse!=null)
		{
			int i = 0;
			Iterator consentResponseCollectionIter = participantResponseList.iterator();
			while(consentResponseCollectionIter.hasNext())
			{
				ConsentTierResponse consentTierResponse = (ConsentTierResponse)consentResponseCollectionIter.next();
				Iterator statusCollectionIter = consentResponse.iterator();	
				consentTierID=consentTierResponse.getConsentTier().getId();
				while(statusCollectionIter.hasNext())
				{
					ConsentTierStatus consentTierstatus=(ConsentTierStatus)statusCollectionIter.next();
					consentID=consentTierstatus.getConsentTier().getId();
					if(consentTierID.longValue()==consentID.longValue())						
					{
						ConsentTier consent = consentTierResponse.getConsentTier();
						String idKey="ConsentBean:"+i+"_consentTierID";
						String statementKey="ConsentBean:"+i+"_statement";
						String responseKey="ConsentBean:"+i+"_participantResponse";
						String participantResponceIdKey="ConsentBean:"+i+"_participantResponseID";
						String scgResponsekey  = "ConsentBean:"+i+"_specimenCollectionGroupLevelResponse";
						String scgResponseIDkey ="ConsentBean:"+i+"_specimenCollectionGroupLevelResponseID";
						String specimenResponsekey  = "ConsentBean:"+i+"_specimenLevelResponse";
						String specimenResponseIDkey ="ConsentBean:"+i+"_specimenLevelResponseID";
					
						tempMap.put(idKey, consent.getId());
						tempMap.put(statementKey,consent.getStatement());
						tempMap.put(responseKey,consentTierResponse.getResponse());
						tempMap.put(participantResponceIdKey,consentTierResponse.getId());
						tempMap.put(scgResponsekey, consentTierstatus.getStatus());
						tempMap.put(scgResponseIDkey, consentTierstatus.getId());
						tempMap.put(specimenResponsekey,consentTierstatus.getStatus());
						tempMap.put(specimenResponseIDkey,null);
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
	 * This function will return CollectionProtocolRegistration object 
	 * @param scg_id Selected SpecimenCollectionGroup ID
	 * @return collectionProtocolRegistration
	 */
	private Specimen getSpecimenObj(String specimenID) throws DAOException
	{
		NewSpecimenBizLogic newSpecimenBizLogic = (NewSpecimenBizLogic)BizLogicFactory.getInstance().getBizLogic(Constants.NEW_SPECIMEN_FORM_ID);
		//SpecimenCollectionGroupBizLogic specimenCollectionBizLogic = (SpecimenCollectionGroupBizLogic)BizLogicFactory.getInstance().getBizLogic(Constants.SPECIMEN_COLLECTION_GROUP_FORM_ID);
		String colName = "id";			
		List getNewSpecimenIdFromDB = newSpecimenBizLogic.retrieve(Specimen.class.getName(), colName, specimenID);
		Specimen SpecimenObject = null;
		if(getNewSpecimenIdFromDB!=null && !getNewSpecimenIdFromDB.isEmpty())
		{
			SpecimenObject = (Specimen)getNewSpecimenIdFromDB.get(0);
		}
		return SpecimenObject;
	}
	//Consent Tracking (Virender Mehta)	

	/**
	 * @param specimenForm instance of NewSpecimenForm
	 * @param specimenCollectionGroupId String containing specimen collection group Id
	 * @param specimenCollectionGroupName String containing specimen collection group name
	 */
	private void setFormValues(NewSpecimenForm specimenForm, String specimenCollectionGroupId,
			                   String specimenCollectionGroupName)
	{
		specimenForm.setSpecimenCollectionGroupId(specimenCollectionGroupId);
		specimenForm.setSpecimenCollectionGroupName(specimenCollectionGroupName);
		specimenForm.setVirtuallyLocated(true);
		specimenForm.setParentSpecimenId("");
		specimenForm.setLabel("");
		specimenForm.setBarcode("");
		specimenForm.setPositionInStorageContainer("");
		specimenForm.setPositionDimensionOne("");
		specimenForm.setPositionDimensionTwo("");
		specimenForm.setStorageContainer("");

	}
	
	/**
	 * Patch ID: Bug#3184_2
	 */
	/**
	 * This method initializes the List of SpecimenCollectionGroup in the system and sets the list as an
	 * attribute in the request.
	 * @param bizLogic NewSpecimenBizLogic to fetch the SpecimenCollectionGroup list
	 * @param request HttpServletRequest in which the list is set as an attribute
	 * @throws DAOException on failure to initialize the list
	 */
	 /**For Migration Start**/
/*	private void initializeAndSetSpecimenCollectionGroupIdList(NewSpecimenBizLogic bizLogic, HttpServletRequest request) throws DAOException 
	{
		String sourceObjectName = SpecimenCollectionGroup.class.getName();
		String[] displayNameFields = {"name"};
		String valueField = Constants.SYSTEM_IDENTIFIER;
		
		List specimenCollectionGroupList = bizLogic.getList(sourceObjectName, displayNameFields, valueField, true);
		request.setAttribute(Constants.SPECIMEN_COLLECTION_GROUP_LIST, specimenCollectionGroupList);
	}
*/
	/**
	 * This method generates Map of SpecimenClass and the List of the corresponding Types. 
	 * @param tempMap a temporary Map for avoiding duplication of values.
	 * @param subTypeMap the Map of SpecimenClass and the List of the corresponding Types
	 * @param specimenClass Class of Speciment
	 * @param specimenType Type of Specimen
	 */
	private void populateSpecimenTypeLists(Map<String, String> tempMap, Map<String, List<NameValueBean>> subTypeMap, 
			String specimenClass, String specimenType) 
	{
		List<NameValueBean> tempList = subTypeMap.get(specimenClass);
		if (tempList == null)
		{
			tempList = new ArrayList<NameValueBean>();
			tempList.add(new NameValueBean(Constants.SELECT_OPTION, "-1"));
			tempList.add(new NameValueBean(specimenType, specimenType));
			
			subTypeMap.put(specimenClass, tempList);
			tempMap.put(specimenClass + specimenType + Constants.SPECIMEN_TYPE, specimenType);
		}
		else
		{
			tempList = subTypeMap.get(specimenClass);
			tempList.add(new NameValueBean(specimenType, specimenType));
			Collections.sort(tempList);
			
			subTypeMap.put(specimenClass, tempList);
			tempMap.put(specimenClass + specimenType + Constants.SPECIMEN_TYPE, specimenType);
		}
	}
	
	/**
	 * This method populates all the values form the system for the respective lists. 
	 * @param specimenForm NewSpecimenForm to set the List of TissueSite and PathologicalStatus
	 * @param specimenClassList List of Specimen Class
	 * @param specimenTypeList List of Specimen Type
	 * @param tissueSiteList List of Tissue Site
	 * @param tissueSideList List of Tissue Side
	 * @param pathologicalStatusList List of Pathological Status
	 * @param subTypeMap Map of the Class and their corresponding Types
	 * @throws DAOException on failure to populate values from the system
	 */private void populateAllLists(NewSpecimenForm specimenForm, List<NameValueBean> specimenClassList, List<NameValueBean> specimenTypeList, 
			List<NameValueBean> tissueSiteList,	List<NameValueBean> tissueSideList,	List<NameValueBean> pathologicalStatusList,
			Map<String, List<NameValueBean>> subTypeMap) 
			throws DAOException
	{
		// Getting the specimen type list
		specimenTypeList = Utility.getListFromCDE(Constants.CDE_NAME_SPECIMEN_TYPE);
		
		/**
	     * Name : Virender Mehta
	     * Reviewer: Sachin Lale
	     * Bug ID: TissueSiteCombo_BugID
	     * Patch ID:TissueSiteCombo_BugID_1
	     * See also:TissueSiteCombo_BugID_2
	     * Description: Getting TissueList with only Leaf node
		 */
		tissueSiteList.addAll(Utility.tissueSiteList());
    	
		//Getting tissue side list
		tissueSideList.addAll(Utility.getListFromCDE(Constants.CDE_NAME_TISSUE_SIDE));
		
		//Getting pathological status list
		pathologicalStatusList.addAll(Utility.getListFromCDE(Constants.CDE_NAME_PATHOLOGICAL_STATUS));
					
		// get the Specimen class and type from the cde
		CDE specimenClassCDE = CDEManager.getCDEManager().getCDE(Constants.CDE_NAME_SPECIMEN_CLASS);
		Set<PermissibleValue> setPV = specimenClassCDE.getPermissibleValues();
		for (PermissibleValue pv : setPV)
		{
			String tmpStr = pv.getValue();
			Logger.out.debug(tmpStr);
			specimenClassList.add(new NameValueBean(tmpStr, tmpStr));

			List<NameValueBean> innerList = new ArrayList<NameValueBean>();
			innerList.add(new NameValueBean(Constants.SELECT_OPTION, "-1"));
			
			Set<PermissibleValue> list1 = pv.getSubPermissibleValues();
			Logger.out.debug("list1 " + list1);
			for (PermissibleValue pv1 : list1)
			{
				// set specimen type
				String tmpInnerStr = pv1.getValue();
				Logger.out.debug("\t\t" + tmpInnerStr);
				innerList.add(new NameValueBean(tmpInnerStr, tmpInnerStr));
			}
			Collections.sort(innerList);
			subTypeMap.put(tmpStr, innerList);
		} // class and values set
		Logger.out.debug("\n\n\n\n**********MAP DATA************\n");
		
		// Setting the default values
		if (specimenForm.getTissueSite() == null)
		{
			specimenForm.setTissueSite((String)DefaultValueManager.getDefaultValue(Constants.DEFAULT_TISSUE_SITE));
		}
		if (specimenForm.getPathologicalStatus() == null)
		{
			specimenForm.setPathologicalStatus((String)DefaultValueManager.getDefaultValue(Constants.DEFAULT_PATHOLOGICAL_STATUS));
		}
	}
	
	/**
	 * This method populates the values for the lists form Specimen Requirements.
	 * @param request HttpServletRequest to set some attribute values
	 * @param specimenForm NewSpecimenForm to check the value of SpecimenClass
	 * @param specimenCollectionGroupId Identifier of SpecimenCollectionGroup
	 * @param specimenClassList List of Specimen Class
	 * @param specimenTypeList List of Specimen Type
	 * @param tissueSiteList List of Tissue Site
	 * @param tissueSideList List of Tissue Side
	 * @param pathologicalStatusList List of Pathological Status
	 * @param subTypeMap Map of the Class and their corresponding Types
	 * @throws DAOException on failure to populate values from the system
	 */
	 private int populateAllRestrictedLists(HttpServletRequest request, NewSpecimenForm specimenForm, String specimenCollectionGroupId, 
			List<NameValueBean> specimenClassList, List<NameValueBean> specimenTypeList, List<NameValueBean> tissueSiteList, 
			List<NameValueBean> tissueSideList,	List<NameValueBean> pathologicalStatusList, Map<String, List<NameValueBean>> subTypeMap) 
			throws DAOException
	{
		// Get SpecimenCollectionGroup given a SpecimenCollectionGroupId
		SpecimenCollectionGroup scg = Utility.getSpecimenCollectionGroup(specimenCollectionGroupId);
		request.setAttribute("SpecimenCollectionGroupId", specimenCollectionGroupId);
		/**For Migration Start**/
		int numberOfSpecimen=0; 
		if(scg!=null)
		{
			
			request.setAttribute("SpecimenCollectionGroupName", scg.getName());
		
		String operation = (String) request.getParameter(Constants.OPERATION);
		if (operation.equals(Constants.EDIT)) 
		{
			request.setAttribute("scgName", scg.getName());
		}
		
		String specimenClass = null;
		String specimenType = null;
		String tissueSite = null;
		String pathologicalStatus = null;
		Double quantity = null;
		
		Map<String,String> tempMap = new HashMap<String,String>();
		CollectionProtocolEvent collectionProtocolEvent = scg.getCollectionProtocolEvent();
		
		/**
		 * Name: Vijay Pande
		 * Reviewer Name: Aarti Sharma
		 * specimenRequirementCollection is explicitly retrived from DB since its lazy load property is true
		 */
		DefaultBizLogic defaultBizLogic=new DefaultBizLogic();
		Collection<SpecimenRequirement> specimenRequirementCollection=(Collection)defaultBizLogic.retrieveAttribute(CollectionProtocolEvent.class.getName(), collectionProtocolEvent.getId(), Constants.COLUMN_NAME_SPECIEMEN_REQUIREMENT_COLLECTION);
		for(SpecimenRequirement specimenRequirement : specimenRequirementCollection)
		{
			specimenClass = specimenRequirement.getSpecimenClass();
			if (tempMap.get(specimenClass + Constants.SPECIMEN_CLASS) == null) 
			{
				specimenClassList.add(new NameValueBean(specimenClass, specimenClass));
				tempMap.put(specimenClass + Constants.SPECIMEN_CLASS, specimenClass);
			}

			specimenType = specimenRequirement.getSpecimenType();
			if (tempMap.get(specimenClass + specimenType + Constants.SPECIMEN_TYPE) == null) 
			{
				populateSpecimenTypeLists(tempMap, subTypeMap, specimenClass, specimenType);
			}

			tissueSite = specimenRequirement.getTissueSite();
			if (tempMap.get(tissueSite + Constants.TISSUE_SITE) == null) 
			{
				tissueSiteList.add(new NameValueBean(tissueSite, tissueSite));
				tempMap.put(tissueSite + Constants.TISSUE_SITE, tissueSite);
			}

			pathologicalStatus = specimenRequirement.getPathologyStatus();
			if (tempMap.get(pathologicalStatus + Constants.CDE_NAME_PATHOLOGICAL_STATUS) == null) 
			{
				pathologicalStatusList.add(new NameValueBean(pathologicalStatus, pathologicalStatus));
				tempMap.put(pathologicalStatus + Constants.CDE_NAME_PATHOLOGICAL_STATUS, pathologicalStatus);
			}
			
			/**
			 * Patch ID: Bug#4245_3
			 * Description: Pre-population of quantity value in case of a single specimen and single specimen requirement.
			 */
			quantity = specimenRequirement.getQuantity().getValue();
		}

		//Patch ID: Bug#3184_26
		// Setting tissue side list
		tissueSideList.addAll(Utility.getListFromCDE(Constants.CDE_NAME_TISSUE_SIDE));
		
		//Set the values as selected only if one specimen requirement is present
		numberOfSpecimen = specimenRequirementCollection.size();
		if(numberOfSpecimen == 1)
		{
			specimenForm.setClassName(specimenClass);
			specimenForm.setType(specimenType);
			specimenForm.setTissueSite(tissueSite);
			specimenForm.setPathologicalStatus(pathologicalStatus);
			//Patch ID: Bug#4245_4
			if(Utility.isQuantityDouble(specimenClass, specimenType))
			{						
				specimenForm.setQuantity(quantity.toString());
			}
			else
			{        				
				int qty = quantity.intValue();
				specimenForm.setQuantity(Integer.toString(qty));
			}
		}
		}
		else
		{
			populateAllLists(specimenForm, specimenClassList, specimenTypeList, tissueSiteList, tissueSideList, pathologicalStatusList, subTypeMap);
		}
		return numberOfSpecimen;
	}
	 /**
		 * Name : Ashish Gupta
		 * Reviewer Name : Sachin Lale 
		 * Bug ID: 2741
		 * Patch ID: 2741_13 
		 * Description: Method to propagate Events to multiple specimens from scg
		*/
		/**
		 * @param specimenCollectionGroupForm instance of SpecimenCollectionGroupForm
		 * @param specimenForm instance of NewSpecimenForm
		 */
		public static void populateEventsFromScg(SpecimenCollectionGroupForm specimenCollectionGroupForm,NewSpecimenForm specimenForm)
		{
			specimenForm.setCollectionEventUserId(specimenCollectionGroupForm.getCollectionEventUserId());		
			specimenForm.setReceivedEventUserId(specimenCollectionGroupForm.getReceivedEventUserId());	
			specimenForm.setCollectionEventCollectionProcedure("Not Specified");	
			specimenForm.setCollectionEventContainer("Not Specified");	
			specimenForm.setReceivedEventReceivedQuality("Not Specified");	
			specimenForm.setCollectionEventdateOfEvent(specimenCollectionGroupForm.getCollectionEventdateOfEvent());	
			specimenForm.setCollectionEventTimeInHours(specimenCollectionGroupForm.getCollectionEventTimeInHours());	
			specimenForm.setCollectionEventTimeInMinutes(specimenCollectionGroupForm.getCollectionEventTimeInMinutes());	
			specimenForm.setReceivedEventDateOfEvent(specimenCollectionGroupForm.getReceivedEventDateOfEvent());	
			specimenForm.setReceivedEventTimeInHours(specimenCollectionGroupForm.getReceivedEventTimeInHours());	
			specimenForm.setReceivedEventTimeInMinutes(specimenCollectionGroupForm.getReceivedEventTimeInMinutes());		
		}
    /**
	 * This function is used for retriving specimen and sub specimen's attributes
	 * @param specimenObj
	 * @param finalDataList
     * @throws DAOException 
	 */
	private void getSpecimenDetails(Specimen specimenObj, List finalDataList) throws DAOException
	{
		List specimenDetailList=new ArrayList();
		IBizLogic bizLogic = BizLogicFactory.getInstance().getBizLogic(Constants.DEFAULT_BIZ_LOGIC);
		specimenDetailList.add(specimenObj.getLabel());
		specimenDetailList.add(specimenObj.getType());
		if(specimenObj.getStorageContainer()==null)
		{
			specimenDetailList.add(Constants.VIRTUALLY_LOCATED);
		}
		else
		{
			StorageContainer storageContainer = (StorageContainer) bizLogic.retrieveAttribute(Specimen.class.getName(), specimenObj.getId(), "storageContainer");
			//specimenObj.getStorageContainer().getName()+": X-Axis-"+specimenObj.getPositionDimensionOne()+", Y-Axis-"+specimenObj.getPositionDimensionTwo();
			String storageLocation=storageContainer.getName()+": X-Axis-"+specimenObj.getPositionDimensionOne()+", Y-Axis-"+specimenObj.getPositionDimensionTwo();
			specimenDetailList.add(storageLocation);
		}
		specimenDetailList.add(specimenObj.getClassName());
		finalDataList.add(specimenDetailList);
		Collection childrenSpecimen = (Collection)bizLogic.retrieveAttribute(Specimen.class.getName(),specimenObj.getId(),"elements(childrenSpecimen)");
		//if(specimenObj.getChildrenSpecimen().size()>0)
		if(childrenSpecimen.size()>0)
		{
			Collection childSpecimenCollection = childrenSpecimen;
			Iterator itr = childSpecimenCollection.iterator();
			while(itr.hasNext())
			{
				Specimen childSpecimen = (Specimen) itr.next();
				getSpecimenDetails(childSpecimen,finalDataList);
			}			
		}		
	}
	/**
	 * This function adds the columns to the List
	 * @return columnList 
	 */
	public List columnNames()
	{
		List columnList = new ArrayList();
		columnList.add(Constants.LABLE);
		columnList.add(Constants.TYPE);
		columnList.add(Constants.STORAGE_CONTAINER_LOCATION);
		columnList.add(Constants.CLASS_NAME);
		return columnList; 
	}
	
	private Long getAssociatedIdentifiedReportId(Long specimenId) throws DAOException, ClassNotFoundException
	{
		String hqlString="select scg.identifiedSurgicalPathologyReport.id " +
		" from edu.wustl.catissuecore.domain.SpecimenCollectionGroup as scg, " +
		" edu.wustl.catissuecore.domain.Specimen as specimen" +
		" where specimen.id = " + specimenId +
		" and specimen.id in elements(scg.specimenCollection)";
		List reportIDList=Utility.executeQuery(hqlString);
		if(reportIDList!=null && reportIDList.size()>0)
		{
			return ((Long)reportIDList.get(0));
		}
		return null;
	}
}
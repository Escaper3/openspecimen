/**
 * <p>Title: RequestDetailsAction Class>
 * <p>Description:	This class initializes the fields of RequestDetails.jsp Page</p>
 * Copyright:    Copyright (c) year
 * Company: Washington University, School of Medicine, St. Louis.
 * @author Ashish Gupta
 * @version 1.00
 * Created on Oct 05,2006
 */

package edu.wustl.catissuecore.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.wustl.catissuecore.actionForm.RequestDetailsForm;
import edu.wustl.catissuecore.bean.DefinedArrayDetailsBean;
import edu.wustl.catissuecore.bean.DefinedArrayRequestBean;
import edu.wustl.catissuecore.bean.ExistingArrayDetailsBean;
import edu.wustl.catissuecore.bean.RequestDetailsBean;
import edu.wustl.catissuecore.bean.RequestViewBean;
import edu.wustl.catissuecore.bizlogic.BizLogicFactory;
import edu.wustl.catissuecore.bizlogic.DistributionBizLogic;
import edu.wustl.catissuecore.bizlogic.NewSpecimenBizLogic;
import edu.wustl.catissuecore.bizlogic.OrderBizLogic;
import edu.wustl.catissuecore.bizlogic.SpecimenCollectionGroupBizLogic;
import edu.wustl.catissuecore.domain.DerivedSpecimenOrderItem;
import edu.wustl.catissuecore.domain.DistributedItem;
import edu.wustl.catissuecore.domain.DistributionProtocol;
import edu.wustl.catissuecore.domain.ExistingSpecimenArrayOrderItem;
import edu.wustl.catissuecore.domain.ExistingSpecimenOrderItem;
import edu.wustl.catissuecore.domain.NewSpecimenArrayOrderItem;
import edu.wustl.catissuecore.domain.NewSpecimenOrderItem;
import edu.wustl.catissuecore.domain.OrderDetails;
import edu.wustl.catissuecore.domain.OrderItem;
import edu.wustl.catissuecore.domain.PathologicalCaseOrderItem;
import edu.wustl.catissuecore.domain.Site;
import edu.wustl.catissuecore.domain.Specimen;
import edu.wustl.catissuecore.domain.SpecimenArray;
import edu.wustl.catissuecore.domain.SpecimenArrayType;
import edu.wustl.catissuecore.domain.SpecimenCollectionGroup;
import edu.wustl.catissuecore.domain.SpecimenOrderItem;
import edu.wustl.catissuecore.domain.User;
import edu.wustl.catissuecore.util.OrderingSystemUtil;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.action.BaseAction;
import edu.wustl.common.beans.NameValueBean;
import edu.wustl.common.cde.CDEManager;
import edu.wustl.common.dao.AbstractDAO;
import edu.wustl.common.dao.DAOFactory;
import edu.wustl.common.util.dbManager.DAOException;
import edu.wustl.common.util.dbManager.HibernateMetaData;
import edu.wustl.common.util.logger.Logger;

public class RequestDetailsAction extends BaseAction
{
	/**
	 * Overrides the execute method of Action class.
	 * Initializes the various fields in RequestDetails.jsp Page.
	 * @param mapping object
	 * @param form object
	 * @param request object
	 * @param response object
	 * @return ActionForward object
	 * @throws Exception object
	 * */
	public ActionForward executeAction(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		RequestDetailsForm requestDetailsForm = (RequestDetailsForm) form;
		// The request Id on which the user has clicked
		String requestId = "";
			
		if (request.getParameter("id") != null && !request.getParameter("id").equals("0"))
		{
			requestId = request.getParameter("id"); //		
			//Setting the order id in the form to retrieve the corresponding orderitems from the db in CommonAddEditAction.
			requestDetailsForm.setId((new Long(requestId)).longValue());
		}
		else
		// while returning from specimen page
		{
			Object obj = request.getSession().getAttribute("REQUEST_DETAILS_FORM");
			RequestDetailsForm rDForm = null;
			if (obj != null)
				rDForm = (RequestDetailsForm) obj;
			requestId = "" + rDForm.getId();
			requestDetailsForm.setId((new Long(requestId)).longValue());
			
		}

		// order items status to display
		List requestedItemsStatusList = CDEManager.getCDEManager().getPermissibleValueList(Constants.CDE_NAME_REQUESTED_ITEMS_STATUS, null);
		request.setAttribute(Constants.REQUESTED_ITEMS_STATUS_LIST, requestedItemsStatusList);

		//Setting the Site List
		List siteList = getSiteListToDisplay();
		request.setAttribute(Constants.SITE_LIST_OBJECT, siteList);

		//setting Item Status List in Request
		request.setAttribute(Constants.ITEM_STATUS_LIST, OrderingSystemUtil.getPossibleStatusList(Constants.ORDER_REQUEST_STATUS_NEW));

		//setting Items status list without "Distributed" option.
		List tempList = OrderingSystemUtil.getPossibleStatusList(Constants.ORDER_REQUEST_STATUS_NEW);
		Iterator tempListIter = tempList.iterator();
		while (tempListIter.hasNext())
		{
			NameValueBean nameValueBean = (NameValueBean) tempListIter.next();
			if (nameValueBean.getValue().equalsIgnoreCase(Constants.ORDER_REQUEST_STATUS_DISTRIBUTED))
			{
				tempList.remove(nameValueBean);
				tempList.add(new NameValueBean(Constants.ORDER_REQUEST_STATUS_READY_FOR_ARRAY_PREPARATION,
						Constants.ORDER_REQUEST_STATUS_READY_FOR_ARRAY_PREPARATION));
				break;
			}
		}
		//For orderitems in defined array, "Ready for array preparation" status is present instead of "Distribute"		
		request.setAttribute(Constants.ITEM_STATUS_LIST_FOR_ITEMS_IN_ARRAY, tempList);

		//The order items list corresponding to the request Id
		getRequestDetailsList(requestId, requestDetailsForm, request);

		if(request.getParameter(Constants.TAB_INDEX_ID)!=null)
		{
			Integer tabIndexId = new Integer(request.getParameter(Constants.TAB_INDEX_ID));
			requestDetailsForm.setTabIndex(tabIndexId.intValue());
		}
		return mapping.findForward("success");
	}

	private void cleanSession(HttpServletRequest request)
	{
		request.getSession().removeAttribute(Constants.EXISISTINGARRAY_REQUESTS_LIST);
		request.getSession().removeAttribute(Constants.DEFINEDARRAY_REQUESTS_LIST);
		request.getSession().removeAttribute(Constants.REQUEST_DETAILS_LIST);
	}
	
	/**
	 * This function constructs lists of RequestDetails bean objects,map of definedarrays and ExistingArrayDetails bean instances to display
	 * on RequestDetails.jsp and ArrayRequests.jsp by setting all the lists in HttpServletRequest object.
	 * @param id String containing the requestId
	 * @param requestDetailsForm RequestDetailsForm object
	 * @param request HttpServletRequest object
	 * @throws DAOException object
	 */
	private void getRequestDetailsList(String id, RequestDetailsForm requestDetailsForm, HttpServletRequest request) throws DAOException
	{
		//fetching the order object corresponding to obtained id.

		long startTime = System.currentTimeMillis();
		AbstractDAO dao = DAOFactory.getInstance().getDAO(Constants.HIBERNATE_DAO);

		try
		{
			dao.openSession(null);

			OrderBizLogic orderBizLogic = (OrderBizLogic) BizLogicFactory.getInstance().getBizLogic(Constants.REQUEST_LIST_FILTERATION_FORM_ID);
			OrderDetails orderDetails = (OrderDetails)orderBizLogic.getOrderListFromDB(id,dao);

//			 The request details  corresponding to the request Id
			RequestViewBean requestListBean = getRequestObject(orderDetails);
			request.setAttribute(Constants.REQUEST_HEADER_OBJECT, requestListBean);
					
			Collection orderItemsListFromDB = orderDetails.getOrderItemCollection();

			//populating Map in the form only on loading. If error is present, use form present in request.
			ActionErrors errors = (ActionErrors) request.getAttribute(Globals.ERROR_KEY);
			if (errors == null)
			{
				cleanSession(request);
				//Showing specimen tab by default.
				requestDetailsForm.setTabIndex(1);
				requestDetailsForm.setAllValuesForOrder(orderDetails, request);

				List requestDetailsList = new ArrayList();
				OrderItem orderItem = null;
				//List containing the list of defined arrays and existing arrays  
				List arrayRequestDetailsList = new ArrayList();
				List arrayRequestDetailsMapList = new ArrayList();
				if ((orderItemsListFromDB != null) && (orderItemsListFromDB.isEmpty() == false))
				{
					Iterator iter = orderItemsListFromDB.iterator();
					while (iter.hasNext())
					{
						orderItem = (OrderItem) iter.next();
						if (orderItem instanceof SpecimenOrderItem)
						{
							requestDetailsList = populateRequestDetailsListForSpecimenOrderItems(orderItem, request, requestDetailsList);
						}
						//In case of Defined Array
						if (orderItem instanceof NewSpecimenArrayOrderItem)
						{
							Map arrayMap = new HashMap();
							NewSpecimenArrayOrderItem newSpecimenArrayOrderItem = (NewSpecimenArrayOrderItem) orderItem;
							arrayMap = populateArrayMap(newSpecimenArrayOrderItem);
							//Add defined-array list into the arrayRequestDetails list 
							arrayRequestDetailsMapList.add(arrayMap);
						}
						//In case of Existing BioSpecimen Array Items
						if (orderItem instanceof ExistingSpecimenArrayOrderItem)
						{
							ExistingSpecimenArrayOrderItem existingSpecimenArrayOrderItem = (ExistingSpecimenArrayOrderItem) orderItem;
							ExistingArrayDetailsBean existingArrayDetailsBean = populateExistingArrayItemsList(existingSpecimenArrayOrderItem);
							//Add existingArrayBEan list into arrayRequestDetails list
							arrayRequestDetailsList.add(existingArrayDetailsBean);
						}
					}//End while
				}
				//Call to populateItemStatusList() when order items present in OrderList
				if (requestDetailsList.size() > 0)
				{
					populateItemStatusList(requestDetailsList, request, "addToOrderList");
				}
				//Call to populateItemStatusList() when order items are added to Defined Array
				if (arrayRequestDetailsMapList.size() > 0)
				{
					populateItemStatusList(arrayRequestDetailsMapList, request, "addToDefinedArrays");
				}
				//Call to populateItemStatusList() when order items are added to Existing Array
				if (arrayRequestDetailsList.size() > 0)
				{
					populateItemStatusList(arrayRequestDetailsList, request, "addToExistingArrays");
				}

				long endTime = System.currentTimeMillis();
				System.out.println("Execute time of getRequestDetailsList :" + (endTime-startTime));

			}
			else
			{
				populateRequestForMap(requestDetailsForm , orderDetails);
			}

		}

		catch(DAOException e)
		{
			Logger.out.error(e.getMessage(), e);


		}
		finally
		{
			try
			{
				dao.closeSession();
			}
			catch(DAOException daoEx)
			{
				Logger.out.error(daoEx.getMessage(), daoEx);

			}
		}
	}


private OrderItem getOrderItem(OrderDetails orderDetails , Long orderItemId )
{
	Collection orderItemsList = orderDetails.getOrderItemCollection();	
	
	Iterator orderItemsListItr = orderItemsList.iterator();
	while(orderItemsListItr.hasNext())
	{
		OrderItem orderItem = (OrderItem)orderItemsListItr.next();
		if(orderItem.getId().equals(orderItemId))
		{
			return orderItem;
		}
	}
	
	return null;
	
}	
	
	/**
	 * 
	 * Need to change this method ...
	 * to remove all the DB retrieves...
	 * @param requestDetailsForm
	 * @param orderItemsListFromDB
	 */
	private void populateRequestForMap(RequestDetailsForm requestDetailsForm, OrderDetails orderDetails) throws DAOException
	{
		Map valuesMap = requestDetailsForm.getValues();
		Set keySet = valuesMap.keySet();
		Iterator iter = keySet.iterator();
		Map requestForMap = new HashMap();
		while (iter.hasNext())
		{
			String rowNumber = "";
			String orderItemId = "";
			String key = (String) iter.next();
		
			if (key.endsWith("orderItemId"))
			{
				List childrenSpecimenListToDisplay = new ArrayList();
				rowNumber = getRowNumber(key);
				//				Fetching the order item id
				orderItemId = (String) valuesMap.get(key);
				OrderItem orderItem =(OrderItem) getOrderItem(orderDetails , Long.parseLong(orderItemId) );
				//OrderItem orderItem = (OrderItem) orderItemListFromDb.get(0);
			
				
					List finalChildrenSpecimenList = new ArrayList();
										
					if (orderItem instanceof DerivedSpecimenOrderItem)
					{
						DerivedSpecimenOrderItem derivedSpecimenOrderItem = (DerivedSpecimenOrderItem) orderItem;
						Collection childrenSpecimenList = OrderingSystemUtil.getAllChildrenSpecimen(derivedSpecimenOrderItem.getParentSpecimen(), derivedSpecimenOrderItem.getParentSpecimen().getChildrenSpecimen());

						finalChildrenSpecimenList = OrderingSystemUtil.getChildrenSpecimenForClassAndType(childrenSpecimenList,
								derivedSpecimenOrderItem.getSpecimenClass(), derivedSpecimenOrderItem.getSpecimenType());
					}
					childrenSpecimenListToDisplay = OrderingSystemUtil.getNameValueBeanList(finalChildrenSpecimenList);
					if ((orderItem instanceof SpecimenOrderItem) && !(orderItem instanceof ExistingSpecimenOrderItem))
					{
						SpecimenOrderItem specimenOrderItem = (SpecimenOrderItem) orderItem;
						if (specimenOrderItem.getNewSpecimenArrayOrderItem() == null)
						{
							requestForMap.put("RequestForDropDownList:" + rowNumber, childrenSpecimenListToDisplay);
						}
						else
						{
							requestForMap.put("RequestForDropDownListArray:" + rowNumber, childrenSpecimenListToDisplay);
						}
					}
								
					if (orderItem instanceof PathologicalCaseOrderItem)
					{
						
																	
						/* chnages finsh */
						PathologicalCaseOrderItem pathologicalCaseOrderItem = (PathologicalCaseOrderItem) orderItem;
						List totalChildrenSpecimenColl = OrderingSystemUtil.getRequestForListForPathologicalCases(pathologicalCaseOrderItem.getSpecimenCollectionGroup(),
								pathologicalCaseOrderItem);
						childrenSpecimenListToDisplay = OrderingSystemUtil.getNameValueBeanList(totalChildrenSpecimenColl);
						SpecimenOrderItem specimenOrderItem = (SpecimenOrderItem) orderItem;
						if (specimenOrderItem.getNewSpecimenArrayOrderItem() == null)
						{
							requestForMap.put("RequestForDropDownList:" + rowNumber, childrenSpecimenListToDisplay);
						}
						else
						{
							requestForMap.put("RequestForDropDownListArray:" + rowNumber, childrenSpecimenListToDisplay);
						}
					}
				
			}
		}
		requestDetailsForm.setRequestForDropDownMap(requestForMap);
	}

	/**
	 * @param str
	 * @return
	 */
	private String getRowNumber(String str)
	{
		StringTokenizer stringTokenizer = new StringTokenizer(str, "_");
		String firstToken = stringTokenizer.nextToken();
		int indexOfColon = firstToken.indexOf(":");
		String rowNumber = firstToken.substring(indexOfColon + 1);
		return rowNumber;
	}

	/**
	 * @param orderItem
	 * @param request
	 * @return
	 * @throws DAOException 
	 */
	private List populateRequestDetailsListForSpecimenOrderItems(OrderItem orderItem, HttpServletRequest request, List requestDetailsList) throws DAOException
	{
		// The row number to update available quantity on selecting the required specimen from 'request for' drop down.
		int finalSpecimenListId = 0;
		SpecimenOrderItem specimenOrderItem = (SpecimenOrderItem) orderItem;
		//Incase of order items in the Order List
		if (specimenOrderItem.getNewSpecimenArrayOrderItem() == null)
		{
			RequestDetailsBean requestDetailsBean = new RequestDetailsBean();
			if (orderItem instanceof ExistingSpecimenOrderItem)
			{
				ExistingSpecimenOrderItem existingSpecimenorderItem = (ExistingSpecimenOrderItem) orderItem;
				requestDetailsBean = populateRequestDetailsBeanForExistingSpecimen(requestDetailsBean, existingSpecimenorderItem);
				requestDetailsList.add(requestDetailsBean);
				finalSpecimenListId++;
			}
			else if (orderItem instanceof DerivedSpecimenOrderItem)
			{
				DerivedSpecimenOrderItem derivedSpecimenorderItem = (DerivedSpecimenOrderItem) orderItem;
				requestDetailsBean = populateRequestDetailsBeanForDerivedSpecimen(requestDetailsBean, derivedSpecimenorderItem, request,
						finalSpecimenListId);
				requestDetailsList.add(requestDetailsBean);
				finalSpecimenListId++;
			}
			else if (orderItem instanceof PathologicalCaseOrderItem)
			{
				PathologicalCaseOrderItem pathologicalCaseOrderItem = (PathologicalCaseOrderItem) orderItem;
				requestDetailsBean = populateRequestDetailsBeanForPathologicalCase(requestDetailsBean, pathologicalCaseOrderItem, request,
						finalSpecimenListId);
				requestDetailsList.add(requestDetailsBean);
				finalSpecimenListId++;
			}
		}
		return requestDetailsList;
	}

	/**
	 * This function populates existingArrayDetailsList with the existing Bio Speicmen Array Information by fetching the data from
	 * ExistingSpecimenArrayOrderItem domain object.
	 * @param existingArrayDetailsBean ExistingArrayDetailsBean object
	 * @param existingSpecimenArrayOrderItem ExistingSpecimenArrayOrderItem object
	 * @return existingArrayDetailsBean ExistingArrayDetailsBean object
	 */
	private ExistingArrayDetailsBean populateExistingArrayItemsList(ExistingSpecimenArrayOrderItem existingSpecimenArrayOrderItem)
	{
		ExistingArrayDetailsBean existingArrayDetailsBean = new ExistingArrayDetailsBean();
		existingArrayDetailsBean.setOrderItemId(existingSpecimenArrayOrderItem.getId().toString());
		existingArrayDetailsBean.setBioSpecimenArrayName(existingSpecimenArrayOrderItem.getSpecimenArray().getName());
		existingArrayDetailsBean.setDescription(existingSpecimenArrayOrderItem.getDescription());
		existingArrayDetailsBean.setAssignedStatus(existingSpecimenArrayOrderItem.getStatus());
		existingArrayDetailsBean.setArrayId(existingSpecimenArrayOrderItem.getSpecimenArray().getId().toString());
		existingArrayDetailsBean.setRequestedQuantity(existingSpecimenArrayOrderItem.getRequestedQuantity().toString());

		return existingArrayDetailsBean;
	}

	/**
	 * This function populates the arrayDetailsList with map objects.Each map contains arrayRequestBean object as teh key and list of order items
	 * (for that defined array) as the value.
	 * @param newSpecimenArrayOrderItem NewSpecimenArrayOrderItem object
	 * @return definedArrayMap Map object
	 * @throws DAOException object
	 */
	private Map populateArrayMap(NewSpecimenArrayOrderItem newSpecimenArrayOrderItem) throws DAOException
	{
		Map definedArrayMap = new HashMap();
		List arrayItemsList = new ArrayList();
		//Create new instance of ArrayRequestBean to save the name,type,dimensions of the defined array 
		DefinedArrayRequestBean arrayRequestBean = new DefinedArrayRequestBean();
		arrayRequestBean = populateArrayRequestBean(arrayRequestBean, newSpecimenArrayOrderItem);
		Collection specimenOrderItemCollection = newSpecimenArrayOrderItem.getSpecimenOrderItemCollection();
		//Calculating the condition to enable or disable "Create Array Button"
		String condition = OrderingSystemUtil.determineCreateArrayCondition(specimenOrderItemCollection);

		if (arrayRequestBean.getAssignedStatus().trim().equalsIgnoreCase(Constants.ORDER_REQUEST_STATUS_DISTRIBUTED))
		{
			condition = "true";
		}
		arrayRequestBean.setCreateArrayButtonDisabled(condition);
		Iterator specimenOrderItemCollectionItr = specimenOrderItemCollection.iterator();
		while (specimenOrderItemCollectionItr.hasNext())
		{
			SpecimenOrderItem specimenOrderItem = (SpecimenOrderItem) specimenOrderItemCollectionItr.next();
			DefinedArrayDetailsBean arrayDetailsBean = new DefinedArrayDetailsBean();
			if (specimenOrderItem instanceof ExistingSpecimenOrderItem)
			{
				arrayDetailsBean = populateExistingSpecimensForArrayDetails(arrayDetailsBean, specimenOrderItem);
			}
			else if (specimenOrderItem instanceof DerivedSpecimenOrderItem)
			{
				arrayDetailsBean = populateDerivedSpecimensForArrayDetails(arrayDetailsBean, specimenOrderItem);
			}
			else if (specimenOrderItem instanceof PathologicalCaseOrderItem)
			{
				arrayDetailsBean = populatePathologicalCasesForArrayDetails(arrayDetailsBean, specimenOrderItem);
			}
			//arrayDetailsBean.setArrayRequestBean(arrayRequestBean);
			//Add all the arrayDetailsBean in the list
			arrayItemsList.add(arrayDetailsBean);
		}
		definedArrayMap.put(arrayRequestBean, arrayItemsList);

		//Return the list containing arrayDetailsBean instances
		return definedArrayMap;
	}

	/**
	 * @param arrayRequestBean object
	 * @param newSpecimenArrayOrderItem object
	 * @return DefinedArrayRequestBean object
	 */
	private DefinedArrayRequestBean populateArrayRequestBean(DefinedArrayRequestBean arrayRequestBean,
			NewSpecimenArrayOrderItem newSpecimenArrayOrderItem)
	{
		arrayRequestBean.setArrayName(newSpecimenArrayOrderItem.getName());
		arrayRequestBean.setArrayClass(newSpecimenArrayOrderItem.getSpecimenArrayType().getSpecimenClass());
		arrayRequestBean.setOneDimensionCapacity((newSpecimenArrayOrderItem.getSpecimenArrayType().getCapacity().getOneDimensionCapacity())
				.toString());
		arrayRequestBean.setTwoDimensionCapacity((newSpecimenArrayOrderItem.getSpecimenArrayType().getCapacity().getTwoDimensionCapacity())
				.toString());
		arrayRequestBean.setArrayType(newSpecimenArrayOrderItem.getSpecimenArrayType().getName());
		arrayRequestBean.setArrayTypeId(newSpecimenArrayOrderItem.getSpecimenArrayType().getId().toString());
		arrayRequestBean.setAssignedStatus(newSpecimenArrayOrderItem.getStatus());
		arrayRequestBean.setOrderItemId(newSpecimenArrayOrderItem.getId().toString());

		SpecimenArray specimenArrayObj = newSpecimenArrayOrderItem.getSpecimenArray();
		if (specimenArrayObj != null)
		{
			arrayRequestBean.setArrayId(specimenArrayObj.getId().toString());
		}
		//Populate status list of individual array
		List arrayStatusList = OrderingSystemUtil.getPossibleStatusList(Constants.ORDER_REQUEST_STATUS_NEW);
		arrayRequestBean.setArrayStatusList(arrayStatusList);
		return arrayRequestBean;
	}

	/**
	 * @param arrayDetailsBean object
	 * @param specimenOrderItem object
	 * @return DefinedArrayDetailsBean object
	 */
	private DefinedArrayDetailsBean populateExistingSpecimensForArrayDetails(DefinedArrayDetailsBean arrayDetailsBean,
			SpecimenOrderItem specimenOrderItem)
	{
		ExistingSpecimenOrderItem existingSpecimenOrderItem = (ExistingSpecimenOrderItem) specimenOrderItem;
		arrayDetailsBean.setRequestedItem(existingSpecimenOrderItem.getSpecimen().getLabel());
		//Add empty list since it is the case of existing speicmen
		arrayDetailsBean.setSpecimenList(new ArrayList());
		arrayDetailsBean.setSpecimenId((existingSpecimenOrderItem.getSpecimen().getId()).toString());
		arrayDetailsBean.setRequestedQuantity(existingSpecimenOrderItem.getRequestedQuantity().toString());
		arrayDetailsBean.setAvailableQuantity(existingSpecimenOrderItem.getSpecimen().getAvailableQuantity().toString());
		arrayDetailsBean.setAssignedStatus(existingSpecimenOrderItem.getStatus());
		arrayDetailsBean.setClassName(existingSpecimenOrderItem.getSpecimen().getClassName());
		arrayDetailsBean.setType(existingSpecimenOrderItem.getSpecimen().getSpecimenType());
		arrayDetailsBean.setDescription(existingSpecimenOrderItem.getDescription());
		arrayDetailsBean.setOrderItemId(existingSpecimenOrderItem.getId().toString());
		arrayDetailsBean.setInstanceOf("Existing");

		return arrayDetailsBean;
	}

	/**
	 * @param arrayDetailsBean object
	 * @param specimenOrderItem object
	 * @return DefinedArrayDetailsBean object
	 */
	private DefinedArrayDetailsBean populateDerivedSpecimensForArrayDetails(DefinedArrayDetailsBean arrayDetailsBean,
			SpecimenOrderItem specimenOrderItem)
	{
		DerivedSpecimenOrderItem derivedSpecimenOrderItem = (DerivedSpecimenOrderItem) specimenOrderItem;
		arrayDetailsBean.setRequestedItem(derivedSpecimenOrderItem.getParentSpecimen().getLabel());
		arrayDetailsBean.setSpecimenId(derivedSpecimenOrderItem.getParentSpecimen().getId().toString());
		//Obtain all children specimens
		Collection childrenSpecimenList = OrderingSystemUtil.getAllChildrenSpecimen(derivedSpecimenOrderItem.getParentSpecimen(),
				derivedSpecimenOrderItem.getParentSpecimen().getChildrenSpecimen());
		//Obtain only those specimens of this class and type from the above list
		List finalChildrenSpecimenList = OrderingSystemUtil.getChildrenSpecimenForClassAndType(childrenSpecimenList, derivedSpecimenOrderItem
				.getSpecimenClass(), derivedSpecimenOrderItem.getSpecimenType());
		List childrenSpecimenListToDisplay = OrderingSystemUtil.getNameValueBeanList(finalChildrenSpecimenList);
		arrayDetailsBean.setSpecimenList(childrenSpecimenListToDisplay);

		arrayDetailsBean.setRequestedQuantity(derivedSpecimenOrderItem.getRequestedQuantity().toString());
		arrayDetailsBean.setAvailableQuantity(derivedSpecimenOrderItem.getParentSpecimen().getAvailableQuantity().toString());
		arrayDetailsBean.setAssignedStatus(derivedSpecimenOrderItem.getStatus());
		arrayDetailsBean.setClassName(derivedSpecimenOrderItem.getSpecimenClass());
		arrayDetailsBean.setType(derivedSpecimenOrderItem.getSpecimenType());
		arrayDetailsBean.setDescription(derivedSpecimenOrderItem.getDescription());
		arrayDetailsBean.setOrderItemId(derivedSpecimenOrderItem.getId().toString());
		arrayDetailsBean.setInstanceOf("Derived");

		return arrayDetailsBean;
	}

	/**
	 * @param arrayDetailsBean object
	 * @param specimenOrderItem object
	 * @return DefinedArrayDetailsBean object
	 */
	private DefinedArrayDetailsBean populatePathologicalCasesForArrayDetails(DefinedArrayDetailsBean arrayDetailsBean,
			SpecimenOrderItem specimenOrderItem)
	{
		PathologicalCaseOrderItem pathologicalCaseOrderItem = (PathologicalCaseOrderItem) specimenOrderItem;
		boolean isDerived = false;
		arrayDetailsBean.setRequestedItem(pathologicalCaseOrderItem.getSpecimenCollectionGroup().getSurgicalPathologyNumber());
		Collection childrenSpecimenList = pathologicalCaseOrderItem.getSpecimenCollectionGroup().getSpecimenCollection();
		//		 Removing distributed option if no specimens are present in that SCG. ie. childrenSpecimenList.size() == 0
		//TODO
		if (childrenSpecimenList.size() == 0)
		{
			isDerived = true;
		}
		List totalChildrenSpecimenColl = null;
		List childrenSpecimenListToDisplay = null;
		Iterator childrenSpecimenListIterator = childrenSpecimenList.iterator();
		while (childrenSpecimenListIterator.hasNext())
		{
			Specimen specimen = (Specimen) childrenSpecimenListIterator.next();
			List childSpecimenCollection = OrderingSystemUtil.getAllChildrenSpecimen(specimen, specimen.getChildrenSpecimen());
			List finalChildrenSpecimenCollection = null;
			if (pathologicalCaseOrderItem.getSpecimenClass() != null && pathologicalCaseOrderItem.getSpecimenType() != null
					&& !pathologicalCaseOrderItem.getSpecimenClass().trim().equalsIgnoreCase("")
					&& !pathologicalCaseOrderItem.getSpecimenType().trim().equalsIgnoreCase(""))
			{ //"Derived"	   
				finalChildrenSpecimenCollection = OrderingSystemUtil.getChildrenSpecimenForClassAndType(childSpecimenCollection,
						pathologicalCaseOrderItem.getSpecimenClass(), pathologicalCaseOrderItem.getSpecimenType());
				isDerived = true;
			}
			else
			{ //"Block" . Specimen class = "Tissue" , Specimen Type = "Block".
				finalChildrenSpecimenCollection = OrderingSystemUtil.getChildrenSpecimenForClassAndType(childSpecimenCollection, "Tissue", "Block");
			}
			if (finalChildrenSpecimenCollection != null)
			{
				Iterator finalChildrenSpecimenCollectionIterator = finalChildrenSpecimenCollection.iterator();
				while (finalChildrenSpecimenCollectionIterator.hasNext())
				{
					totalChildrenSpecimenColl.add((Specimen) (finalChildrenSpecimenCollectionIterator.next()));
				}
			}
		}
		if (totalChildrenSpecimenColl == null)
		{
			totalChildrenSpecimenColl = new ArrayList();
		}
		childrenSpecimenListToDisplay = OrderingSystemUtil.getNameValueBeanList(totalChildrenSpecimenColl);
		arrayDetailsBean.setSpecimenList(childrenSpecimenListToDisplay);
		arrayDetailsBean.setSpecimenCollGroupId(pathologicalCaseOrderItem.getSpecimenCollectionGroup().getId().toString());
		if (isDerived)
		{
			arrayDetailsBean.setInstanceOf("DerivedPathological");
		}
		else
		{
			arrayDetailsBean.setInstanceOf("Pathological");
		}
		arrayDetailsBean.setRequestedQuantity(pathologicalCaseOrderItem.getRequestedQuantity().toString());
		//Displaying the quantity of the first specimen in the request for drop down.
		if (childrenSpecimenListToDisplay.size() != 0)
		{
			arrayDetailsBean.setAvailableQuantity(((Specimen) totalChildrenSpecimenColl.get(0)).getAvailableQuantity().toString());
		}
		else
		{
			arrayDetailsBean.setAvailableQuantity("-");
		}
		//Assigned Quantity
		if (pathologicalCaseOrderItem.getDistributedItem() != null)
		{
			arrayDetailsBean.setAssignedQty(pathologicalCaseOrderItem.getDistributedItem().getQuantity().toString());
		}
		arrayDetailsBean.setAssignedStatus(pathologicalCaseOrderItem.getStatus());
		arrayDetailsBean.setClassName(pathologicalCaseOrderItem.getSpecimenClass());
		arrayDetailsBean.setType(pathologicalCaseOrderItem.getSpecimenType());
		arrayDetailsBean.setDescription(pathologicalCaseOrderItem.getDescription());
		arrayDetailsBean.setOrderItemId(pathologicalCaseOrderItem.getId().toString());

		return arrayDetailsBean;
	}

	//	 Populates a list of Requset object to display  as header info on RequestDetails.jsp	
	/**
	 * @param orderDetails OrderDetails
	 * @return RequestViewBean object
	 * @throws DAOException object
	 */
	private RequestViewBean getRequestObject(OrderDetails orderDetails) throws DAOException
	{
		
		RequestViewBean requestViewBean = null;
		requestViewBean = OrderingSystemUtil.getRequestViewBeanToDisplay(orderDetails);
		requestViewBean.setComments(orderDetails.getComment());
		
		return requestViewBean;
	}

	
	
	

	/**
	 * @param id
	 * @return
	 * @throws DAOException
	 */
	private Specimen getSpecimenFromDB(String id) throws DAOException
	{
		NewSpecimenBizLogic newSpecimenBizLogic = (NewSpecimenBizLogic) BizLogicFactory.getInstance().getBizLogic(Constants.NEW_SPECIMEN_FORM_ID);
		Object object = newSpecimenBizLogic.retrieve(Specimen.class.getName(), new Long(id));
		return (Specimen) object;
	}

	/**
	 * @param id
	 * @return
	 * @throws DAOException
	 */
	private SpecimenCollectionGroup getSpecimenCollGrpFromDB(String id) throws DAOException
	{
		SpecimenCollectionGroupBizLogic specimenCollectionGroupBizLogic = (SpecimenCollectionGroupBizLogic) BizLogicFactory.getInstance()
				.getBizLogic(Constants.SPECIMEN_COLLECTION_GROUP_FORM_ID);
		Object object = specimenCollectionGroupBizLogic.retrieve(SpecimenCollectionGroup.class.getName(), new Long(id));
		
		return (SpecimenCollectionGroup) object;
	}

	/**
	 * @param id
	 * @return
	 * @throws DAOException
	 */
	private OrderItem getOrderItemFromDB(String id) throws DAOException
	{
		OrderBizLogic orderBizLogic = (OrderBizLogic) BizLogicFactory.getInstance().getBizLogic(Constants.REQUEST_LIST_FILTERATION_FORM_ID);
		Object object = orderBizLogic.retrieve(OrderItem.class.getName(), new Long(id));
		return (OrderItem) object;
	}

	/**
	 * @return List of site objects.
	 * @throws DAOException object
	 */
	private List getSiteListToDisplay() throws DAOException
	{
		OrderBizLogic orderBizLogic = (OrderBizLogic) BizLogicFactory.getInstance().getBizLogic(Constants.REQUEST_LIST_FILTERATION_FORM_ID);

		//Sets the Site list.
		String sourceObjectName = Site.class.getName();
		String[] displayNameFields = {"name"};
		String valueField = Constants.SYSTEM_IDENTIFIER;

		List siteList = orderBizLogic.getList(sourceObjectName, displayNameFields, valueField, true);

		return siteList;
	}

	/**
	 * @param requestDetailsBean RequestDetailsBean
	 * @param existingSpecimenorderItem ExistingSpecimenOrderItem
	 * @return RequestDetailsBean object
	 * @throws DAOException 
	 */
	private RequestDetailsBean populateRequestDetailsBeanForExistingSpecimen(RequestDetailsBean requestDetailsBean,
			ExistingSpecimenOrderItem existingSpecimenorderItem) throws DAOException
	{
		requestDetailsBean.setRequestedItem(existingSpecimenorderItem.getSpecimen().getLabel());
		requestDetailsBean.setSpecimenId(existingSpecimenorderItem.getSpecimen().getId().toString());
		List childrenSpecimenListToDisplay = new ArrayList();
		requestDetailsBean.setSpecimenList(childrenSpecimenListToDisplay);
		
			
		requestDetailsBean.setInstanceOf("Existing");
	
		if(existingSpecimenorderItem.getSpecimen().getConsentTierStatusCollection().isEmpty())
		{	
			requestDetailsBean.setConsentVerificationkey(Constants.NO_CONSENTS);
			
		}	
		else
		{
			requestDetailsBean.setConsentVerificationkey(Constants.VIEW_CONSENTS);
			
		}		

		
		// condition added by vaishali beacause requested Qty is coming NULL.
		//@FIX ME
	
		if (existingSpecimenorderItem.getRequestedQuantity() != null)
			requestDetailsBean.setRequestedQty(existingSpecimenorderItem.getRequestedQuantity().toString());
		requestDetailsBean.setAvailableQty(existingSpecimenorderItem.getSpecimen().getAvailableQuantity().toString());

		requestDetailsBean.setAssignedStatus(existingSpecimenorderItem.getStatus());
		requestDetailsBean.setClassName(existingSpecimenorderItem.getSpecimen().getClassName());
		requestDetailsBean.setType(existingSpecimenorderItem.getSpecimen().getSpecimenType());
		requestDetailsBean.setDescription(existingSpecimenorderItem.getDescription());
		requestDetailsBean.setOrderItemId(existingSpecimenorderItem.getId().toString());
		//	  Assigned Quantity
		if (existingSpecimenorderItem.getDistributedItem() != null)
		{
			requestDetailsBean.setAssignedQty(existingSpecimenorderItem.getDistributedItem().getQuantity().toString());
		}

		return requestDetailsBean;
	}

	/**
	 * This function populates RequestDetailsBean object by fethcing data from DerivedSpecimenOrderItem domain object
	 * @param requestDetailsBean RequestDetailsBean object
	 * @param derivedSpecimenorderItem DerivedSpecimenOrderItem object
	 * @param request HttpServletRequest object
	 * @param finalSpecimenListId primitive integer value
	 * @return RequestDetailsBean object
	 */
	private RequestDetailsBean populateRequestDetailsBeanForDerivedSpecimen(RequestDetailsBean requestDetailsBean,
			DerivedSpecimenOrderItem derivedSpecimenorderItem, HttpServletRequest request, int finalSpecimenListId)
	{
		requestDetailsBean.setRequestedItem(derivedSpecimenorderItem.getParentSpecimen().getLabel());
		Long specimenId = derivedSpecimenorderItem.getParentSpecimen().getId();
		Collection childrenSpecimenList = OrderingSystemUtil.getAllChildrenSpecimen(derivedSpecimenorderItem.getParentSpecimen(),
				derivedSpecimenorderItem.getParentSpecimen().getChildrenSpecimen());
		List finalChildrenSpecimenList = OrderingSystemUtil.getChildrenSpecimenForClassAndType(childrenSpecimenList, derivedSpecimenorderItem
				.getSpecimenClass(), derivedSpecimenorderItem.getSpecimenType());
		//	  removing final specimen List from session
		request.getSession().removeAttribute("finalSpecimenList" + finalSpecimenListId);
		//To display the available quantity of the selected specimen from RequestFor dropdown.
		//request.getSession().setAttribute("finalSpecimenList"+finalSpecimenListId, finalChildrenSpecimenList);

		List childrenSpecimenListToDisplay = OrderingSystemUtil.getNameValueBeanList(finalChildrenSpecimenList);

		//setting requestFor list in request
		//request.setAttribute(Constants.REQUEST_FOR_LIST, childrenSpecimenListToDisplay);

		requestDetailsBean.setSpecimenList(childrenSpecimenListToDisplay);
		requestDetailsBean.setSpecimenId(specimenId.toString());
		requestDetailsBean.setInstanceOf("Derived");
		requestDetailsBean.setRequestedQty(derivedSpecimenorderItem.getRequestedQuantity().toString());
		//Displaying the quantity of the first specimen in the request for drop down.
		if (childrenSpecimenListToDisplay.size() != 0)
		{
			requestDetailsBean.setAvailableQty(((Specimen) finalChildrenSpecimenList.get(0)).getAvailableQuantity().toString());
		}
		else
		{
			requestDetailsBean.setAvailableQty("NA");//derivedSpecimenorderItem.getSpecimen().getAvailableQuantity().getValue().toString()	  		
		}
		//Assigned Quantity
		if (derivedSpecimenorderItem.getDistributedItem() != null)
		{
			requestDetailsBean.setAssignedQty(derivedSpecimenorderItem.getDistributedItem().getQuantity().toString());
		}
		requestDetailsBean.setAssignedStatus(derivedSpecimenorderItem.getStatus());
		requestDetailsBean.setClassName(derivedSpecimenorderItem.getSpecimenClass());
		requestDetailsBean.setType(derivedSpecimenorderItem.getSpecimenType());
		requestDetailsBean.setDescription(derivedSpecimenorderItem.getDescription());
		requestDetailsBean.setOrderItemId(derivedSpecimenorderItem.getId().toString());

		return requestDetailsBean;
	}

	/**
	 * This function populates RequestDetailsBean instances by fetching data from PathologicalCaseOrderItem domain instance
	 * @param requestDetailsBean RequestDetailsBean object
	 * @param pathologicalCaseOrderItem DerivedSpecimenOrderItem object
	 * @param request HttpServletRequest object
	 * @param finalSpecimenListId primitive integer value
	 * @return requestDetailsBean RequestDetailsBean object
	 */
	private RequestDetailsBean populateRequestDetailsBeanForPathologicalCase(RequestDetailsBean requestDetailsBean,
			PathologicalCaseOrderItem pathologicalCaseOrderItem, HttpServletRequest request, int finalSpecimenListId)
	{
		boolean isDerived = false;
		requestDetailsBean.setRequestedItem(pathologicalCaseOrderItem.getSpecimenCollectionGroup().getSurgicalPathologyNumber());

		Collection childrenSpecimenList = pathologicalCaseOrderItem.getSpecimenCollectionGroup().getSpecimenCollection();
		// Removing distributed option if no specimens are present in that SCG. ie. childrenSpecimenList.size() == 0
		//TODO find better option for this
		if (childrenSpecimenList.size() == 0)
		{
			isDerived = true;
		}
		Iterator childrenSpecimenListIterator = childrenSpecimenList.iterator();
		List totalChildrenSpecimenColl = new ArrayList();
		List childrenSpecimenListToDisplay = new ArrayList();
		while (childrenSpecimenListIterator.hasNext())
		{
			Specimen specimen = (Specimen) childrenSpecimenListIterator.next();
			List childSpecimenCollection = OrderingSystemUtil.getAllChildrenSpecimen(specimen, specimen.getChildrenSpecimen());
			List finalChildrenSpecimenCollection = null;
			if (pathologicalCaseOrderItem.getSpecimenClass() != null && pathologicalCaseOrderItem.getSpecimenType() != null
					&& !pathologicalCaseOrderItem.getSpecimenClass().trim().equalsIgnoreCase("")
					&& !pathologicalCaseOrderItem.getSpecimenType().trim().equalsIgnoreCase(""))
			{
				//"Derived"	   
				finalChildrenSpecimenCollection = OrderingSystemUtil.getChildrenSpecimenForClassAndType(childSpecimenCollection,
						pathologicalCaseOrderItem.getSpecimenClass(), pathologicalCaseOrderItem.getSpecimenType());
				isDerived = true;
			}
			else
			{
				//"Block" . Specimen class = "Tissue" , Specimen Type = "Block".
				finalChildrenSpecimenCollection = OrderingSystemUtil.getChildrenSpecimenForClassAndType(childSpecimenCollection, "Tissue", "Block");
			}
			if (finalChildrenSpecimenCollection != null)
			{
				Iterator finalChildrenSpecimenCollectionIterator = finalChildrenSpecimenCollection.iterator();
				while (finalChildrenSpecimenCollectionIterator.hasNext())
				{
					totalChildrenSpecimenColl.add((Specimen) (finalChildrenSpecimenCollectionIterator.next()));
				}
			}
		}
		//	    	removing final specimen List from session
		request.getSession().removeAttribute("finalSpecimenList" + finalSpecimenListId);
		//To display the available quantity of the selected specimen from RequestFor dropdown.
		//request.getSession().setAttribute("finalSpecimenList"+finalSpecimenListId, totalChildrenSpecimenColl);

		childrenSpecimenListToDisplay = OrderingSystemUtil.getNameValueBeanList(totalChildrenSpecimenColl);
		requestDetailsBean.setSpecimenList(childrenSpecimenListToDisplay);

		requestDetailsBean.setSpecimenCollGroupId(pathologicalCaseOrderItem.getSpecimenCollectionGroup().getId().toString());
		if (isDerived)
		{
			requestDetailsBean.setInstanceOf("DerivedPathological");
		}
		else
		{
			requestDetailsBean.setInstanceOf("Pathological");
		}
		requestDetailsBean.setRequestedQty(pathologicalCaseOrderItem.getRequestedQuantity().toString());
		//Displaying the quantity of the first specimen in the request for drop down.
		if (childrenSpecimenListToDisplay.size() != 0)
		{
			requestDetailsBean.setAvailableQty(((Specimen) totalChildrenSpecimenColl.get(0)).getAvailableQuantity().toString());
		}
		else
		{
			requestDetailsBean.setAvailableQty("-");
		}
		//	  Assigned Quantity
		if (pathologicalCaseOrderItem.getDistributedItem() != null)
		{
			requestDetailsBean.setAssignedQty(pathologicalCaseOrderItem.getDistributedItem().getQuantity().toString());
		}
		requestDetailsBean.setAssignedStatus(pathologicalCaseOrderItem.getStatus());
		requestDetailsBean.setClassName(pathologicalCaseOrderItem.getSpecimenClass());
		requestDetailsBean.setType(pathologicalCaseOrderItem.getSpecimenType());
		requestDetailsBean.setDescription(pathologicalCaseOrderItem.getDescription());
		requestDetailsBean.setOrderItemId(pathologicalCaseOrderItem.getId().toString());

		return requestDetailsBean;
	}

	/**
	 * This function populates the items status for individual order items in OrderList,DefinedArray List and Existing Array List
	 * and sets each of the list in the request attribute. 
	 * @param requestList ArrayList containing the list of orderitem objects 
	 * @param request HttpServletRequest object
	 * @param itemsAddedToArray String indicating whether items are to be added to defined array,existing array or to the speicmen orderlist
	 */
	void populateItemStatusList(List requestList, HttpServletRequest request, String itemsAddedToArray)
	{
		Iterator requestlistItr = requestList.iterator();
		while (requestlistItr.hasNext())
		{
			Object orderItemObj = requestlistItr.next();
			//In case of define array
			if (orderItemObj instanceof Map)
			{
				Map defineArrayMap = (HashMap) orderItemObj;
				Set keys = defineArrayMap.keySet();
				Iterator keySetItr = keys.iterator();
				while (keySetItr.hasNext())
				{
					DefinedArrayRequestBean definedArrayRequestBean = (DefinedArrayRequestBean) keySetItr.next();
					//Obtain request list for each defined array 
					List defineArrayDetailsList = (ArrayList) defineArrayMap.get(definedArrayRequestBean);
					Iterator defineArrayDetailsListItr = defineArrayDetailsList.iterator();
					while (defineArrayDetailsListItr.hasNext())
					{
						DefinedArrayDetailsBean definedArrayDetailsBean = (DefinedArrayDetailsBean) defineArrayDetailsListItr.next();
						//Get possible next statuses for a given status
						List possibleStatusList = (List) request.getAttribute(Constants.ITEM_STATUS_LIST_FOR_ITEMS_IN_ARRAY);
						//Set possibleStatusList in the DefinedArrayDetails bean instance.
						definedArrayDetailsBean.setItemStatusList(possibleStatusList);
					} //End while (while loop to iterate definedArrayList)
				}//End while (While loop to iterate list containing map objects for deifned array)				
			}//End if(orderItemObj instanceof Map)
			else
			{ //In case of Existing Array
				if (orderItemObj instanceof ExistingArrayDetailsBean)
				{
					ExistingArrayDetailsBean existingArrayDetailsBean = (ExistingArrayDetailsBean) orderItemObj;
					List possibleStatusList = OrderingSystemUtil.getPossibleStatusList(Constants.ORDER_REQUEST_STATUS_NEW);
					existingArrayDetailsBean.setItemStatusList(possibleStatusList);
				}
				//In case of Request Details List displayed in RequestDetails.jsp
				if (orderItemObj instanceof RequestDetailsBean)
				{
					RequestDetailsBean requestDetailsBean = (RequestDetailsBean) orderItemObj;
					List possibleStatusList = null;
					possibleStatusList = OrderingSystemUtil.getPossibleStatusList(Constants.ORDER_REQUEST_STATUS_NEW);
					requestDetailsBean.setItemsStatusList(possibleStatusList);
				}
			}//End else			
		}//End OuterMost while		
		/*Set the requestList in request Attribute*/
		if (itemsAddedToArray.equals("addToDefinedArrays"))
		{ //Set the request attribute ARRAY_REQUESTSMAP_LIST when items are to be displayed in the DefinedArrays List(ArrayRequests.jsp)
			request.setAttribute(Constants.DEFINEDARRAY_REQUESTS_LIST, requestList);
		}
		if (itemsAddedToArray.equals("addToExistingArrays"))
		{ //Set the request attribute ARRAY_REQUESTSMAP_LIST when items are to be displayed in the Existing Arrays List(ArrayRequests.jsp)
			request.setAttribute(Constants.EXISISTINGARRAY_REQUESTS_LIST, requestList);
		}
		if (itemsAddedToArray.equals("addToOrderList"))
		{ //Set the request attribute REQUEST_DETAILS_LIST when items are to be displayed in the RequestDetails.jsp
			request.setAttribute(Constants.REQUEST_DETAILS_LIST, requestList);
		}
	}
	
	
	private void getChildrenSpecimen(Specimen specimen) throws DAOException
	{
		OrderBizLogic orderBizLogic = (OrderBizLogic) BizLogicFactory.getInstance().getBizLogic(Constants.REQUEST_LIST_FILTERATION_FORM_ID);
		if (specimen != null)
		{
			Collection childrenSpecimenCollection = (Collection) orderBizLogic.retrieveAttribute(Specimen.class.getName(), specimen.getId(),
					"elements(childrenSpecimen)");
			if (childrenSpecimenCollection != null)
			{
				specimen.setChildrenSpecimen(childrenSpecimenCollection);
				Iterator iterator = childrenSpecimenCollection.iterator();
				while (iterator.hasNext())
				{
					Specimen childSpecimen = (Specimen) iterator.next();
					getChildrenSpecimen(childSpecimen);
				}
			}
		}
	}
	


	
	
}
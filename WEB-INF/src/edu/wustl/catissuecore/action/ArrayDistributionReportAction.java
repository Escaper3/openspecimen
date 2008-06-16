
package edu.wustl.catissuecore.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.wustl.catissuecore.actionForm.ConfigureResultViewForm;
import edu.wustl.catissuecore.actionForm.DistributionReportForm;
import edu.wustl.catissuecore.bizlogic.BizLogicFactory;
import edu.wustl.catissuecore.domain.DistributedItem;
import edu.wustl.catissuecore.domain.Distribution;
import edu.wustl.catissuecore.domain.Specimen;
import edu.wustl.catissuecore.domain.SpecimenArray;
import edu.wustl.catissuecore.domain.SpecimenArrayContent;
import edu.wustl.catissuecore.domain.SpecimenArrayType;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.catissuecore.util.global.Utility;
import edu.wustl.catissuecore.vo.ArrayDistributionReportEntry;
import edu.wustl.common.beans.SessionDataBean;
import edu.wustl.common.bizlogic.DefaultBizLogic;
import edu.wustl.common.bizlogic.IBizLogic;
import edu.wustl.common.dao.AbstractDAO;
import edu.wustl.common.dao.DAOFactory;
import edu.wustl.common.util.dbManager.DAOException;
import edu.wustl.common.util.logger.Logger;

/**
 * This is the action class for displaying the Distribution report
 * @author Rahul Ner
 *  
 */

public class ArrayDistributionReportAction extends BaseDistributionReportAction
{
	/**
	 * @param mapping object of ActionMapping
	 * @param form object of ActionForm
	 * @param request object of HttpServletRequest
	 * @param response object of HttpServletResponse
	 * @throws Exception generic exception
	 */
	protected ActionForward executeAction(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		ConfigureResultViewForm configForm = (ConfigureResultViewForm) form;

		//Retrieve the distribution ID which is set in CommonAddEdit Action 
		Long distributionId = (Long) request.getAttribute(Constants.DISTRIBUTION_ID);

		//retrieve from configuration form if it is null
		if (distributionId == null)
			distributionId = configForm.getDistributionId();

		//retireve the distribution id from forward to hasmap
		Map forwardToHashMap = (Map) request.getAttribute("forwardToHashMap");
		if(forwardToHashMap != null && forwardToHashMap.get("distributionId") != null)
		{
			distributionId = (Long) forwardToHashMap.get("distributionId");
		}
		/*Retrieve from request attribute if it null. 
		 */
		if (distributionId == null)
		{
			distributionId = (Long) request.getAttribute(Constants.SYSTEM_IDENTIFIER);
		}

		/*Retrieve from request parameter if it null. This request parameter is set in Distribution page incase the Report button 
		 *is clicked from Distribution Edit page
		 */
		if (distributionId == null)
		{
			distributionId = new Long(request.getParameter(Constants.SYSTEM_IDENTIFIER));
		}

		//Set it in configuration form if it is not null 
		if (distributionId != null)
			configForm.setDistributionId(distributionId);

		Distribution dist = getDistribution(distributionId, getSessionData(request), Constants.CLASS_LEVEL_SECURE_RETRIEVE);

		//Retrieve the distributed items data
		DistributionReportForm distributionReportForm = getDistributionReportForm(dist);
		distributionReportForm.setDistributionType(new Integer(Constants.SPECIMEN_ARRAY_DISTRIBUTION_TYPE));
		SessionDataBean sessionData = getSessionData(request);
		String action = configForm.getNextAction();

		/*    	//Set the columns for Distribution report
		 String action = configForm.getNextAction();
		 String selectedColumns[] = getSelectedColumns(action,configForm,true);
		 String []columnNames = getColumnNames(selectedColumns);
		 */

		String selectedColumns[] = getSelectedColumns(action, configForm, true);
		String[] columnNames = getColumnNames(selectedColumns);

		String[] specimenColumns = Constants.SPECIMEN_IN_ARRAY_SELECTED_COLUMNS;
		String[] specimenColumnNames = getColumnNames(specimenColumns);

		//List listOfData = getListOfArrayData(dist, configForm, sessionData, selectedColumns, specimenColumns);
		List listOfData = getListOfArray(dist);

		//Set the request attributes for the Distribution report data
		request.setAttribute(Constants.DISTRIBUTION_REPORT_FORM, distributionReportForm);
		request.setAttribute(Constants.COLUMN_NAMES_LIST, columnNames);
		request.setAttribute(Constants.DISTRIBUTED_ITEMS_DATA, listOfData);
		request.setAttribute(Constants.SPECIMEN_COLUMN_NAMES_LIST, specimenColumnNames);

		setSelectedMenuRequestAttribute(request);
		return (mapping.findForward("Success"));
	}

	/**
	 * @param dist
	 * @param configForm
	 * @param sessionData
	 * @return
	 */
	protected List getListOfArrayData(Distribution dist, ConfigureResultViewForm configForm, SessionDataBean sessionData)
	{
		//Get the list of data for Distributed items data for the report.
		List listOfData = new ArrayList();
		Collection specimenArrayCollection = dist.getSpecimenArrayCollection();
		//Specimen Ids which are getting distributed.
		String[] specimenArrayIds = new String[specimenArrayCollection.size()];
		int i = 0;
		Iterator itr = specimenArrayCollection.iterator();

		while (itr.hasNext())
		{
			SpecimenArray array = (SpecimenArray) itr.next();
			List tempList = new ArrayList();
			tempList.add(array.getId().toString());
			tempList.add(Utility.toString(array.getBarcode()));
			i++;
			List tempList1 = new ArrayList();
			tempList1.add(tempList);
			listOfData.add(tempList1);
		}
		return listOfData;
	}

	/**
	 * @param array
	 * @param specimenColumns
	 * @param sessionData
	 * @param arrayEntry
	 * @param fillerList
	 * @throws DAOException 
	 */
	private void getSpecimenDetails(SpecimenArray array, String[] specimenColumns, SessionDataBean sessionData,
			ArrayDistributionReportEntry arrayEntry) throws DAOException
	{
		List specimensDetails = new ArrayList();
		List gridInfo = new ArrayList();
		int dimensionOne = array.getCapacity().getOneDimensionCapacity().intValue();
		int dimensionTwo = array.getCapacity().getTwoDimensionCapacity().intValue();

		for (int i = 0; i < dimensionOne; i++)
		{
			List temp = new ArrayList(dimensionTwo);

			for (int j = 0; j < dimensionTwo; j++)
			{
				temp.add(Constants.UNUSED);
			}

			gridInfo.add(i, temp);
		}
		/**
		 * Name : Virender
		 * Reviewer: Prafull
		 * Retriving collection of Specimen Type.
		 * Replaced array.getSpecimenArrayContentCollection().iterator();
		 */
		DefaultBizLogic bizLogic = (DefaultBizLogic) BizLogicFactory.getInstance().getBizLogic(Constants.DEFAULT_BIZ_LOGIC);
		Collection specimenArrayContentCollection = (Collection) bizLogic.retrieveAttribute(SpecimenArray.class.getName(), array.getId(),
				"elements(specimenArrayContentCollection)");
		Iterator itr = specimenArrayContentCollection.iterator();

		while (itr.hasNext())
		{
			SpecimenArrayContent arrayContent = (SpecimenArrayContent) itr.next();
			/**
			 * Name : Virender
			 * Reviewer: Prafull
			 * Retriving specimenObject
			 * replaced arrayContent.getSpecimen()
			 */
			Specimen specimen = null;
			specimen = (Specimen) bizLogic.retrieveAttribute(SpecimenArrayContent.class.getName(), arrayContent.getId(), "specimen");
			List specimenDetails = new ArrayList();

			if (arrayContent.getPositionDimensionOne() != null && arrayContent.getPositionDimensionTwo() != null)
			{
				int postionOneInArray = arrayContent.getPositionDimensionOne().intValue();
				int postionTwoInArray = arrayContent.getPositionDimensionTwo().intValue();
				if (postionOneInArray > 0)
				{
					((List) gridInfo.get(postionOneInArray - 1)).set(postionTwoInArray - 1, specimen.getLabel());
				}
			}

			specimenDetails.add(specimen.getLabel());
			specimenDetails.add(Utility.toString(specimen.getBarcode()));
			specimenDetails.add(Utility.toString(arrayContent.getPositionDimensionOne()));
			specimenDetails.add(Utility.toString(arrayContent.getPositionDimensionTwo()));
			specimenDetails.add(specimen.getClassName());
			specimenDetails.add(specimen.getType());
			specimenDetails.add(specimen.getSpecimenCharacteristics().getTissueSide());
			specimenDetails.add(specimen.getSpecimenCharacteristics().getTissueSite());

			specimensDetails.add(specimenDetails);
		}
		arrayEntry.setGridInfo(gridInfo);
		arrayEntry.setSpecimenEntries(specimensDetails);
	}

	/**
	 * @param array
	 * @param selectedColumns
	 * @param sessionData
	 * @return
	 * @throws Exception
	 */
	private List getArrayDetails(SpecimenArray array, String[] selectedColumns, SessionDataBean sessionData) throws Exception
	{
		List arrayDetails = new ArrayList();
		arrayDetails.add(array.getName());
		arrayDetails.add(Utility.toString(array.getBarcode()));
		/**
		 * Name : Virender
		 * Reviewer: Prafull
		 * Retriving collection of Distributed Items.
		 * array.getSpecimenArrayType().getname()
		 * array.getSpecimenArrayType().getSpecimenClass()
		 * array.getSpecimenArrayType().getSpecimenTypeCollection()
		 */
		IBizLogic bizLogic = BizLogicFactory.getInstance().getBizLogic(Constants.SPECIMEN_ARRAY_TYPE_FORM_ID);
		SpecimenArrayType specimenArrayType = (SpecimenArrayType) bizLogic.retrieveAttribute(SpecimenArray.class.getName(), array.getId(),
				"specimenArrayType");
		arrayDetails.add(Utility.toString(specimenArrayType.getName()));
		if(array != null && array.getLocatedAtPosition() != null)
		{
			arrayDetails.add(Utility.toString(array.getLocatedAtPosition().getPositionDimensionOne()));
			arrayDetails.add(Utility.toString(array.getLocatedAtPosition().getPositionDimensionTwo()));
		}
		arrayDetails.add(Utility.toString(array.getCapacity().getOneDimensionCapacity()));
		arrayDetails.add(Utility.toString(array.getCapacity().getTwoDimensionCapacity()));
		arrayDetails.add(Utility.toString(specimenArrayType.getSpecimenClass()));
		/**
		 * Name : Virender
		 * Reviewer: Prafull
		 * Retriving collection of Specimen Type.
		 * specimenArrayType.getSpecimenTypeCollection();
		 */
		Collection specimenTypeCollection = (Collection) bizLogic.retrieveAttribute(SpecimenArrayType.class.getName(), specimenArrayType.getId(),
				"elements(specimenTypeCollection)");
		arrayDetails.add(Utility.toString(specimenTypeCollection));
		arrayDetails.add(Utility.toString(array.getComment()));
		return arrayDetails;
	}

	/**
	 * @param dist
	 * @param configForm
	 * @param sessionData
	 * @param arrayColumns
	 * @param specimenColumns
	 * @return
	 * @throws Exception
	 */
	protected List getListOfArrayDataForSave(Distribution dist, ConfigureResultViewForm configForm, SessionDataBean sessionData,
			String[] arrayColumns, String[] specimenColumns) throws Exception
	{
		List arrayEntries = new ArrayList();
		Iterator itr = getSpecimenArrayCollection(dist).iterator();
		while (itr.hasNext())
		{
			SpecimenArray array = (SpecimenArray) itr.next();
			ArrayDistributionReportEntry arrayEntry = new ArrayDistributionReportEntry();
			arrayEntry.setArrayInfo(getArrayDetails(array, arrayColumns, sessionData));
			getSpecimenDetails(array, specimenColumns, sessionData, arrayEntry);

			arrayEntries.add(arrayEntry);
		}
		return arrayEntries;
	}

	/**
	 * @param dist
	 * @param configForm
	 * @param sessionData
	 * @param arrayColumns
	 * @param specimenColumns
	 * @return
	 * @throws Exception
	 */
	protected List getListOfArrayData(Distribution dist, ConfigureResultViewForm configForm, SessionDataBean sessionData, String[] arrayColumns,
			String[] specimenColumns) throws Exception
	{
		List arrayEntries = new ArrayList();
		Iterator itr = getSpecimenArrayCollection(dist).iterator();
		while (itr.hasNext())
		{
			SpecimenArray array = (SpecimenArray) itr.next();
			List arrayEntry = new ArrayList();
			arrayEntry.add(getArrayDetails(array, arrayColumns, sessionData));
					
			arrayEntries.add(arrayEntry);
		}
		return arrayEntries;
	}
	
	protected List setSpecimenArrayDetails(Distribution distribution)
	{
		List arrayDetails = new ArrayList();
		distribution.getDistributedItemCollection();
		Iterator itr = distribution.getDistributedItemCollection().iterator();
		while (itr.hasNext())
		{
			DistributedItem distributedItem = (DistributedItem) itr.next();
			SpecimenArray specimenArray = (SpecimenArray)distributedItem.getSpecimenArray();
			arrayDetails.add(specimenArray.getName());
			arrayDetails.add(Utility.toString(specimenArray.getBarcode()));
			arrayDetails.add(Utility.toString(specimenArray.getSpecimenArrayType().getName()));
			if(specimenArray != null && specimenArray.getLocatedAtPosition() != null)
			{
				arrayDetails.add(Utility.toString(specimenArray.getLocatedAtPosition().getPositionDimensionOne()));
				arrayDetails.add(Utility.toString(specimenArray.getLocatedAtPosition().getPositionDimensionTwo()));
			}
			arrayDetails.add(Utility.toString(specimenArray.getCapacity().getOneDimensionCapacity()));
			arrayDetails.add(Utility.toString(specimenArray.getCapacity().getTwoDimensionCapacity()));
			arrayDetails.add(Utility.toString(specimenArray.getSpecimenArrayType().getSpecimenClass()));
			arrayDetails.add(Utility.toString(specimenArray.getSpecimenArrayType().getSpecimenTypeCollection()));
			arrayDetails.add(Utility.toString(specimenArray.getComment()));
		}
		
		return arrayDetails;
	}
	
	protected List getListOfArray(Distribution dist) throws Exception
	{
		List arrayEntries = new ArrayList();
		long startTime = System.currentTimeMillis();
		AbstractDAO dao = DAOFactory.getInstance().getDAO(Constants.HIBERNATE_DAO);
				
		try
    	{
			dao.openSession(null);
			
			Object object = dao.retrieve(Distribution.class.getName(), dist.getId());
			Distribution distribution = (Distribution)object;
			arrayEntries.add(setSpecimenArrayDetails(distribution));
			
			long endTime = System.currentTimeMillis();
			System.out.println("Execute time of getRequestDetailsList :" + (endTime-startTime));
			return arrayEntries;
			
    	}
    	catch(DAOException e)
    	{
    		Logger.out.error(e.getMessage(), e);
    		return null;	
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
				return null;
			}
		}	
		
	}

	
	
	
	
	

	/**
	 * Name: Virender Mehta
	 * Reviewer: Prafull
	 * Retrive SpecimenArrayCollection from parent Specimen
	 * Replaced Iterator itr = dist.getSpecimenArrayCollection().iterator();
	 * @throws DAOException 
	 */
	private Collection getSpecimenArrayCollection(Distribution dist) throws DAOException
	{
		Collection specimenArrayCollection = new HashSet();
		IBizLogic bizLogic = BizLogicFactory.getInstance().getBizLogic(Constants.DISTRIBUTION_FORM_ID);
		Collection distributedItemCollection = (Collection) bizLogic.retrieveAttribute(Distribution.class.getName(), dist.getId(),
				"elements(distributedItemCollection)");
		Iterator itr = distributedItemCollection.iterator();
		while (itr.hasNext())
		{
			DistributedItem distributedItem = (DistributedItem) itr.next();
			String[] selectColumnName = {"specimenArray"};
			String[] whereColumnName = {Constants.SYSTEM_IDENTIFIER};
			Object[] whereColumnValue = {distributedItem.getId()};
			String[] whereColumnCond = {"="};
			List list = bizLogic.retrieve(DistributedItem.class.getName(), selectColumnName, whereColumnName, whereColumnCond, whereColumnValue,
					Constants.AND_JOIN_CONDITION);
			if (list != null && list.size() > 0)
			{
				Iterator listItr = list.iterator();
				while (listItr.hasNext())
				{
					SpecimenArray array = (SpecimenArray) listItr.next();
					if (array != null)
						specimenArrayCollection.add(array);
				}
			}
		}

		return specimenArrayCollection;
	}
}
package edu.wustl.catissuecore.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.wustl.catissuecore.bizlogic.AdvanceQueryBizlogic;
import edu.wustl.catissuecore.bizlogic.StorageContainerBizLogic;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.action.BaseAction;
import edu.wustl.common.beans.SessionDataBean;
import edu.wustl.common.bizlogic.CDEBizLogic;
import edu.wustl.common.tree.StorageContainerTreeNode;
import edu.wustl.common.tree.TreeDataInterface;
import edu.wustl.common.util.logger.Logger;

/**
 * This action is for getting the tree-vector for storage container; to be given to DHTMLX tree 
 * @author pallavi_mistry
 *
 */
public class TreeNodeDataAction extends BaseAction
{
	Vector finalDataListVector=null;
    public ActionForward executeAction(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception
    {
    
    	Map columnIdsMap = new HashMap();
        String pageOf  = request.getParameter(Constants.PAGEOF);
        Logger.out.debug("pageOf in treeview........"+pageOf);
        request.setAttribute(Constants.PAGEOF,pageOf);
        String reload=null;
       
        if (pageOf.equals(Constants.PAGEOF_STORAGE_LOCATION))
        {
        	String storageContainerType = request.getParameter(Constants.STORAGE_CONTAINER_TYPE);
        	request.setAttribute(Constants.STORAGE_CONTAINER_TYPE,storageContainerType);
        	String storageContainerID = request.getParameter(Constants.STORAGE_CONTAINER_TO_BE_SELECTED);
        	request.setAttribute(Constants.STORAGE_CONTAINER_TO_BE_SELECTED,storageContainerID);
        	String position = request.getParameter(Constants.STORAGE_CONTAINER_POSITION);
        	request.setAttribute(Constants.STORAGE_CONTAINER_POSITION,position); 	
        }
        else if (pageOf.equals(Constants.PAGEOF_TISSUE_SITE))
        {	HttpSession session = request.getSession();
            String cdeName = (String)session.getAttribute(Constants.CDE_NAME);
            session.removeAttribute(Constants.CDE_NAME);
            request.setAttribute(Constants.CDE_NAME, cdeName);
        }
       
        try
        {	
        	reload =request.getParameter(Constants.RELOAD);
         	if(reload!=null && reload.equals("true"))
         	{
         		String treeNodeIDToBeReloaded=request.getParameter(Constants.TREE_NODE_ID);
         		request.setAttribute(Constants.TREE_NODE_ID, treeNodeIDToBeReloaded);
         		request.setAttribute(Constants.RELOAD,reload);
         	}
            TreeDataInterface bizLogic = new StorageContainerBizLogic();
            Vector dataList =  new Vector();
            List disableSpecimenIdsList = new ArrayList();
            if (pageOf.equals(Constants.PAGEOF_TISSUE_SITE))
            {
            	bizLogic = new CDEBizLogic();
            	CDEBizLogic cdeBizLogic = (CDEBizLogic) bizLogic;
            	String cdeName = request.getParameter(Constants.CDE_NAME);
            	dataList= cdeBizLogic.getTreeViewData(cdeName);
            }
            else if (pageOf.equals(Constants.PAGEOF_QUERY_RESULTS))
            {
            	bizLogic = new AdvanceQueryBizlogic();
                HttpSession session = request.getSession();
                columnIdsMap = (Map)session.getAttribute(Constants.COLUMN_ID_MAP);
                SessionDataBean sessionData = getSessionData(request);
            	dataList = bizLogic.getTreeViewData(sessionData,columnIdsMap, disableSpecimenIdsList);
            }
					            else
            if (pageOf.equals(Constants.PAGEOF_STORAGE_LOCATION) || pageOf.equals(Constants.PAGEOF_MULTIPLE_SPECIMEN) || pageOf.equals(Constants.PAGEOF_SPECIMEN) ||
            		pageOf.equals(Constants.PAGEOF_ALIQUOT))
            {
            	StorageContainerBizLogic scBizLogic = new StorageContainerBizLogic();
            	dataList = scBizLogic.getSiteWithDummyContainer();
            }
            
            if(dataList!=null)
            {
            	finalDataListVector=new Vector();
            }
            createTreeNodeVector(dataList,finalDataListVector);
            request.setAttribute(Constants.TREE_DATA, finalDataListVector);
           
        }
        catch (Exception exp)
        {
        	Logger.out.error(exp.getMessage(), exp);
        }
        return mapping.findForward(Constants.SUCCESS);
    }
    
   

/**
 * This is a recursive method to make the final-vector for DHTML tree of storage containers
 * @param datalist
 * @param finalDataListVector
 */
    void createTreeNodeVector(Vector datalist,Vector finalDataListVector)
    {
    	if(datalist!=null && datalist.size() != 0)
		{ 
    		Iterator itr=datalist.iterator();
			while(itr.hasNext())
			{
				StorageContainerTreeNode node=(StorageContainerTreeNode)itr.next();
    			boolean contains=finalDataListVector.contains(node.getValue());
    			if(contains==false)
    			{
    				finalDataListVector.add(node);
    			}
    			Vector childNodeVector=node.getChildNodes();
    			createTreeNodeVector(childNodeVector,finalDataListVector);
			}
			return;
		}
    	else
    	{
    		return;
    	}
	}
    
    
}
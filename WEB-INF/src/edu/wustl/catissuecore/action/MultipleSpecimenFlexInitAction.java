package edu.wustl.catissuecore.action;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.wustl.catissuecore.util.global.Constants;

//import edu.wustl.common.action.SecureAction;

public class MultipleSpecimenFlexInitAction extends Action
{
	public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        //Gets the value of the operation parameter.
        String operation = request.getParameter(Constants.OPERATION);
        
		String mode = Constants.ADD;
        if(operation != null && operation.equals(Constants.EDIT))
        	mode = Constants.EDIT;
		
		String showParentSelection = "false";
		String showLabel = "true";
		String showBarcode = "true";
		
		String parentType = request.getParameter("parentType");
		if(parentType == null)
		{
			parentType = Constants.NEW_SPECIMEN_TYPE;
			showParentSelection = "true";
			
		}
		String numberOfSpecimens = getNumberOfSpecimens(request);
		String parentName = getParentName(request, parentType);
		
		if(edu.wustl.catissuecore.util.global.Variables.isSpecimenLabelGeneratorAvl )
		{
			showLabel = "false";
		}
		if(edu.wustl.catissuecore.util.global.Variables.isSpecimenBarcodeGeneratorAvl )
		{
			showBarcode = "false";
		}
		setMSPRequestParame(request, mode, parentType, parentName, numberOfSpecimens,showParentSelection,showLabel,showBarcode);
		
		String pageOf = (String) request.getParameter("pageOf");
		if(pageOf!=null)
		{
			request.setAttribute(Constants.PAGEOF,pageOf);
			return mapping.findForward(pageOf);
		}
        return mapping.findForward("success");
    }
	
	private void setMSPRequestParame(HttpServletRequest request, String mode, String parentType, String parentName, String numberOfSpecimens,String showParentSelection,String showLabel,String showBarcode)
	{
		
        request.setAttribute("MODE",mode);
        request.setAttribute("PARENT_TYPE", parentType);
        request.setAttribute("PARENT_NAME", parentName);
        request.setAttribute("SP_COUNT",numberOfSpecimens);
        request.setAttribute("SHOW_PARENT_SELECTION",showParentSelection);
        request.setAttribute("SHOW_LABEL",showLabel);
        request.setAttribute("SHOW_BARCODE",showBarcode);
	}
	
	private String getParentName(HttpServletRequest request, String parentType)
	{
		if(Constants.NEW_SPECIMEN_TYPE.equals(parentType))
		{
			String specimenCollectionGroupName = "";
			HashMap forwardToHashMap = (HashMap) request.getAttribute("forwardToHashMap");
			if(forwardToHashMap!=null)
			{
				Object obj = forwardToHashMap.get("specimenCollectionGroupName");
				if(obj!=null)
				{
					specimenCollectionGroupName = (String)obj;
				}
			}
			return specimenCollectionGroupName;
		}
		else if(Constants.DERIVED_SPECIMEN_TYPE.equals(parentType))
		{
			//TODO
			return ""; 
		}
		return "";	
	}
	
	private String getNumberOfSpecimens(HttpServletRequest request)
	{
		String numberOfSpecimens = request.getParameter(Constants.NUMBER_OF_SPECIMENS);
		System.out.println("numberOfSpecimens "+numberOfSpecimens);
		if( numberOfSpecimens==null || numberOfSpecimens.equals(""))
		{
			numberOfSpecimens = "1";
		}
		return numberOfSpecimens;
	}
}
package edu.wustl.catissuecore.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.action.BaseAction;
/**
 * Forward to collection protocol main page 
 * @author pathik_sheth
 *
 */
public class OpenCollectionProtocolAction extends BaseAction{

	@Override
	/**
	 *Set  add/edit operation is being performed    
	 */
	protected ActionForward executeAction(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		String operation = (String)request.getParameter(Constants.OPERATION);
		String pageOf = (String)request.getParameter(Constants.PAGEOF);
		HttpSession session = request.getSession();
		if("pageOfmainCP".equalsIgnoreCase(pageOf))
		{
			session.removeAttribute(Constants.COLLECTION_PROTOCOL_SESSION_BEAN);
			session.removeAttribute(Constants.ROW_ID_OBJECT_BEAN_MAP);
		}

		request.setAttribute(Constants.OPERATION, operation);
		return mapping.findForward(Constants.SUCCESS);
	}

}

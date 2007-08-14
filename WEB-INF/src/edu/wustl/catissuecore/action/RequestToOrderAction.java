/*
 * Copyright: Copyright (c) year
 * Company: Washington University, School of Medicine, St. Louis.
 */

package edu.wustl.catissuecore.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.wustl.catissuecore.bizlogic.BizLogicFactory;
import edu.wustl.catissuecore.bizlogic.UserBizLogic;
import edu.wustl.catissuecore.domain.DistributionProtocol;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.action.BaseAction;
import edu.wustl.common.beans.SessionDataBean;
import edu.wustl.common.bizlogic.IBizLogic;
import edu.wustl.common.security.SecurityManager;
import edu.wustl.common.util.dbManager.DAOException;
import flex.messaging.io.ArrayList;
import gov.nih.nci.security.authorization.domainobjects.Role;

/**
 * <p>Title: RequestToOrderAction Class>
 * <p>Description:	This class initializes the fields of jsp page to request the bio-specimens.</p>
 * @author deepti_phadnis
 * @version 1.00
 */
public class RequestToOrderAction extends BaseAction
{
	/**
	 * Method to initialize the fields required on the UI pages.
	 * @param mapping Struts's ActionMapping
	 * @param form Referance to associated actionform with action and jsp
	 * @param request Referance to HTTP request 
	 * @param response Referance to HTTP response
	 * @return Returns the path of the mapped JSP/Action to which page will be forwared after sucessful execution of action
	 * @throws Exception throws exception in case of any logical or runtime error.
	 */
	public ActionForward executeAction(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		//Sets the Distribution Protocol Id List.
		SessionDataBean sessionLoginInfo = getSessionData(request);
		Long loggedInUserID = sessionLoginInfo.getUserId();
		long csmUserId = new Long(sessionLoginInfo.getCsmUserId()).longValue();
		Role role = SecurityManager.getInstance(UserBizLogic.class).getUserRole(csmUserId);

		List distributionProtocolList = loadDistributionProtocol(loggedInUserID, role.getName());
		request.setAttribute(Constants.DISTRIBUTIONPROTOCOLLIST, distributionProtocolList);
		return mapping.findForward("requestOrderPage");
	}

	/**
	 * This method loads the title as Name and id as value of distribution protocol from database 
	 * and return the namevalue bean of ditribution protocol for a given PI.
	 * @param piID User id of PI for which all the distribution protocol is to be loaded.
	 * @return Returns the list of namevalue bean of ditribution protocol for a given PI.
	 * @throws DAOException Throws DAOException if any database releated error occures
	 **/
	private List loadDistributionProtocol(final Long piID, String roleName) throws DAOException
	{
		IBizLogic bizLogic = BizLogicFactory.getInstance().getBizLogic(Constants.DEFAULT_BIZ_LOGIC);
		List distributionProtocolList = new ArrayList();

		String sourceObjectName = DistributionProtocol.class.getName();
		String[] displayName = {"title"};
		String valueFieldCol = Constants.ID;

		// checking for the role. if role is admin / supervisor then show all the distribution protocols.
		if (roleName.equals(Constants.ADMINISTRATOR) || roleName.equals(Constants.SUPERVISOR))
		{
			distributionProtocolList = bizLogic.getList(sourceObjectName, displayName, valueFieldCol, true);
		}
		else
		{
			String[] whereColumnName = {"principalInvestigator.id"};
			String[] colCondition = {"="};
			Object[] whereColumnValue = {piID};
			String joinCondition = null;
			String separatorBetweenFields = "";
			boolean isToExcludeDisabled = true;

			//Get data from database
			distributionProtocolList = bizLogic.getList(sourceObjectName, displayName, valueFieldCol, whereColumnName, colCondition,
					whereColumnValue, joinCondition, separatorBetweenFields, isToExcludeDisabled);
		}
		return distributionProtocolList;
	}
}
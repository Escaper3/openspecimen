
/**
 * <p>Title: ConflictParticipantDataDetailsAction Class>
 * <p>Description: To retrieve the participant details
 * Copyright:    Copyright (c) year
 * Company: Washington University, School of Medicine, St. Louis.
 * @version 1.00
 * @Date 9/18/2007
 * @author kalpana Thakur
 */

package edu.wustl.catissuecore.action;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.wustl.catissuecore.actionForm.ConflictParticipantDataDetailsForm;
import edu.wustl.catissuecore.actionForm.ParticipantForm;
import edu.wustl.catissuecore.applet.AppletConstants;
import edu.wustl.catissuecore.domain.Participant;
import edu.wustl.catissuecore.domain.SpecimenCollectionGroup;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.action.BaseAction;
import edu.wustl.common.bizlogic.DefaultBizLogic;

public class ConflictParticipantDataDetailsAction extends BaseAction{

	public ActionForward executeAction(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		ConflictParticipantDataDetailsForm conflictParticipantDataDetailsForm = (ConflictParticipantDataDetailsForm) form;
		String participantId = (String)request.getParameter(Constants.ID);

		HttpSession session = request.getSession();
		session.setAttribute(Constants.PARTICIPANT_ID_TO_ASSOCIATE, participantId);
		session.removeAttribute(Constants.SCG_ID_TO_ASSOCIATE);
			
		DefaultBizLogic defaultBizLogic=new DefaultBizLogic();
		
		//retrieved the participant object and populated the bean
		Object object = defaultBizLogic.retrieve(Participant.class.getName(), new Long(participantId));
		Participant participant = null;
		if(object != null)
		{
			participant = (Participant)object;
			defaultBizLogic.populateUIBean(Participant.class.getName(), participant.getId(), conflictParticipantDataDetailsForm);
		}
			
		return mapping.findForward(Constants.SUCCESS);
	}
}

package com.krishagni.catissueplus.rest.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.krishagni.catissueplus.core.biospecimen.events.ShareSpecimenListOp;
import com.krishagni.catissueplus.core.biospecimen.events.SpecimenDetail;
import com.krishagni.catissueplus.core.biospecimen.events.SpecimenListDetails;
import com.krishagni.catissueplus.core.biospecimen.events.SpecimenListSummary;
import com.krishagni.catissueplus.core.biospecimen.events.UpdateListSpecimensOp;
import com.krishagni.catissueplus.core.biospecimen.services.SpecimenListService;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;
import com.krishagni.catissueplus.core.common.events.UserSummary;

@Controller
@RequestMapping("/specimen-lists")
public class SpecimenListsController {
	
	@Autowired
	private HttpServletRequest httpServletRequest;

	@Autowired
	private SpecimenListService specimenListSvc;
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<SpecimenListSummary> getSpecimenListsForUser(){
		ResponseEvent<List<SpecimenListSummary>> resp = specimenListSvc.getUserSpecimenLists(getRequest(null));
		resp.throwErrorIfUnsuccessful();
		return resp.getPayload();
	}
	
	@RequestMapping(method = RequestMethod.GET, value="/{listId}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public SpecimenListDetails getSpecimenList(@PathVariable Long listId) {
		ResponseEvent<SpecimenListDetails> resp = specimenListSvc.getSpecimenList(getRequest(listId));
		resp.throwErrorIfUnsuccessful();
		return resp.getPayload();
	}
		
	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public SpecimenListDetails createSpecimenList(@RequestBody SpecimenListDetails details) {
		ResponseEvent<SpecimenListDetails> resp = specimenListSvc.createSpecimenList(getRequest(details));
		resp.throwErrorIfUnsuccessful();
		return resp.getPayload();
	}

	@RequestMapping(method = RequestMethod.PUT, value="/{listId}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public SpecimenListDetails updateSpecimenList(@PathVariable Long listId, @RequestBody SpecimenListDetails details) {
		ResponseEvent<SpecimenListDetails> resp = specimenListSvc.updateSpecimenList(getRequest(details));
		resp.throwErrorIfUnsuccessful();
		return resp.getPayload();
	}
	
	@RequestMapping(method = RequestMethod.DELETE, value="/{listId}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public SpecimenListDetails deleteSpecimenList(@PathVariable Long listId) {
		ResponseEvent<SpecimenListDetails> resp = specimenListSvc.deleteSpecimenList(getRequest(listId));
		resp.throwErrorIfUnsuccessful();
		return resp.getPayload();
	}
			
	@RequestMapping(method = RequestMethod.GET, value="/{listId}/specimens")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<SpecimenDetail> getListSpecimens(@PathVariable("listId") Long listId) {
		ResponseEvent<List<SpecimenDetail>> resp = specimenListSvc.getListSpecimens(getRequest(listId));
		resp.throwErrorIfUnsuccessful();
		return resp.getPayload();
	}
	
	@RequestMapping(method = RequestMethod.PUT, value="/{listId}/specimens")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<SpecimenDetail> updateListSpecimens(
			@PathVariable("listId") Long listId,
			@RequestParam(value = "operation", required = false, defaultValue = "UPDATE") String operation,
			@RequestBody List<String> specimenLabels) {
		
		UpdateListSpecimensOp opDetail = new UpdateListSpecimensOp();
		opDetail.setListId(listId);
		opDetail.setSpecimens(specimenLabels);
		opDetail.setOp(com.krishagni.catissueplus.core.biospecimen.events.UpdateListSpecimensOp.Operation.valueOf(operation));
		

		ResponseEvent<List<SpecimenDetail>> resp = specimenListSvc.updateListSpecimens(getRequest(opDetail));
		resp.throwErrorIfUnsuccessful();
		return resp.getPayload();
	}
	
	@RequestMapping(method = RequestMethod.PUT, value="/{listId}/users")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<UserSummary> shareSpecimenList(
			@PathVariable("listId") Long listId,
			@RequestParam(value = "operation", required = false, defaultValue = "UPDATE") String operation,
			@RequestBody List<Long> userIds) {

		ShareSpecimenListOp opDetail = new ShareSpecimenListOp();
		opDetail.setListId(listId);
		opDetail.setOp(com.krishagni.catissueplus.core.biospecimen.events.ShareSpecimenListOp.Operation.valueOf(operation));
		opDetail.setUserIds(userIds);
		
		ResponseEvent<List<UserSummary>> resp = specimenListSvc.shareSpecimenList(getRequest(opDetail));
		resp.throwErrorIfUnsuccessful();
		return resp.getPayload();
	}
		
	private <T> RequestEvent<T> getRequest(T payload) {
		return new RequestEvent<T>(payload);
	}
 }

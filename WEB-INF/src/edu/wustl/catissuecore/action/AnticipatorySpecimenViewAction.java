package edu.wustl.catissuecore.action;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.wustl.catissuecore.actionForm.SpecimenCollectionGroupForm;
import edu.wustl.catissuecore.actionForm.ViewSpecimenSummaryForm;
import edu.wustl.catissuecore.bean.CollectionProtocolEventBean;
import edu.wustl.catissuecore.bean.GenericSpecimen;
import edu.wustl.catissuecore.bean.GenericSpecimenVO;
import edu.wustl.catissuecore.bizlogic.SpecimenCollectionGroupBizLogic;
import edu.wustl.catissuecore.domain.AbstractSpecimen;
import edu.wustl.catissuecore.domain.MolecularSpecimen;
import edu.wustl.catissuecore.domain.Specimen;
import edu.wustl.catissuecore.domain.SpecimenCharacteristics;
import edu.wustl.catissuecore.domain.SpecimenCollectionGroup;
import edu.wustl.catissuecore.domain.SpecimenRequirement;
import edu.wustl.catissuecore.domain.StorageContainer;
import edu.wustl.catissuecore.util.SpecimenAutoStorageContainer;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.action.BaseAction;
import edu.wustl.common.beans.SessionDataBean;
import edu.wustl.common.util.dbManager.DAOException;
import edu.wustl.common.util.logger.Logger;

/**
 * @author abhijit_naik
 *
 */
public class AnticipatorySpecimenViewAction extends BaseAction
{
	/**
	 * 
	 */
	private static final String SPECIMEN_KEY_PREFIX = "S_";
	Long cpId = null;
	private SpecimenAutoStorageContainer autoStorageContainer;

	/* (non-Javadoc)
	 * @see edu.wustl.common.action.BaseAction#executeAction(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward executeAction(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception
		{
		String target=Constants.SUCCESS;
		SpecimenCollectionGroupForm specimenCollectionGroupForm=
			(SpecimenCollectionGroupForm)form;
		HttpSession session = request.getSession();
		Long id = specimenCollectionGroupForm.getId();
		SessionDataBean bean = (SessionDataBean) session.getAttribute(Constants.SESSION_DATA);
		SpecimenCollectionGroupBizLogic scgBizLogic = 
			new SpecimenCollectionGroupBizLogic();
		autoStorageContainer = new SpecimenAutoStorageContainer ();
		
		try{
			target=Constants.SUCCESS;
			session.setAttribute(Constants.SCGFORM, specimenCollectionGroupForm.getId());
			SpecimenCollectionGroup specimencollectionGroup =
				scgBizLogic.getSCGFromId(id, bean,true);
			if(specimencollectionGroup.getActivityStatus().equalsIgnoreCase(Constants.ACTIVITY_STATUS_DISABLED))
			{
				target=Constants.ACTIVITY_STATUS_DISABLED;
			}
			cpId = specimencollectionGroup.getCollectionProtocolRegistration().getCollectionProtocol().getId();

			addSCGSpecimensToSession(session, specimencollectionGroup);			
		
			request.setAttribute("RequestType",ViewSpecimenSummaryForm.REQUEST_TYPE_ANTICIPAT_SPECIMENS);
			autoStorageContainer.setCollectionProtocol(cpId);
			autoStorageContainer.setSpecimenStoragePositions(bean);
			return mapping.findForward(target);

		}catch(Exception e){
			e.printStackTrace();
		}finally{
			autoStorageContainer = null;
		}
		return null;
	}

	/**
	 * @param session
	 * @param specimencollectionGroup
	 * @throws DAOException
	 */
	private void addSCGSpecimensToSession(HttpSession session,
			SpecimenCollectionGroup specimencollectionGroup) throws DAOException
	{
		LinkedHashMap<String, CollectionProtocolEventBean> cpEventMap 
		= new LinkedHashMap<String, CollectionProtocolEventBean> ();

		CollectionProtocolEventBean eventBean = new CollectionProtocolEventBean();

		eventBean.setUniqueIdentifier(String.valueOf(specimencollectionGroup.getId().longValue()));

		eventBean.setSpecimenRequirementbeanMap(getSpecimensMap(
				specimencollectionGroup.getSpecimenCollection(),cpId ));

		String globalSpecimenId = "E"+eventBean.getUniqueIdentifier() + "_";
		cpEventMap.put(globalSpecimenId, eventBean);			
		session.removeAttribute(Constants.COLLECTION_PROTOCOL_EVENT_SESSION_MAP);
		session
		.setAttribute(Constants.COLLECTION_PROTOCOL_EVENT_SESSION_MAP, cpEventMap);
	}

	protected LinkedHashMap<String, GenericSpecimen> getSpecimensMap(
			Collection<Specimen> specimenCollection, long collectionProtocolId)
				throws DAOException
	{
		LinkedHashMap<String, GenericSpecimen> specimenMap = 
						new LinkedHashMap<String, GenericSpecimen>();

		Iterator<Specimen> specimenIterator = specimenCollection.iterator();
		while(specimenIterator.hasNext())
		{
			Specimen specimen = specimenIterator.next();
			if (specimen.getParentSpecimen() == null)
			{
				GenericSpecimenVO specBean =getSpecimenBean(specimen, null);
				specBean.setUniqueIdentifier(SPECIMEN_KEY_PREFIX+specimen.getId());
				specBean.setCollectionProtocolId(collectionProtocolId);
				specimenMap = getOrderedMap(
						specimenMap, specimen.getId(), specBean, SPECIMEN_KEY_PREFIX);			
			}
			
		}
		return specimenMap;
	}
	
	private LinkedHashMap<String, GenericSpecimen>getOrderedMap(
					LinkedHashMap<String, GenericSpecimen> specimenMap, 
					Long id,GenericSpecimenVO specBean, String prefix) 
	{
		LinkedHashMap<String, GenericSpecimen> orderedMap = new 
				LinkedHashMap<String, GenericSpecimen>();
		Object [] keyArray = specimenMap.keySet().toArray();
			for(int ctr=0;ctr<keyArray.length;ctr++)
			{
				String keyVal =(String) keyArray[ctr];
				String keyId = keyVal.substring(prefix.length());
				if(Long.parseLong(keyId)>id)
				{
					orderedMap.put(keyVal, specimenMap.get(keyVal));
					specimenMap.remove(keyVal);
				}
			}
			specimenMap.put(prefix+id, specBean);
			if (!orderedMap.isEmpty())
			{
				specimenMap.putAll(orderedMap);
			}
		return specimenMap;
	}


	protected void setChildren(Specimen specimen, GenericSpecimen parentSpecimenVO) 
	throws DAOException
	{
		Collection<AbstractSpecimen> specimenChildren = specimen.getChildSpecimenCollection();
		
		if(specimenChildren == null ||specimenChildren.isEmpty())
		{
			return;
		}
		
		Iterator<AbstractSpecimen> iterator = specimenChildren.iterator();
		
		LinkedHashMap<String, GenericSpecimen>  aliquotMap = new
			LinkedHashMap<String, GenericSpecimen> ();
		LinkedHashMap<String, GenericSpecimen>  derivedMap = new
		LinkedHashMap<String, GenericSpecimen> ();
		
		while(iterator.hasNext())
		{
			Specimen childSpecimen = (Specimen)iterator.next();
			String lineage = childSpecimen.getLineage();
				
			GenericSpecimenVO specimenBean = getSpecimenBean(childSpecimen, specimen.getLabel());
			String prefix = lineage + specimen.getId() +"_";
			specimenBean.setUniqueIdentifier(prefix + childSpecimen.getId());
		
			if(Constants.ALIQUOT.equals(childSpecimen.getLineage()))
			{
				aliquotMap = getOrderedMap(
						aliquotMap, childSpecimen.getId(), specimenBean, prefix);	
			}
			else
			{
				derivedMap = getOrderedMap(
						derivedMap, childSpecimen.getId(), specimenBean, prefix);
			}
			specimenBean.setCollectionProtocolId(cpId);
			
		}
		parentSpecimenVO.setAliquotSpecimenCollection(aliquotMap);
		parentSpecimenVO.setDeriveSpecimenCollection(derivedMap);
	}

	protected GenericSpecimenVO getSpecimenBean(Specimen specimen, String parentName) throws DAOException
	{
		GenericSpecimenVO specimenDataBean = new GenericSpecimenVO();
		specimenDataBean.setBarCode(specimen.getBarcode());
		specimenDataBean.setClassName(specimen.getClassName());
		specimenDataBean.setDisplayName(specimen.getLabel());
		specimenDataBean.setPathologicalStatus(specimen.getPathologicalStatus());
		specimenDataBean.setId(specimen.getId().longValue());
		specimenDataBean.setParentName(parentName);
		if(specimen.getInitialQuantity()!=null)
		{	
			specimenDataBean.setQuantity(specimen.getInitialQuantity().toString());
		}
	
		specimenDataBean.setCheckedSpecimen(true);
		if (Constants.SPECIMEN_COLLECTED.equals(specimen.getCollectionStatus()))
		{
			specimenDataBean.setReadOnly(true);
		}
		specimenDataBean.setType(specimen.getSpecimenType());
		SpecimenCharacteristics characteristics = specimen.getSpecimenCharacteristics();
		if (characteristics != null)
		{
			specimenDataBean.setTissueSide(characteristics.getTissueSide());
			specimenDataBean.setTissueSite(characteristics.getTissueSite());
		}
		//specimenDataBean.setExternalIdentifierCollection(specimen.getExternalIdentifierCollection());
		//specimenDataBean.setBiohazardCollection(specimen.getBiohazardCollection());
		//specimenDataBean.setSpecimenEventCollection(specimen.getSpecimenEventCollection());
		
//		specimenDataBean.setSpecimenCollectionGroup(specimen.getSpecimenCollectionGroup());
				
//		specimenDataBean.setStorageContainer(null);
		String concentration ="";
		if ("Molecular".equals(specimen.getClassName()))
		{
			concentration =String.valueOf(
					((MolecularSpecimen) specimen).getConcentrationInMicrogramPerMicroliter()
					);
		}
		specimenDataBean.setConcentration(concentration);
		
		String storageType = null;
		
		if(specimen != null && specimen.getSpecimenPosition() != null)
		{
				StorageContainer container = specimen.getSpecimenPosition().getStorageContainer();
				Logger.out.info("-----------Container while getting from domain--:"+container);
				specimenDataBean.setContainerId( String.valueOf(container.getId()));
				specimenDataBean.setSelectedContainerName(container.getName());
				specimenDataBean.setPositionDimensionOne(String.valueOf(specimen.getSpecimenPosition().getPositionDimensionOne()));
				specimenDataBean.setPositionDimensionTwo(String.valueOf(specimen.getSpecimenPosition().getPositionDimensionTwo()));
				specimenDataBean.setStorageContainerForSpecimen("Auto");
		}
		else
		{
			//TODO:After model change 
			storageType = getStorageType(specimen);
			specimenDataBean.setStorageContainerForSpecimen(storageType);				

		}
		if ("Auto".equals(storageType))
		{

			autoStorageContainer.addSpecimen(specimenDataBean, specimenDataBean.getClassName());
			
		}
		setChildren(specimen, specimenDataBean);
		//specimenDataBean.setAliquotSpecimenCollection(getChildren(specimen, Constants.ALIQUOT));
		//specimenDataBean.setDeriveSpecimenCollection(getChildren(specimen, Constants.ALIQUOT));
		return specimenDataBean;
	}

	private String getStorageType(Specimen specimen)
	{
		String storageType;
		SpecimenRequirement reqSpecimen = specimen.getSpecimenRequirement();
		if(reqSpecimen==null)
		{
			storageType = "Virtual";
		}
		else
		{
			storageType = reqSpecimen.getStorageType();
		}
		return storageType;
	}
		
}

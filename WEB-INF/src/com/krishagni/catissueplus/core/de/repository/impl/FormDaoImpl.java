package com.krishagni.catissueplus.core.de.repository.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.BooleanType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.TimestampType;

import com.krishagni.catissueplus.core.administrative.repository.FormListCriteria;
import com.krishagni.catissueplus.core.biospecimen.domain.Specimen;
import com.krishagni.catissueplus.core.biospecimen.domain.Visit;
import com.krishagni.catissueplus.core.biospecimen.events.CollectionProtocolSummary;
import com.krishagni.catissueplus.core.common.Pair;
import com.krishagni.catissueplus.core.common.access.AccessCtrlMgr;
import com.krishagni.catissueplus.core.common.access.SiteCpPair;
import com.krishagni.catissueplus.core.common.events.DependentEntityDetail;
import com.krishagni.catissueplus.core.common.events.UserSummary;
import com.krishagni.catissueplus.core.common.repository.AbstractDao;
import com.krishagni.catissueplus.core.de.domain.Form;
import com.krishagni.catissueplus.core.de.events.FormContextDetail;
import com.krishagni.catissueplus.core.de.events.FormCtxtSummary;
import com.krishagni.catissueplus.core.de.events.FormRecordSummary;
import com.krishagni.catissueplus.core.de.events.FormSummary;
import com.krishagni.catissueplus.core.de.events.ObjectCpDetail;
import com.krishagni.catissueplus.core.de.repository.FormDao;

import krishagni.catissueplus.beans.FormContextBean;
import krishagni.catissueplus.beans.FormRecordEntryBean;

public class FormDaoImpl extends AbstractDao<FormContextBean> implements FormDao {

	@Override
	public Class<FormContextBean> getType() {
		return FormContextBean.class;
	}
	
	@Override
	public FormContextBean getById(Long id) {
		return getById(id, "deletedOn is null");
	}

	@Override
	public Form getFormById(Long formId) {
		return (Form) getCurrentSession().createCriteria(Form.class)
			.add(Restrictions.eq("id", formId))
			.add(Restrictions.isNull("deletedOn"))
			.uniqueResult();
	}

	@Override
	public Form getFormByName(String name) {
		return (Form) getCurrentSession().createCriteria(Form.class)
			.add(Restrictions.eq("name", name))
			.add(Restrictions.isNull("deletedOn"))
			.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Form> getFormsByIds(Collection<Long> formIds) {
		return getCurrentSession().createCriteria(Form.class)
			.add(Restrictions.in("id", formIds))
			.add(Restrictions.isNull("deletedOn"))
			.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FormSummary> getAllFormsSummary(FormListCriteria crit) {
		return getForms(getAllFormsQuery(crit, false).list());
	}

	@Override
	public Long getAllFormsCount(FormListCriteria crit) {
		return ((Number) getAllFormsQuery(crit, true).uniqueResult()).longValue();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isSystemForm(Long formId) {
		List<FormContextBean> result = sessionFactory.getCurrentSession()
				.getNamedQuery(GET_SYSTEM_FORM_CTXTS)
				.setLong("formId", formId)
				.setMaxResults(1)
				.list();

		return !result.isEmpty();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Date getUpdateTime(Long formId) {
		return (Date) getCurrentSession().getNamedQuery(GET_FORM_UPDATE_TIME)
				.setParameter("formId", formId)
				.uniqueResult();
	}

	@SuppressWarnings("unchecked")
 	@Override
  	public List<FormSummary> getFormsByEntityType(String entityType) {
		List<Object[]> rows = sessionFactory.getCurrentSession()
				.getNamedQuery(GET_FORMS_BY_ENTITY_TYPE)
				.setString("entityType", entityType)
				.list();
		return getForms(rows);
    }

	@SuppressWarnings("unchecked")
	@Override
	public List<FormSummary> getFormsByCpAndEntityType(Long cpId, String[] entityTypes) {
		List<Object[]> rows = getCurrentSession()
				.getNamedQuery(GET_FORMS_BY_CP_N_ENTITY_TYPE)
				.setLong("cpId", cpId)
				.setParameterList("entityTypes", entityTypes)
				.list();
		return getForms(rows, false, true);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<FormSummary> getQueryForms() {
		Query query = sessionFactory.getCurrentSession().getNamedQuery(GET_QUERY_FORMS);
		List<Object[]> rows = query.list();

		List<FormSummary> forms = new ArrayList<FormSummary>();
		for (Object[] row : rows) {
			FormSummary form = new FormSummary();
			form.setFormId((Long)row[0]);
			form.setName((String)row[1]);
			form.setCaption((String)row[2]);
			form.setCreationTime((Date)row[3]);
			form.setModificationTime((Date)row[4]);
			form.setCpCount(-1);

			UserSummary user = new UserSummary();
			user.setId((Long)row[5]);
			user.setFirstName((String)row[6]);
			user.setLastName((String)row[7]);
			form.setCreatedBy(user);
			
			forms.add(form);						
		}
				
		return forms;		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<FormContextDetail> getFormContexts(Long formId) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery(GET_FORM_CTXTS);
		List<Object[]> rows = query.setLong("formId", formId).list();
		
		List<FormContextDetail> formCtxts = new ArrayList<>();
		for (Object[] row : rows) {
			int idx = -1;
			FormContextDetail formCtxt = new FormContextDetail();
			formCtxt.setFormCtxtId((Long)row[++idx]);
			formCtxt.setFormId((Long)row[++idx]);
			formCtxt.setLevel((String)row[++idx]);
			formCtxt.setEntityId((Long)row[++idx]);
			formCtxt.setMultiRecord((Boolean)row[++idx]);
			formCtxt.setSysForm((Boolean)row[++idx]);

			CollectionProtocolSummary cp = new CollectionProtocolSummary();
			cp.setId((Long)row[++idx]);
			cp.setShortTitle((String)row[++idx]);
			cp.setTitle((String)row[++idx]);
			formCtxt.setCollectionProtocol(cp);
			
			formCtxts.add(formCtxt);
		}
		
		return formCtxts;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public FormContextBean getQueryFormContext(Long formId) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery(GET_QUERY_FORM_CONTEXT);
		List<Object> queryFormContext = query.setLong("formId", formId).setString("entityType", "Query").list();
		return (FormContextBean)(queryFormContext.size() == 1 ? queryFormContext.get(0) : null);
	}

	@Override
	public List<FormContextBean> getFormContexts(Collection<Long> cpIds, String entityType) {
		return sessionFactory.getCurrentSession()
			.createCriteria(FormContextBean.class)
			.add(Restrictions.in("cpId", cpIds))
			.add(Restrictions.eq("entityType", entityType))
			.add(Restrictions.isNull("deletedOn"))
			.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FormCtxtSummary> getCprForms(Long cprId) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery(GET_CPR_FORMS);
		query.setLong("cprId", cprId);		
		return getEntityForms(query.list());
	}

	public List<FormCtxtSummary> getParticipantForms(Long cprId) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery(GET_PARTICIPANT_FORMS);
		query.setLong("cprId", cprId);
		return getEntityForms(query.list());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FormCtxtSummary> getSpecimenForms(Long specimenId) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery(GET_SPECIMEN_FORMS);
		query.setLong("specimenId", specimenId);		
		return getEntityForms(query.list());
	}
	
    @SuppressWarnings("unchecked")
    @Override
    public List<FormCtxtSummary> getSpecimenEventForms(Long specimenId) {
        Query query = sessionFactory.getCurrentSession().getNamedQuery(GET_SPECIMEN_EVENT_FORMS);
        query.setLong("specimenId", specimenId);
        return getEntityForms(query.list());
    }
	
	@SuppressWarnings("unchecked")
	@Override
	public List<FormCtxtSummary> getScgForms(Long scgId) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery(GET_SCG_FORMS);
		query.setLong("scgId", scgId);		
		return getEntityForms(query.list());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<FormCtxtSummary> getFormContexts(Long cpId, String entityType) {
		List<FormContextBean> result = sessionFactory.getCurrentSession()
			.getNamedQuery(GET_FORM_CTXTS_BY_ENTITY)
			.setLong("cpId", cpId)
			.setString("entityType", entityType)
			.list();
				
		return FormCtxtSummary.from(result);
	}
			
	@SuppressWarnings("unchecked")
	@Override
	public List<FormRecordSummary> getFormRecords(Long formCtxtId, Long objectId) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery(GET_FORM_RECORDS);
		query.setLong("formCtxtId", formCtxtId).setLong("objectId", objectId);
		
		List<Object[]> rows = query.list();
		
		List<FormRecordSummary> formRecords = new ArrayList<FormRecordSummary>();
		for (Object[] row : rows) {
			FormRecordSummary record = new FormRecordSummary();
			record.setId((Long)row[0]);
			record.setRecordId((Long)row[1]);
			record.setUpdateTime((Date)row[2]);
			
			UserSummary user = new UserSummary();
			user.setId((Long)row[3]);
			user.setFirstName((String)row[4]);
			user.setLastName((String)row[5]);
			record.setUser(user);
			
			formRecords.add(record);
		}
		
		return formRecords;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public FormSummary getFormByContext(Long formCtxtId) {
		List<Object[]> rows = sessionFactory.getCurrentSession()
			.getNamedQuery(GET_FORM_BY_CTXT)
			.setLong("formCtxtId", formCtxtId)
			.list();
		
		if (rows.isEmpty()) {
			return null;
		}
		
		Object[] row = rows.iterator().next();
		FormSummary result = new FormSummary();
		result.setFormId((Long)row[0]);
		result.setCaption((String)row[1]);
		return result;		
	}
		
	@SuppressWarnings("unchecked")
	@Override
	public FormContextBean getFormContext(Long formId, Long cpId, String entity) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery(GET_FORM_CTXT);		
		query.setLong("formId", formId);
		query.setLong("cpId", cpId);		
		query.setString("entityType", entity);
		
		List<FormContextBean> objs = query.list();
		return objs != null && !objs.isEmpty() ? objs.iterator().next() : null;
	}

	@Override
	public FormContextBean getFormContext(boolean cpBased, String entityType, Long entityId, Long formId) {
		entityId = entityId == null ? -1L : entityId;

		Criteria query = getCurrentSession().createCriteria(FormContextBean.class)
			.add(Restrictions.eq("entityType", entityType))
			.add(Restrictions.isNull("deletedOn"));

		if (cpBased) {
			query.add(Restrictions.or(Restrictions.eq("cpId", -1L), Restrictions.eq("cpId", entityId)));
		} else {
			query.add(Restrictions.eq("entityId", entityId));
		}

		if (formId != null) {
			query.add(Restrictions.eq("containerId", formId));
		}

		return (FormContextBean) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FormContextBean> getFormContextsById(List<Long> formContextIds) {
		List<FormContextBean> formContexts = new ArrayList<FormContextBean>();
		
		int i = 0;
		int numIds = formContextIds.size();
		while (i < numIds) {
			List<Long> params = formContextIds.subList(i, i + 500 > numIds ? numIds : i + 500);
			i += 500;
			
			formContexts.addAll(
				sessionFactory.getCurrentSession()
					.getNamedQuery(GET_FORM_CTXTS_BY_ID)
					.setParameterList("ids", params)
					.list());					
		}
		
		return formContexts;
	}

	@Override
	public Pair<String, Long> getFormNameContext(Long cpId, String entityType, Long entityId) {
		List<Object[]> rows = getCurrentSession()
			.getNamedQuery(GET_FORM_NAME_CTXT_ID)
			.setParameter("cpId", cpId)
			.setParameter("entityType", entityType)
			.setParameter("entityId", entityId, LongType.INSTANCE)
			.list();

		if (CollectionUtils.isEmpty(rows)) {
			return null;
		}

		Object[] row = rows.iterator().next();
		return Pair.make((String)row[0], (Long)row[1]);
	}
	
	@Override
	public void saveOrUpdateRecordEntry(FormRecordEntryBean recordEntry) {
		sessionFactory.getCurrentSession().saveOrUpdate(recordEntry);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<FormRecordEntryBean> getRecordEntries(Long formCtxtId, Long objectId) {
		return sessionFactory.getCurrentSession()
				.getNamedQuery(GET_RECORD_ENTRIES)
				.setLong("formCtxtId", formCtxtId)
				.setLong("objectId", objectId)
				.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public FormRecordEntryBean getRecordEntry(Long formCtxtId, Long objectId, Long recordId) {
		List<Object[]> rows = sessionFactory.getCurrentSession()
				.getNamedQuery(GET_RECORD_ENTRY)
				.setLong("formCtxtId", formCtxtId)
				.setLong("objectId", objectId)
				.setLong("recordId", recordId)
				.list();
		return CollectionUtils.isEmpty(rows) ? null : getFormRecordEntry(rows.iterator().next());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public FormRecordEntryBean getRecordEntry(Long formId, Long recordId) {
		List<Object[]> rows = sessionFactory.getCurrentSession()
				.getNamedQuery(GET_REC_BY_FORM_N_REC_ID)
				.setLong("formId", formId)
				.setLong("recordId", recordId)
				.list();
		return CollectionUtils.isEmpty(rows) ? null : getFormRecordEntry(rows.iterator().next());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<Long, Pair<Long, Long>> getLatestRecordIds(Long formId, String entityType, List<Long> objectIds) {
		List<Object[]> rows = sessionFactory.getCurrentSession()
				.getNamedQuery(GET_LATEST_RECORD_IDS)
				.setParameter("formId", formId)
				.setParameter("entityType", entityType)
				.setParameterList("objectIds", objectIds)
				.list();
		return rows.stream().collect(Collectors.toMap(
			row -> (Long) row[0],
			row -> Pair.make((Long) row[1], (Long) row[2])
		));
	}

	private List<FormCtxtSummary> getEntityForms(List<Object[]> rows) {
		Map<Long, FormCtxtSummary> formsMap = new LinkedHashMap<>();
		
		for (Object[] row : rows) {
			Long cpId = (Long)row[3];
			Long formId = (Long)row[1];

			FormCtxtSummary form = formsMap.get(formId);
			if (form != null && cpId == -1) {
				continue;
			}
			
			form = new FormCtxtSummary();
			form.setFormCtxtId((Long)row[0]);
			form.setFormId(formId);
			form.setFormCaption((String)row[2]);
			form.setEntityType((String)row[4]);
			form.setCreationTime((Date)row[5]);
			form.setModificationTime((Date)row[6]);
			
			UserSummary user = new UserSummary();
			user.setId((Long)row[7]);
			user.setFirstName((String)row[8]);
			user.setLastName((String)row[9]);
			form.setCreatedBy(user);
			
			form.setMultiRecord((Boolean)row[10]);
			form.setSysForm((Boolean)row[11]);
			form.setNoOfRecords((Integer)row[12]);
			formsMap.put(formId, form);
		}
		
		return new ArrayList<>(formsMap.values());
	}
		
	@Override
	public ObjectCpDetail getObjectCpDetail(Map<String, Object> dataHookingInformation) {
		ObjectCpDetail objCp = null;
		String entityType = (String)dataHookingInformation.get("entityType"); 
		if (entityType.equals("Participant") ) {
			objCp = getObjectIdForParticipant(dataHookingInformation);
		} else if (entityType.equals("Specimen") || entityType.equals("SpecimenEvent")) {
			objCp = getObjectIdForSpecimen(entityType, dataHookingInformation);
		} else if (entityType.equals("SpecimenCollectionGroup")) {
			objCp = getObjectIdForSCG(dataHookingInformation);
		}
		
		return objCp;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Long getFormCtxtId(Long containerId, String entityType, Long cpId) {
        Long formCtxtId = null;

        Query query = sessionFactory.getCurrentSession().getNamedQuery(GET_FORM_CTX_ID);
		query.setLong("containerId", containerId);
		query.setString("entityType", entityType);
        query.setLong("cpId", cpId);

        List<Object[]> rows = query.list();

        if (rows == null || rows.isEmpty()) {
            return null;
        }

        for (Object[] row : rows) {
            formCtxtId = (Long) row[0];
            if (cpId.equals((Long) row[1])) {
                break;
            }
        }

		return formCtxtId;
	}
	
	@Override
	public List<Long> getFormIds(Long cpId, String entityType) {
		return getFormIds(cpId, Collections.singletonList(entityType));
	}
	
	@Override
	public List<Long> getFormIds(Long cpId, List<String> entityTypes) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery(GET_FORM_IDS);
		query.setLong("cpId", cpId).setParameterList("entityTypes", entityTypes);
		
		List<Long> formIds = new ArrayList<Long>();
		for (Object id : query.list()) {
			formIds.add((Long)id);
		}
		
		return formIds;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Long getRecordsCount(Long formCtxtId, Long objectId) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery(GET_RECORD_CNT);
		query.setLong("formCtxtId", formCtxtId).setLong("objectId", objectId);
		List<Long> result = query.list();
		return result.iterator().next();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<Long, List<FormRecordSummary>> getFormRecords(Long objectId, String entityType, Long formId) {
		
		Query query = null;
		if (formId == null) {
			query = sessionFactory.getCurrentSession()
				.getNamedQuery(GET_RECS_BY_TYPE_AND_OBJECT);					
		} else {
			query = sessionFactory.getCurrentSession()
				.getNamedQuery(GET_RECS)
				.setLong("formId", formId);
		}
		
		List<Object[]> rows = query.setString("entityType", entityType)
				.setLong("objectId", objectId)
				.list();
		
		Map<Long, List<FormRecordSummary>> result = new HashMap<Long, List<FormRecordSummary>>();
		for (Object[] row : rows) {
			Long form = (Long)row[0];
			
			FormRecordSummary record = new FormRecordSummary();
			record.setFcId((Long)row[1]);
			record.setRecordId((Long)row[2]);
			record.setUpdateTime((Date)row[3]);
			
			UserSummary user = new UserSummary();
			user.setId((Long)row[4]);
			user.setFirstName((String)row[5]);
			user.setLastName((String)row[6]);
			record.setUser(user);
			
			List<FormRecordSummary> recs = result.get(form);
			if (recs == null) {
				recs = new ArrayList<FormRecordSummary>();
				result.put(form, recs);
			}
			
			recs.add(record);
		}
				
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<DependentEntityDetail> getDependentEntities(Long formId) {
		List<Object[]> rows = getCurrentSession().getNamedQuery(GET_DEPENDENT_ENTITIES)
				.setLong("formId", formId)
				.list();
		
		return getDependentEntities(rows);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String getFormChangeLogDigest(String file) {
		List<Object> rows = getCurrentSession().createSQLQuery(GET_CHANGE_LOG_DIGEST_SQL)
				.setString("filename", file)
				.list();
		
		if (rows == null || rows.isEmpty()) {
			return null;
		}		
		return (String)rows.iterator().next();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getLatestFormChangeLog(String file) {
		List<Object[]> rows = getCurrentSession().createSQLQuery(GET_LATEST_CHANGE_LOG_SQL)
				.addScalar("filename", StringType.INSTANCE) //fl.filename, fl.form_id, fl.md5_digest, fl.executed_on
				.addScalar("form_id", LongType.INSTANCE)
				.addScalar("md5_digest", StringType.INSTANCE)
				.addScalar("executed_on", TimestampType.INSTANCE)
				.setParameter("filename", file)
				.list();

		if (rows == null || rows.isEmpty()) {
			return null;
		}

		return rows.iterator().next();
	}

	@Override
	public void insertFormChangeLog(String file, String digest, Long formId) {
		sessionFactory.getCurrentSession()
				.createSQLQuery(INSERT_CHANGE_LOG_SQL)
				.setString("filename", file)
				.setString("digest", digest)
				.setLong("formId", formId)
				.setTimestamp("executedOn", Calendar.getInstance().getTime())
				.executeUpdate();
	}
	
	@Override
	public void deleteFormContexts(Collection<Long> formIds) {
		sessionFactory.getCurrentSession()
			.createSQLQuery(SOFT_DELETE_FORM_CONTEXTS_SQL)
			.setTimestamp("deletedOn", Calendar.getInstance().getTime())
			.setParameterList("formIds", formIds)
			.executeUpdate(); 
	}

	@Override
	public void deleteRecords(Long formCtxtId, Collection<Long> recordIds) {
		getCurrentSession().createSQLQuery(SOFT_DELETE_RECS_SQL)
			.setLong("formCtxtId", formCtxtId)
			.setParameterList("recordIds", recordIds)
			.executeUpdate();
	}

	@Override
	public int deleteRecords(Long cpId, List<String> entityTypes, Long objectId) {
		return getCurrentSession().getNamedQuery(SOFT_DELETE_ENTITY_RECS)
			.setParameter("cpId", cpId)
			.setParameterList("entityTypes", entityTypes)
			.setParameter("objectId", objectId)
			.executeUpdate();
	}

	@Override
	public int deleteFormContexts(Long cpId, List<String> entityTypes) {
		return getCurrentSession().getNamedQuery(SOFT_DELETE_CP_FORMS)
			.setParameter("deleteTime", Calendar.getInstance().getTime())
			.setParameter("cpId", cpId)
			.setParameterList("entityTypes", entityTypes)
			.executeUpdate();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<Long, List<Long>> getRecordIds(Long formCtxtId, Collection<Long> objectIds) {
		List<Object[]> rows = getCurrentSession().getNamedQuery(GET_RECORD_IDS)
			.setParameter("formCtxtId", formCtxtId)
			.setParameterList("objectIds", objectIds)
			.list();

		Map<Long, List<Long>> result = new HashMap<>();
		for (Object[] row : rows) {
			List<Long> recordIds = result.computeIfAbsent((Long)row[0], (u) -> new ArrayList<>());
			recordIds.add((Long)row[1]);
		}

		return result;
	}

	@Override
	public List<Map<String, Object>> getRegistrationRecords(Long cpId, Collection<SiteCpPair> siteCps, Long formId, List<String> ppids, int startAt, int maxResults) {
		return getEntityRecords(
			cpId, siteCps, formId, GET_REG_FORM_RECORDS,
			ppids, "ppids", "cpr.protocol_participant_id in (:ppids)", null,
			startAt, maxResults,
			row -> {
				Map<String, Object> regRecord = new HashMap<>();
				regRecord.put("cpId", ((Number) row[0]).longValue());
				regRecord.put("cpShortTitle", row[1]);
				regRecord.put("cprId", ((Number) row[2]).longValue());
				regRecord.put("ppid", row[3]);
				regRecord.put("recordId", ((Number) row[4]).longValue());
				return regRecord;
			});
	}

	@Override
	public List<Map<String, Object>> getParticipantRecords(Long cpId, Collection<SiteCpPair> siteCps, Long formId, List<String> ppids, int startAt, int maxResults) {
		return getEntityRecords(
			cpId, siteCps, formId, GET_PART_FORM_RECORDS,
			ppids, "ppids", "cpr.protocol_participant_id in (:ppids)", null,
			startAt, maxResults,
			row -> {
				Map<String, Object> partRecord = new HashMap<>();
				partRecord.put("cpId", ((Number) row[0]).longValue());
				partRecord.put("cpShortTitle", row[1]);
				partRecord.put("cprId", ((Number) row[2]).longValue());
				partRecord.put("participantId", ((Number) row[3]).longValue());
				partRecord.put("ppid", row[4]);
				partRecord.put("recordId", ((Number) row[5]).longValue());
				return partRecord;
			});
	}

	@Override
	public List<Map<String, Object>> getVisitRecords(Long cpId, Collection<SiteCpPair> siteCps, Long formId, List<String> visitNames, int startAt, int maxResults) {
		return getEntityRecords(
			cpId, siteCps, formId, GET_VISIT_FORM_RECORDS,
			visitNames, "visitNames", "v.name in (:visitNames)", null,
			startAt, maxResults,
			row -> {
				Map<String, Object> visitRecord = new HashMap<>();
				visitRecord.put("cpId", ((Number) row[0]).longValue());
				visitRecord.put("cpShortTitle", row[1]);
				visitRecord.put("visitId", ((Number) row[2]).longValue());
				visitRecord.put("visitName", row[3]);
				visitRecord.put("recordId", ((Number) row[4]).longValue());
				return visitRecord;
			});
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> getSpecimenRecords(Long cpId, Collection<SiteCpPair> siteCps, Long formId, String entityType, List<String> spmnLabels, int startAt, int maxResults) {
		return getEntityRecords(
			cpId, siteCps, formId, GET_SPMN_FORM_RECORDS,
			spmnLabels, "spmnLabels", "s.label in (:spmnLabels)",
			Collections.singletonMap("entityType", entityType),
			startAt, maxResults,
			(row) -> {
				Map<String, Object> spmnRecord = new HashMap<>();
				spmnRecord.put("cpId", ((Number) row[0]).longValue());
				spmnRecord.put("cpShortTitle", row[1]);
				spmnRecord.put("spmnId", ((Number) row[2]).longValue());
				spmnRecord.put("spmnLabel", row[3]);
				spmnRecord.put("spmnBarcode", row[4]);
				spmnRecord.put("recordId", ((Number) row[5]).longValue());
				return spmnRecord;
			});
	}

	private Query getAllFormsQuery(FormListCriteria crit, boolean countReq) {
		Query query = countReq ? getCountFormsQuery(crit) : getListFormsQuery(crit);

		if (StringUtils.isNotBlank(crit.query())) {
			query.setParameter("caption", "%" + crit.query().toLowerCase() + "%");
		}
		
		if (crit.userId() != null) {
			query.setParameter("userId", crit.userId());
		}
		
		if (crit.excludeSysForm() != null) {
			query.setParameter("sysForm", !crit.excludeSysForm());
		}

		return query;
	}

	private Query getListFormsQuery(FormListCriteria crit) {
		return getCurrentSession().createSQLQuery(getListFormsSql(crit, false))
			.addScalar("formId", new LongType())
			.addScalar("formName", new StringType())
			.addScalar("formCaption", new StringType())
			.addScalar("creationTime", new TimestampType())
			.addScalar("modificationTime", new TimestampType())
			.addScalar("cpCount", new IntegerType())
			.addScalar("allCp", new IntegerType())
			.addScalar("sysForm", new BooleanType())
			.addScalar("userId", new LongType())
			.addScalar("userFirstName", new StringType())
			.addScalar("userLastName", new StringType())
			.setFirstResult(crit.startAt())
			.setMaxResults(crit.maxResults());
	}

	private Query getCountFormsQuery(FormListCriteria crit) {
		return getCurrentSession().createSQLQuery(getListFormsSql(crit, true));
	}

	private String getCpIdsSql(Collection<SiteCpPair> siteCps) {
		return AccessCtrlMgr.getInstance().getAllowedCpIdsSql(siteCps);
	}

	private String getListFormsSql(FormListCriteria crit, boolean countReq) {
		boolean joinCps = CollectionUtils.isNotEmpty(crit.siteCps());
		String cpFormsSql =  joinCps ? CP_FORMS_JOIN : "";
		String proj = String.format(countReq ? GET_FORMS_COUNT_PROJ : GET_FORMS_LIST_PROJ, joinCps ? " distinct " : "");
		String whereClause = "";
		if (crit.excludeSysForm() != null) {
			whereClause = crit.excludeSysForm() ? NON_SYS_FORM_COND : SYS_FORM_COND;
		}

		StringBuilder sqlBuilder = new StringBuilder(String.format(GET_ALL_FORMS, proj, whereClause, cpFormsSql));
		if (StringUtils.isNotBlank(crit.query())) {
			sqlBuilder.append(" and lower(c.caption) like :caption ");
		}
		
		if (crit.userId() != null) {
			sqlBuilder.append(" and (c.created_by = :userId ");
			
			if (joinCps) {
				sqlBuilder.append(" or cp.identifier in (").append(getCpIdsSql(crit.siteCps())).append(")");
			}

			sqlBuilder.append(")");
		}

		if (!countReq) {
			sqlBuilder.append(" order by modificationTime desc ");
		}

		return sqlBuilder.toString();
	}

	@SuppressWarnings("unchecked")
	private ObjectCpDetail getObjectIdForParticipant(Map<String, Object> dataHookingInformation) {
		ObjectCpDetail objCp = new ObjectCpDetail();
		String cpTitle = (String) dataHookingInformation.get("collectionProtocol");
		String ppId = (String) dataHookingInformation.get("ppi");

		Query query = sessionFactory.getCurrentSession().getNamedQuery(GET_PARTICIPANT_OBJ_ID);
		query.setString("ppId", ppId);
		query.setString("cpTitle", cpTitle);

		List<Object[]> objs = query.list();
		if (objs == null || objs.isEmpty()) {
			return null;
		}

		Object[] row = objs.get(0);
		objCp.setObjectId((Long) row[0]);
		objCp.setCpId((Long) row[1]);
		return objCp;
	}
	
	@SuppressWarnings("unchecked")
	private ObjectCpDetail getObjectIdForSpecimen(String entityType, Map<String, Object> dataHookingInformation) {
		ObjectCpDetail objCp = new ObjectCpDetail();
		String id = null, label = null, barcode = null;

		if (entityType.equals("Specimen")) {
			id = (String) dataHookingInformation.get("specimenId");
			label = (String) dataHookingInformation.get("specimenLabel");
			barcode = (String) dataHookingInformation.get("specimenBarcode");
		} else if (entityType.equals("SpecimenEvent")) {
			id = (String) dataHookingInformation.get("specimenIdForEvent");
			label = (String) dataHookingInformation.get("specimenLabelForEvent");
			barcode = (String) dataHookingInformation.get("specimenBarcodeForEvent");
		} else {
			throw new RuntimeException("Unknown entity type: " + entityType);
		}

		Criteria query = sessionFactory
				.getCurrentSession()
				.createCriteria(Specimen.class)
				.createAlias("specimenCollectionGroup", "scg")
				.createAlias("scg.collectionProtocolRegistration", "cpr")
				.createAlias("cpr.collectionProtocol", "cp")
				.setProjection(
						Projections.projectionList()
								.add(Projections.property("id"))
								.add(Projections.property("cp.id")));

		if (id != null) {
			Long specimenId = Long.parseLong(id);
			query.add(Restrictions.eq("id", specimenId));
		} else if (label != null) {
			query.add(Restrictions.eq("label", label));
		} else if (barcode != null) {
			query.add(Restrictions.eq("barcode", barcode));
		} else {
			throw new RuntimeException("Require either Specimen ID, Specimen Label or Specimen Barcode");
		}

		List<Object[]> objs = query.list();
		if (objs == null || objs.isEmpty()) {
			return null;
		}

		Object[] row = objs.get(0);
		objCp.setObjectId((Long) row[0]);
		objCp.setCpId((Long) row[1]);
		return objCp;
	}

	@SuppressWarnings("unchecked")
	private ObjectCpDetail getObjectIdForSCG(Map<String, Object> dataHookingInformation) {
		ObjectCpDetail objCp = new ObjectCpDetail();
		String id = (String) dataHookingInformation.get("scgId");
		String name = (String) dataHookingInformation.get("scgName");
		String barcode = (String) dataHookingInformation.get("scgBarcode");

		Criteria query = sessionFactory
				.getCurrentSession()
				.createCriteria(Visit.class)
				.createAlias("collectionProtocolRegistration", "cpr")
				.createAlias("cpr.collectionProtocol", "cp")
				.setProjection(
						Projections.projectionList()
								.add(Projections.property("id"))
								.add(Projections.property("cp.id")));

		if (id != null) {
			Long scgId = Long.parseLong(id);
			query.add(Restrictions.eq("id", scgId));
		} else if (name != null) {
			query.add(Restrictions.eq("name", name));
		} else if (barcode != null) {
			query.add(Restrictions.eq("barcode", barcode));
		} else {
			throw new RuntimeException("Require either SCG ID, SCG Name or SCG Barcode");
		}

		List<Object[]> objs = query.list();
		if (objs == null || objs.isEmpty()) {
			return null;
		}

		Object[] row = objs.get(0);
		objCp.setObjectId((Long) row[0]);
		objCp.setCpId((Long) row[1]);
		return objCp;
	}
	
	private FormRecordEntryBean getFormRecordEntry(Object[] row) {
		FormRecordEntryBean re = new FormRecordEntryBean();
		re.setIdentifier((Long)row[0]);
		re.setFormCtxtId((Long)row[1]);
		re.setObjectId((Long)row[2]);
		re.setRecordId((Long)row[3]);
		re.setUpdatedBy((Long)row[4]);
		re.setUpdatedTime((Date)row[5]);
		re.setActivityStatusStr((String)row[6]);
		re.setEntityType((String)row[7]);
		return re;
	}
	
	private List<FormSummary> getForms(List<Object[]> rows) {
		return getForms(rows, true, false);
	}

	private List<FormSummary> getForms(List<Object[]> rows, boolean incCpCount, boolean incEntityType) {
		List<FormSummary> forms = new ArrayList<>();

		for (Object[] row : rows) {
			int idx = 0;

			FormSummary form = new FormSummary();
			form.setFormId((Long)row[idx++]);
			form.setName((String)row[idx++]);
			form.setCaption((String)row[idx++]);
			form.setCreationTime((Date)row[idx++]);
			form.setModificationTime((Date)row[idx++]);

			if (incCpCount) {
				Integer cpCount = (Integer) row[idx++];
				Integer allCps = (Integer) row[idx++];
				form.setCpCount(allCps != null && allCps == -1 ? -1 : cpCount);
			}

			if (incEntityType) {
				form.setEntityType((String)row[idx++]);
			}

			Boolean sysForm = (Boolean)row[idx++];
			form.setSysForm(sysForm == null ? false : sysForm);

			UserSummary user = new UserSummary();
			user.setId((Long)row[idx++]);
			user.setFirstName((String)row[idx++]);
			user.setLastName((String)row[idx++]);
			form.setCreatedBy(user);
			forms.add(form);
		}

		return forms;
	}

	private List<DependentEntityDetail> getDependentEntities(List<Object[]> rows) {
		List<DependentEntityDetail> dependentEntities = new ArrayList<DependentEntityDetail>();
		
		for (Object[] row: rows) {
			String name = (String)row[0];
			int count = ((Integer)row[1]).intValue();
			dependentEntities.add(DependentEntityDetail.from(name, count));
		}
		
		return dependentEntities;
 	}

	private List<Map<String, Object>> getEntityRecords(
		Long cpId, Collection<SiteCpPair> siteCps, Long formId, String queryName,
		List<String> names, String namesVar, String namesCond,
		Map<String, Object> params,
		int startAt, int maxResults,
		Function<Object[], Map<String, Object>> rowMapper) {

		String sql = getCurrentSession().getNamedQuery(queryName).getQueryString();
		if (CollectionUtils.isNotEmpty(names)) {
			int orderByIdx = sql.lastIndexOf("order by");
			sql = sql.substring(0, orderByIdx) + " and " + namesCond + " " + sql.substring(orderByIdx);
		}

		if (cpId != null && cpId != -1L) {
			int orderByIdx = sql.lastIndexOf("order by");
			sql = sql.substring(0, orderByIdx) + " and cp.identifier = " + cpId + " " + sql.substring(orderByIdx);
		}

		if (CollectionUtils.isNotEmpty(siteCps)) {
			int orderByIdx = sql.lastIndexOf("order by");
			sql = sql.substring(0, orderByIdx) + " and cp.identifier in (" + getCpIdsSql(siteCps) + ") " + sql.substring(orderByIdx);
		}

		Query query = getCurrentSession().createSQLQuery(sql)
			.setParameter("formId", formId)
			.setFirstResult(startAt)
			.setMaxResults(maxResults);

		if (CollectionUtils.isNotEmpty(names)) {
			query.setParameterList(namesVar, names);
		}

		if (params != null) {
			for (Map.Entry<String, Object> param : params.entrySet()) {
				query.setParameter(param.getKey(), param.getValue());
			}
		}

		return ((List<Object[]>)query.list()).stream().map(rowMapper).collect(Collectors.toList());
	}

	private static final String FQN = FormContextBean.class.getName();
	
	private static final String GET_FORMS_BY_ENTITY_TYPE = FQN + ".getFormsByEntityType";

	private static final String GET_FORMS_BY_CP_N_ENTITY_TYPE = FQN + ".getFormsByCpAndEntityType";
	
	private static final String GET_QUERY_FORMS = FQN + ".getQueryForms";
	
	private static final String GET_FORM_CTXTS = FQN + ".getFormContexts";
	
	private static final String GET_FORM_CTXT = FQN + ".getFormContext";
	
	private static final String GET_SYSTEM_FORM_CTXTS = FQN + ".getSysFormContexts";

	private static final String GET_FORM_CTXTS_BY_ID = FQN + ".getFormContextsById";
	
	private static final String GET_CPR_FORMS = FQN + ".getCprForms";

	private static final String GET_PARTICIPANT_FORMS = FQN + ".getParticipantForms";
	
	private static final String GET_SPECIMEN_FORMS = FQN + ".getSpecimenForms";
	
	private static final String GET_SPECIMEN_EVENT_FORMS = FQN + ".getSpecimenEventForms";
	
	private static final String GET_SCG_FORMS = FQN + ".getScgForms";
	
	private static final String GET_FORM_CTXTS_BY_ENTITY = FQN + ".getFormContextsByEntity";
	
	private static final String GET_FORM_BY_CTXT = FQN + ".getFormByCtxt";
	
	private static final String GET_FORM_RECORDS = FQN + ".getFormRecords";
	
	private static final String GET_PARTICIPANT_OBJ_ID = FQN + ".getParticipantObjId";

	private static final String GET_FORM_CTX_ID = FQN + ".getFormContextId";

	private static final String GET_FORM_NAME_CTXT_ID = FQN + ".getFormNameContextId";

	private static final String GET_FORM_UPDATE_TIME = FQN + ".getUpdateTime";

	private static final String RE_FQN = FormRecordEntryBean.class.getName();
	
	private static final String GET_RECORD_ENTRY = RE_FQN + ".getRecordEntry";
	
	private static final String GET_RECORD_ENTRIES = RE_FQN + ".getRecordEntries";

	private static final String GET_REC_BY_FORM_N_REC_ID = RE_FQN + ".getRecordEntryByFormAndRecId";

	private static final String GET_LATEST_RECORD_IDS = RE_FQN + ".getLatestRecordIds";

	private static final String GET_RECORD_IDS = RE_FQN + ".getRecordIds";

	private static final String GET_FORM_IDS = FQN + ".getFormIds";
	
	private static final String GET_QUERY_FORM_CONTEXT = FQN + ".getQueryFormCtxtByContainerId";

	private static final String GET_RECORD_CNT = FQN + ".getRecordCount"; 
	
	private static final String GET_RECS_BY_TYPE_AND_OBJECT = FQN  + ".getRecordsByEntityAndObject";

	private static final String GET_RECS = FQN + ".getRecords";
	
	private static final String GET_DEPENDENT_ENTITIES = FQN + ".getDependentEntities";
	
	private static final String GET_CHANGE_LOG_DIGEST_SQL =
			"select " +
			"  md5_digest " +
			"from " +
			"  os_import_forms_log fl " +
			"where " +
			"  fl.filename = :filename and fl.executed_on in (" +
			"    select " +
			"      max(executed_on) " +
			"    from " +
			"      os_import_forms_log " +
			"    where " +
			"      filename = :filename )";

	private static final String GET_LATEST_CHANGE_LOG_SQL =
			"select " +
			"  fl.filename, fl.form_id, fl.md5_digest, fl.executed_on " +
			"from " +
			"  os_import_forms_log fl " +
			"where " +
			"  fl.filename = :filename and fl.executed_on in (" +
			"    select " +
			"      max(executed_on) " +
			"    from " +
			"      os_import_forms_log " +
			"    where " +
			"      filename = :filename )";
	
	private static final String INSERT_CHANGE_LOG_SQL =
			"insert into os_import_forms_log " +
			"	(filename, form_id, md5_digest, executed_on) " +
			"values " +
			"   (:filename, :formId, :digest, :executedOn) ";
	
	private static final String SOFT_DELETE_FORM_CONTEXTS_SQL = 
			"update catissue_form_context set deleted_on = :deletedOn where container_id in (:formIds)";

	private static final String SOFT_DELETE_RECS_SQL =
			"update catissue_form_record_entry " +
			"set " +
			"  activity_status = 'CLOSED' " +
			"where " +
			"  form_ctxt_id = :formCtxtId and record_id in (:recordIds)";


	private static final String SOFT_DELETE_ENTITY_RECS = RE_FQN + ".deleteEntityRecords";

	private static final String SOFT_DELETE_CP_FORMS = FQN + ".deleteCpEntityForms";

	private static final String GET_ALL_FORMS =
			"select %s " +
			"from " +
			"  dyextn_containers c " +
			"  inner join catissue_user u on u.identifier = c.created_by " +
			"  inner join ( " +
			"    select " +
			"      ic.identifier as formId, min(ctxt.cp_id) as allCp, count(distinct ctxt.cp_id) as cpCount, " +
			"      max(ctxt.is_sys_form) as sysForm " +
			"    from " +
			"      dyextn_containers ic " +
			"      left join catissue_form_context ctxt on ctxt.container_id = ic.identifier and ctxt.deleted_on is null " +
			"      left join catissue_collection_protocol cp on ctxt.cp_id = cp.identifier and cp.activity_status != 'Disabled'" +
			"    where " +
			"      ic.deleted_on is null and " +
			"      (ctxt.entity_type is null or ctxt.entity_type != 'Query') " +
			"      %s " +
			"    group by " +
			"      ic.identifier " +
			"  ) derived on derived.formId = c.identifier " +
			"  %s " + // placeholder to join cp forms
			"where " +
			"  c.deleted_on is null ";

	private static final String GET_FORMS_LIST_PROJ = "%s " + // placeholder to add distinct when joined with cp forms
			"  c.identifier as formId, c.name as formName, c.caption as formCaption, c.create_time as creationTime, " +
			"  case when c.last_modify_time is null then c.create_time else c.last_modify_time end as modificationTime, " +
			"  derived.cpCount as cpCount, derived.allCp, derived.sysForm, " +
			"  u.identifier as userId, u.first_name as userFirstName, u.last_name as userLastName ";

	private static final String GET_FORMS_COUNT_PROJ = "count(%s c.identifier) ";

	private static final String CP_FORMS_JOIN =
			"left join catissue_form_context ctxt " +
			"  on ctxt.container_id = c.identifier and ctxt.deleted_on is null " +
			"left join catissue_collection_protocol cp " +
			"  on ctxt.cp_id = cp.identifier and cp.activity_status != 'Disabled' ";

	private static final String SYS_FORM_COND = " and ctxt.is_sys_form = :sysForm";

	private static final String NON_SYS_FORM_COND = " and (ctxt.is_sys_form is null or ctxt.is_sys_form = :sysForm) ";

	//
	// used for form records export
	//
	private static final String GET_PART_FORM_RECORDS  = RE_FQN + ".getParticipantRecords";

	private static final String GET_REG_FORM_RECORDS   = RE_FQN + ".getRegistrationRecords";

	private static final String GET_VISIT_FORM_RECORDS = RE_FQN + ".getVisitRecords";

	private static final String GET_SPMN_FORM_RECORDS  = RE_FQN + ".getSpecimenRecords";
}

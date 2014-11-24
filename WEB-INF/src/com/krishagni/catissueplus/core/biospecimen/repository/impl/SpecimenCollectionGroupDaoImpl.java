
package com.krishagni.catissueplus.core.biospecimen.repository.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.krishagni.catissueplus.core.biospecimen.domain.Specimen;
import com.krishagni.catissueplus.core.biospecimen.domain.SpecimenCollectionGroup;
import com.krishagni.catissueplus.core.biospecimen.repository.SpecimenCollectionGroupDao;
import com.krishagni.catissueplus.core.common.repository.AbstractDao;
import com.krishagni.catissueplus.core.common.util.Status;

@Repository("specimenCollectionGroupDao")
public class SpecimenCollectionGroupDaoImpl extends AbstractDao<SpecimenCollectionGroup>
		implements
			SpecimenCollectionGroupDao {

	@Override
	public List<SpecimenCollectionGroup> getAllScgs(int startAt, int maxRecords, String... searchString) {
		Criteria criteria = sessionFactory.getCurrentSession()
				.createCriteria(SpecimenCollectionGroup.class, "scg")
				.add(Restrictions.or(
						Restrictions.eq("scg.activityStatus", Status.ACTIVITY_STATUS_ACTIVE.getStatus()),
						Restrictions.eq("scg.activityStatus", Status.ACTIVITY_STATUS_CLOSED.getStatus())));
		
		addSearchConditions(criteria, searchString);
		criteria.addOrder(Order.asc("scg.name"));
		addLimits(criteria, startAt, maxRecords);
		return getScgs(criteria);
	}
	
	@Override
	public Long getScgsCount(String... searchString) {
		Criteria criteria = sessionFactory.getCurrentSession()
				.createCriteria(SpecimenCollectionGroup.class, "scg")
				.add(Restrictions.or(
						Restrictions.eq("scg.activityStatus", Status.ACTIVITY_STATUS_ACTIVE.getStatus()),
						Restrictions.eq("scg.activityStatus", Status.ACTIVITY_STATUS_CLOSED.getStatus())))
				.setProjection(Projections.countDistinct("scg.id"));
		
		addSearchConditions(criteria, searchString);
		return ((Number)criteria.uniqueResult()).longValue();
	}
	
	@Override
	public List<Specimen> getSpecimensList(Long scgId) {
		Object object = sessionFactory.getCurrentSession().get(SpecimenCollectionGroup.class.getName(), scgId);
		if (object == null) {
			return Collections.emptyList();
		}

		SpecimenCollectionGroup scg = (SpecimenCollectionGroup) object;
		return new ArrayList<Specimen>(scg.getSpecimenCollection());
	}

	@Override
	@SuppressWarnings("unchecked")
	public SpecimenCollectionGroup getScgByName(String name) {
		Criteria query = sessionFactory.getCurrentSession().createCriteria(SpecimenCollectionGroup.class);
		query.add(Restrictions.eq("name", name));
		List<SpecimenCollectionGroup> scgs = query.list();
		
		return scgs.size() > 0 ? scgs.get(0) : null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public SpecimenCollectionGroup getScgByBarcode(String barcode) {
		Criteria query = sessionFactory.getCurrentSession().createCriteria(SpecimenCollectionGroup.class);
		query.add(Restrictions.eq("barcode", barcode));
		List<SpecimenCollectionGroup> scgs = query.list();
		
		return scgs.size() > 0 ? scgs.get(0) : null;
	}

	@Override
	public SpecimenCollectionGroup getscg(Long id) {
		return (SpecimenCollectionGroup)sessionFactory.getCurrentSession().get(SpecimenCollectionGroup.class, id);
	}
	
	private void addSearchConditions(Criteria criteria, String[] searchString) {
		if (searchString == null || searchString.length == 0 || StringUtils.isBlank(searchString[0])) {
			return;
		}
		
		Disjunction srchCond = Restrictions.disjunction();
		srchCond.add(Restrictions.or(
				Restrictions.ilike("scg.name", searchString[0], MatchMode.ANYWHERE),
				Restrictions.ilike("scg.barcode", searchString[0], MatchMode.ANYWHERE)
				));
		criteria.add(srchCond);
	}
	
	
	private void addLimits(Criteria criteria, int start, int maxRecords) {
		criteria.setFirstResult(start <= 0 ? 0 : start);
		if (maxRecords > 0) {
			criteria.setMaxResults(maxRecords);
		}
	}
	
	@SuppressWarnings("unchecked")
	private List<SpecimenCollectionGroup> getScgs(Criteria criteria) {
		List<SpecimenCollectionGroup> result = criteria.list();
		return result;		
	}
}

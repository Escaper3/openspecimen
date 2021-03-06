package com.krishagni.catissueplus.core.administrative.repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.krishagni.catissueplus.core.administrative.domain.ScheduledJob;
import com.krishagni.catissueplus.core.administrative.domain.ScheduledJobRun;
import com.krishagni.catissueplus.core.administrative.events.ScheduledJobListCriteria;
import com.krishagni.catissueplus.core.administrative.events.JobRunsListCriteria;
import com.krishagni.catissueplus.core.common.repository.Dao;

public interface ScheduledJobDao extends Dao<ScheduledJob> {
	List<ScheduledJob> getScheduledJobs(ScheduledJobListCriteria listCriteria);

	Long getScheduledJobsCount(ScheduledJobListCriteria listCriteria);

	ScheduledJobRun getJobRun(Long id);
	
	ScheduledJob getJobByName(String name);
	
	List<ScheduledJobRun> getJobRuns(JobRunsListCriteria listCriteria);

	Map<Long, Date> getJobsLastRunTime(Collection<Long> jobIds);

	void saveOrUpdateJobRun(ScheduledJobRun job);
}

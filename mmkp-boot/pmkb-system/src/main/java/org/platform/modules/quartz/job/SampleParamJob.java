package org.platform.modules.quartz.job;

import org.jeecg.common.util.DateUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @Author Scott
 */
@Slf4j
public class SampleParamJob implements Job {

	/**
	 */
	private String parameter;

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		log.info(" Job Execution key："+jobExecutionContext.getJobDetail().getKey());
		log.info( String.format("welcome %s! Jeecg-Boot SampleParamJob !   time:" + DateUtils.now(), this.parameter));
	}
}

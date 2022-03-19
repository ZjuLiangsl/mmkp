package org.platform.modules.quartz.service;

import java.util.List;

import org.platform.modules.quartz.entity.QuartzJob;
import org.quartz.SchedulerException;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Author: jeecg-boot
 * @Date: 2019-04-28
 * @Version: V1.1
 */
public interface IQuartzJobService extends IService<QuartzJob> {

	List<QuartzJob> findByJobClassName(String jobClassName);

	boolean saveAndScheduleJob(QuartzJob quartzJob);

	boolean editAndScheduleJob(QuartzJob quartzJob) throws SchedulerException;

	boolean deleteAndStopJob(QuartzJob quartzJob);

	boolean resumeJob(QuartzJob quartzJob);

	/**
	 * @param quartzJob
	 */
	void execute(QuartzJob quartzJob) throws Exception;

	/**
	 * @param quartzJob
	 * @throws SchedulerException
	 */
	void pause(QuartzJob quartzJob);
}

package org.platform.modules.quartz.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.ImportExcelUtil;
import org.platform.modules.quartz.entity.QuartzJob;
import org.platform.modules.quartz.service.IQuartzJobService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Author: jeecg-boot
 * @Date: 2019-01-02
 * @Version:V1.0
 */
@RestController
@RequestMapping("/sys/quartzJob")
@Slf4j
@Api(tags = "QuartzJobController")
public class QuartzJobController {
	@Autowired
	private IQuartzJobService quartzJobService;
	@Autowired
	private Scheduler scheduler;

	/**
	 *
	 * @param quartzJob
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<?> queryPageList(QuartzJob quartzJob, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
		QueryWrapper<QuartzJob> queryWrapper = QueryGenerator.initQueryWrapper(quartzJob, req.getParameterMap());
		Page<QuartzJob> page = new Page<QuartzJob>(pageNo, pageSize);
		IPage<QuartzJob> pageList = quartzJobService.page(page, queryWrapper);
        return Result.ok(pageList);

	}

	/**
	 *
	 * @param quartzJob
	 * @return
	 */
	//@RequiresRoles("admin")
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Result<?> add(@RequestBody QuartzJob quartzJob) {
		quartzJobService.saveAndScheduleJob(quartzJob);
		return Result.ok("success");
	}

	/**
	 *
	 * @param quartzJob
	 * @return
	 */
	//@RequiresRoles("admin")
	@RequestMapping(value = "/edit", method = RequestMethod.PUT)
	public Result<?> eidt(@RequestBody QuartzJob quartzJob) {
		try {
			quartzJobService.editAndScheduleJob(quartzJob);
		} catch (SchedulerException e) {
			log.error(e.getMessage(),e);
			return Result.error("Exception!");
		}
	    return Result.ok("success!");
	}

	/**
	 *
	 * @param id
	 * @return
	 */
	//@RequiresRoles("admin")
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
		QuartzJob quartzJob = quartzJobService.getById(id);
		if (quartzJob == null) {
			return Result.error("No corresponding entity found");
		}
		quartzJobService.deleteAndStopJob(quartzJob);
        return Result.ok("success!");

	}

	/**
	 *
	 * @param ids
	 * @return
	 */
	//@RequiresRoles("admin")
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
		if (ids == null || "".equals(ids.trim())) {
			return Result.error("Parameter nonidentification！");
		}
		for (String id : Arrays.asList(ids.split(","))) {
			QuartzJob job = quartzJobService.getById(id);
			quartzJobService.deleteAndStopJob(job);
		}
        return Result.ok("success!");
	}

	/**
	 *
	 * @param id
	 * @return
	 */
	//@RequiresRoles("admin")
	@GetMapping(value = "/pause")
	public Result<Object> pauseJob(@RequestParam(name = "id") String id) {
		QuartzJob job = quartzJobService.getById(id);
		if (job == null) {
			return Result.error("The scheduled task does not exist！");
		}
		quartzJobService.pause(job);
		return Result.ok("success");
	}

	/**
	 *
	 * @param id
	 * @return
	 */
	//@RequiresRoles("admin")
	@GetMapping(value = "/resume")
	public Result<Object> resumeJob(@RequestParam(name = "id") String id) {
		QuartzJob job = quartzJobService.getById(id);
		if (job == null) {
			return Result.error("The scheduled task does not exist！");
		}
		quartzJobService.resumeJob(job);
		//scheduler.resumeJob(JobKey.jobKey(job.getJobClassName().trim()));
		return Result.ok("success");
	}

	/**
	 *
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/queryById", method = RequestMethod.GET)
	public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
		QuartzJob quartzJob = quartzJobService.getById(id);
        return Result.ok(quartzJob);
	}

	/**
	 *
	 * @param request
	 * @param quartzJob
	 */
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, QuartzJob quartzJob) {
		QueryWrapper<QuartzJob> queryWrapper = QueryGenerator.initQueryWrapper(quartzJob, request.getParameterMap());
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		List<QuartzJob> pageList = quartzJobService.list(queryWrapper);
		mv.addObject(NormalExcelConstants.FILE_NAME, "Scheduled Task List");
		mv.addObject(NormalExcelConstants.CLASS, QuartzJob.class);
		mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("Scheduled Task List", "Jeecg", "export"));
		mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
		return mv;
	}

	/**
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		List<String> errorMessage = new ArrayList<>();
		int successLines = 0, errorLines = 0;
		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
			MultipartFile file = entity.getValue();
			ImportParams params = new ImportParams();
			params.setTitleRows(2);
			params.setHeadRows(1);
			params.setNeedSave(true);
			try {
				List<Object> listQuartzJobs = ExcelImportUtil.importExcel(file.getInputStream(), QuartzJob.class, params);
				List<String> list = ImportExcelUtil.importDateSave(listQuartzJobs, IQuartzJobService.class, errorMessage,CommonConstant.SQL_INDEX_UNIQ_JOB_CLASS_NAME);
				errorLines+=list.size();
				successLines+=(listQuartzJobs.size()-errorLines);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return Result.error("failure！");
			} finally {
				try {
					file.getInputStream().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return ImportExcelUtil.imporReturnRes(errorLines,successLines,errorMessage);
	}

	/**
	 * @param id
	 * @return
	 */
	//@RequiresRoles("admin")
	@GetMapping("/execute")
	public Result<?> execute(@RequestParam(name = "id", required = true) String id) {
		QuartzJob quartzJob = quartzJobService.getById(id);
		if (quartzJob == null) {
			return Result.error("No corresponding entity found");
		}
		try {
			quartzJobService.execute(quartzJob);
		} catch (Exception e) {
			//e.printStackTrace();
			log.info("failure >>"+e.getMessage());
			return Result.error("failure!");
		}
		return Result.ok("success!");
	}
}

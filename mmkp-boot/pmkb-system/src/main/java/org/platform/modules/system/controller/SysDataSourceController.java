package org.platform.modules.system.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.DynamicDataSourceModel;
import org.jeecg.common.util.dynamic.db.DataSourceCachePool;
import org.platform.modules.system.entity.SysDataSource;
import org.platform.modules.system.service.ISysDataSourceService;
import org.platform.modules.system.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: 多数据源管理
 * @Author: jeecg-boot
 * @Date: 2019-12-25
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "多数据源管理")
@RestController
@RequestMapping("/sys/dataSource")
public class SysDataSourceController extends JeecgController<SysDataSource, ISysDataSourceService> {
    @Autowired
    private ISysDataSourceService sysDataSourceService;

    /**
     * 分页列表查询
     *
     * @param sysDataSource
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "多数据源管理-分页列表查询")
    @ApiOperation(value = "多数据源管理-分页列表查询", notes = "多数据源管理-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(
            SysDataSource sysDataSource,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req
    ) {
        QueryWrapper<SysDataSource> queryWrapper = QueryGenerator.initQueryWrapper(sysDataSource, req.getParameterMap());
        Page<SysDataSource> page = new Page<>(pageNo, pageSize);
        IPage<SysDataSource> pageList = sysDataSourceService.page(page, queryWrapper);
        try {
            List<SysDataSource> records = pageList.getRecords();
            records.forEach(item->{
                String dbPassword = item.getDbPassword();
                if(StringUtils.isNotBlank(dbPassword)){
                    String decodedStr = SecurityUtil.jiemi(dbPassword);
                    item.setDbPassword(decodedStr);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.ok(pageList);
    }

    @GetMapping(value = "/options")
    public Result<?> queryOptions(SysDataSource sysDataSource, HttpServletRequest req) {
        QueryWrapper<SysDataSource> queryWrapper = QueryGenerator.initQueryWrapper(sysDataSource, req.getParameterMap());
        List<SysDataSource> pageList = sysDataSourceService.list(queryWrapper);
        JSONArray array = new JSONArray(pageList.size());
        for (SysDataSource item : pageList) {
            JSONObject option = new JSONObject(3);
            option.put("value", item.getCode());
            option.put("label", item.getName());
            option.put("text", item.getName());
            array.add(option);
        }
        return Result.ok(array);
    }

    /**
     * 添加
     *
     * @param sysDataSource
     * @return
     */
    @AutoLog(value = "多数据源管理-添加")
    @ApiOperation(value = "多数据源管理-添加", notes = "多数据源管理-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody SysDataSource sysDataSource) {
        try {
            String dbPassword = sysDataSource.getDbPassword();
            if(StringUtils.isNotBlank(dbPassword)){
                String encrypt = SecurityUtil.jiami(dbPassword);
                sysDataSource.setDbPassword(encrypt);
            }
            sysDataSourceService.save(sysDataSource);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param sysDataSource
     * @return
     */
    @AutoLog(value = "多数据源管理-编辑")
    @ApiOperation(value = "多数据源管理-编辑", notes = "多数据源管理-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody SysDataSource sysDataSource) {
        try {
            SysDataSource d = sysDataSourceService.getById(sysDataSource.getId());
            DataSourceCachePool.removeCache(d.getCode());
            String dbPassword = sysDataSource.getDbPassword();
            if(StringUtils.isNotBlank(dbPassword)){
                String encrypt = SecurityUtil.jiami(dbPassword);
                sysDataSource.setDbPassword(encrypt);
            }
            sysDataSourceService.updateById(sysDataSource);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "多数据源管理-通过id删除")
    @ApiOperation(value = "多数据源管理-通过id删除", notes = "多数据源管理-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        SysDataSource sysDataSource = sysDataSourceService.getById(id);
        DataSourceCachePool.removeCache(sysDataSource.getCode());
        sysDataSourceService.removeById(id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "多数据源管理-批量删除")
    @ApiOperation(value = "多数据源管理-批量删除", notes = "多数据源管理-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        idList.forEach(item->{
            SysDataSource sysDataSource = sysDataSourceService.getById(item);
            DataSourceCachePool.removeCache(sysDataSource.getCode());
        });
        this.sysDataSourceService.removeByIds(idList);
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "多数据源管理-通过id查询")
    @ApiOperation(value = "多数据源管理-通过id查询", notes = "多数据源管理-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        SysDataSource sysDataSource = sysDataSourceService.getById(id);
        return Result.ok(sysDataSource);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param sysDataSource
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, SysDataSource sysDataSource) {
        return super.exportXls(request, sysDataSource, SysDataSource.class, "多数据源管理");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, SysDataSource.class);
    }

    @PostMapping({"/testConnection"})
    public Result testConn(@RequestBody DynamicDataSourceModel var1) {
        Connection var2 = null;
        Result var4;
        try {
            Class.forName(var1.getDbDriver());
            var2 = DriverManager.getConnection(var1.getDbUrl(), var1.getDbUsername(), var1.getDbPassword());
            Result var3;
            if (var2 == null) {
                var3 = Result.ok("数据库连接失败：错误未知");
                return var3;
            }

            var3 = Result.ok("数据库连接成功");
            return var3;
        } catch (ClassNotFoundException var17) {
            log.error(var17.toString());
            var4 = Result.error("数据库连接失败：驱动类不存在");
        } catch (Exception var18) {
            log.error(var18.toString());
            var4 = Result.error("数据库连接失败：" + var18.getMessage());
            return var4;
        } finally {
            try {
                if (var2 != null && !var2.isClosed()) {
                    var2.close();
                }
            } catch (SQLException var16) {
                log.error(var16.toString());
            }

        }

        return var4;
    }

    @RequestMapping(value = "/getDsDict", method = RequestMethod.GET)
    public Result<List<DictModel>> getDictItems(HttpServletRequest request) {
        Result<List<DictModel>> result = new Result<List<DictModel>>();
        try {
            List<SysDataSource> list = sysDataSourceService.list();
            List<DictModel> ls = list.stream().map(ds -> {
                DictModel dm = new DictModel();
                dm.setText(ds.getName());
                dm.setValue(ds.getCode());
                return dm;
            }).collect(Collectors.toList());
            if (ls==null || ls.size()==0) {
                result.error500("数据源为空！");
                return result;
            }
            result.setSuccess(true);
            result.setResult(ls);
            log.debug(result.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败");
            return result;
        }
        return result;
    }
}

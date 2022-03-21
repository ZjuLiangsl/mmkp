package org.platform.modules.pmkb.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.dynamic.db.DataSourceCachePool;
import org.jeecg.common.util.dynamic.db.DynamicDBUtil;
import org.platform.modules.pmkb.entity.MetadataTables;
import org.platform.modules.pmkb.service.IMetadataFieldsService;
import org.platform.modules.pmkb.service.IMetadataTablesService;
import org.platform.modules.pmkb.vo.MetadataFieldsVO;
import org.platform.modules.pmkb.vo.MetadataTablesVO;
import org.platform.modules.system.entity.SysDataSource;
import org.platform.modules.system.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/dqe")
public class DataQueryEditContorller {

    @Autowired
    private IMetadataTablesService tablesService;
    @Autowired
    private IMetadataFieldsService fieldsService;


    /**
     *
     * @param po
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping(value = "/page")
    public Result<?> page(
            MetadataTables po,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req
    ) {
        try {
            Page page = new Page(pageNo, pageSize);
            List<MetadataFieldsVO> listFieldsVO = fieldsService.listMetadataFieldsVO(po);
            StringBuilder sql = new StringBuilder();
            sql.append(" select ");
            List<String> fields = new ArrayList<>();
            List<MetadataFieldsVO> searchFields = new ArrayList<>();
            List<String> conditions = new ArrayList<>();
            listFieldsVO.forEach(vo -> {
                if (vo.getFieldType().indexOf("time") > -1) {
                    fields.add("date_format(`" + vo.getFieldName() + "`,'" + (StrUtil.isNotBlank(vo.getExtField2()) ? vo.getExtField2() : "%Y-%m-%d %H:%i:%S") + "') as " + vo.getFieldName());
                } else if (vo.getFieldType().indexOf("date") > -1) {
                    fields.add("date_format(`" + vo.getFieldName() + "`,'" + (StrUtil.isNotBlank(vo.getExtField2()) ? vo.getExtField2() : "%Y-%m-%d") + "') as " + vo.getFieldName());
                } else {
                    fields.add("`"+vo.getFieldName()+"`");
                }
                if ("1".equals(vo.getIsSearchField())) {
                    searchFields.add(vo);
                }
            });
            sql.append(String.join(",", fields));
            sql.append(" from ");
            sql.append("`" + po.getDbName() + "`." + po.getTableName());
            searchFields.forEach(vo -> {
                String param = req.getParameter(vo.getFieldName());
                if (StrUtil.isNotBlank(param)) {
                    if (vo.getFieldType().indexOf("int") > -1 || vo.getFieldType().indexOf("decimal") > -1
                            || vo.getFieldType().indexOf("num") > -1 || vo.getFieldType().indexOf("double") > -1) {
                        conditions.add(vo.getFieldName() + "=" + param);
                    } else {
                        conditions.add(vo.getFieldName() + "='" + param + "'");
                    }
                }
            });
            if (conditions.size() > 0) {
                sql.append(" where ");
                sql.append(String.join(" and ", conditions));
            }
            Page result = DynamicDBUtil.page(po.getDsCode(), sql.toString(), page, null);
            return Result.ok(result);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("failure!"+e.getLocalizedMessage());
        }
    }

    /**
     *
     * @param po
     * @param req
     * @return
     */
    @GetMapping(value = "/list")
    public Result<?> list(MetadataTables po, HttpServletRequest req) {
        try{
            List<MetadataFieldsVO> listFieldsVO = fieldsService.listMetadataFieldsVO(po);
            StringBuilder sql = new StringBuilder();
            sql.append(" select ");
            List<String> fields = new ArrayList<>();
            List<MetadataFieldsVO> searchFields = new ArrayList<>();
            List<String> conditions = new ArrayList<>();
            listFieldsVO.forEach(vo -> {
                if (vo.getFieldType().indexOf("time") > -1) {
                    fields.add("date_format(" + vo.getFieldName() + ",'" + (StrUtil.isNotBlank(vo.getExtField2()) ? vo.getExtField2() : "%Y-%m-%d %H:%i:%S") + "') as " + vo.getFieldName());
                } else if (vo.getFieldType().indexOf("date") > -1) {
                    fields.add("date_format(" + vo.getFieldName() + ",'" + (StrUtil.isNotBlank(vo.getExtField2()) ? vo.getExtField2() : "%Y-%m-%d") + "') as " + vo.getFieldName());
                } else {
                    fields.add(vo.getFieldName());
                }
                if ("1".equals(vo.getIsSearchField())) {
                    searchFields.add(vo);
                }
            });
            sql.append(String.join(",", fields));
            sql.append(" from ");
            sql.append("`" + po.getDbName() + "`." + po.getTableName());
            searchFields.forEach(vo -> {
                String param = req.getParameter(vo.getFieldName());
                if (StrUtil.isNotBlank(param)) {
                    if (vo.getFieldType().indexOf("int") > -1 || vo.getFieldType().indexOf("decimal") > -1
                            || vo.getFieldType().indexOf("num") > -1 || vo.getFieldType().indexOf("double") > -1) {
                        conditions.add(vo.getFieldName() + "=" + param);
                    } else {
                        conditions.add(vo.getFieldName() + "='" + param + "'");
                    }
                }
            });
            if (conditions.size() > 0) {
                sql.append(" where ");
                sql.append(String.join(" and ", conditions));
            }
            List<Map<String, Object>> list = DynamicDBUtil.findList(po.getDsCode(), sql.toString());
            return Result.ok(list);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("failure!"+e.getLocalizedMessage());
        }
    }

    /**
     *
     * @return
     */
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody JSONObject formData) {
        try {
            JSONObject table = formData.getJSONObject("table");
            MetadataTablesVO tableVo = JSONObject.toJavaObject(table, MetadataTablesVO.class);
            JSONObject record = formData.getJSONObject("record");
            StringBuilder sql = new StringBuilder();
            sql.append(" insert into ");
            sql.append("`" + tableVo.getDbName() + "`." + tableVo.getTableName());
            List<String> fields = new ArrayList<>();
            List<String> values = new ArrayList<>();
            for (Map.Entry<String, Object> entry : record.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                fields.add(key);
                values.add("'" + (value == null ? "" : value.toString()) + "'");
            }
            sql.append("(" + String.join(",", fields) + ")");
            sql.append(" values ");
            sql.append("(" + String.join(",", values) + ")");
            log.info("dynamicSQL : "+sql.toString());

            DynamicDBUtil.update(tableVo.getDsCode(), sql.toString());
            return Result.ok("Add a success!");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("Add failure!"+e.getLocalizedMessage());
        }
    }

    /**
     *
     * @return
     */
    @PostMapping(value = "/edit")
    public Result<?> edit(@RequestBody JSONObject formData) {
        try {
            JSONObject table = formData.getJSONObject("table");
            MetadataTablesVO tableVo = JSONObject.toJavaObject(table, MetadataTablesVO.class);
            JSONObject record = formData.getJSONObject("record");
            JSONArray pkFields = formData.getJSONArray("pkFields");
            StringBuilder sql = new StringBuilder();
            sql.append(" update ");
            sql.append("`" + tableVo.getDbName() + "`." + tableVo.getTableName());
            sql.append(" set ");
            List<String> fields = new ArrayList<>();
            List<String> conditions = new ArrayList<>();
            if (pkFields == null || pkFields.size() == 0) {
                return Result.error("The current table is not set primary key, do not support editing operation!");
            } else {
                for (Map.Entry<String, Object> entry : record.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    fields.add(key + "='" + (value == null ? "" : value.toString()) + "' ");
                }
                pkFields.stream().forEach(pk -> {
                    String field = String.valueOf(pk);
                    conditions.add(field + "='" + (record.getString(field) == null ? "" : record.getString(field)) + "' ");
                });
            }
            sql.append(String.join(",", fields));
            sql.append(" where ");
            sql.append(String.join(" and ", conditions));
            log.info("dynamicSQL : "+sql.toString());

            DynamicDBUtil.update(tableVo.getDsCode(), sql.toString());
            return Result.ok("successful!");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("Exception!"+e.getLocalizedMessage());
        }
    }

    /**
     *
     * @return
     */
    @PostMapping(value = "/delete")
    public Result<?> delete(@RequestBody JSONObject formData) {
        try{
            JSONObject table = formData.getJSONObject("table");
            MetadataTablesVO tableVo = JSONObject.toJavaObject(table, MetadataTablesVO.class);
            JSONObject record = formData.getJSONObject("record");
            JSONArray pkFields = formData.getJSONArray("pkFields");
            StringBuilder sql = new StringBuilder();
            sql.append(" delete from ");
            sql.append("`" + tableVo.getDbName() + "`." + tableVo.getTableName());
            sql.append(" where ");
            List<String> conditions = new ArrayList<>();
            if (pkFields == null || pkFields.size() == 0) {
                return Result.error("The current table does not have a primary key and cannot be deleted!");
            } else {
                pkFields.stream().forEach(pk -> {
                    String field = String.valueOf(pk);
                    conditions.add(field + "='" + (record.getString(field) == null ? "" : record.getString(field)) + "' ");
                });
            }
            sql.append(String.join(" and ", conditions));
            log.info("dynamicSQL : "+sql.toString());

            DynamicDBUtil.update(tableVo.getDsCode(), sql.toString());
            return Result.ok("successful!");
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("Exception!"+e.getLocalizedMessage());
        }
    }

    /**
     *
     * @return
     */
    @PostMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestBody JSONObject formData) {
        System.out.println(formData.toString());
        return Result.ok("Succeeded in batch deleting！");
    }
}

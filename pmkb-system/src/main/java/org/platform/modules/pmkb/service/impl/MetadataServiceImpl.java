package org.platform.modules.pmkb.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.common.util.dynamic.db.DynamicDBUtil;
import org.platform.modules.pmkb.entity.MetadataFields;
import org.platform.modules.pmkb.entity.MetadataTables;
import org.platform.modules.pmkb.mapper.*;
import org.platform.modules.pmkb.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MetadataServiceImpl implements IMetadataService {
    @Autowired
    MetadataTablesMapper tablesMapper;
    @Autowired
    IMetadataTablesService tablesService;

    @Autowired
    MetadataTablesExtMapper tablesExtMapper;
    @Autowired
    IMetadataTablesExtService tablesExtService;

    @Autowired
    MetadataFieldsMapper fieldsMapper;
    @Autowired
    IMetadataFieldsService fieldsService;

    @Autowired
    MetadataFieldsExtMapper fieldsExtMapper;
    @Autowired
    IMetadataFieldsExtService fieldsExtService;

    @Override
    public List<String> showDatabases(String dsCode) {
        String sql = " show databases";
        log.info("dynamic datasource sql:{}",sql);
        List<String> list = DynamicDBUtil.findList(dsCode, sql, String.class);
        return list;
    }

    @Override
    public List<Map<String, Object>> showTables(String dsCode, List<String> schemaList) {
        StringBuilder sql = new StringBuilder();
        sql.append(" select concat(table_schema,'.',table_name) as `key`,table_schema as dbName,table_name as tableName,table_comment as tableComment from information_schema.tables ");
        if (schemaList != null && schemaList.size() > 0) {
            sql.append(" where table_schema in ('");
            sql.append(StringUtils.join(schemaList, "','"));
            sql.append("') ");
        }
        sql.append(" order by table_schema,table_name ");
        log.info("dynamic datasource sql:{}",sql.toString());
        List<Map<String, Object>> list = DynamicDBUtil.findList(dsCode, sql.toString());
        return list;
    }

    @Override
    public List<Map<String, Object>> showFields(String dsCode, String dbName,String tableName) {
        StringBuilder sql = new StringBuilder();
        sql.append(" select column_name,column_comment from information_schema.COLUMNS where 1=1 ");
        if (StrUtil.isNotBlank(dbName)) {
            sql.append(" and table_schema ='");
            sql.append(dbName);
            sql.append("' ");
        }
        if (StrUtil.isNotBlank(tableName)) {
            sql.append(" and table_name ='");
            sql.append(tableName);
            sql.append("' ");
        }
        sql.append(" order by column_name ");
        log.info("dynamic datasource sql:{}",sql.toString());
        List<Map<String, Object>> list = DynamicDBUtil.findList(dsCode, sql.toString());
        return list;
    }

    @Override
    public void extractMetadata(String dsCode, List<String> schemaList, List<String> tableList) throws Exception {
        QueryWrapper delWrapper = new QueryWrapper();
        delWrapper.eq("ds_code", dsCode);
        delWrapper.in("db_name", schemaList);
        QueryWrapper delTablesExtWrapper = new QueryWrapper();
        delTablesExtWrapper.eq("ds_code", dsCode);
        delTablesExtWrapper.in("db_name", schemaList);
        QueryWrapper delFieldsExtWrapper = new QueryWrapper();
        delFieldsExtWrapper.eq("ds_code", dsCode);
        delFieldsExtWrapper.in("db_name", schemaList);

        StringBuilder tableSql = new StringBuilder();
        tableSql.append(" select '"+dsCode+"' as dsCode,t.table_schema as dbName,t.table_name as tableName,t.table_comment as tableComment ");
        tableSql.append(" from information_schema.TABLES t ");
        tableSql.append(" where t.table_schema in ('");
        tableSql.append(StringUtils.join(schemaList, "','"));
        tableSql.append("') ");

        StringBuilder columnSql = new StringBuilder();
        columnSql.append(" select '"+dsCode+"' as dsCode,t.table_schema as dbName,t.table_name as tableName, ");
        columnSql.append(" t.column_name as fieldName,t.column_type as fieldTypeFull,t.data_type as fieldType, ");
//        columnSql.append(" (case when t.character_maximum_length is null then t.numeric_precision else t.character_maximum_length end) as fieldLength, ");
        columnSql.append(" t.character_maximum_length as fieldLength, ");
        columnSql.append(" t.numeric_scale as fieldDigit,t.column_comment as fieldComment, ");
        columnSql.append(" (case when t.is_nullable = 'YES' then '1' else '0' end) as isNullable, ");
        columnSql.append(" (case when k.constraint_name = 'PRIMARY' then '1' else '0' end) as isPk, ");
        columnSql.append(" (case when s.non_unique = 0 then '1' else '0' end) as isUnique, ");
        columnSql.append(" t.column_default as defaultValue, ");
        columnSql.append(" k.referenced_table_schema as fkDbName,k.referenced_table_name as fkTableName,k.referenced_column_name as fkFieldName ");
        columnSql.append(" from information_schema.COLUMNS t left join information_schema.key_column_usage k  ");
        columnSql.append(" on t.table_schema=k.table_schema and t.table_name=k.table_name and t.column_name=k.column_name ");
        columnSql.append(" left join information_schema.statistics s ");
        columnSql.append(" on t.table_schema=s.table_schema and t.table_name=s.table_name and t.column_name=s.column_name ");
        columnSql.append(" where t.table_schema in ('");
        columnSql.append(StringUtils.join(schemaList, "','"));
        columnSql.append("') ");

        if (tableList != null && tableList.size() > 0) {
            tableSql.append(" and t.table_name in ('");
            tableSql.append(StringUtils.join(tableList, "','"));
            tableSql.append("') ");

            columnSql.append(" and t.table_name in ('");
            columnSql.append(StringUtils.join(tableList, "','"));
            columnSql.append("') ");

            delWrapper.in("table_name", tableList);
            delTablesExtWrapper.in("table_name", tableList);
            delFieldsExtWrapper.in("table_name", tableList);
        }
        columnSql.append(" order by t.table_schema,t.table_name,t.ORDINAL_POSITION ");

        log.info("dynamic datasource sql:{}",tableSql.toString());
        List<MetadataTables> tablesList = DynamicDBUtil.findListEntities(dsCode, tableSql.toString(), MetadataTables.class);
        log.info("dynamic datasource sql:{}",columnSql.toString());
        List<MetadataFields> fieldsList = DynamicDBUtil.findListEntities(dsCode, columnSql.toString(), MetadataFields.class);


        tablesMapper.delete(delWrapper);
        tablesService.saveBatch(tablesList);
        delTablesExtWrapper.notExists("SELECT table_name FROM pmkb_metadata_tables t WHERE t.ds_code=pmkb_metadata_tables_ext.ds_code and t.db_name=pmkb_metadata_tables_ext.db_name and t.table_name=pmkb_metadata_tables_ext.table_name");
        tablesExtMapper.delete(delTablesExtWrapper);

        fieldsMapper.delete(delWrapper);
        fieldsService.saveBatch(fieldsList);
        delFieldsExtWrapper.notExists("SELECT table_name FROM pmkb_metadata_fields t WHERE t.ds_code=pmkb_metadata_fields_ext.ds_code and t.db_name=pmkb_metadata_fields_ext.db_name and t.table_name=pmkb_metadata_fields_ext.table_name");
        fieldsExtMapper.delete(delFieldsExtWrapper);

    }


    @Override
    public List<Map<String, Object>> showTablesByText(String dsCode, String dbCode,String text) {
        StringBuilder sql = new StringBuilder();
        sql.append(" select concat(table_schema,'.',table_name) as `key`,table_schema as dbName,table_name as tableName,table_comment as tableComment from information_schema.tables ");
        sql.append(" where table_schema ='");
        sql.append(dbCode);
        sql.append("' ");
        if(StrUtil.isNotBlank(text)){
            sql.append(" and (table_name like '%"+text+"%' or table_comment like '%"+text+"%' )");
        }
        sql.append(" order by table_schema,table_name ");
        log.info("dynamic datasource sql:{}",sql.toString());
        List<Map<String, Object>> list = DynamicDBUtil.findList(dsCode, sql.toString());
        return list;
    }

    @Override
    public List<MetadataFields> showFieldsForCanvas(String dsCode, String dbCode, String tableName) {
        StringBuilder columnSql = new StringBuilder();
        columnSql.append(" select '"+dsCode+"' as dsCode,t.table_schema as dbName,t.table_name as tableName, ");
        columnSql.append(" t.column_name as fieldName,t.column_type as fieldTypeFull,t.data_type as fieldType, ");
        columnSql.append(" (case when t.character_maximum_length is null then t.numeric_precision else t.character_maximum_length end) as fieldLength, ");
        columnSql.append(" t.numeric_scale as fieldDigit,t.column_comment as fieldComment, ");
        columnSql.append(" (case when t.is_nullable = 'YES' then '1' else '0' end) as isNullable, ");
        columnSql.append(" (case when k.constraint_name = 'PRIMARY' then '1' else '0' end) as isPk, ");
        columnSql.append(" (case when s.non_unique = 0 then '1' else '0' end) as isUnique, ");
        columnSql.append(" t.column_default as defaultValue, ");
        columnSql.append(" k.referenced_table_schema as fkDbName,k.referenced_table_name as fkTableName,k.referenced_column_name as fkFieldName,'0' as dataState ");
        columnSql.append(" from information_schema.COLUMNS t left join information_schema.key_column_usage k  ");
        columnSql.append(" on t.table_schema=k.table_schema and t.table_name=k.table_name and t.column_name=k.column_name ");
        columnSql.append(" left join information_schema.statistics s ");
        columnSql.append(" on t.table_schema=s.table_schema and t.table_name=s.table_name and t.column_name=s.column_name ");
        columnSql.append(" where t.table_schema ='" + dbCode +"' and t.table_name='"+tableName+"' ");
        columnSql.append(" order by t.table_schema,t.table_name,t.ORDINAL_POSITION ");
        List<MetadataFields> fieldsList = DynamicDBUtil.findListEntities(dsCode, columnSql.toString(), MetadataFields.class);
        return fieldsList;
    }

}

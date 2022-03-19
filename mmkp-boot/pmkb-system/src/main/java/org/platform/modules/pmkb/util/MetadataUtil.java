package org.platform.modules.pmkb.util;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import org.platform.modules.pmkb.entity.MetadataFields;
import org.platform.modules.pmkb.entity.MetadataTables;
import org.platform.modules.pmkb.entity.StageData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MetadataUtil {
    public final static String INPUT = "input";
    public final static String TEXTAREA = "a-textarea";
    public final static String INPUT_NUMBER = "a-input-number";
    public final static String SELECT = "select";
    public final static String DATE = "a-date-picker";
    public final static String TIME = "a-time-picker";
    public final static String SEARCH = "a-input-search";


    public static String compSelector(String dataType){
        if(dataType.indexOf("text")>-1 || dataType.indexOf("blob")>-1){
            return TEXTAREA;
        }else if(dataType.indexOf("int")>-1 || dataType.indexOf("float")>-1 || dataType.indexOf("double")>-1
                || dataType.indexOf("num")>-1  || dataType.indexOf("decimal")>-1 ){
            return INPUT_NUMBER;
        }else if(dataType.indexOf("time")>-1){
            return TIME;
        }else if(dataType.indexOf("date")>-1){
            return DATE;
        }else {
            return INPUT;
        }
    }


    public static String genCreateSql(MetadataTables table, List<MetadataFields> fieldsList){
        StringBuilder sb = new StringBuilder();
        StringBuilder field = new StringBuilder();
        StringBuilder pk = new StringBuilder();
        StringBuilder fk = new StringBuilder();
        sb.append("CREATE TABLE ");
        sb.append("`" + table.getDbName() + "`.");
        sb.append(table.getTableName() + "(");
        for (int i=0;i<fieldsList.size();i++){
            MetadataFields f = fieldsList.get(i);
            field.append("`"+f.getFieldName() + "` " + f.getFieldTypeFull() + " ");
            if("1".equals(f.getIsPk()) || "0".equals(f.getIsNullable())){
                field.append(" NOT NULL ");
                if("1".equals(f.getIsPk()) && "dpkid".equals(f.getFieldName()) && "int".equals(f.getFieldTypeFull())){
                    field.append(" AUTO_INCREMENT ");
                }
            }else{
                field.append(" NULL ");
            }
            if (StrUtil.isNotBlank(f.getFieldComment())) {
                field.append(" COMMENT '" + f.getFieldComment() + "'");
            }
            if("1".equals(f.getIsPk())){
                pk.append("`"+f.getFieldName()+"`,");
            }
            /*if (StrUtil.isNotBlank(f.getFkDbName()) && StrUtil.isNotBlank(f.getFkTableName()) && StrUtil.isNotBlank(f.getFkTableName())) {
                fk.append(" CONSTRAINT `" + table.getTableName() + "_fk_" + i + "` FOREIGN KEY (`"+f.getFieldName()+"`)" );
                fk.append(" REFERENCES `" + f.getFkDbName() + "`.`" + f.getFkTableName() + "` (`" + f.getFkFieldName() + "`),");
            }*/
            field.append(" , ");
        }

        if (StrUtil.isNotBlank(pk)) {
            field.append("PRIMARY KEY (");
            field.append(pk.substring(0, pk.length() - 1));
            field.append("),");
        }
        if(StrUtil.isNotBlank(fk)){
            field.append(fk);
        }

        sb.append(field.substring(0,field.length()-1));
        sb.append(" )");
        if(StrUtil.isNotBlank(table.getTableComment())){
            sb.append(" COMMENT = '" + table.getTableComment() + "'");
        }

        return sb.toString();
    }

    public static List<String> genInsertSql(List<StageData> list){
        List<String> sqlList = new ArrayList<>();
        for (StageData d: list){
            StringBuilder sb = new StringBuilder();
            if("1".equals(d.getDataState())){
                sb.append(" insert into ");
                sb.append("`" + d.getDbName() + "`.`" + d.getTableName() + "`");
                sb.append("(");
                StringBuilder colums = new StringBuilder();
                StringBuilder values = new StringBuilder();
                JSONObject jsonObject = JSONObject.parseObject(d.getDataJsonStr());
                Set<String> keys = jsonObject.keySet();
                for(String k:keys){
                    colums.append("`" + k + "`,");
                    values.append("'" + jsonObject.getString(k) + "',");
                }
                sb.append(colums.substring(0, colums.length() - 1));
                sb.append(") VALUES (");
                sb.append(values.substring(0, values.length() - 1));
                sb.append(")");
            }else if("2".equals(d.getDataState())){
                sb.append(" update ");
                sb.append("`" + d.getDbName() + "`.`" + d.getTableName() + "`");
                sb.append(" set ");
                StringBuilder setStr = new StringBuilder();
                JSONObject jsonObject = JSONObject.parseObject(d.getDataJsonStr());
                Set<String> keys = jsonObject.keySet();
                for(String k:keys){
                    setStr.append(" `" + k + "`='"+jsonObject.getString(k)+"',");
                }
                sb.append(setStr.substring(0, setStr.length() - 1));
                sb.append(" where ");
                sb.append(d.getPkFieldName());
                sb.append("=");
                sb.append("'" + d.getPkFieldValue() + "' ");
            }
            sqlList.add(sb.toString());
        }
        return sqlList;
    }
}

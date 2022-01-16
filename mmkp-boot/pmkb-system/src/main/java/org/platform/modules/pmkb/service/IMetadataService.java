package org.platform.modules.pmkb.service;


import org.platform.modules.pmkb.entity.MetadataFields;

import java.util.List;
import java.util.Map;

public interface IMetadataService {

    List<String> showDatabases(String dsCode);

    List<Map<String,Object>> showTables(String dsCode, List<String> schemaList);

    List<Map<String,Object>> showFields(String dsCode, String dbName,String tableName);

    void extractMetadata(String dsCode, List<String> schemaList,List<String> tableList) throws Exception;


    List<Map<String,Object>> showTablesByText(String dsCode, String dbCode,String text);

    List<MetadataFields> showFieldsForCanvas(String dsCode, String dbCode, String tableName);
}

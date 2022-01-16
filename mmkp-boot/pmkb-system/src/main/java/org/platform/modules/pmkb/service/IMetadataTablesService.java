package org.platform.modules.pmkb.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.platform.modules.pmkb.entity.MetadataTables;
import org.platform.modules.pmkb.vo.MetadataTablesVO;

import java.util.List;
import java.util.Map;

public interface IMetadataTablesService extends IService<MetadataTables> {

    IPage<MetadataTablesVO> pageTables(Page<MetadataTablesVO> page, Map<String, Object> paramsMap);

    MetadataTablesVO getMetadataTablesVO(MetadataTables po);

    List<MetadataTablesVO> listTables(Map<String, Object> paramsMap);
}

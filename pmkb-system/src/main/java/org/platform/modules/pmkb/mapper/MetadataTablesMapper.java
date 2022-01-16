package org.platform.modules.pmkb.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.platform.modules.pmkb.entity.MetadataTables;
import org.platform.modules.pmkb.vo.MetadataTablesVO;

import java.util.List;
import java.util.Map;

public interface MetadataTablesMapper extends BaseMapper<MetadataTables> {

    IPage<MetadataTablesVO> pageTables(IPage<MetadataTablesVO> page, @Param("map") Map<String, Object> paramsMap);

    MetadataTablesVO getMetadataTablesVO(@Param("po") MetadataTables po);

    List<MetadataTablesVO> listTables(@Param("map") Map<String, Object> paramsMap);
}
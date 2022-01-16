package org.platform.modules.pmkb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.platform.modules.pmkb.entity.MetadataFields;
import org.platform.modules.pmkb.entity.MetadataTables;
import org.platform.modules.pmkb.vo.MetadataFieldsVO;
import org.platform.modules.pmkb.vo.MetadataTablesVO;

import java.util.List;

public interface MetadataFieldsMapper extends BaseMapper<MetadataFields> {

    List<MetadataFieldsVO> listMetadataFieldsVO(@Param("po") MetadataTables po);
}
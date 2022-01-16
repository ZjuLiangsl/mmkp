package org.platform.modules.pmkb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.platform.modules.pmkb.entity.MetadataFields;
import org.platform.modules.pmkb.entity.MetadataTables;
import org.platform.modules.pmkb.vo.MetadataFieldsVO;
import org.platform.modules.pmkb.vo.MetadataTablesVO;

import java.util.List;

public interface IMetadataFieldsService extends IService<MetadataFields> {

    public List<MetadataFieldsVO> listMetadataFieldsVO(MetadataTables po);
}

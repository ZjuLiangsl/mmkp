package org.platform.modules.pmkb.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.platform.modules.pmkb.entity.MetadataFields;
import org.platform.modules.pmkb.entity.MetadataTables;
import org.platform.modules.pmkb.mapper.MetadataFieldsMapper;
import org.platform.modules.pmkb.service.IMetadataFieldsService;
import org.platform.modules.pmkb.vo.MetadataFieldsVO;
import org.platform.modules.pmkb.vo.MetadataTablesVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetadataFieldsServiceImpl extends ServiceImpl<MetadataFieldsMapper, MetadataFields> implements IMetadataFieldsService {
    @Autowired
    MetadataFieldsMapper fieldsMapper;
    @Override
    public List<MetadataFieldsVO> listMetadataFieldsVO(MetadataTables po) {
        return fieldsMapper.listMetadataFieldsVO(po);
    }
}

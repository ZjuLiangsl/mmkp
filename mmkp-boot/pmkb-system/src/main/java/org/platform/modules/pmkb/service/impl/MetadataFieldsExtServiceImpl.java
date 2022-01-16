package org.platform.modules.pmkb.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.platform.modules.pmkb.entity.MetadataFieldsExt;
import org.platform.modules.pmkb.mapper.MetadataFieldsExtMapper;
import org.platform.modules.pmkb.service.IMetadataFieldsExtService;
import org.springframework.stereotype.Service;

@Service
public class MetadataFieldsExtServiceImpl extends ServiceImpl<MetadataFieldsExtMapper, MetadataFieldsExt> implements IMetadataFieldsExtService {
}

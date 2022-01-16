package org.platform.modules.pmkb.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.platform.modules.pmkb.entity.ReferenceRelation;
import org.platform.modules.pmkb.entity.StageData;
import org.platform.modules.pmkb.mapper.ReferenceRelationMapper;
import org.platform.modules.pmkb.mapper.StageDataMapper;
import org.platform.modules.pmkb.service.IReferenceRelationService;
import org.platform.modules.pmkb.service.IStageDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReferenceReleationServiceImpl extends ServiceImpl<ReferenceRelationMapper, ReferenceRelation> implements IReferenceRelationService {

    @Autowired
    ReferenceRelationMapper mapper;

    @Override
    public List<ReferenceRelation> listByParam(String dsCode, String dbName, String tableName) {
        return mapper.listByParam(dsCode,dbName,tableName);
    }
}

package org.platform.modules.pmkb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.platform.modules.pmkb.entity.ReferenceRelation;
import org.platform.modules.pmkb.entity.StageData;

import java.util.List;

public interface ReferenceRelationMapper extends BaseMapper<ReferenceRelation> {


    List<ReferenceRelation> listByParam(String dsCode, String dbName, String tableName);
}
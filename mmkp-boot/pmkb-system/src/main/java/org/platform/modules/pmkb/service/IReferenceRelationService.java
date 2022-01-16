package org.platform.modules.pmkb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.platform.modules.pmkb.entity.ReferenceRelation;

import java.util.List;

public interface IReferenceRelationService extends IService<ReferenceRelation> {

    List<ReferenceRelation> listByParam(String dsCode, String dbName, String tableName);
}

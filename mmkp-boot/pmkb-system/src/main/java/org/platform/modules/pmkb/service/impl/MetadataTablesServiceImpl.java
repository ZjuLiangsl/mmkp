package org.platform.modules.pmkb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.platform.modules.pmkb.entity.MetadataTables;
import org.platform.modules.pmkb.mapper.MetadataTablesMapper;
import org.platform.modules.pmkb.service.IMetadataTablesService;
import org.platform.modules.pmkb.vo.MetadataTablesVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MetadataTablesServiceImpl extends ServiceImpl<MetadataTablesMapper, MetadataTables> implements IMetadataTablesService {

    @Autowired
    MetadataTablesMapper tablesMapper;

    @Override
    public IPage<MetadataTablesVO> pageTables(Page<MetadataTablesVO> page, Map<String, Object> paramsMap) {
        return tablesMapper.pageTables(page,paramsMap);
    }

    @Override
    public MetadataTablesVO getMetadataTablesVO(MetadataTables po) {
        if(po==null){
            return new MetadataTablesVO();
        }
        return tablesMapper.getMetadataTablesVO(po);
    }

    @Override
    public List<MetadataTablesVO> listTables(Map<String, Object> paramsMap) {
        return tablesMapper.listTables(paramsMap);
    }

}

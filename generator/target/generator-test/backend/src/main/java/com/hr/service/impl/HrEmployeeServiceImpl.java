package com.hr.hr.service.impl;

import com.hr.hr.entity.HrEmployee;
import com.hr.hr.mapper.HrEmployeeMapper;
import com.hr.hr.service.IHrEmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 员工 Service 实现
 */
@Service
public class HrEmployeeServiceImpl implements IHrEmployeeService {

    @Autowired
    private HrEmployeeMapper hrEmployeeMapper;

    @Override
    public List<HrEmployee> selectList(HrEmployee query) {
        return hrEmployeeMapper.selectList(query);
    }

    @Override
    public HrEmployee selectById(Long id) {
        return hrEmployeeMapper.selectById(id);
    }

    @Override
    public int insert(HrEmployee record) {
        return hrEmployeeMapper.insert(record);
    }

    @Override
    public int update(HrEmployee record) {
        return hrEmployeeMapper.update(record);
    }

    @Override
    public int deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return hrEmployeeMapper.deleteByIds(ids);
    }
}

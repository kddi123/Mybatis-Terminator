package com.hr.hr.service;

import com.hr.hr.entity.HrEmployee;
import java.util.List;

/**
 * 员工 Service
 */
public interface IHrEmployeeService {

    List<HrEmployee> selectList(HrEmployee query);

    HrEmployee selectById(Long id);

    int insert(HrEmployee record);

    int update(HrEmployee record);

    int deleteByIds(List<Long> ids);
}

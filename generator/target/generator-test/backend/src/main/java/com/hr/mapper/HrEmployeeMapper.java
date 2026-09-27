package com.hr.hr.mapper;

import com.hr.hr.entity.HrEmployee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 员工 Mapper
 */
@Mapper
public interface HrEmployeeMapper {

    List<HrEmployee> selectList(HrEmployee query);

    HrEmployee selectById(Long id);

    int insert(HrEmployee record);

    int update(HrEmployee record);

    int deleteByIds(@Param("ids") List<Long> ids);
}

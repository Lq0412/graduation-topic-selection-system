package cn.com.edtechhub.worktopicselection.service;

import cn.com.edtechhub.worktopicselection.model.dto.dept.DeptQueryRequest;
import cn.com.edtechhub.worktopicselection.model.entity.Dept;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Administrator
* @description 针对表【dept(系部)】的数据库操作Service
* @createDate 2024-05-22 10:27:28
*/
public interface DeptService extends IService<Dept> {

    // 获取查询条件
    QueryWrapper<Dept> getQueryWrapper(DeptQueryRequest deptQueryRequest);

}

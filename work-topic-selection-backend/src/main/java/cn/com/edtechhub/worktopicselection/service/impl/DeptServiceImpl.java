package cn.com.edtechhub.worktopicselection.service.impl;

import cn.com.edtechhub.worktopicselection.constant.CommonConstant;
import cn.com.edtechhub.worktopicselection.mapper.DeptMapper;
import cn.com.edtechhub.worktopicselection.model.dto.dept.DeptQueryRequest;
import cn.com.edtechhub.worktopicselection.model.entity.Dept;
import cn.com.edtechhub.worktopicselection.service.DeptService;
import cn.com.edtechhub.worktopicselection.utils.SqlUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author Administrator
* @description 针对表【dept(系部)】的数据库操作Service实现
* @createDate 2024-05-22 10:27:28
*/
@Service
@Transactional
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept>
    implements DeptService{

    @Override
    public QueryWrapper<Dept> getQueryWrapper(DeptQueryRequest deptQueryRequest) {
        String sortField = deptQueryRequest.getSortField();
        String sortOrder = deptQueryRequest.getSortOrder();
        QueryWrapper<Dept> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(deptQueryRequest.getDeptName()), "deptName", deptQueryRequest.getDeptName());
        queryWrapper.orderBy(SqlUtils.validDeptSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}




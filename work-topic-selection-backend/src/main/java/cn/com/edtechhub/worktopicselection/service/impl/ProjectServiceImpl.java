package cn.com.edtechhub.worktopicselection.service.impl;

import cn.com.edtechhub.worktopicselection.constant.CommonConstant;
import cn.com.edtechhub.worktopicselection.exception.BusinessException;
import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import cn.com.edtechhub.worktopicselection.mapper.ProjectMapper;
import cn.com.edtechhub.worktopicselection.model.dto.project.ProjectQueryRequest;
import cn.com.edtechhub.worktopicselection.model.entity.Project;
import cn.com.edtechhub.worktopicselection.service.ProjectService;
import cn.com.edtechhub.worktopicselection.utils.SqlUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Administrator
 * @description 针对表【project(系部)】的数据库操作Service实现
 * @createDate 2024-06-12 10:34:21
 */
@Service
@Transactional
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project>
        implements ProjectService {

    /**
     * 获取查询条件
     */
    @Override
    public QueryWrapper<Project> getQueryWrapper(ProjectQueryRequest projectQueryRequest) {
        if (projectQueryRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "请求参数为空");
        }
        String sortField = projectQueryRequest.getSortField();
        String sortOrder = projectQueryRequest.getSortOrder();
        QueryWrapper<Project> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(projectQueryRequest.getProjectName()), "projectName", projectQueryRequest.getProjectName());
        queryWrapper.like(StringUtils.isNotBlank(projectQueryRequest.getDeptName()), "deptName", projectQueryRequest.getDeptName());
        queryWrapper.orderBy(SqlUtils.validProjectSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

}




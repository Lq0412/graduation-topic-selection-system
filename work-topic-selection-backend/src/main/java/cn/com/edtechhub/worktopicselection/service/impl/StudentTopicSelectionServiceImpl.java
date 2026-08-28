package cn.com.edtechhub.worktopicselection.service.impl;

import cn.com.edtechhub.worktopicselection.mapper.StudentTopicSelectionMapper;
import cn.com.edtechhub.worktopicselection.model.entity.StudentTopicSelection;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.com.edtechhub.worktopicselection.service.StudentTopicSelectionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author Administrator
* @description 针对表【student_topic_selection(学生选题)】的数据库操作Service实现
* @createDate 2024-05-22 15:21:19
*/
@Service
@Transactional
public class StudentTopicSelectionServiceImpl extends ServiceImpl<StudentTopicSelectionMapper, StudentTopicSelection>
    implements StudentTopicSelectionService{

}





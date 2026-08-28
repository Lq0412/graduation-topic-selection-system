package cn.com.edtechhub.worktopicselection.mapper;

import cn.com.edtechhub.worktopicselection.model.entity.StudentTopicSelection;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author ljp
* @description 针对表【student_topic_selection(用户选题关联表)】的数据库操作Mapper
* @createDate 2025-09-21 11:24:02
* @Entity cn.com.edtechhub.worktopicselection.model.entity.StudentTopicSelection
*/
public interface StudentTopicSelectionMapper extends BaseMapper<StudentTopicSelection> {

    @Select("SELECT * FROM student_topic_selection " +
            "WHERE userAccount = #{userAccount} AND isDelete = 0 FOR UPDATE")
    List<StudentTopicSelection> selectByUserForUpdate(@Param("userAccount") String userAccount);

    @Select("SELECT * FROM student_topic_selection " +
            "WHERE userAccount = #{userAccount} AND topicId = #{topicId} AND isDelete = 0 FOR UPDATE")
    StudentTopicSelection selectByUserAndTopicForUpdate(
            @Param("userAccount") String userAccount,
            @Param("topicId") Long topicId
    );
}




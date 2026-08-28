package cn.com.edtechhub.worktopicselection.mapper;

import cn.com.edtechhub.worktopicselection.model.entity.Topic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
* @author ljp
* @description 针对表【topic(选题表)】的数据库操作Mapper
* @createDate 2025-09-21 11:24:02
* @Entity cn.com.edtechhub.worktopicselection.model.entity.Topic
*/
public interface TopicMapper extends BaseMapper<Topic> {

    /**
     * 锁定题目行，保证余量检查与扣减处于同一数据库事务。
     */
    @Select("SELECT * FROM topic WHERE id = #{id} AND isDelete = 0 FOR UPDATE")
    Topic selectByIdForUpdate(@Param("id") Long id);
}




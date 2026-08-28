package cn.com.edtechhub.worktopicselection.mapper;

import cn.com.edtechhub.worktopicselection.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
* @author ljp
* @description 针对表【user(用户表)】的数据库操作Mapper
* @createDate 2025-09-21 11:24:02
* @Entity cn.com.edtechhub.worktopicselection.model.entity.User
*/
public interface UserMapper extends BaseMapper<User> {

    /**
     * 在选题事务中锁定学生账号，串行化同一学生的最终选题操作。
     */
    @Select("SELECT * FROM `user` WHERE id = #{id} AND isDelete = 0 FOR UPDATE")
    User selectByIdForUpdate(@Param("id") Long id);
}




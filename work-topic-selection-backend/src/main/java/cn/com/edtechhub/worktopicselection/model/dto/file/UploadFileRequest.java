package cn.com.edtechhub.worktopicselection.model.dto.file;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传请求
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class UploadFileRequest implements Serializable {

    private MultipartFile file;

    // 这个字段其实是角色字段, 但是我懒得修改了前端...
    private Integer status;

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
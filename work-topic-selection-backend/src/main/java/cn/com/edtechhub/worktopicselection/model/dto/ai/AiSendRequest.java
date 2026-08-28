package cn.com.edtechhub.worktopicselection.model.dto.ai;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * AI 问答请求
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class AiSendRequest implements Serializable {

    /**
     * 消息内容
     */
    private String content;

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}

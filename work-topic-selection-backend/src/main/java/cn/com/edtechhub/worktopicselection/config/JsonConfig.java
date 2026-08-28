package cn.com.edtechhub.worktopicselection.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.annotation.PostConstruct;

/**
 * Spring MVC Json 配置
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@JsonComponent
@Slf4j
public class JsonConfig {

    /**
     * 添加 Long 转 json 精度丢失的配置, 就无需为每个实体类添加 @JsonSerialize(using = ToStringSerializer.class) 来避免 id 过大前端出错
     */
    @Bean
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(module);
        return objectMapper;
    }

    /**
     * 打印配置
     */
    @PostConstruct
    public void printConfig() {
        log.debug("[JsonConfig] 当前项目自动解决了 Long 在前后端传递过程中的精度问题");
    }

}

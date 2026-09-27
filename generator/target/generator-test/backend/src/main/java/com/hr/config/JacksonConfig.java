package com.hr.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Jackson 序列化配置：Long / BigDecimal → JSON 字符串。
 *
 * 背景：MySQL bigint 范围到 2^63-1，而 JS Number 只能精确表示到 2^53-1
 * （9007199254740991）。后端若以 JSON 数字返回 Long（如雪花 ID），
 * 前端 JSON.parse 会静默丢失精度。因此本项目的类型契约为：
 *
 *   数据库 bigint  →  Java Long  →  JSON "123..."（字符串）  →  TS string
 *   数据库 decimal →  BigDecimal →  JSON "12.34"（字符串）   →  TS string
 *
 * 本配置保证后端实际输出与前端 TS 类型声明在运行时一致。
 * 反向无影响：前端提交字符串 "5"，Jackson 会自动强转为 Long 字段。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> builder
                .serializerByType(Long.class, ToStringSerializer.instance)
                .serializerByType(Long.TYPE, ToStringSerializer.instance)
                .serializerByType(BigDecimal.class, ToStringSerializer.instance);
    }
}

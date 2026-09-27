<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${packageName}.mapper.${className}Mapper">

    <resultMap id="BaseResultMap" type="${packageName}.entity.${className}">
<#list columns as column>
        <result property="${column.fieldName}" column="${column.name}"/>
</#list>
    </resultMap>

    <sql id="BaseColumns">
<#list columns as column>${column.name}<#if column?has_next>,</#if>
</#list>
    </sql>

    <!-- 复合主键 where 片段 -->
    <sql id="KeyWhere">
        where<#list primaryKeys as pk> ${pk.name} = <#noparse>#{</#noparse>key.${pk.fieldName},jdbcType=${pk.jdbcType}}<#if pk?has_next> and</#if></#list>
    </sql>

    <select id="selectList" parameterType="${packageName}.entity.${className}" resultMap="BaseResultMap">
        select <include refid="BaseColumns"/> from ${tableName}
        <where>
<#list listColumns as column>
            <if test="${column.fieldName} != null<#if column.javaType == "String"> and ${column.fieldName} != ''</#if>">
                and ${column.name} = <#noparse>#{</#noparse>${column.fieldName},jdbcType=${column.jdbcType}}
            </if>
</#list>
        </where>
        order by<#list primaryKeys as pk> ${pk.name}<#if pk?has_next>,</#if></#list>
    </select>

    <select id="selectByKey" resultMap="BaseResultMap">
        select <include refid="BaseColumns"/> from ${tableName}
        <include refid="KeyWhere"/>
    </select>

    <insert id="insert" parameterType="${packageName}.entity.${className}">
        insert into ${tableName}
        <trim prefix="(" suffix=")" suffixOverrides=",">
<#list columns as column>
            <if test="${column.fieldName} != null">${column.name},</if>
</#list>
        </trim>
        <trim prefix="values (" suffix=")" suffixOverrides=",">
<#list columns as column>
            <if test="${column.fieldName} != null"><#noparse>#{</#noparse>${column.fieldName},jdbcType=${column.jdbcType}},</if>
</#list>
        </trim>
    </insert>

    <delete id="deleteByKey" parameterType="${packageName}.entity.${className}">
        delete from ${tableName}
        <include refid="KeyWhere"/>
    </delete>

    <delete id="deleteByCondition" parameterType="${packageName}.entity.${className}">
        delete from ${tableName}
        <where>
<#list listColumns as column>
            <if test="${column.fieldName} != null<#if column.javaType == "String"> and ${column.fieldName} != ''</#if>">
                and ${column.name} = <#noparse>#{</#noparse>${column.fieldName},jdbcType=${column.jdbcType}}
            </if>
</#list>
        </where>
    </delete>
</mapper>

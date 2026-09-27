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

    <select id="selectList" parameterType="${packageName}.entity.${className}" resultMap="BaseResultMap">
        select <include refid="BaseColumns"/> from ${tableName}
        <where>
            deleted = 0
<#list listColumns as column>
            <if test="${column.fieldName} != null<#if column.javaType == "String"> and ${column.fieldName} != ''</#if>">
                and ${column.name} = <#noparse>#{</#noparse>${column.fieldName},jdbcType=${column.jdbcType}}
            </if>
</#list>
        </where>
        order by ${primaryKey.name} desc
    </select>

    <select id="selectById" parameterType="${primaryKey.javaType}" resultMap="BaseResultMap">
        select <include refid="BaseColumns"/> from ${tableName}
        where ${primaryKey.name} = <#noparse>#{</#noparse>id,jdbcType=${primaryKey.jdbcType}} and deleted = 0
    </select>

    <insert id="insert" parameterType="${packageName}.entity.${className}" useGeneratedKeys="true" keyProperty="${primaryKey.fieldName}">
        insert into ${tableName}
        <trim prefix="(" suffix=")" suffixOverrides=",">
<#list editableColumns as column>
            <if test="${column.fieldName} != null">${column.name},</if>
</#list>
        </trim>
        <trim prefix="values (" suffix=")" suffixOverrides=",">
<#list editableColumns as column>
            <if test="${column.fieldName} != null"><#noparse>#{</#noparse>${column.fieldName},jdbcType=${column.jdbcType}},</if>
</#list>
        </trim>
    </insert>

    <update id="update" parameterType="${packageName}.entity.${className}">
        update ${tableName}
        <set>
<#list editableColumns as column>
            <if test="${column.fieldName} != null">${column.name} = <#noparse>#{</#noparse>${column.fieldName},jdbcType=${column.jdbcType}},</if>
</#list>
            updated_at = now()
        </set>
        where ${primaryKey.name} = <#noparse>#{</#noparse>${primaryKey.fieldName},jdbcType=${primaryKey.jdbcType}} and deleted = 0
    </update>

    <update id="deleteByIds">
        update ${tableName} set deleted = 1
        where ${primaryKey.name} in
        <foreach collection="ids" item="id" open="(" separator="," close=")">
            <#noparse>#{</#noparse>id,jdbcType=${primaryKey.jdbcType}}
        </foreach>
    </update>
</mapper>

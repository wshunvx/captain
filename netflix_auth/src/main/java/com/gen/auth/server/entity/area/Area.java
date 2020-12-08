/**
 * Create time 2019-08-04
 * Create by wangkai
 * Copyright © 南京捷鹰数码测绘有限公司 版权所有 All Rights Reserved.
 */
package com.gen.auth.server.entity.area;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gen.auth.server.entity.base.Entity;


/**
 * 区域 数据模型
 *
 * @author wangkai
 * @version V 1.0
 * @since JDK1.8
 */
@Data
@EqualsAndHashCode(callSuper=false)
@TableName(value = "area")
public class Area extends Entity {

    /**
     * 区域名称
     */
    private String name;

    /**
     * 区域编码
     */
    private String zone;

    /**
     * 父节点ID
     */
    @TableField(value = "parent_id")
    private String parentId;


    /**
     * 排序
     */
    private Integer sort;

}

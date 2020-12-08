package com.gen.auth.server.biz;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.gen.auth.server.entity.Uri;
import com.gen.auth.server.mapper.UriMapper;
import com.google.common.collect.Lists;

@Service
@Transactional(rollbackFor = Exception.class)
public class UriBiz extends BaseBiz<UriMapper, Uri> {

    @SuppressWarnings("deprecation")
	public boolean saveOrUpdate(List<Uri> entityList, int batchSize) {
        Assert.notEmpty(entityList, "error: entityList must not be empty");
        Class<?> cls = currentModelClass();
        TableInfo tableInfo = TableInfoHelper.getTableInfo(cls);
        Assert.notNull(tableInfo, "error: can not execute. because can not find cache of TableInfo for entity!");
        String keyProperty = tableInfo.getKeyProperty();
        Assert.notEmpty(keyProperty, "error: can not execute. because can not find column for id from entity!");

        List<Object> updates = new ArrayList<Object>();
        List<String> ids = entityList.stream().map(entity -> ReflectionKit.getMethodValue(cls, entity, keyProperty).toString()).filter(id -> StringUtils.checkValNotNull(id)).collect(Collectors.toList());
        if (ids != null && ids.size() > 0) {
            List<Uri> entitys = mapper.selectBatchIds((Collection<? extends Serializable>) ids);
            if (entitys != null) {
                updates.addAll(entitys.parallelStream().map(entity -> ReflectionKit.getMethodValue(cls, entity, keyProperty)).collect(Collectors.toList()));
            }
        }

        try (SqlSession batchSqlSession = sqlSessionBatch()) {
            Lists.partition(entityList, batchSize).stream().forEach(list -> {
                list.stream().forEach(entity -> {
                    Object idVal = ReflectionKit.getMethodValue(cls, entity, keyProperty);
                    if (StringUtils.checkValNotNull(idVal)) {
                        if (!updates.isEmpty() && updates.contains(idVal)) {
                            MapperMethod.ParamMap<Uri> param = new MapperMethod.ParamMap<>();
                            param.put(Constants.ENTITY, entity);

                            batchSqlSession.update(sqlStatement(SqlMethod.UPDATE_BY_ID), param);
                        } else {
                            batchSqlSession.insert(sqlStatement(SqlMethod.INSERT_ONE), entity);
                        }
                    } else {
                        batchSqlSession.insert(sqlStatement(SqlMethod.INSERT_ONE), entity);
                    }
                });
                batchSqlSession.flushStatements();
            });
        }

        return true;
    }
}

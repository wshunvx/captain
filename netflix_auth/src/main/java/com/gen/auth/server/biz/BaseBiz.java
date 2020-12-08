package com.gen.auth.server.biz;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.MyBatisExceptionTranslator;
import org.mybatis.spring.SqlSessionHolder;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.google.common.collect.Lists;

public abstract class BaseBiz<M extends BaseMapper<T>, T> {
	@Autowired
	protected M mapper;

	/**
	 * 鍒ゆ柇鏁版嵁搴撴搷浣滄槸鍚︽垚鍔�
	 *
	 * @param result 鏁版嵁搴撴搷浣滆繑鍥炲奖鍝嶆潯鏁�
	 * @return boolean
	 */
	protected boolean retBool(Integer result) {
		return SqlHelper.retBool(result);
	}

	@SuppressWarnings("unchecked")
	protected Class<T> currentModelClass() {
		return (Class<T>) ReflectionKit.getSuperClassGenericType(getClass(), 1);
	}

	/**
	 * 鎵归噺鎿嶄綔 SqlSession
	 */
	protected SqlSession sqlSessionBatch() {
		return SqlHelper.sqlSessionBatch(currentModelClass());
	}

	/**
	 * 閲婃斁sqlSession
	 *
	 * @param sqlSession session
	 */
	protected void closeSqlSession(SqlSession sqlSession) {
		SqlSessionUtils.closeSqlSession(sqlSession, GlobalConfigUtils.currentSessionFactory(currentModelClass()));
	}

	/**
	 * 鑾峰彇 SqlStatement
	 *
	 * @param sqlMethod ignore
	 * @return ignore
	 */
	protected String sqlStatement(SqlMethod sqlMethod) {
		return SqlHelper.table(currentModelClass()).getSqlStatement(sqlMethod.getMethod());
	}

	public int save(T entity) {
		return mapper.insert(entity);
	}

	@Transactional(rollbackFor = Exception.class)
	public boolean saveBatch(List<T> entityList, int batchSize) {
		Assert.notEmpty(entityList, "error: entityList must not be empty");
		String sqlStatement = sqlStatement(SqlMethod.INSERT_ONE);
		return executeBatch(entityList, batchSize, (sqlSession, entity) -> sqlSession.insert(sqlStatement, entity));
	}

	@Transactional(rollbackFor = Exception.class)
	public boolean saveOrUpdateBatch(List<T> entityList, int batchSize) {
		Assert.notEmpty(entityList, "error: entityList must not be empty");
		Class<?> cls = currentModelClass();
		TableInfo tableInfo = TableInfoHelper.getTableInfo(cls);
		Assert.notNull(tableInfo, "error: can not execute. because can not find cache of TableInfo for entity!");
		String keyProperty = tableInfo.getKeyProperty();
		Assert.notEmpty(keyProperty, "error: can not execute. because can not find column for id from entity!");

		List<Object> updates = new ArrayList<Object>();
		List<String> ids = entityList.stream()
				.map(entity -> ReflectionKit.getFieldValue(entity, keyProperty).toString())
				.filter(id -> StringUtils.checkValNotNull(id)).collect(Collectors.toList());
		if (ids != null && ids.size() > 0) {
			List<T> entitys = mapper.selectBatchIds((Collection<? extends Serializable>) ids);
			if (entitys != null) {
				updates.addAll(entitys.parallelStream().map(entity -> ReflectionKit.getFieldValue(entity, keyProperty))
						.collect(Collectors.toList()));
			}
		}

		return executeBatch(entityList, batchSize, (sqlSession, entity) -> {
			Object idVal = ReflectionKit.getFieldValue(entity, keyProperty);
			if (StringUtils.checkValNotNull(idVal)) {
				if (!updates.isEmpty() && updates.contains(idVal)) {
					MapperMethod.ParamMap<T> param = new MapperMethod.ParamMap<>();
					param.put(Constants.ENTITY, entity);
					sqlSession.update(sqlStatement(SqlMethod.UPDATE_BY_ID), param);
				} else {
					sqlSession.insert(sqlStatement(SqlMethod.INSERT_ONE), entity);
				}
			} else {
				sqlSession.insert(sqlStatement(SqlMethod.INSERT_ONE), entity);
			}
		});
	}

	public int removeById(Serializable id) {
		return mapper.deleteById(id);
	}

	public int removeByMap(Map<String, Object> columnMap) {
		Assert.notEmpty(columnMap, "error: columnMap must not be empty");
		return mapper.deleteByMap(columnMap);
	}

	public int remove(Wrapper<T> wrapper) {
		return mapper.delete(wrapper);
	}

	public int removeByIds(List<? extends Serializable> idList) {
		Assert.notEmpty(idList, "error: idList must not be empty");
		return mapper.deleteBatchIds(idList);
	}

	public int updateById(T entity) {
		return mapper.updateById(entity);
	}

	public int update(T entity, Wrapper<T> updateWrapper) {
		return mapper.update(entity, updateWrapper);
	}

	@Transactional(rollbackFor = Exception.class)
	public boolean updateBatchById(List<T> entityList, int batchSize) {
		Assert.notEmpty(entityList, "error: entityList must not be empty");
		String sqlStatement = sqlStatement(SqlMethod.UPDATE_BY_ID);
		return executeBatch(entityList, batchSize, (sqlSession, entity) -> {
			MapperMethod.ParamMap<T> param = new MapperMethod.ParamMap<>();
			param.put(Constants.ENTITY, entity);
			sqlSession.update(sqlStatement, param);
		});
	}

	protected <E> boolean executeBatch(List<E> entityList, int batchSize, BiConsumer<SqlSession, E> consumer) {
		Assert.isFalse(batchSize < 1, "batchSize must not be less than one");
		return executeBatch(sqlSession -> {
			Lists.partition(entityList, batchSize).stream().forEach(list -> {
				for (E entity : list) {
					consumer.accept(sqlSession, entity);
				}
				sqlSession.flushStatements();
			});
		});
	}

	@Deprecated
	protected boolean executeBatch(Consumer<SqlSession> consumer) {
		Class<?> entityClass = currentModelClass();
		SqlSessionFactory sqlSessionFactory = SqlHelper.sqlSessionFactory(entityClass);
		SqlSessionHolder sqlSessionHolder = (SqlSessionHolder) TransactionSynchronizationManager
				.getResource(sqlSessionFactory);
		boolean transaction = TransactionSynchronizationManager.isSynchronizationActive();
		if (sqlSessionHolder != null) {
			SqlSession sqlSession = sqlSessionHolder.getSqlSession();
			sqlSession.commit(!transaction);
		}
		SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
		try {
			consumer.accept(sqlSession);
			sqlSession.commit(!transaction);
			return true;
		} catch (Throwable t) {
			sqlSession.rollback();
			Throwable unwrapped = ExceptionUtil.unwrapThrowable(t);
			if (unwrapped instanceof RuntimeException) {
				MyBatisExceptionTranslator myBatisExceptionTranslator = new MyBatisExceptionTranslator(
						sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(), true);
				throw Objects.requireNonNull(
						myBatisExceptionTranslator.translateExceptionIfPossible((RuntimeException) unwrapped));
			}
			throw ExceptionUtils.mpe(unwrapped);
		} finally {
			sqlSession.close();
		}
	}

	public T getById(Serializable id) {
		return mapper.selectById(id);
	}

	public List<T> listByIds(List<? extends Serializable> idList) {
		return mapper.selectBatchIds(idList);
	}

	public List<T> listByMap(Map<String, Object> columnMap) {
		return mapper.selectByMap(columnMap);
	}

	public T getOne(Wrapper<T> query) {
		return mapper.selectOne(query);
	}

	public int count(Wrapper<T> query) {
		return SqlHelper.retCount(mapper.selectCount(query));
	}

	public List<T> list(T query) {
		QueryWrapper<T> wrapper = new QueryWrapper<T>(query);
		return mapper.selectList(wrapper);
	}

	public List<T> list(Wrapper<T> query) {
		return mapper.selectList(query);
	}

	public IPage<T> page(IPage<T> page, Wrapper<T> query) {
		return mapper.selectPage(page, query);
	}

	public List<Map<String, Object>> listMaps(Wrapper<T> query) {
		return mapper.selectMaps(query);
	}

	public IPage<Map<String, Object>> pageMaps(IPage<Map<String, Object>> page, Wrapper<T> query) {
		return mapper.selectMapsPage(page, query);
	}

}

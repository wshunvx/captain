package com.netflix.eureka.found.resources.actuator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.csp.sentinel.util.StringUtil;

import com.netflix.eureka.dashboard.datasource.entity.MetricEntity;
import com.netflix.eureka.dashboard.domain.vo.MetricVo;
import com.netflix.eureka.dashboard.repository.metric.MetricsRepository;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.found.sentinel.SentinelServerContext;
import com.netflix.eureka.found.sentinel.SentinelServerContextHolder;

@Path("/{version}/metric")
@Produces({"application/xml", "application/json"})
public class MetricResource {

    private static Logger logger = LoggerFactory.getLogger(MetricResource.class);

    private static final long maxQueryIntervalMs = 1000 * 60 * 60;

    private MetricsRepository<MetricEntity> repository;

    @Inject
    MetricResource(SentinelServerContext serverContext) {
        this.repository = serverContext.getMetri();
    }

    public MetricResource() {
        this(SentinelServerContextHolder.getSentinel().getServerContext());
    }
    
    @GET
    @Path("queryTopResourceMetric")
    public Restresult<Map<String, Object>> queryTopResourceMetric(@QueryParam("app") String app,
    		@QueryParam("pageIndex") Integer pageIndex, @QueryParam("pageSize") Integer pageSize,
    		@QueryParam("desc") Boolean desc, @QueryParam("startTime") Long startTime, @QueryParam("endTime") Long endTime, 
    		@QueryParam("searchKey") String searchKey) {
        if (StringUtil.isEmpty(app)) {
            return new Restresult<>(-1, "app can't be null or empty");
        }
        if (pageIndex == null || pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize == null) {
            pageSize = 6;
        }
        if (pageSize >= 20) {
            pageSize = 20;
        }
        if (desc == null) {
            desc = true;
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        if (startTime == null) {
            startTime = endTime - 1000 * 60 * 5;
        }
        if (endTime - startTime > maxQueryIntervalMs) {
            return new Restresult<>(-1, "time intervalMs is too big, must <= 1h");
        }
        List<String> resources = repository.listResourcesOfApp(app);
        logger.debug("queryTopResourceMetric(), resources.size()={}", resources.size());

        Map<String, Object> resultMap = new HashMap<>(16);
        if (resources == null || resources.isEmpty()) {
            return new Restresult<>(resultMap);
        }
        if (!desc) {
            Collections.reverse(resources);
        }
        if (StringUtil.isNotEmpty(searchKey)) {
            List<String> searched = new ArrayList<>();
            for (String resource : resources) {
                if (resource.contains(searchKey)) {
                    searched.add(resource);
                }
            }
            resources = searched;
        }
        int totalPage = (resources.size() + pageSize - 1) / pageSize;
        List<String> topResource = new ArrayList<>();
        if (pageIndex <= totalPage) {
            topResource = resources.subList((pageIndex - 1) * pageSize,
                Math.min(pageIndex * pageSize, resources.size()));
        }
        final Map<String, Iterable<MetricVo>> map = new ConcurrentHashMap<>();
        logger.debug("topResource={}", topResource);
        long time = System.currentTimeMillis();
        for (final String resource : topResource) {
            List<MetricEntity> entities = repository.queryByAppAndResourceBetween(
                app, resource, startTime, endTime);
            logger.debug("resource={}, entities.size()={}", resource, entities == null ? "null" : entities.size());
            Iterable<MetricVo> vosSorted = sortMetricVoAndDistinct(entities, resource);
            if(vosSorted != null) {
            	map.put(resource, vosSorted);
            }
        }
        logger.debug("queryTopResourceMetric() total query time={} ms", System.currentTimeMillis() - time);
        
        resultMap.put("totalCount", resources.size());
        resultMap.put("totalPage", totalPage);
        resultMap.put("pageIndex", pageIndex);
        resultMap.put("pageSize", pageSize);

        Map<String, Iterable<MetricVo>> map2 = new LinkedHashMap<>();
        // order matters.
        for (String identity : topResource) {
            map2.put(identity, map.get(identity));
        }
        resultMap.put("metric", map2);
        return new Restresult<>(resultMap);
    }

    @GET
    @Path("queryByAppAndResource")
    public Restresult<Iterable<MetricVo>> queryByAppAndResource(@QueryParam("app") String app, @QueryParam("identity") String identity, 
    		@QueryParam("startTime") Long startTime, @QueryParam("endTime") Long endTime) {
        if (StringUtil.isEmpty(app)) {
            return new Restresult<>(-1, "app can't be null or empty");
        }
        if (StringUtil.isEmpty(identity)) {
            return new Restresult<>(-1, "identity can't be null or empty");
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        if (startTime == null) {
            startTime = endTime - 1000 * 60;
        }
        if (endTime - startTime > maxQueryIntervalMs) {
            return new Restresult<>(-1, "time intervalMs is too big, must <= 1h");
        }
        List<MetricEntity> entities = repository.queryByAppAndResourceBetween(
            app, identity, startTime, endTime);
        return new Restresult<>(sortMetricVoAndDistinct(entities, identity));
    }

    private Iterable<MetricVo> sortMetricVoAndDistinct(List<MetricEntity> entities, String identity) {
        if (entities == null) {
            return null;
        }
        
        Map<Long, MetricVo> map = new TreeMap<>();
        List<MetricVo> vos = MetricVo.fromMetricEntities(entities, identity);
        if(vos != null) {
        	for (MetricVo vo : vos) {
                MetricVo oldVo = map.get(vo.getTimestamp());
                if (oldVo == null || vo.getGmtCreate() > oldVo.getGmtCreate()) {
                    map.put(vo.getTimestamp(), vo);
                }
            }
        }
        return map.values();
    }
}

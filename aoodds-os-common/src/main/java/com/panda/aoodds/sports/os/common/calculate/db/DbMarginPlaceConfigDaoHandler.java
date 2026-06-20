package com.panda.aoodds.sports.os.common.calculate.db;

import com.alibaba.fastjson.JSONObject;
import com.panda.aoodds.sports.os.common.config.RedisConfig;
import com.panda.aoodds.sports.os.common.entity.MarginPlaceConfig;
import com.panda.aoodds.sports.os.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.panda.aoodds.sports.os.common.constant.RedisKeyConstant.MARGIN_PLACE_CONFIG_CACHE;


@Slf4j
@Component
public class DbMarginPlaceConfigDaoHandler {
    @Autowired
    @Qualifier("producerMongoTemplate")
    private MongoTemplate aoProducerMongoTemp;
    @Autowired
    private RedisService redisService;

    //MarginPlaceConfig
    public Map<String, String> mongoTempFindAll(String aoMatchId, Integer marketType) {
        Map<String, String> marginPlaceConfigObj = redisService.hGetAll(MARGIN_PLACE_CONFIG_CACHE + aoMatchId + "_" + marketType);
        if (Objects.isNull(marginPlaceConfigObj)) {
            try {
                Query marginQuery = Query.query(Criteria.where("aoMatchId").is(aoMatchId).and("marketType").is(marketType));
                List<MarginPlaceConfig> marginPlaceConfigs = aoProducerMongoTemp.find(marginQuery, MarginPlaceConfig.class);
                if (!CollectionUtils.isEmpty(marginPlaceConfigs)) {
                    marginPlaceConfigs.stream().forEach(m -> {
                        marginPlaceConfigObj.put(m.getChildStandardCategoryId() + "_" + m.getPlaceNum(), JSONObject.toJSONString(m));
                    });
                    redisService.hSetAll(MARGIN_PLACE_CONFIG_CACHE + aoMatchId + "_" + marketType, marginPlaceConfigObj, RedisConfig.REDIS_WEEK_TIME.longValue());
                    return marginPlaceConfigObj;
                }
            } catch (Exception e) {

            }
            return new HashMap<>();
        }
        return marginPlaceConfigObj;

    }
}

package com.panda.aoodds.sports.os.timer;

import com.panda.aoodds.sports.os.api.entity.MarketParamEntiy;
import com.panda.aoodds.sports.os.common.constant.CommonConstant;
import com.panda.aoodds.sports.os.common.entity.AoMatchInfoEntity;
import com.panda.aoodds.sports.os.common.entity.AutoApplyMarketOddsErrorMsgEntity;
import com.panda.aoodds.sports.os.producer.AutoApplyMarketOddsErrorProducer;
import com.panda.aoodds.sports.os.service.RedisService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.panda.aoodds.sports.os.common.utils.TraceIdGenerator.createTraceId;


@Slf4j
@Component
@RefreshScope
@Data
public class AoCheckAutoParamTimer {

    private static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(32);
    @Autowired
    @Qualifier("producerMongoTemplate")
    private MongoTemplate aoProducerMongoTemp;
    @Autowired
    AutoApplyMarketOddsErrorProducer autoApplyMarketOddsErrorProducer;
    @Autowired
    RedisService redisService;


    @PostConstruct
    public void checkAutoRevAndApplyMatchs() {
        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                String linkId = createTraceId() + "_o_goal";
                try {
                    log.info(linkId + ",o_goal,AoCheckAutoParamTimer start");
                    Long lastTime = System.currentTimeMillis() + (100 * 24 * 60 * 60 * 1000L);
                    Map<String, Long> aoMatchIdAndstandardMatchId = new HashMap<>();
                    log.info(linkId + ",o_goal,AoCheckAutoParamTimer size1:" + lastTime);
                    List<AoMatchInfoEntity> aoMatchInfoEntitys = aoProducerMongoTemp.find(Query.query(Criteria.where("sportId").is(CommonConstant.SPORT_OS).and("beginTime").gt(System.currentTimeMillis()).lt(lastTime)), AoMatchInfoEntity.class, CommonConstant.MATCH_INFO);
                    log.info(linkId + ",o_goal,AoCheckAutoParamTimer size2:" + lastTime);
                    List<String> aoMatchIds = aoMatchInfoEntitys.stream().map(f -> {
                        aoMatchIdAndstandardMatchId.put(String.valueOf(f.getAoMatchId()), f.getStandardMatchId());
                        return String.valueOf(f.getAoMatchId());
                    }).collect(Collectors.toList());
                    Query query = Query.query(Criteria.where("aoMatchId").in(aoMatchIds).and("matchUiStatus").is(0).and("aoAuto").is(1));
                    List<MarketParamEntiy> marketParamEntiys = aoProducerMongoTemp.find(query, MarketParamEntiy.class, CommonConstant.OS_MATCH_MARKET_CONFIG);
                    List<Long> standardMatchIds = marketParamEntiys.stream().map(f -> aoMatchIdAndstandardMatchId.get(f.getAoMatchId())).collect(Collectors.toList());
                    AutoApplyMarketOddsErrorMsgEntity autoApplyMarketOddsErrorMsgEntity = new AutoApplyMarketOddsErrorMsgEntity();
                    autoApplyMarketOddsErrorMsgEntity.setMatchIds(standardMatchIds);
                    autoApplyMarketOddsErrorMsgEntity.setDataTime(System.currentTimeMillis());
                    autoApplyMarketOddsErrorMsgEntity.setLinkId(linkId);
                    autoApplyMarketOddsErrorMsgEntity.setTempType("o_goal");
                    autoApplyMarketOddsErrorMsgEntity.setSportId(CommonConstant.SPORT_OS);
                    autoApplyMarketOddsErrorProducer.sendStandardMatchIds(autoApplyMarketOddsErrorMsgEntity);
                    log.info(linkId + ",o_goal,AoCheckAutoParamTimer end");
                } catch (Exception ex) {

                    log.error(linkId + ",o_goal,AoCheckAutoParamTimer error", ex);
                }
            }
        }, 0, 5000, TimeUnit.MILLISECONDS);
    }
}

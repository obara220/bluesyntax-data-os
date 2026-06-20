package com.panda.aoodds.sports.os.common.calculate;

import com.panda.aoodds.sports.os.api.entity.BetPieceEntity;
import com.panda.aoodds.sports.os.api.entity.MarketsEntity;
import com.panda.aoodds.sports.os.common.calculate.db.DbMarginPlaceConfigDaoHandler;
import com.panda.aoodds.sports.os.common.entity.MarginPlaceConfig;
import com.panda.aoodds.sports.os.common.entity.TradeMarketItemConfig;
import com.panda.aoodds.sports.os.service.RedisService;
import com.panda.aoodds.sports.os.common.utils.MatchInLiveUtil;
import com.panda.sports.algo.api.enums.MarketCategory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 赔率计算
 */
@Slf4j
@Component
public class CalculateOdds {


    @Autowired
    private InitializeComponent initializeComponent;

    @Autowired
    @Qualifier("producerMongoTemplate")
    private MongoTemplate aoProducerMongoTemp;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MarketOddsLinkageNew marketOddsLinkageNew;

    @Autowired
    private MyCalculationMarketProcessor myCalculationMarketProcessor;

    @Autowired
    private MatchInLiveUtil matchInLiveUtil;

    @Autowired
    private CorrectScoreMarketCalculateProcess correctScoreMarketCalculateProcess;
    @Autowired
    private DbMarginPlaceConfigDaoHandler dbMarginPlaceConfigDaoHandler;

    @Resource(name = "notifyMyCalculateMarketOddsMessage")
    private ThreadPoolTaskExecutor notifyMyCalculateMarketOddsMessageTask;

    /**
     * AO 赔率计算 ,使用融合下发的水差 和 margin 计算
     *
     * @param linkId
     * @param aoMatchId
     * @param
     * @param marketsList
     */
    public void calculate(String linkId, String aoMatchId, List<MarketsEntity> marketsList) {
        Map<Integer, List<MarketsEntity>> marketsEntityMap = marketsList.stream().filter(m -> m.getStatus() == 0).collect(Collectors.groupingBy(MarketsEntity::getMarketId));
        if (MapUtils.isEmpty(marketsEntityMap)) {
            return;
        }
        StopWatch swConfig = new StopWatch(UUID.randomUUID().toString());
        swConfig.start("查询赔率计算配置耗时");
        //AO赛事  0=未开赛、1=滚球 融合配置状态是相反的（0=滚球、1=赛前）
        Integer marketType = matchInLiveUtil.matchInLive(aoMatchId) == 1 ? 0 : 1;
        //获取赛事全部坑位margin
        Map<String, String> marginPlaceConfigMap = dbMarginPlaceConfigDaoHandler.mongoTempFindAll(aoMatchId, marketType);
        log.info("::{}::,AO赛事ID:{},滚球标识：{}", linkId, aoMatchId, marketType);
        Map<String, TradeMarketItemConfig> marketMarginAndMaxMinOddsConfigMap = getMarginAndMaxMinOddsByAoMatchId(linkId, aoMatchId);
        swConfig.stop();
        //异步循环执行
        List<CompletableFuture<?>> futures = new ArrayList<>();
        log.info("::{}::,AO赛事ID:{},准备开始抽水计算,", linkId, aoMatchId);
        for (Map.Entry<Integer, List<MarketsEntity>> entry : marketsEntityMap.entrySet()) {
            MarketCategory marketCategory = MarketCategory.getMarketCategoryById(entry.getKey());
            if (null == marketCategory) {
                continue;
            }
            String calculateType = marketCategory.getCalculateType();
            List<MarketsEntity> marketDataMessages = entry.getValue();
            log.info("::{}::,AO赛事ID:{},开始抽水计算,玩法ID：{},条数：{}", linkId, aoMatchId, entry.getKey(), marketDataMessages.size());
            Map<String, String> finalMarginPlaceConfigMap = marginPlaceConfigMap;
            futures.add(CompletableFuture.supplyAsync(() -> {
                for (MarketsEntity marketDataMessage : marketDataMessages) {
                    //盘口计算
                    String marketIdStr = marketDataMessage.getMarketId() + "_" + marketDataMessage.getHandicap() + "_" + marketDataMessage.getMarketName();
                    if ("MY".equals(calculateType)) {
                        //新抽水逻辑
                        boolean isOk = myCalculationMarketProcessor.calculationMarketAuto(linkId, aoMatchId, marketDataMessage, marketType, marketIdStr, finalMarginPlaceConfigMap);
                        if (!isOk) {
                            continue;
                        }
                        //最终的马来赔转欧赔
                        for (BetPieceEntity marketOdds : marketDataMessage.getBetPieceEntities()) {
                            //设置最终的paOddsValue，将马来赔转欧赔后，计算赔率差绝对值
                            Double aoOddsValue = initializeComponent.getConvertMalayToEurope(marketOdds.getMalayOddsValue());
                            marketOdds.setAoOddsValue(aoOddsValue);
                        }
                    } else {
                        String key = marketDataMessage.getMarketId() + "_" + marketDataMessage.getOrder();
                        //获取模板margin
                        TradeMarketItemConfig tradeMarketItemConfig = marketMarginAndMaxMinOddsConfigMap.get(key);
                        //margin计算
                        correctScoreMarketCalculateProcess.marginCalculate(linkId, aoMatchId, tradeMarketItemConfig, marketDataMessage, marketIdStr);
                    }
                }
                return null;
            }, notifyMyCalculateMarketOddsMessageTask));
            log.info("::{}::,AO赛事ID:{},开始抽水计算,结束，玩法ID：{}", linkId, aoMatchId, entry.getKey());
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("::{}::,AO赛事ID:{},准备开始抽水计算,结束,", linkId, aoMatchId);
        swConfig.start("赔率最大最小计算耗时");
        //赔率联动
        marketOddsLinkageNew.marketOddsVerify(linkId, aoMatchId, marketsList, marketMarginAndMaxMinOddsConfigMap);
        swConfig.stop();
        log.info("::{}::AO赛事ID:{},赔率计算耗时:{}ms," + swConfig.prettyPrint(), linkId, aoMatchId, swConfig.getTotalTimeMillis());
    }

    public Map<String, TradeMarketItemConfig> getMarginAndMaxMinOddsByAoMatchId(String linkId, String aoMatchId) {
        List<TradeMarketItemConfig> tradeMarketItemConfigs = aoProducerMongoTemp.find(Query.query(Criteria.where("aoMatchId").is(aoMatchId)), TradeMarketItemConfig.class);
        if (CollectionUtils.isEmpty(tradeMarketItemConfigs)) {
            log.info("::{}::AO赛事ID：{}，多项盘margin,最大最小赔率配置不存在", linkId, aoMatchId);
            return new HashMap<>();
        }
        Map<String, TradeMarketItemConfig> tradeMarketItemConfigMap = tradeMarketItemConfigs.stream().collect(Collectors.toMap(m -> m.getAoCategoryId() + "_" + m.getPlaceNum(), x -> x, (k1, k2) -> k1));
        log.info("::{}::AO赛事ID：{}，多项盘margin,最大最小赔率配置", linkId, aoMatchId);
        return tradeMarketItemConfigMap;
    }

}
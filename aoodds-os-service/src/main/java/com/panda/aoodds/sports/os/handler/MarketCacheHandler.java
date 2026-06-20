package com.panda.aoodds.sports.os.handler;

import com.alibaba.fastjson.JSONObject;
import com.panda.aoodds.sports.os.api.entity.AoMatchMarketInfo;
import com.panda.aoodds.sports.os.api.entity.BetPieceEntity;
import com.panda.aoodds.sports.os.api.entity.MarketsEntity;
import com.panda.aoodds.sports.os.common.config.RedisConfig;
import com.panda.aoodds.sports.os.common.constant.RedisKeyConstant;
import com.panda.aoodds.sports.os.producer.OsMarketMessageProducer;
import com.panda.aoodds.sports.os.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Component
public class MarketCacheHandler {

    @Autowired
    private RedisService redisService;
    @Autowired
    OsMarketMessageProducer osMarketMessageProducer;

    /**
     * 1.缓存赛事滚球盘口
     *
     * @param matchId
     * @param matchInfo
     */
    public void cacheLiveMarketOdds(String linkId, String matchId, AoMatchMarketInfo<MarketsEntity> matchInfo) {
        log.info("::{}::AO赛事ID:{},开始缓存赛事滚球盘口", linkId, matchId);
        String key = RedisKeyConstant.AO_OS_MATCH_INFO + matchId;
        JSONObject aoMatchMarketInfoObj = JSONObject.parseObject(JSONObject.toJSONString(matchInfo));
        aoMatchMarketInfoObj.remove("marketList");
        //刷新赛事缓存
        redisService.set(key, aoMatchMarketInfoObj);
        //缓存盘口
        List<MarketsEntity> marketList = matchInfo.getMarketList();
        Map<String, MarketsEntity> listMap = marketList.stream().collect(Collectors.toMap(t -> t.getMarketId() + "-" + t.getHandicap(), Function.identity(), (v1, v2) -> v2));
        String marketKey = RedisKeyConstant.AO_OS_MATCH_MARKET_ODDS + matchId;
        //刷新赛事赔率缓存
        redisService.hSetAll(marketKey, listMap, RedisConfig.REDIS_DEFAULT_TIME);
        log.info("::{}::AO赛事ID:{},结束缓存赛事滚球盘口:{}", linkId, matchId, listMap.size());
    }


    /**
     * 滚球盘口兜底关
     *
     * @param linkId
     * @param aoMatchId
     * @param
     */
    public void cacheOsLiveMarketProcessor(String linkId, String aoMatchId, AoMatchMarketInfo<MarketsEntity> matchInfo) {
        String marketKey = RedisKeyConstant.AO_OS_MATCH_MARKET_ODDS + aoMatchId;
        //刷新赛事赔率缓存
        Map<String, MarketsEntity> cacheMarketOdds = redisService.hGetAll(marketKey);
        if (MapUtils.isEmpty(cacheMarketOdds) || cacheMarketOdds.size() == 0) {
            return;
        }
        List<MarketsEntity> marketList = matchInfo.getMarketList();
        List<String> delKeys = new ArrayList<>();
        Map<String, MarketsEntity> newMarketOdds = marketList.stream().collect(Collectors.toMap(t -> t.getMarketId() + "-" + t.getHandicap(), Function.identity(), (v1, v2) -> v2));
        for (Map.Entry<String, MarketsEntity> entry : cacheMarketOdds.entrySet()) {
            String key = entry.getKey();
            if (null == newMarketOdds.get(key)) {
                MarketsEntity cacheMarketsEntity = entry.getValue();
                cacheMarketsEntity.setStatus(2);
                cacheMarketsEntity.setOrder(999);
                cacheMarketsEntity.setModifyTime(matchInfo.getModifyTime());
                marketList.add(cacheMarketsEntity);
                delKeys.add(key);
            }
        }
        if (!CollectionUtils.isEmpty(delKeys)) {
            redisService.hDel(marketKey, delKeys.toArray());
            log.info("::{}::marketDifferentClose,ao赛事ID：{},delKeys：{} ", linkId, aoMatchId, JSONObject.toJSONString(delKeys));
        }
        //有效盘口兜底
        Map<Integer, List<MarketsEntity>> listMap = marketList.stream().collect((Collectors.groupingBy(MarketsEntity::getMarketId)));
        //投注项<=1个未激活 关盘处理
        betPieceNotActive(linkId, listMap);
        //排序
        marketOrderSort(listMap);
    }


    /**
     * 投注项<=1个未激活 关盘处理
     *
     * @param
     */
    private void betPieceNotActive(String linkId, Map<Integer, List<MarketsEntity>> listMap) {
        for (Map.Entry<Integer, List<MarketsEntity>> entry : listMap.entrySet()) {
            List<MarketsEntity> value = entry.getValue();
            for (MarketsEntity marketsEntity : value) {
                List<BetPieceEntity> betPieceEntities = marketsEntity.getBetPieceEntities();
                if (!CollectionUtils.isEmpty(betPieceEntities)) {
                    boolean count = betPieceEntities.stream().filter(m -> m.isActive()).count() <= 1;
                    if (count) {
                        marketsEntity.setStatus(2);
                        marketsEntity.setOrder(999);
                    }
                }
            }
        }
    }

    /**
     * 排序
     * 开盘为有效排序，关盘排序位置需要在开盘有效盘口最大位置叠加
     *
     * @param listMap
     */
    public void marketOrderSort(Map<Integer, List<MarketsEntity>> listMap) {
        for (Map.Entry<Integer, List<MarketsEntity>> marketsEntityMap : listMap.entrySet()) {
            //有效盘口
            List<MarketsEntity> marketsEntity = marketsEntityMap.getValue();
            List<MarketsEntity> open = marketsEntity.stream().filter(m -> m.getStatus() == 0).collect(Collectors.toList());
            //关盘盘口
            List<MarketsEntity> close = marketsEntity.stream().filter(m -> m.getStatus() != 0).collect(Collectors.toList());
            int order = 1;
            if (!CollectionUtils.isEmpty(open)) {
                order = open.stream().max(Comparator.comparing(MarketsEntity::getOrder)).get().getOrder() + 1;
            }
            if (!CollectionUtils.isEmpty(close)) {
                for (MarketsEntity entity : close) {
                    entity.setOrder(order);
                    order++;
                }
            }
        }
    }

    /**
     * 最新赛事盘口数据
     *
     * @param linkId
     * @param aoMatchId
     * @return
     */
    public AoMatchMarketInfo<MarketsEntity> getCacheMarketInfo(String linkId, String aoMatchId) {
        Long nowTime = System.currentTimeMillis();
        log.info("::{}::AO赛事ID:{},获取缓存赛事盘口，时间：{}", linkId, aoMatchId, nowTime);
        String key = RedisKeyConstant.AO_OS_MATCH_INFO + aoMatchId;
        Object cacheMarketObj = redisService.get(key);
        if (Objects.isNull(cacheMarketObj)) {
            log.info("::{}::AO赛事ID:{},获取缓存赛事盘口,缓存KEY不存在:{}", linkId, aoMatchId, key);
            return null;
        }
        AoMatchMarketInfo<MarketsEntity> matchMarketInfo = JSONObject.parseObject(cacheMarketObj.toString(), AoMatchMarketInfo.class);
        String marketKey = RedisKeyConstant.AO_OS_MATCH_MARKET_ODDS + aoMatchId;
        Map<String, MarketsEntity> cacheMarkets = redisService.hGetAll(marketKey);
        if (CollectionUtils.isEmpty(cacheMarkets)) {
            return null;
        }
        matchMarketInfo.setLinkeId(linkId);
        matchMarketInfo.setModifyTime(nowTime);
        matchMarketInfo.setStartTime(nowTime);
        matchMarketInfo.setPushTime(nowTime);
        List<MarketsEntity> marketsEntities = cacheMarkets.values().stream().collect(Collectors.toList());
        matchMarketInfo.setMarketList(marketsEntities);
        return matchMarketInfo;
    }

    /**
     * 判断阶段 100 999 完赛直接关盘下发
     *
     * @param linkId
     * @param aoMatchId
     * @return FALSE 不满足 ，true 满足
     */
    public Boolean periodMarketClose(String linkId, String aoMatchId, Integer currentPeriod) {
        if (100 == currentPeriod || 999 == currentPeriod) {
            AoMatchMarketInfo<MarketsEntity> cacheMarketInfo = getCacheMarketInfo(linkId, aoMatchId);
            if (null == cacheMarketInfo) {
                log.info("::{}::periodMarketClose:aoMatchId {},currentPeriod {}，赛事不存在赔率", linkId, aoMatchId, currentPeriod);
                return Boolean.FALSE;
            }
            List<MarketsEntity> marketList = cacheMarketInfo.getMarketList();
            marketList.stream().map(m -> {
                m.setModifyTime(cacheMarketInfo.getModifyTime());
                m.setStatus(2);
                return m;
            }).collect(Collectors.toList());
            log.info("::{}::periodMarketClose:aoMatchId {},currentPeriod {}，关盘条数：{}", linkId, aoMatchId, currentPeriod, marketList.size());
            osMarketMessageProducer.sendMarketMessage(cacheMarketInfo);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

}

package com.panda.aoodds.sports.os.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.panda.aoodds.sports.os.api.entity.AoMatchMarketInfo;
import com.panda.aoodds.sports.os.api.entity.MarketParamEntiy;
import com.panda.aoodds.sports.os.api.entity.MarketsEntity;
import com.panda.aoodds.sports.os.api.enums.ScoreType;
import com.panda.aoodds.sports.os.common.CommonUtil;
import com.panda.aoodds.sports.os.common.calculate.CalculateOdds;
import com.panda.aoodds.sports.os.common.constant.CommonConstant;
import com.panda.aoodds.sports.os.common.constant.RedisKeyConstant;
import com.panda.aoodds.sports.os.common.entity.AoMatchInfoEntity;
import com.panda.aoodds.sports.os.common.entity.MarketDto;
import com.panda.aoodds.sports.os.common.utils.BigDecimalUtils;
import com.panda.aoodds.sports.os.common.utils.MatchInLiveUtil;
import com.panda.aoodds.sports.os.handler.MarketCacheHandler;
import com.panda.aoodds.sports.os.handler.MarketLoadBalanceHandler;
import com.panda.aoodds.sports.os.handler.SupportHandle;
import com.panda.aoodds.sports.os.producer.OsMarketMessageProducer;
import com.panda.aoodds.sports.os.service.RedisService;
import com.panda.aoodds.sports.os.service.SubjectOsMatchMarketPushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.panda.aoodds.sports.os.common.constant.CommonConstant.AO_OPT_QUERY_APPLY;
import static com.panda.aoodds.sports.os.common.constant.CommonConstant.COMPLEX_QUARTER_LIST_ALL;
import static com.panda.aoodds.sports.os.common.constant.CommonConstant.TABLETENNIS_INPLAY_PERIOD;
import static com.panda.aoodds.sports.os.common.constant.RedisKeyConstant.AO_MATCH_NO_ODDS_ISSUED;
import static com.panda.sports.algo.api.enums.SportEnum.TABLETENNIS;

@Slf4j
@Service
public class SubjectOsMatchMarketPushImpl implements SubjectOsMatchMarketPushService {
    @Autowired
    @Qualifier("producerMongoTemplate")
    private MongoTemplate aoProducerMongoTemp;
    @Autowired
    MarketLoadBalanceHandler marketLoadBalanceHandler;

    @Autowired
    RedisService redisService;
    @Autowired
    private CalculateOdds calculateOdds;
    @Autowired
    private MarketCacheHandler marketCacheHandler;
    @Autowired
    OsMarketMessageProducer osMarketMessageProducer;
    @Autowired
    SupportHandle supportHandle;
    @Autowired
    MatchInLiveUtil matchInLiveUtil;

    @Override
    public void notifyMarketMessage(String aoMatchId, String linkId, String optType, MarketParamEntiy calcMarketParamEntiy) {
        log.info("::{}::notifyMarketMessage,A0赛事:{}",linkId,aoMatchId);
        Long startTime = System.currentTimeMillis();
        MarketParamEntiy marketParamEntiy = new MarketParamEntiy();
        Integer[] eventInfo = supportHandle.matchPeriod(aoMatchId);
        Integer currentHomeAway = eventInfo[0];
        Integer currentPeriod = eventInfo[1];
        Integer liveFlag = matchInLiveUtil.matchInLive(aoMatchId);
        liveFlag = liveFlag == 1 || currentPeriod == 1 ? 1 : 0;
        AoMatchInfoEntity aoMatchInfoEntity = aoProducerMongoTemp.findOne(Query.query(Criteria.where("aoMatchId").is(Long.valueOf(aoMatchId))), AoMatchInfoEntity.class, CommonConstant.MATCH_INFO);
        if (null == aoMatchInfoEntity) {
            log.info("::{}::notifyMarketMessage,A0赛事:{}，赛事不存在", linkId, aoMatchId);
            return;
        }
        if (liveFlag == 1 && redisService.hHasKey(AO_MATCH_NO_ODDS_ISSUED + TABLETENNIS.getId(), aoMatchInfoEntity.getTournamentLevel().toString())) {
            log.info("::{}::notifyMarketMessage,A0赛事:{}，联赛等级限频", linkId, aoMatchId);
            return;
        }
        if (AO_OPT_QUERY_APPLY.equals(optType)) {
            //赛种开关
            Object sportStatusObj = redisService.hGet(RedisKeyConstant.AO_MATCH_SPORT_SWITCH, TABLETENNIS.getId() + "");
            if (null != sportStatusObj && Integer.parseInt(sportStatusObj.toString()) == 0) {
                log.info("::{}::notifyMarketMessage,A0赛事:{}，赛种状态为关不下发", linkId, aoMatchId);
                return;
            }
            log.info("::{}::notifyMarketMessage,A0赛事:{}，阶段:{}", aoMatchId, JSON.toJSONString(marketParamEntiy), liveFlag);
            marketParamEntiy = aoProducerMongoTemp.findOne(Query.query(Criteria.where("aoMatchId").is(aoMatchId)), MarketParamEntiy.class, CommonConstant.OS_MATCH_MARKET_CONFIG);
        } else {
            marketParamEntiy = calcMarketParamEntiy;
        }
        log.info("::{}::notifyMarketMessage,A0赛事:{}，阶段:{}", linkId, JSON.toJSONString(marketParamEntiy), currentPeriod);
        marketParamEntiy.setPreLive(currentPeriod > 0 ? 0 : 1);
        marketParamEntiy.setMatchPeriod(currentPeriod);
        marketParamEntiy = supportHandle.supportScore(marketParamEntiy);
        Properties properties = marketParamEntiy.toProperties();
        log.info("::{}::notifyMarketMessage,A0赛事:{}，计算赔率入参:{}", linkId, aoMatchId, JSON.toJSONString(properties));
        //判断阶段 100 999 完赛直接关盘下发
        if (marketCacheHandler.periodMarketClose(linkId, aoMatchId, currentPeriod)) {
            return;
        }
        String markets = marketLoadBalanceHandler.getMarkets(properties, "", null);
        log.info("::{}::notifyMarketMessage,A0赛事:{}，计算赔率返回:{}", linkId, aoMatchId, markets);
        List<MarketsEntity> marketsEntityList = JSONObject.parseObject(markets).toJavaObject(MarketDto.class).getMarketsEntityList();
        //101051 【A01】【操盘】乒乓球玩法依照赛况进行开盘/关盘
        List<MarketsEntity> lastMarketsEntityList =
                getLastMarketsEntityList(linkId, marketsEntityList, currentPeriod, marketParamEntiy.getFullGameScore());

        AoMatchMarketInfo aoMatchMarketInfo = new AoMatchMarketInfo();
        aoMatchMarketInfo.setModifyTime(startTime);
        aoMatchMarketInfo.setStartTime(startTime);
        aoMatchMarketInfo.setMatchSourceId(aoMatchId);
        aoMatchMarketInfo.setMarketTime("0");
        aoMatchMarketInfo.setSportId(8L);
        aoMatchMarketInfo.setLiveFlag(liveFlag);
        aoMatchMarketInfo.setPeriod(currentPeriod);
        aoMatchMarketInfo.setLinkeId(linkId);
        aoMatchMarketInfo.setRequestType("o_goal");
        aoMatchMarketInfo.setScoreSummary(formatScoreOfMap(marketParamEntiy));
        Map<String, String> currentHomeAwayServe = new HashMap<>();
        if (currentPeriod > 0 && TABLETENNIS_INPLAY_PERIOD.contains(String.valueOf(currentPeriod))) {
            currentHomeAwayServe = CommonUtil.bulidFirstServeMatrix(currentHomeAway == 1 ? "H" : "A").get(currentPeriod - 7);
        }


        JSONObject jsonRemark = new JSONObject();
        Double serveHome = (marketParamEntiy.getAvgServe() * 2 + marketParamEntiy.getSup()) / 2;
        jsonRemark.put("ServeH", BigDecimalUtils.subDoubleTwo(serveHome));
        jsonRemark.put("ServeA", BigDecimalUtils.subDoubleTwo(marketParamEntiy.getAvgServe() * 2 - serveHome));
        jsonRemark.put("serve", currentHomeAway == 1 ? "Home" : "Away");
        jsonRemark.put("rserve", currentHomeAwayServe);
        aoMatchMarketInfo.setRemark(jsonRemark.toJSONString());


        //赔率计算
        calculateOdds.calculate(linkId, aoMatchId, lastMarketsEntityList);
        aoMatchMarketInfo.setMarketList(lastMarketsEntityList);
        if (aoMatchMarketInfo.getLiveFlag() == 1) {
            //缓存赛事盘口缓存
            marketCacheHandler.cacheLiveMarketOdds(linkId, aoMatchId, aoMatchMarketInfo);
            //兜底关盘
            marketCacheHandler.cacheOsLiveMarketProcessor(linkId, aoMatchId, aoMatchMarketInfo);
        }
        if (optType.equals(AO_OPT_QUERY_APPLY)) {
            osMarketMessageProducer.sendMarketMessage(aoMatchMarketInfo);
        }

        osMarketMessageProducer.sendWsMarketMessage(aoMatchMarketInfo);
    }

    private List<MarketsEntity> getLastMarketsEntityList(String linkId,
                                                         List<MarketsEntity> marketsEntityList,
                                                         Integer currentPeriod,
                                                         String fullGameScore) {
        log.info("::{}::,当前赛事阶段:{}, 当前全局比分信息:{}", linkId, currentPeriod, fullGameScore);
        //非小节玩法集合
        List<MarketsEntity> lastMarketsEntityList =
                marketsEntityList.stream().filter(e -> !COMPLEX_QUARTER_LIST_ALL.contains(e.getMarketId())).collect(Collectors.toList());
        //当前阶段小节玩法
        List<Integer> currentPeriodMarketsList = supportHandle.getCurrentPeriodMarketsList(currentPeriod, fullGameScore);
        if (!CollectionUtils.isEmpty(currentPeriodMarketsList)) {
            log.info("::{}::,当前小节玩法:{}", linkId, JSON.toJSONString(currentPeriodMarketsList));
            //需要展示的小节玩法信息集合
            List<MarketsEntity> periodMarketsList =
                    marketsEntityList.stream().filter(e -> currentPeriodMarketsList.contains(e.getMarketId())).collect(Collectors.toList());
            lastMarketsEntityList.addAll(periodMarketsList);
        }
        return lastMarketsEntityList;
    }

    public Map<ScoreType, String> formatScoreOfMap(MarketParamEntiy marketsEntity) {

        Map<ScoreType, String> scoreMap = new HashMap<>();
        scoreMap.put(ScoreType.FULL_SCORE, null==marketsEntity.getFullGameScore()?"0-0":marketsEntity.getFullGameScore());
        scoreMap.put(ScoreType.SET_SCORE1, null==marketsEntity.getGame1Score()?"0-0":marketsEntity.getGame1Score());
        scoreMap.put(ScoreType.SET_SCORE2, null==marketsEntity.getGame2Score()?"0-0":marketsEntity.getGame2Score());
        scoreMap.put(ScoreType.SET_SCORE3, null==marketsEntity.getGame3Score()?"0-0":marketsEntity.getGame3Score());
        scoreMap.put(ScoreType.SET_SCORE4, null==marketsEntity.getGame4Score()?"0-0":marketsEntity.getGame4Score());
        scoreMap.put(ScoreType.SET_SCORE5, null==marketsEntity.getGame5Score()?"0-0":marketsEntity.getGame5Score());
        scoreMap.put(ScoreType.FULL_SET_SCORE, null==marketsEntity.getFullSetScore()?"0-0":marketsEntity.getFullSetScore());
        scoreMap.put(ScoreType.ROIUND, null==marketsEntity.getRoiund()?"0-0":marketsEntity.getRoiund());
        scoreMap.put(ScoreType.CURRENT_SET_SCORE, null==marketsEntity.getScore()?"0-0":marketsEntity.getScore());
        return scoreMap;
    }
}

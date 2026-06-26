package com.panda.aoodds.sports.os.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.panda.aoodds.sports.os.api.entity.MarketParamEntiy;
import com.panda.aoodds.sports.os.api.entity.ParamVo;
import com.panda.aoodds.sports.os.api.entity.RequestReverParamEntity;
import com.panda.aoodds.sports.os.api.entity.TTReverseEntity;
import com.panda.aoodds.sports.os.common.constant.CommonConstant;
import com.panda.aoodds.sports.os.common.constant.RedisKeyConstant;
import com.panda.aoodds.sports.os.common.exception.ApiException;
import com.panda.aoodds.sports.os.common.utils.BigDecimalUtils;
import com.panda.aoodds.sports.os.common.verifyParam;
import com.panda.aoodds.sports.os.handler.MaintainDataSourceHandler;
import com.panda.aoodds.sports.os.handler.MarketLoadBalanceHandler;
import com.panda.aoodds.sports.os.service.RedisService;
import com.panda.aoodds.sports.os.service.ReverseApplyOsRecordService;
import com.panda.aoodds.sports.os.service.TableTennisCalcMarketsService;
import com.panda.aoodds.sports.os.service.TableTennisReverseParamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Properties;

import static com.panda.aoodds.sports.os.common.constant.CommonConstant.AO_OPT_QUERY_APPLY;

@Slf4j
@Service
public class ReverseParamServiceImpl implements TableTennisReverseParamService {
    @Autowired
    MarketLoadBalanceHandler marketLoadBalanceHandler;
    @Autowired
    TableTennisCalcMarketsService tableTennisCalcMarketsService;
    @Autowired
    ReverseApplyOsRecordService reverseApplyOsRecordService;
    @Autowired
    RedisService redisService;
    @Autowired
    MaintainDataSourceHandler maintainDataSourceHandler;

    @Autowired
    @Qualifier("producerMongoTemplate")
    private MongoTemplate mongoTemplate;

    @Override
    public TTReverseEntity reverseTableTennisConfig(RequestReverParamEntity requestReverParamEntity, String userId) {
        String aoMatchId = requestReverParamEntity.getAoMatchId();
        String linkId = requestReverParamEntity.getLinkId();
        String dataSourceCode = requestReverParamEntity.getDataSourceCode();
        if (maintainDataSourceHandler.cheack(linkId, aoMatchId, dataSourceCode)) {
            log.info("::{}::【reverseParam】此数据源维护中,AO赛事ID:{},数据源:{}", linkId, aoMatchId, dataSourceCode);
            throw new ApiException("此数据源维护中," + linkId);
        }
        Integer matchUiStatus = requestReverParamEntity.getMatchUiStatus();
        String categoryTypeKey = RedisKeyConstant.THIRD_OS_AO_MARKET_ODDS_KEY;
        String key = categoryTypeKey + aoMatchId + ":" + dataSourceCode;
        Object obj = redisService.hGetAll(key);
        if (ObjectUtils.isEmpty(obj)) {
            log.info("::{}::【reverseParam】赛事球头参数不全1,AO赛事ID:{},数据源:{}，缓存:{}", linkId, aoMatchId, dataSourceCode, obj);
            throw new ApiException("No data ，plz choose other bookmaker ");
        }
        Properties properties = verifyParam.fusionBallData(linkId, aoMatchId, dataSourceCode, (Map<String, Properties>) obj);
        if (!verifyParam.basketballHeadVerify(linkId, properties)) {
            log.info("::{}::【reverseParam】赛事球头参数不全2,AO赛事ID:{},数据源:{}，缓存:{}", linkId, aoMatchId, dataSourceCode, obj);
            throw new ApiException("No data ，plz choose other bookmaker ");
        }
        MarketParamEntiy  marketParamEntiy = mongoTemplate.findOne(Query.query(Criteria.where("aoMatchId").is(aoMatchId).and("matchUiStatus").is(matchUiStatus)), MarketParamEntiy.class, CommonConstant.OS_MATCH_MARKET_CONFIG);
        Properties matchTemplateConfigProperties = new Properties();
        matchTemplateConfigProperties.put("winHomeOdds", properties.get("ftWinnerOdds1"));
        matchTemplateConfigProperties.put("winAwayOdds", properties.get("ftWinnerOdds2"));
        log.info("::{}::【reverseParam】reverse 入参：{}", linkId, JSON.toJSONString(matchTemplateConfigProperties));
        String result = marketLoadBalanceHandler.getReverseParam(matchTemplateConfigProperties);
        log.info("::{}::【reverseParam】reverse 返回：{}", linkId, result);
        TTReverseEntity ttReverseEntity = JSONObject.parseObject(result, TTReverseEntity.class);
        ttReverseEntity.setWinHomeOdds(BigDecimalUtils.originalOddsScale(ttReverseEntity.getWinHomeOdds().toString()));
        ttReverseEntity.setWinAwayOdds(BigDecimalUtils.originalOddsScale(ttReverseEntity.getWinAwayOdds().toString()));
        reverseApplyOsRecordService.insertRev(requestReverParamEntity, ttReverseEntity,marketParamEntiy, userId);
        return ttReverseEntity;
    }

    @Override
    public void reverseAndApplyTableTennisConfigAuto(RequestReverParamEntity requestReverParamEntity) {
        String linkId = requestReverParamEntity.getLinkId();
        String aoMatchId = requestReverParamEntity.getAoMatchId();
        String dataSourceCode = requestReverParamEntity.getDataSourceCode();
        MarketParamEntiy marketParamEntiy = mongoTemplate.findOne(Query.query(Criteria.where("aoMatchId").is(aoMatchId).and("matchUiStatus").is(requestReverParamEntity.getMatchUiStatus())), MarketParamEntiy.class, CommonConstant.OS_MATCH_MARKET_CONFIG);
        if (null == marketParamEntiy) {
            log.info("::{}::自动revAndApply,AO赛事ID:{},数据源:{}，apply参数不存在不处理", linkId, aoMatchId, dataSourceCode);
            return;
        }
        if (null == marketParamEntiy.getAoAuto() || marketParamEntiy.getAoAuto() == 0) {
            log.info("::{}::自动revAndApply,AO赛事ID:{},数据源:{}，没有auto不处理", linkId, aoMatchId, dataSourceCode);
            return;
        }
        reverseAndApplyTableTennisConfig(requestReverParamEntity);
    }

    @Override
    public void reverseAndApplyTableTennisConfig(RequestReverParamEntity requestReverParamEntity) {
        String linkId = requestReverParamEntity.getLinkId();
        String aoMatchId = requestReverParamEntity.getAoMatchId();
        Integer matchUiStatus = requestReverParamEntity.getMatchUiStatus();
        MarketParamEntiy marketParamEntiy = mongoTemplate.findOne(
                Query.query(Criteria.where("aoMatchId").is(aoMatchId).and("matchUiStatus").is(matchUiStatus)),
                MarketParamEntiy.class, CommonConstant.OS_MATCH_MARKET_CONFIG);
        if (null == marketParamEntiy) {
            log.info("::{}::revAndApply,AO赛事ID:{},apply参数不存在不处理", linkId, aoMatchId);
            return;
        }
        if (StringUtils.isBlank(requestReverParamEntity.getDataSourceCode())) {
            requestReverParamEntity.setDataSourceCode(marketParamEntiy.getDataSourceCode());
        }
        TTReverseEntity ttReverseEntity = reverseTableTennisConfig(requestReverParamEntity, null);
        marketParamEntiy.setLinkId(linkId);
        marketParamEntiy.setSup(ttReverseEntity.getSup());
        ParamVo<MarketParamEntiy> paramVo = new ParamVo<>();
        paramVo.setType(AO_OPT_QUERY_APPLY);
        paramVo.setUserName("A01 System");
        paramVo.setParam(marketParamEntiy);
        tableTennisCalcMarketsService.applyOrcacl(paramVo);
    }

}

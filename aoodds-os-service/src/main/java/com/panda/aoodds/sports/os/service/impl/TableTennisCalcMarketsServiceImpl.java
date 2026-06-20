package com.panda.aoodds.sports.os.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.panda.aoodds.sports.os.api.entity.MarketParamEntiy;
import com.panda.aoodds.sports.os.api.entity.PageModel;
import com.panda.aoodds.sports.os.api.entity.ParamVo;
import com.panda.aoodds.sports.os.api.entity.RecordPageVo;
import com.panda.aoodds.sports.os.api.service.SubjectOsMatchMarketPushApi;
import com.panda.aoodds.sports.os.common.constant.CommonConstant;
import com.panda.aoodds.sports.os.common.entity.AoMatchInfoEntity;
import com.panda.aoodds.sports.os.common.entity.ApplyParamQueryEntity;
import com.panda.aoodds.sports.os.common.exception.ApiException;
import com.panda.aoodds.sports.os.common.utils.MatchInLiveUtil;
import com.panda.aoodds.sports.os.handler.SupportHandle;
import com.panda.aoodds.sports.os.producer.CommonSendMessageProducer;
import com.panda.aoodds.sports.os.service.ReverseApplyOsRecordService;
import com.panda.aoodds.sports.os.service.TableTennisCalcMarketsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.panda.aoodds.sports.os.common.constant.CommonConstant.*;

@Slf4j
@Service
public class TableTennisCalcMarketsServiceImpl implements TableTennisCalcMarketsService {
    @Autowired
    @Qualifier("producerMongoTemplate")
    private MongoTemplate mongoTemplate;
    @Autowired
    private SubjectOsMatchMarketPushApi subjectOsMatchMarketPush;
    @Autowired
    SupportHandle supportHandle;
    @Autowired
    CommonSendMessageProducer commonSendMessageProducer;
    @Autowired
    ReverseApplyOsRecordService reverseApplyOsRecordService;
    @Autowired
    MatchInLiveUtil matchInLiveUtil;

    @Override
    public void applyOrcacl(ParamVo<MarketParamEntiy> paramVo) {
        log.info("applyOrcacl入参：{} ", paramVo);
        MarketParamEntiy marketParamEntiy = paramVo.getParam();
        String linkId = marketParamEntiy.getLinkId();
        try {
            marketParamEntiy.setLinkId(linkId);
            marketParamEntiy.setId(marketParamEntiy.getAoMatchId());
            Integer currentPeriod = supportHandle.matchPeriod(marketParamEntiy.getAoMatchId())[1];
            Integer liveFlag = matchInLiveUtil.matchInLive(marketParamEntiy.getAoMatchId());
            liveFlag = liveFlag == 1 || currentPeriod == 1 ? 1 : 0;
            if ( paramVo.getType().equals(AO_OPT_QUERY_APPLY)) {
                mongoTemplate.save(marketParamEntiy, CommonConstant.OS_MATCH_MARKET_CONFIG);
                subjectOsMatchMarketPush.notifyMarketMessage(marketParamEntiy.getAoMatchId(), marketParamEntiy.getLinkId(), AO_OPT_QUERY_APPLY, marketParamEntiy);
            }else if (paramVo.getType().equals(AO_OPT_QUERY_CALC)) {
                subjectOsMatchMarketPush.notifyMarketMessage(marketParamEntiy.getAoMatchId(), marketParamEntiy.getLinkId(), AO_OPT_QUERY_CALC, marketParamEntiy);
            }
            AoMatchInfoEntity aoMatchInfoEntity = mongoTemplate.findOne(Query.query(Criteria.where("aoMatchId").is(Long.valueOf(marketParamEntiy.getAoMatchId()))), AoMatchInfoEntity.class, CommonConstant.MATCH_INFO);
            ApplyParamQueryEntity applyParamQueryEntity = new ApplyParamQueryEntity(Lists.newArrayList(aoMatchInfoEntity.getStandardMatchId()), "o_goal", 8, marketParamEntiy.getMatchUiStatus(), linkId);
            commonSendMessageProducer.sendMarketMessage(A01_MATCH_APPLY_INIT, JSON.toJSONString(applyParamQueryEntity), linkId);
        } catch (Exception e) {
            log.error(linkId + "，【applyOrcacl】Ao赛事ID:" + marketParamEntiy.getAoMatchId() + ",数据异常:", e);
            throw new ApiException("获取盘口信息数据提示:" + e.getMessage());
        }
    }

    @Override
    public MarketParamEntiy queryMarketParamEntiy(MarketParamEntiy paramEntiy) {

        MarketParamEntiy marketParamEntiy = mongoTemplate.findOne(Query.query(Criteria.where("aoMatchId").is(paramEntiy.getAoMatchId())), MarketParamEntiy.class, CommonConstant.OS_MATCH_MARKET_CONFIG);
        if (null == marketParamEntiy) {
            return marketParamEntiy;
        }
        // marketParamEntiy.setMatchPeriod(supportHandle.matchPeriod(paramEntiy.getAoMatchId())[1]);
        // marketParamEntiy=supportHandle.supportScore(paramEntiy);
        // subjectOsMatchMarketPush.notifyMarketMessage(marketParamEntiy.getAoMatchId(),paramEntiy.getLinkId(),"calc",marketParamEntiy);
        return marketParamEntiy;
    }

    @Override
    public List<JSONObject> getLogByAoMatchId(String aoMatchId) {
        return reverseApplyOsRecordService.getLogByAoMatchId(aoMatchId);
    }

    @Override
    public PageModel<List<JSONObject>> getRecordByMatchAndRequestTypePageList(RecordPageVo recordPageVo) {
        return reverseApplyOsRecordService.getRecordByMatchAndRequestTypePageList(recordPageVo);
    }


}

package com.panda.aoodds.sports.os.common.calculate;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.panda.aoodds.sports.os.api.entity.BetPieceEntity;
import com.panda.aoodds.sports.os.api.entity.MarketsEntity;
import com.panda.aoodds.sports.os.common.entity.TradeMarketItemConfig;
import com.panda.aoodds.sports.os.service.RedisService;
import com.panda.aoodds.sports.os.common.utils.BigDecimalUtils;
import com.panda.aoodds.sports.os.common.utils.MatchInLiveUtil;
import com.panda.sports.algo.api.enums.MarketCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 2663
 * 波胆计算
 */
@Slf4j
@Component
public class CorrectScoreMarketCalculateProcess {

    @Autowired
    @Qualifier("producerMongoTemplate")
    private MongoTemplate aoProducerMongoTemp;

    @Autowired
    private CalculateOdds calculateOdds;
    @Autowired
    private MatchInLiveUtil matchInLiveUtil;
    /**
     * 双重机会玩法
     */
    private static List<Integer> DOUBLE_CHANCE = Lists.newArrayList(MarketCategory.FT_DOUBLE_CHANCE.getId(), MarketCategory.HALF1_DOUBLE_CHANCE.getId(), MarketCategory.HALF2_DOUBLE_CHANCE.getId(), MarketCategory.FT_DOUBLE_CHANCE_AND_BOTH_TEAM_SCORE.getId(), MarketCategory.FT_DOUBLE_CHANCE_AND_TOTAL.getId(), MarketCategory.BOOKING_H1_DOUBLE_CHANCE.getId(), MarketCategory.CORNER_DOUBLE_CHANCE.getId(), MarketCategory.BOOKING_DOUBLE_CHANCE.getId(), MarketCategory.FT_DOUBLE_CHANCE_H1_BOTH_TEAM_SCORE.getId(), MarketCategory.FT_DOUBLE_CHANCE_H2_BOTH_TEAM_SCORE.getId(), MarketCategory.FT_DOUBLE_CHANCE_AND_FIRST_SCORE_TEAM.getId(), MarketCategory.HALF1_DOUBLE_CHANCE_BOTH_TEAM_SCORE.getId(), MarketCategory.HALF2_DOUBLE_CHANCE_BOTH_TEAM_SCORE.getId());

    /**
     * margin大于 300单独处理
     */
    private static List<Integer> MARGIN_GREATER_THAN_300 = Lists.newArrayList(MarketCategory.FT_TEAM_TO_WIN.getId(), MarketCategory.FT_1X2_OR_ANY_CLEAN_SHEET.getId());
    /**
     * margin大于 300单独处理 直接除margin
     */
    private static List<Integer> DIVIDE_MARGIN_GREATER_THAN_300 = Lists.newArrayList(MarketCategory.FT_1X2_OR_ANY_CLEAN_SHEET.getId(), MarketCategory.HALF1_FIRST_LAST_GOAL.getId());

    @Autowired
    private RedisService redisService;


    /**
     * margin计算
     *
     * @param marketsEntity
     */
    public void marginCalculate(String linkId, String aoMatchId, TradeMarketItemConfig tradeMarketItemConfig, MarketsEntity marketsEntity, String marketIdStr) {
        Double margin = 110D;
        if (null != tradeMarketItemConfig) {
            margin = tradeMarketItemConfig.getMargin();
        }
        if (margin <= 0) {
            margin = 110D;
        }
        log.info("::{}::marginCalculate,AO赛事ID:{},margin:{},盘口ID:{},盘口信息:{}", linkId, aoMatchId, margin, marketIdStr, JSONObject.toJSONString(marketsEntity));
        List<BetPieceEntity> betPieceEntities = marketsEntity.getBetPieceEntities();
        for (BetPieceEntity betPieceEntity : betPieceEntities) {
            Double originalOdds = Double.valueOf(betPieceEntity.getOdds());
            if (0 != originalOdds) {
                double value = BigDecimalUtils.scale(originalOdds / ((double) margin / 100), 2);
                //计算出最终赔率小于1.01 并且 概率小于1 返回1.01
                if (value < 1.01D && originalOdds < 1D) {
                    betPieceEntity.setAoOddsValue(1.01D);
                } else {
                    betPieceEntity.setAoOddsValue(value);
                }
            }
        }
    }
}

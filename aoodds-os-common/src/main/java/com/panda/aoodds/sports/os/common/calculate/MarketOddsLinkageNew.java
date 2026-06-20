package com.panda.aoodds.sports.os.common.calculate;

import com.panda.aoodds.sports.os.api.entity.BetPieceEntity;
import com.panda.aoodds.sports.os.api.entity.MarketsEntity;
import com.panda.aoodds.sports.os.common.entity.TradeMarketItemConfig;
import com.panda.aoodds.sports.os.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 盘口赔率联动
 */
@Slf4j
@Component
public class MarketOddsLinkageNew {

    @Autowired
    private RedisService redisService;


    /**
     * 最大最小赔率，超过最大或者最小赔率以
     *
     * @param linkId
     * @param aoMatchId
     * @param marketsList
     * @param marketMaxMinOddsConfigMap 最大最小赔率配置
     */
    public void marketOddsVerify(String linkId, String aoMatchId, List<MarketsEntity> marketsList, Map<String, TradeMarketItemConfig> marketMaxMinOddsConfigMap) {
        if (CollectionUtils.isEmpty(marketMaxMinOddsConfigMap)) {
            log.info("::{}::AO赛事ID:{},最大最小配置配置不存在.", linkId, aoMatchId);
            return;
        }
        Set<String> maxMinOddsCategoryId = marketMaxMinOddsConfigMap.keySet();
        log.info("::{}::AO赛事ID:{},最大最小配置配置玩法:{}", linkId, aoMatchId, maxMinOddsCategoryId);
        marketsList.stream().forEach(market -> {
            marketMinOddsVerify(market);
            Integer marketId = market.getMarketId();
            TradeMarketItemConfig tradeMarketItemConfig = marketMaxMinOddsConfigMap.get(marketId + "_" + market.getOrder());
            if (null == tradeMarketItemConfig) {
                tradeMarketItemConfig = marketMaxMinOddsConfigMap.get(marketId + "_1");
            }
            if (null == tradeMarketItemConfig) {
                return;
            }
            Double max = null == tradeMarketItemConfig.getMaxOddsValue() ? 0D : tradeMarketItemConfig.getMaxOddsValue();
            Double min = null == tradeMarketItemConfig.getMinOddsValue() ? 0D : tradeMarketItemConfig.getMinOddsValue();
            if (0 == max || 0 == min) {
                return;
            }
            //投注项最大最小赔率校验
            List<BetPieceEntity> betPieceEntities = market.getBetPieceEntities();
            if (!CollectionUtils.isEmpty(betPieceEntities)) {
                betPieceEntities.forEach(bet -> {
                    Double aoOddsValue = bet.getAoOddsValue();
                    if (aoOddsValue != 0) {
                        if (aoOddsValue >= max) {
                            bet.setAoOddsValue(max);
                        }
                        if (aoOddsValue <= min) {
                            bet.setAoOddsValue(min);
                        }
                    }
                });
            }
        });
    }

    /**
     * 最小赔率固定小于 1.01 默认1.01
     *
     * @param marketsEntity
     */
    private void marketMinOddsVerify(MarketsEntity marketsEntity) {
        Integer marketId = marketsEntity.getMarketId();
//        if (!MarketCategory.MIN_MARKET_ODDS.contains(marketId)) {
//            return;
//        }
        //投注项最大最小赔率校验
        List<BetPieceEntity> betPieceEntities = marketsEntity.getBetPieceEntities();
        if (CollectionUtils.isEmpty(betPieceEntities)) {
            return;
        }
        if (betPieceEntities.size() == 2) {
            betPieceEntities.forEach(bet -> {
                Double aoOddsValue = bet.getAoOddsValue();
                if (aoOddsValue < 1.01D) {
                    bet.setAoOddsValue(1.01D);
                }
            });
        }
    }
}
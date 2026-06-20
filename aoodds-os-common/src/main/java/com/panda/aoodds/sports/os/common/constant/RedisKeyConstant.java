package com.panda.aoodds.sports.os.common.constant;

import java.util.HashSet;
import java.util.Set;

public class RedisKeyConstant {
    public static Set<String> redisHashKey=new HashSet<>(5000);
    public static final String AO_BGTIME_KEY="AO_BGTIME_KEY:";
    public static final String AO_TABLETENNIS_SCORE_KEY="AO_TABLETENNIS_SCORE_KEY";
    //融合下发赛事live状态
    public static final String AO_MATCH_IN_LIVE = "ao_match_in_live:";
    //赛事级别缓存
    public static final String AO_OS_MATCH_INFO = "AO_BASKETBALL_MATCH_INFO:";
    //赛事盘口级别缓存
    public static final String AO_OS_MATCH_MARKET_ODDS= "AO_OS_MATCH_MARKET_ODDS:";
    public static final String AO_TABLETENNIS_BGTIME_KEY="AO_TABLETENNIS_BGTIME_KEY";
    //全局AO赛事赛种级别开关  1 开 0 关
    public static final String AO_MATCH_SPORT_SWITCH = "AO_MATCH_SPORT_SWITCH";
    public static final String MARGIN_PLACE_CONFIG_CACHE = "MARGIN_PLACE_CONFIG_CACHE:";
    /**
     * 三方赔率球头 REDIS KEY
     */
    public static final String THIRD_OS_AO_MARKET_ODDS_KEY = "ThirdOsAoMarketOdds:";
    //赛事过滤不下发赔率 map<联赛,联赛>
    public static final String AO_MATCH_NO_ODDS_ISSUED = "AO_MATCH_NO_ODDS_ISSUED:";
    //维护数据源 Map<数据源,enableSwitch#开始时间#结束时间>
    public static final String AO_MAINTAIN_DATA_SOURCE = "AO_MAINTAIN_DATA_SOURCE";
}

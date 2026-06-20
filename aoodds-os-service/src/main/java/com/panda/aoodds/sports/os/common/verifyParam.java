package com.panda.aoodds.sports.os.common;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 属性验证
 */
@Slf4j
public class verifyParam {

    /**
     * 乒乓球校验
     */
    public final static List<String> BASKETBALL_HEAD_VERIFY = Arrays.asList("ftWinnerHandicap", "ftWinnerOdds1", "ftWinnerOdds2");

    /**
     * 忽略
     */
    public final static List<String> IGNORE = Arrays.asList("aoMatchId", "standardMatchId", "matchType");

    /**
     * 篮球球头属性 校验
     *
     * @return
     */
    public static Boolean basketballHeadVerify(String linkId, Properties properties) {
        Boolean isTrue = Boolean.TRUE;
        for (int i = 0; i < BASKETBALL_HEAD_VERIFY.size(); i++) {
            String key = BASKETBALL_HEAD_VERIFY.get(i);
            if (!IGNORE.contains(key) && !properties.containsKey(key)) {
                isTrue = Boolean.FALSE;
                log.info("::{}::【reverseParam】Ao赛事ID:{},球头KEY缺失:{}", linkId, properties.get("aoMatchId"), key);
            }
        }
        return isTrue;
    }


    /**
     * 组装rev球头数据
     *
     * @param linkId
     * @param aoMatchId      赛事ID
     * @param dataSourceCode 数据源
     * @param propertiesMap  缓存数据
     * @return
     */
    public static Properties fusionBallData(String linkId, String aoMatchId, String dataSourceCode, Map<String, Properties> propertiesMap) {
        Properties propertiesNew = new Properties();
        if (MapUtils.isEmpty(propertiesMap)) {
            log.info("::{}::组装rev球头数据，赛事ID：{}，数据源：{}，缓存不存在不处理", linkId, aoMatchId, dataSourceCode);
            return propertiesNew;
        }
        log.info("::{}::组装rev球头数据，赛事ID：{}，数据源：{}，缓存数据：{}", linkId, aoMatchId, dataSourceCode, JSONObject.toJSONString(propertiesMap));
        for (Properties p : propertiesMap.values()) {
            propertiesNew.putAll(p);
        }
        log.info("::{}::组装rev球头数据，赛事ID：{}，数据源：{}，最终返回数据：{}", linkId, aoMatchId, dataSourceCode, JSONObject.toJSONString(propertiesNew));
        return propertiesNew;
    }

}

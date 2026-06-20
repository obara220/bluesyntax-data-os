package com.panda.aoodds.sports.os.common.utils;

import com.panda.aoodds.sports.os.common.constant.RedisKeyConstant;
import com.panda.aoodds.sports.os.service.RedisService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 判断赛事滚球阶段
 */
@Component
public class MatchInLiveUtil {

    @Autowired
    private RedisService redisService;

    /**
     * AO赛事  0=未开赛、1=滚球
     * @param aoMatchId
     * @return
     */
    public Integer matchInLive(String aoMatchId) {
        //0=未开赛、1=滚球 ,融合滚球标识存在就以融合为准，不存在（旧数据），以事件阶段为准
        Integer matchType = 0;
        Object o = redisService.get(RedisKeyConstant.AO_MATCH_IN_LIVE + aoMatchId);
        if (ObjectUtils.isNotEmpty(o)) {
            matchType = (Integer) o;
        }
        return matchType;
    }



}

package com.panda.aoodds.sports.os.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.panda.aoodds.sports.os.api.entity.MarketParamEntiy;
import com.panda.aoodds.sports.os.api.entity.MarketsEntity;
import com.panda.aoodds.sports.os.common.constant.CommonConstant;
import com.panda.aoodds.sports.os.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.panda.aoodds.sports.os.common.constant.CommonConstant.COMPLEX_QUARTER_LIST_ALL;
import static com.panda.aoodds.sports.os.common.constant.CommonConstant.TABLETENNIS_INPLAY_PERIOD;
import static com.panda.aoodds.sports.os.common.constant.RedisKeyConstant.AO_TABLETENNIS_BGTIME_KEY;
import static com.panda.aoodds.sports.os.common.constant.RedisKeyConstant.AO_TABLETENNIS_SCORE_KEY;
@Slf4j
@Component
public class SupportHandle {
    @Autowired
    RedisService redisService;
    public MarketParamEntiy supportScore(MarketParamEntiy marketParamEntiy){
        Integer eventPeriod = matchPeriod(marketParamEntiy.getAoMatchId())[1];
        if(eventPeriod==0){
            return marketParamEntiy;
        }
        Object matchScore = redisService.hGet(AO_TABLETENNIS_SCORE_KEY,marketParamEntiy.getAoMatchId());
        if(Objects.isNull(matchScore)){
            return marketParamEntiy;
        }
        JSONObject jsonScore = JSONObject.parseObject(matchScore.toString());
        String periodId = jsonScore.getString("periodId");
        String firstNum = jsonScore.getString("firstNum");
        String setRoiund = jsonScore.getString("setRoiund");
log.info("supportScore  periodId:{}#scoreInfo:{}",eventPeriod, JSON.toJSONString(matchScore));

        String gameScore1 = formatGameScore(jsonScore.getJSONObject("gameScore1"));
        String gameScore2 = formatGameScore(jsonScore.getJSONObject("gameScore2"));
        String gameScore3 = formatGameScore(jsonScore.getJSONObject("gameScore3"));
        String gameScore4 = formatGameScore(jsonScore.getJSONObject("gameScore4"));
        String gameScore5 = formatGameScore(jsonScore.getJSONObject("gameScore5"));

        String setScore1 = formatGameScore(jsonScore.getJSONObject("setScore1"));
        String setScore2 = formatGameScore(jsonScore.getJSONObject("setScore2"));
        String setScore3 = formatGameScore(jsonScore.getJSONObject("setScore3"));
        String setScore4 = formatGameScore(jsonScore.getJSONObject("setScore4"));
        String setScore5 = formatGameScore(jsonScore.getJSONObject("setScore5"));

        String fullGameScore = formatGameScore(jsonScore.getJSONObject("fullScore"));
        String fullSetScore = formatGameScore(jsonScore.getJSONObject("fullSetScore"));
        if(TABLETENNIS_INPLAY_PERIOD.contains(periodId)){
            JSONObject extraScores = formatPeriodScore(jsonScore.getJSONObject("extraScores"));
            if(null!=extraScores){
                marketParamEntiy.setScore(extraScores.toJSONString());
            }
        }
        marketParamEntiy.setGameOneScore(gameScore1);
        marketParamEntiy.setGameTwoScore(gameScore2);
        marketParamEntiy.setGameThreeScore(gameScore3);
        marketParamEntiy.setGameFourScore(gameScore4);
        marketParamEntiy.setGameFiveScore(gameScore5);
        marketParamEntiy.setGame1Score(setScore1);
        marketParamEntiy.setGame2Score(setScore2);
        marketParamEntiy.setGame3Score(setScore3);
        marketParamEntiy.setGame4Score(setScore4);
        marketParamEntiy.setGame5Score(setScore5);
        marketParamEntiy.setRoiund(setRoiund);
        marketParamEntiy.setFullGameScore(fullGameScore);
        marketParamEntiy.setFullSetScore(fullSetScore);
        log.info("supportScore  periodId:{}#marketParamEntiy:{}",eventPeriod, JSON.toJSONString(marketParamEntiy));
        return marketParamEntiy;
    }

    public JSONObject formatPeriodScore(JSONObject param){
        if(null==param){
            return null;
        }
        JSONObject relust=new JSONObject();
        param.forEach((k,v)->{
            log.info("supportScore k and v:{}#{}",k,JSON.toJSONString(v));
            JSONObject jsonValue = JSONObject.parseObject(v.toString());
            relust.put(k,jsonValue.getIntValue("home")+"-"+jsonValue.getIntValue("away"));
        });
        return relust;
    }
    public String formatGameScore(JSONObject param){
        if(null == param){
            return "0-0";
        }
        return param.getIntValue("home")+"-"+param.getIntValue("away");
    }
    public Integer[] matchPeriod(String aoMatchId){
        Object matchScore = redisService.hGet(AO_TABLETENNIS_BGTIME_KEY,aoMatchId);
        if(Objects.isNull(matchScore)){
            return new Integer[]{1,0};
        }
       String[] periodArr = String.valueOf(matchScore).split("#");
        Integer homeAway = periodArr[0].equals("home")?1:2;
        return new Integer[]{homeAway,Integer.valueOf(periodArr[1])};
    }

    public List<Integer> getCurrentPeriodMarketsList(Integer currentPeriod, String fullGameScore){
        List<Integer> currentPeriodMarketsList = new ArrayList<>();
        //早盘~第一节未结束，展示1、2、3节玩法
        if (currentPeriod <= 8) {
            currentPeriodMarketsList.addAll(CommonConstant.COMPLEX_QUARTER_LIST_1);
            currentPeriodMarketsList.addAll(CommonConstant.COMPLEX_QUARTER_LIST_2);
            currentPeriodMarketsList.addAll(CommonConstant.COMPLEX_QUARTER_LIST_3);
        }
        //第一节结束~第二节开始: 展示第二节、第三节
        if (301 == currentPeriod || 9 == currentPeriod) {
            currentPeriodMarketsList.addAll(CommonConstant.COMPLEX_QUARTER_LIST_2);
            currentPeriodMarketsList.addAll(CommonConstant.COMPLEX_QUARTER_LIST_3);
        }
        //第二节结束~第三节开始: 如果比分是1:1，则展示第三节、第四节; 非1:1，则展示第三节
        if (302 == currentPeriod || 10 == currentPeriod) {
            if ("1-1".equals(fullGameScore)) {
                currentPeriodMarketsList.addAll(CommonConstant.COMPLEX_QUARTER_LIST_3);
                currentPeriodMarketsList.addAll(CommonConstant.COMPLEX_QUARTER_LIST_4);
            } else {
                currentPeriodMarketsList.addAll(CommonConstant.COMPLEX_QUARTER_LIST_3);
            }
        }
        //第三节结束~第四节开始: 如果赛事已结束，不展示任任何玩法; 赛事未结束，则展示第四节
        if (303 == currentPeriod || 11 == currentPeriod) {
            currentPeriodMarketsList.addAll(CommonConstant.COMPLEX_QUARTER_LIST_4);
        }
        //第四节结束~第五节开始: 如果赛事已结束，不展示任任何玩法; 赛事未结束，则展示第五节
        if (304 == currentPeriod || 12 == currentPeriod) {
            currentPeriodMarketsList.addAll(CommonConstant.COMPLEX_QUARTER_LIST_5);
        }
        return currentPeriodMarketsList;
    }

}

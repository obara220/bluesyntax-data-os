package com.panda.aoodds.sports.os.api.entity;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.util.Map;
import java.util.Properties;

@Data
public class MarketParamEntiy implements java.io.Serializable{
     String id;
     /**
      * 链路ID
      */
     private String linkId;
     /**
      * AO赛事ID
      */
     private String aoMatchId;
     /**
      * AO赛事ID
      */
     private String dataSourceCode;

     /**
      * 早盘或者滚球 1 早盘  0滚球
      */
     private Integer preLive;
     /**
      * 乒乓球赛事阶段
      */
     private Integer matchPeriod;
     /**
      * 谁先发球(早盘期间为null) H  A
      */
     private String firstServe;
     /**
      * SUP
      */
     private Double sup;
     private String fullGameScore;
     private String fullSetScore;
     private String roiund;
     /**
      * 发球胜率
      */
     private Double avgServe;
     /**
      * 当前比分详情(0-0,1-0,0-1)
      */
     private String score;
     /**
      * 第一局比分结束后-大分(1-0,0-1)
      */
     private String gameOneScore;
     /**
      * 第二局比分结束后-大分
      */
     private String gameTwoScore;
     /**
      * 第三局比分结束后-大分
      */
     private String gameThreeScore;
     /**
      * 第四局比分结束后-大分
      */
     private String gameFourScore;
     /**
      * 第五局比分结束后-大分
      */
     private String gameFiveScore;

     /**
      * 第一局比分比结束后-小分(11-0,0-11)
      */
     private String game1Score;
     /**
      * 第二局比分比赛中当前局小分
      */
     private String game2Score;
     /**
      * 第三局比分比赛中当前局小分
      */
     private String game3Score;
     /**
      * 第四局比分比赛中当前局小分
      */
     private String game4Score;
     /**
      * 第五局比分比赛中当前局小分
      */
     private String game5Score;
     /**
      * 当前比分详情(0-0,1-0,0-1)
      */
     private Map<Integer,String> scores;

     Integer matchUiStatus;
     /**
      * 0 not aotu ,  1 auto
      */
     Integer aoAuto=0;


     Long modifyTime=System.currentTimeMillis();
     public Properties toProperties() {
          return JSON.parseObject(JSON.toJSONString(this), Properties.class);
     }
}

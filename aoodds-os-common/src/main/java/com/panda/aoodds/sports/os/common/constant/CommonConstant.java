package com.panda.aoodds.sports.os.common.constant;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

public class CommonConstant {

   public static final Integer SPORT_OS = 8;

   public static final String OS_MATCH_MARKET_CONFIG ="os_match_market_config";
   /** 马来转欧赔 、欧赔转马来 配置对应表 */
   public static final String EUROPE_CONVERT_MALAY = "europe_convert_malay";
   public static final String MALAY_CONVERT_EUROPE = "malay_convert_europe";
   public static final String AO_OPT_QUERY_APPLY = "apply";
   public static final String AO_OPT_QUERY_CALC = "calc";
   public static final String AO_OPT_QUERY_REVERSE = "reverse";
   /** 通知风控Apply的赛事 */
   public static final String A01_MATCH_APPLY_INIT = "A01_MATCH_APPLY_INIT";
   /**
    * 三方赛事信息
    */
   public static final String MATCH_INFO = "match_info";
   public static final List<String> TABLETENNIS_INPLAY_PERIOD=Lists.newArrayList("8","9","10","11","12","441","442");

   /**
    * 综合赛种(乒乓球)第一节玩法
    */
   public static final List<Integer> COMPLEX_QUARTER_LIST_1 = Arrays.asList(12006,12007,12008,12009,12010,12011);
   /**
    *  综合赛种(乒乓球)第二节玩法
    */
   public static final List<Integer> COMPLEX_QUARTER_LIST_2 = Arrays.asList(12012,12013,12014,12015,12016,12017);
   /**
    *  综合赛种(乒乓球)第三节玩法
    */
   public static final List<Integer> COMPLEX_QUARTER_LIST_3 = Arrays.asList(12018,12019,12020,12021,12022,12023);
   /**
    *  综合赛种(乒乓球)第四节玩法
    */
   public static final List<Integer> COMPLEX_QUARTER_LIST_4 = Arrays.asList(12024,12025,12026,12027,12028,12029);
   /**
    *  综合赛种(乒乓球)第五节玩法
    */
   public static final List<Integer> COMPLEX_QUARTER_LIST_5 = Arrays.asList(12030,12031,12032,12033,12034,12035);
   /**
    *  综合赛种(乒乓球)所有小节玩法
    */
   public static final List<Integer> COMPLEX_QUARTER_LIST_ALL = Arrays.asList(12006,12007,12008,12009,12010,12011,12012,12013,12014,12015,12016,12017,12018,12019,12020,12021,12022,12023,12024,12025,12026,12027,12028,12029,12030,12031,12032,12033,12034,12035);
}

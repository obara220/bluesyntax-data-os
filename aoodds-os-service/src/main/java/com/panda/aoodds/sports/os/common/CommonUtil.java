package com.panda.aoodds.sports.os.common;

import com.alibaba.fastjson.JSON;
import com.panda.sports.algo.api.enums.HaEnum;

import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommonUtil {
   static List<Integer> SERVE1 = Stream.of(1, 2, 5, 6, 9, 10, 13, 14, 17, 18).collect(Collectors.toList());
   static List<Integer> SERVE2 = Stream.of(3, 4, 7, 8, 11, 12, 15, 16, 19, 20).collect(Collectors.toList());
    public static Map<Integer, Map<String, String>> bulidFirstServeMatrix(String firstServe) {
        Map<Integer, Map<String, String>> map = new HashMap<>();

        Map<String, String> map1 = new HashMap<>();
        map1.put("1", firstServe);
        Map<String, String> map2 = new HashMap<>();
        map2.put("1", firstServe.equals(HaEnum.H.name()) ? HaEnum.A.name() : HaEnum.H.name());
        Map<String, String> map3 = new HashMap<>();
        map3.put("1", firstServe);
        Map<String, String> map4 = new HashMap<>();
        map4.put("1", firstServe.equals(HaEnum.H.name()) ? HaEnum.A.name() : HaEnum.H.name());
        Map<String, String> map5 = new HashMap<>();
        map5.put("1", firstServe);

        for (int i = 2; i <= 40; i++) {
            String key=String.valueOf(i);
            if (SERVE1.contains(i) && i <= 20) {
                map1.put(key, firstServe);
                map2.put(key, firstServe.equals(HaEnum.H.name()) ? HaEnum.A.name() : HaEnum.H.name());
                map3.put(key, firstServe);
                map4.put(key, firstServe.equals(HaEnum.H.name()) ? HaEnum.A.name() : HaEnum.H.name());
                map5.put(key, firstServe);
            } else if (SERVE2.contains(i) && i <= 20) {
                map1.put(key, firstServe.equals(HaEnum.H.name()) ? HaEnum.A.name() : HaEnum.H.name());
                map2.put(key, firstServe);
                map3.put(key, firstServe.equals(HaEnum.H.name()) ? HaEnum.A.name() : HaEnum.H.name());
                map4.put(key, firstServe);
                map5.put(key, firstServe.equals(HaEnum.H.name()) ? HaEnum.A.name() : HaEnum.H.name());
            }
            if (i > 20 && i % 2 != 0) {
                map1.put(key, firstServe);
                map2.put(key, firstServe.equals(HaEnum.H.name()) ? HaEnum.A.name() : HaEnum.H.name());
                map3.put(key, firstServe);
                map4.put(key, firstServe.equals(HaEnum.H.name()) ? HaEnum.A.name() : HaEnum.H.name());
                map5.put(key, firstServe);
            } else if (i > 20 && i % 2 == 0) {
                map1.put(key, firstServe.equals(HaEnum.H.name()) ? HaEnum.A.name() : HaEnum.H.name());
                map2.put(key, firstServe);
                map3.put(key, firstServe.equals(HaEnum.H.name()) ? HaEnum.A.name() : HaEnum.H.name());
                map4.put(key, firstServe);
                map5.put(key, firstServe.equals(HaEnum.H.name()) ? HaEnum.A.name() : HaEnum.H.name());
            }
        }
        map.put(1, map1);
        map.put(2, map2);
        map.put(3, map3);
        map.put(4, map4);
        map.put(5, map5);
        return map;
    }

    public static void main(String[] args) {
       System.out.println(JSON.toJSONString(CommonUtil.bulidFirstServeMatrix("A")));
        String aa="AAAAaab";
        System.out.println(aa.toLowerCase());
    }
}

package com.panda.aoodds.sports.os.handler;

import com.panda.aoodds.sports.os.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.panda.aoodds.sports.os.common.constant.RedisKeyConstant.AO_MAINTAIN_DATA_SOURCE;


@Slf4j
@Component
public class MaintainDataSourceHandler {

    @Autowired
    private RedisService redisService;

    /**
     * 单个数据源判断
     *
     * @param linkId
     * @param aoMatchId
     */
    public Boolean cheack(String linkId, String aoMatchId, String dataSourceCode) {
        dataSourceCode = dataSourceCode.split("-")[0];
        Object maintainDataSourceObj = redisService.hGet(AO_MAINTAIN_DATA_SOURCE, dataSourceCode);
        if (null == maintainDataSourceObj) {
            return false;
        }
        String value = (String) maintainDataSourceObj;
        String[] values = value.split("#");
        Integer enableSwitch = Integer.parseInt(values[0]);//是否启用(0:禁用，1:启用)
        if (enableSwitch == 0) {
            return false;
        }
        Long beginTime = Long.parseLong(values[1]);// 维护开始时间
        Long endTime = Long.parseLong(values[2]);// 维护结束时间
        long nowTime = System.currentTimeMillis();
        boolean b = nowTime >= beginTime && nowTime <= endTime;
        log.info("::{}::数据源维护中，赛事id：{}，dataSourceCode：{} ,b :{} ，value：{} ", linkId, aoMatchId, dataSourceCode, b, value);
        if (b) {
            return true;
        }
        return false;
    }

    /**
     * 所有维护中数据源
     *
     * @param linkId
     * @param aoMatchId
     */
    private List<String> underMaintenance(String linkId, String aoMatchId) {
        List<String> maintainDataSources = new ArrayList<>();
        Map<String, String> maintainDataSourceMap = redisService.hGetAll(AO_MAINTAIN_DATA_SOURCE);
        if (CollectionUtils.isEmpty(maintainDataSourceMap)) {
            return maintainDataSources;
        }
        for (Map.Entry<String, String> entity : maintainDataSourceMap.entrySet()) {
            String key = entity.getKey();
            String value = entity.getValue();
            String[] values = value.split("#");
            Integer enableSwitch = Integer.parseInt(values[0]);//是否启用(0:禁用，1:启用)
            if (enableSwitch == 0) {
                continue;
            }
            Long beginTime = Long.parseLong(values[1]);// 维护开始时间
            Long endTime = Long.parseLong(values[2]);// 维护结束时间
            long nowTime = System.currentTimeMillis();
            boolean b = nowTime >= beginTime && nowTime <= endTime;
            if (b) {
                maintainDataSources.add(key);
            }
        }

        log.info("::{}::批量REV进入，数据源维护中，赛事id：{}，maintainDataSources：{}  ", linkId, aoMatchId, maintainDataSources);
        return maintainDataSources;
    }
}

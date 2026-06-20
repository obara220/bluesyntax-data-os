package com.panda.aoodds.sports.os.service.api;

import com.panda.aoodds.sports.os.api.entity.RequestReverParamEntity;
import com.panda.aoodds.sports.os.api.entity.TTReverseEntity;
import com.panda.aoodds.sports.os.api.service.TableTennisReverseParamServiceApi;
import com.panda.aoodds.sports.os.service.TableTennisReverseParamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@DubboService
public class TableTennisReverseParamServiceApiImpl implements TableTennisReverseParamServiceApi {

    @Autowired
    TableTennisReverseParamService tableTennisReverseParamService;

    @Override
    public TTReverseEntity reverseTableTennisConfig(RequestReverParamEntity requestReverParamEntity, String userId) {
        return tableTennisReverseParamService.reverseTableTennisConfig(requestReverParamEntity, userId);
    }

    @Override
    public void reverseAndApplyTableTennisConfigAuto(RequestReverParamEntity requestReverParamEntity) {
        tableTennisReverseParamService.reverseAndApplyTableTennisConfigAuto(requestReverParamEntity);
    }

    @Override
    public void reverseAndApplyTableTennisConfig(RequestReverParamEntity requestReverParamEntity) {
        tableTennisReverseParamService.reverseAndApplyTableTennisConfig(requestReverParamEntity);
    }
}

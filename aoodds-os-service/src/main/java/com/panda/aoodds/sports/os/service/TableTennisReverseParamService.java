package com.panda.aoodds.sports.os.service;

import com.panda.aoodds.sports.os.api.entity.RequestReverParamEntity;
import com.panda.aoodds.sports.os.api.entity.TTReverseEntity;

public interface TableTennisReverseParamService {

    TTReverseEntity reverseTableTennisConfig(RequestReverParamEntity requestReverParamEntity, String userId);

    void reverseAndApplyTableTennisConfigAuto(RequestReverParamEntity requestReverParamEntity);

    void reverseAndApplyTableTennisConfig(RequestReverParamEntity requestReverParamEntity);


}

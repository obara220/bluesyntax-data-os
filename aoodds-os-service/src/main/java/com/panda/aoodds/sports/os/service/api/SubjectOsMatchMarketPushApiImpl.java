package com.panda.aoodds.sports.os.service.api;

import com.panda.aoodds.sports.os.api.entity.MarketParamEntiy;
import com.panda.aoodds.sports.os.api.service.SubjectOsMatchMarketPushApi;
import com.panda.aoodds.sports.os.service.SubjectOsMatchMarketPushService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@DubboService
public class SubjectOsMatchMarketPushApiImpl implements SubjectOsMatchMarketPushApi {

    @Autowired
    SubjectOsMatchMarketPushService subjectOsMatchMarketPushService;

    @Override
    public void notifyMarketMessage(String matchId, String linkId, String optType, MarketParamEntiy marketParamEntiy) {
        subjectOsMatchMarketPushService.notifyMarketMessage(matchId, linkId, optType, marketParamEntiy);
    }
}

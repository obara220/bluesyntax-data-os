package com.panda.aoodds.sports.os.service;

import com.panda.aoodds.sports.os.api.entity.MarketParamEntiy;

public interface SubjectOsMatchMarketPushService {
    public   void notifyMarketMessage(String matchId, String linkId, String optType, MarketParamEntiy marketParamEntiy);
}

package com.panda.aoodds.sports.os.api.service;

import com.panda.aoodds.sports.os.api.entity.MarketParamEntiy;

public interface SubjectOsMatchMarketPushApi {
    public   void notifyMarketMessage(String matchId, String linkId, String optType, MarketParamEntiy marketParamEntiy);
}

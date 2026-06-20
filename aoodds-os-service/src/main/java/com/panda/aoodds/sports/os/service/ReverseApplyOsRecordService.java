package com.panda.aoodds.sports.os.service;

import com.alibaba.fastjson.JSONObject;
import com.panda.aoodds.sports.os.api.entity.*;

import java.util.List;

public interface ReverseApplyOsRecordService {

    void insertApply(ParamVo<MarketParamEntiy> paramVo);

    void insertRev(RequestReverParamEntity requestReverParamEntity, TTReverseEntity ttReverseEntity, MarketParamEntiy  marketParamEntiy ,String userId);

    List<JSONObject> getLogByAoMatchId(String aoMatchId);

    PageModel<List<JSONObject>> getRecordByMatchAndRequestTypePageList(RecordPageVo recordPageVo);

}

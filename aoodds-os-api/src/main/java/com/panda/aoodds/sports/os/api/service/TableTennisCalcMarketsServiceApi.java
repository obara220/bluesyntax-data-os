package com.panda.aoodds.sports.os.api.service;

import com.alibaba.fastjson.JSONObject;
import com.panda.aoodds.sports.os.api.entity.MarketParamEntiy;
import com.panda.aoodds.sports.os.api.entity.PageModel;
import com.panda.aoodds.sports.os.api.entity.ParamVo;
import com.panda.aoodds.sports.os.api.entity.RecordPageVo;

import java.util.List;

public interface TableTennisCalcMarketsServiceApi {
    public void applyOrcacl(ParamVo<MarketParamEntiy> paramVo);
    public MarketParamEntiy queryMarketParamEntiy(MarketParamEntiy marketParamEntiy);

    /**
     * 面板日志
     * @param aoMatchId
     * @return
     */
    List<JSONObject> getLogByAoMatchId(String aoMatchId);

    /**
     * 操盘后台日志
     * @param recordPageVo
     * @return
     */
    PageModel<List<JSONObject>> getRecordByMatchAndRequestTypePageList(RecordPageVo recordPageVo);
}

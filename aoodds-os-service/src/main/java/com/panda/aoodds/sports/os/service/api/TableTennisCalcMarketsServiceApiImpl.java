package com.panda.aoodds.sports.os.service.api;

import com.alibaba.fastjson.JSONObject;
import com.panda.aoodds.sports.os.api.entity.MarketParamEntiy;
import com.panda.aoodds.sports.os.api.entity.PageModel;
import com.panda.aoodds.sports.os.api.entity.ParamVo;
import com.panda.aoodds.sports.os.api.entity.RecordPageVo;
import com.panda.aoodds.sports.os.api.service.TableTennisCalcMarketsServiceApi;
import com.panda.aoodds.sports.os.service.TableTennisCalcMarketsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@DubboService
public class TableTennisCalcMarketsServiceApiImpl implements TableTennisCalcMarketsServiceApi {

    @Autowired
    TableTennisCalcMarketsService tableTennisCalcMarketsService;

    @Override
    public void applyOrcacl(ParamVo<MarketParamEntiy> paramVo) {
        tableTennisCalcMarketsService.applyOrcacl(paramVo);
    }

    @Override
    public MarketParamEntiy queryMarketParamEntiy(MarketParamEntiy marketParamEntiy) {
        return tableTennisCalcMarketsService.queryMarketParamEntiy(marketParamEntiy);
    }

    @Override
    public List<JSONObject> getLogByAoMatchId(String aoMatchId) {
        return tableTennisCalcMarketsService.getLogByAoMatchId(aoMatchId);
    }

    @Override
    public PageModel<List<JSONObject>> getRecordByMatchAndRequestTypePageList(RecordPageVo recordPageVo) {
        return tableTennisCalcMarketsService.getRecordByMatchAndRequestTypePageList(recordPageVo);
    }
}

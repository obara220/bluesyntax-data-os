package com.panda.aoodds.sports.os.handler;

import com.panda.aoodds.sports.os.common.exception.ApiException;
import com.panda.sports.algo.api.service.MarketsAssembleService;
import com.panda.sports.algo.api.service.ReverseParamService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.stereotype.Component;

import java.util.Properties;
@Component
public class MarketLoadBalanceHandler {
    @DubboReference(check = false)
    private MarketsAssembleService marketsAssembleService;
    @DubboReference(check = false)
    private ReverseParamService reverseParamService;
    public String  getMarkets(Properties var1, String var2, String var3){
        if("999".equals(var1.getProperty("matchPeriod"))){
            throw new ApiException("赛事阶段性结束！"+var1.getProperty("matchPeriod"));
        }
        String marketsVersion=var1.getProperty("lineVersion","os");
        RpcContext.getContext().setAttachment("dubbo.tag","os");
        return marketsAssembleService.getTtMarkets(var1,var2);
    }
    public String  getReverseParam(Properties var1){
        if("999".equals(var1.getProperty("matchPeriod"))){
            throw new ApiException("赛事阶段性结束！"+var1.getProperty("matchPeriod"));
        }
        String marketsVersion=var1.getProperty("lineVersion","os");
        RpcContext.getContext().setAttachment("dubbo.tag","os");
        return reverseParamService.reverseTableTennisParam(var1);
    }
}

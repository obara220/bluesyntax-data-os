package com.panda.aoodds.sports.os.producer;

import com.panda.aoodds.sports.api.entity.Request;
import com.panda.aoodds.sports.os.api.entity.AoMatchMarketInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OsMarketMessageProducer {
    @Autowired
    private RocketMQTemplate rocketMqTemplate;
    public void sendMarketMessage(AoMatchMarketInfo aoMatchMarketInfo) {
        if(null == aoMatchMarketInfo){
            return;
        }
        Request<AoMatchMarketInfo> request = new Request<>();
        String linkId = aoMatchMarketInfo.getLinkeId();
        request.setLinkId(linkId);
        request.setData(aoMatchMarketInfo);
        request.setDataSourceTime(System.currentTimeMillis());
        MessageBuilder<Request<AoMatchMarketInfo>> builder = MessageBuilder.withPayload(request).setHeader(MessageConst.PROPERTY_KEYS, linkId);

        //第一个参数表示topic:tag
        rocketMqTemplate.asyncSend("AO_OS_MARKET_ODDS:" + aoMatchMarketInfo.getMatchSourceId(), builder.build(), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("::{}::,send successful", linkId);
            }
            @Override
            public void onException(Throwable throwable) {
                log.error("::{}::TOPIC={}，send fail; ", linkId, "AO_OS_MARKET_ODDS", throwable);
            }
        });
    }
    public void sendWsMarketMessage(AoMatchMarketInfo aoMatchMarketInfo) {
        if(null == aoMatchMarketInfo){
            return;
        }
        Request<AoMatchMarketInfo> request = new Request<>();
        String linkId = aoMatchMarketInfo.getLinkeId();
        request.setLinkId(linkId);
        request.setData(aoMatchMarketInfo);
        request.setDataSourceTime(System.currentTimeMillis());
        MessageBuilder<Request<AoMatchMarketInfo>> builder = MessageBuilder.withPayload(request).setHeader(MessageConst.PROPERTY_KEYS, linkId);

        //第一个参数表示topic:tag
        rocketMqTemplate.asyncSend("AO_OS_WS_MARKET_ODDS:" + aoMatchMarketInfo.getMatchSourceId(), builder.build(), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("::{}::,send successful", linkId);
            }
            @Override
            public void onException(Throwable throwable) {
                log.error("::{}::TOPIC={}，send fail; ", linkId, "AO_OS_WS_MARKET_ODDS", throwable);
            }
        });
    }
}

package com.panda.aoodds.sports.os.producer;

import com.panda.aoodds.sports.os.common.entity.AutoApplyMarketOddsErrorMsgEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AutoApplyMarketOddsErrorProducer {
    @Autowired
    private RocketMQTemplate rocketMqTemplate;

    public void sendStandardMatchIds(AutoApplyMarketOddsErrorMsgEntity autoApplyMarketOddsErrorMsgEntity) {
        MessageBuilder<AutoApplyMarketOddsErrorMsgEntity> builder = MessageBuilder.withPayload(autoApplyMarketOddsErrorMsgEntity).setHeader(MessageConst.PROPERTY_KEYS, autoApplyMarketOddsErrorMsgEntity.getLinkId());
        rocketMqTemplate.send("AO_PRE_REVERSE_MATCHS", builder.build());
    }
}

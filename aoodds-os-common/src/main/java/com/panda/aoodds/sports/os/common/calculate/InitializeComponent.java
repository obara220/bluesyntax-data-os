package com.panda.aoodds.sports.os.common.calculate;

import com.panda.aoodds.sports.os.common.constant.CommonConstant;
import com.panda.aoodds.sports.os.common.entity.EuropeConvertMalay;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 转换
 */
@Component
public class InitializeComponent implements CommandLineRunner {

    @Autowired
    @Qualifier("producerMongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * 欧赔转马来赔缓存
     */
    @Getter
    private Map<Double, Double> europeConvertMalayMap = new HashMap<>();

    /**
     * 马来转欧赔缓存
     */
    @Getter
    private Map<Double, Double> malayConvertEuropeMap = new HashMap<>();


    @Override
    public void run(String... args) {
        //-----------初始化欧赔转马来的对应关系-----------
        europeConvertMalayMap = mongoTemplate.findAll(EuropeConvertMalay.class, CommonConstant.EUROPE_CONVERT_MALAY).stream().collect(Collectors.toMap(EuropeConvertMalay::getEuropeValue, EuropeConvertMalay::getMalayValue));
        //-----------初始化马来转欧赔的对应关系-----------
        malayConvertEuropeMap = mongoTemplate.findAll(EuropeConvertMalay.class, CommonConstant.MALAY_CONVERT_EUROPE).stream().collect(Collectors.toMap(EuropeConvertMalay::getMalayValue, EuropeConvertMalay::getEuropeValue));
    }

    /**
     * 马来转换欧赔
     *
     * @param malayOddsValue
     * @return
     */
    public Double getConvertMalayToEurope(Double malayOddsValue) {
        Double europeValue = this.getMalayConvertEuropeMap().get(malayOddsValue);
        if (null == europeValue) {
            europeValue = 0D;
        }
        return europeValue;
    }

}

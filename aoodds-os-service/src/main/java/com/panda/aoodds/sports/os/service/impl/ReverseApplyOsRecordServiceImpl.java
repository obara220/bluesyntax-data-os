package com.panda.aoodds.sports.os.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.panda.aoodds.sports.os.api.entity.*;
import com.panda.aoodds.sports.os.common.constant.CommonConstant;
import com.panda.aoodds.sports.os.common.entity.AoMatchInfoEntity;
import com.panda.aoodds.sports.os.service.RedisService;
import com.panda.aoodds.sports.os.service.ReverseApplyOsRecordService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.panda.aoodds.sports.os.common.constant.CommonConstant.AO_OPT_QUERY_REVERSE;

@Slf4j
@Service
public class ReverseApplyOsRecordServiceImpl implements ReverseApplyOsRecordService {
    @Autowired
    @Qualifier("producerMongoTemplate")
    private MongoTemplate mongoTemplate;
    @Autowired
    private RedisService redisService;

    @Override
    public void insertApply(ParamVo<MarketParamEntiy> paramVo) {
        JSONObject object = new JSONObject();
        object.put("aoMatchId", paramVo.getParam().getAoMatchId());//ao赛事id
        object.put("matchManageId", paramVo.getMatchManageId());//管理赛事id
        object.put("matchPeriod", paramVo.getParam().getPreLive());//早盘或者滚球 1 早盘  0滚球
        object.put("requestType", paramVo.getType());// apply 或者 rev
        object.put("userName", paramVo.getUserName()); //操作人
        object.put("sup", paramVo.getParam().getSup());// sup
        object.put("avgServe", paramVo.getParam().getAvgServe());//avgServe
        object.put("aoAuto", paramVo.getParam().getAoAuto());//
        object.put("dataSourceCode", paramVo.getParam().getDataSourceCode());//
        object.put("createTime", System.currentTimeMillis());//请求时间
        mongoTemplate.save(object, "reverse_apply_os_record");
    }


    @Override
    public void insertRev(RequestReverParamEntity requestReverParamEntity, TTReverseEntity ttReverseEntity, MarketParamEntiy marketParamEntiy, String userId) {
        JSONObject object = new JSONObject();
        object.put("aoMatchId", requestReverParamEntity.getAoMatchId());//ao赛事id
        object.put("matchPeriod", null == marketParamEntiy ? 1 : marketParamEntiy.getPreLive());//早盘或者滚球 1 早盘  0滚球
        object.put("requestType", AO_OPT_QUERY_REVERSE);// apply 或者 rev
        object.put("userName", userId); //操作人
        object.put("aoAuto", requestReverParamEntity.getAoAuto());//
        object.put("sup", ttReverseEntity.getSup());// sup
        object.put("odds", ttReverseEntity.getWinHomeOdds() + "/" + ttReverseEntity.getWinAwayOdds());
        object.put("dataSourceCode", requestReverParamEntity.getDataSourceCode());//
        object.put("createTime", System.currentTimeMillis());//请求时间
        mongoTemplate.save(object, "reverse_apply_os_record");
    }

    public List<JSONObject> getLogByAoMatchId(String aoMatchId) {
        Query query = Query.query(Criteria.where("aoMatchId").is(aoMatchId));
        query.with(Sort.by(Sort.Order.desc("createTime"))).limit(1000);
        List<JSONObject> osRecords = mongoTemplate.find(query, JSONObject.class, "reverse_apply_os_record");
        if (CollectionUtils.isEmpty(osRecords)) {
            return osRecords;
        }
        String str = JSONObject.toJSONString(osRecords).replace("@type", "errortype");
        return JSONObject.parseArray(str, JSONObject.class);
    }


    @SneakyThrows
    @Override
    public PageModel<List<JSONObject>> getRecordByMatchAndRequestTypePageList(RecordPageVo recordPageVo) {
        String objectId = recordPageVo.getObjectId();
        String objectName = recordPageVo.getObjectName();
        String behavior = recordPageVo.getBehavior();
        String operateStartTime = recordPageVo.getOperateStartTime();
        String operateEndTime = recordPageVo.getOperateEndTime();
        String userName = recordPageVo.getUserName();
        String extObjectId = recordPageVo.getExtObjectId();
        String extObjectName = recordPageVo.getExtObjectName();
        Integer pageNum = recordPageVo.getPageNum();
        Integer pageSize = recordPageVo.getPageSize();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        List<JSONObject> jsons = new ArrayList<>();
        // 创建分页对象
        PageRequest page = PageSort.pageRequest(pageNum, pageSize);
        Query query = new Query();
        if (StringUtils.isBlank(objectId)) {
            return new PageModel(pageNum, pageSize, 0, jsons);
        }
        AoMatchInfoEntity aoMatchInfoEntity = mongoTemplate.findOne(Query.query(Criteria.where("matchManageId").is(objectId)), AoMatchInfoEntity.class, CommonConstant.MATCH_INFO);
        if (null != aoMatchInfoEntity) {
            query.addCriteria(Criteria.where("aoMatchId").is(aoMatchInfoEntity.getAoMatchId() + ""));
        } else {
            return new PageModel(pageNum, pageSize, 0, jsons);
        }
        if (StringUtils.isNotBlank(objectName)) {
            query.addCriteria(Criteria.where("requestType").is(objectName));
        }
        if (StringUtils.isNotBlank(behavior)) {
            if (behavior.contains("赛前")) {
                query.addCriteria(Criteria.where("matchPeriod").lt(1));
            } else {
                query.addCriteria(Criteria.where("matchPeriod").gt(0));
            }
        }
        if (StringUtils.isNotBlank(userName)) {
            query.addCriteria(Criteria.where("userName").is(userName));
        }

        if (StringUtils.isNotBlank(operateStartTime) && StringUtils.isNotBlank(operateEndTime)) {
            Date date = formatter.parse(operateStartTime);
            long startTime = date.getTime();
            Date endDate = formatter.parse(operateEndTime);
            long endTime = endDate.getTime();
            query.addCriteria(Criteria.where("createTime").gte(startTime).lte(endTime));
        } else if (StringUtils.isNotBlank(operateStartTime)) {
            Date date = formatter.parse(operateStartTime);
            long time = date.getTime();
            query.addCriteria(Criteria.where("createTime").gte(time));
        } else if (StringUtils.isNotBlank(operateEndTime)) {
            Date date = formatter.parse(operateEndTime);
            long time = date.getTime();
            query.addCriteria(Criteria.where("createTime").lte(time));
        }
        query.with(Sort.by(Sort.Order.desc("createTime")));
        //计算总数
        Long total = (Long) mongoTemplate.count(query, JSONObject.class, "reverse_apply_os_record");
        //查询结果集
        List<JSONObject> basketballApplyParams = mongoTemplate.find(query.with(page), JSONObject.class, "reverse_apply_os_record");
        if (!CollectionUtils.isEmpty(basketballApplyParams)) {
            basketballApplyParams.forEach(reverseApplyRecord -> {
                JSONObject json = new JSONObject();
                //操作页面
                json.put("operatePageName", "AO TT");
                //操作对象ID
                json.put("objectId", reverseApplyRecord.getString("matchManageId"));
                //操作对象名称
                json.put("objectName", "o_goal");

                json.put("userName", reverseApplyRecord.getString("userName"));
                json.put("operateTimeStr", reverseApplyRecord.getString("createTime"));
                //操作类型
                String marketType = Integer.valueOf(reverseApplyRecord.getOrDefault("matchPeriod", 1).toString()) == 1 ? "PRE" : "INPLAY";
                String requestType = reverseApplyRecord.getString("requestType");
                if (requestType.equals("reverse")) {
                    requestType = "Rev_" + reverseApplyRecord.getString("dataSourceCode");
                }
                String auto = "";
                if (reverseApplyRecord.containsKey("aoAuto") && 1 == reverseApplyRecord.getInteger("aoAuto")) {
                    auto = "Auto_";
                }
                json.put("behavior", auto + requestType + "(" + marketType + ")");
                json.put("extObjectId", "A"); //自动手动
                json.put("extObjectName", "");
                String parameterFt = "SUP:" + reverseApplyRecord.getString("sup") + "@" + reverseApplyRecord.get("avgServe") + "@" + reverseApplyRecord.getString("odds");
                //参数名称
                json.put("parameterName", parameterFt);
                json.put("beforeVal", "");
                json.put("afterVal", "");
                jsons.add(json);
            });
        }
        return new PageModel(pageNum, pageSize, total, jsons);
    }

}

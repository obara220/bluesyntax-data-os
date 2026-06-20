package com.panda.aoodds.sports.os.common.entity;

import lombok.Data;

import java.util.List;

@Data
public class AutoApplyMarketOddsErrorMsgEntity implements java.io.Serializable{
    String linkId;
    List<Long> matchIds;

    Long dataTime;
    Integer sportId;
    String tempType;
}

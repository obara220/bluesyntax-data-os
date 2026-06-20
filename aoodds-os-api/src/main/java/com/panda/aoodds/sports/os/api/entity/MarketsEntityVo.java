package com.panda.aoodds.sports.os.api.entity;


import lombok.Data;

import java.util.List;


@Data
public class MarketsEntityVo implements java.io.Serializable {

    Integer marketId;
    String marketName;
    String handicap;
    Integer status;
    Integer order;
    Double realTimeMargin;
    Long modifyTime;
    List<BetPieceEntityVo> betPieceEntities;


}

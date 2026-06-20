package com.panda.aoodds.sports.os.api.entity;

import lombok.Data;

@Data
public class RequestReverParamEntity implements java.io.Serializable {
    String aoMatchId;
    String dataSourceCode;
    String linkId;
    Integer matchUiStatus;
    /**
     * 0 not aotu ,  1 auto
     */
    Integer aoAuto;
}

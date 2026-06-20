package com.panda.aoodds.sports.os.api.entity;

import lombok.Data;

@Data
public class ParamVo<T> implements java.io.Serializable{


    T param;

    /**
     * 操作人姓名
     */
    String userName;
    /**
     * 操作人ID
     */
    Long userId;

    String matchManageId;
    String type;
}

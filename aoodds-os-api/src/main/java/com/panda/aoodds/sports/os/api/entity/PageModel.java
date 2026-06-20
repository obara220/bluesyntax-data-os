
package com.panda.aoodds.sports.os.api.entity;

import lombok.Data;

import javax.validation.Valid;
import java.io.Serializable;


@Data
public class PageModel<T> implements Serializable {
    private static final long serialVersionUID = 8545996863226528798L;
    /**
     * 当前页
     */
    private Integer pageNum = 1;

    /**
     * 每页显示条数，默认 10
     */
    private Integer pageSize = 10;

    /**
     * 总数
     */
    private long total = 0;


    /**
     * 查询数据列表
     */
    @Valid
    private T list;

    public PageModel() {
    }

    public PageModel(Integer pageNum, Integer pageSize, long total, @Valid T list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.list = list;
    }

}

package com.panda.aoodds.sports.os.api.entity;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * 分页排序数据
 *
 * @author 小懒虫
 * @date 2018/12/8
 */
public class PageSort {

    private static final Integer PAGE_SIZE_DEF = 10;
    private static final String ORDER_BY_COLUMN_DEF = "createDate";
    private static final Sort.Direction SORT_DIRECTION = Sort.Direction.DESC;

    /**
     * 创建分页排序对象
     *
     * @param pageSizeDef      分页数据数量默认值
     * @param orderByColumnDef 排序字段名称默认值
     * @param sortDirection    排序方式默认值
     */
    public static PageRequest pageRequest(int pageNum, int pageSize) {
        return PageRequest.of(pageNum - 1, pageSize);
    }
}

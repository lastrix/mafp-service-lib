package org.lastrix.rest;

import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@SuppressWarnings("unused")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString(callSuper = true)
public class Pagination {
    @Min(0)
    private int page = 0;
    @Min(1)
    @Max(100)
    private int pageSize = 20;
    private Integer pageCount;
    private Long totalCount;

    public Pagination(Pagination pagination) {
        setPage(pagination.getPage());
        setPageSize(pagination.getPageSize());
        setPageCount(pagination.getPageCount());
        setTotalCount(pagination.getTotalCount());
    }

    public PageRequest toPageable() {
        return toPageable(Sort.unsorted());
    }

    public PageRequest toPageable(Sort sort) {
        return PageRequest.of(page, pageSize, sort);
    }

    public Pagination nextPagination() {
        var p = new Pagination(this);
        p.setPage(p.getPage() + 1);
        return p;
    }

}

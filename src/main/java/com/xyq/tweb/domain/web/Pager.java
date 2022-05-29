package com.xyq.tweb.domain.web;

public class Pager {

    private Integer page;

    private Integer pageSize;

    private Integer total;

    public Integer getPage() {
        return page;
    }

    public Pager setPage(Integer page) {
        this.page = page;
        return this;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public Pager setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public Integer getTotal() {
        return total;
    }

    public Pager setTotal(Integer total) {
        this.total = total;
        return this;
    }

    @Override
    public String toString() {
        return "Pager{" +
                "page=" + page +
                ", pageSize=" + pageSize +
                ", total=" + total +
                '}';
    }
}

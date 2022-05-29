package com.xyq.tweb.domain.web;

import com.xyq.tweb.util.ServletUtils;
import org.springframework.util.StringUtils;

import java.util.List;

public class ListPager<T> {

    private List<T> list;

    private Pager pager;

    public ListPager() {
    }

    public ListPager(List<T> list) {
        this.list = list;
    }

    public ListPager(List<T> list, Integer page, Integer pageSize, Integer total) {
        this.list = list;
        this.pager = new Pager().setPage(page).setPageSize(pageSize).setTotal(total);
    }

    public ListPager(List<T> list, Integer page, Integer pageSize, Long total) {
        this.list = list;
        this.pager = new Pager().setPage(page).setPageSize(pageSize).setTotal(total.intValue());
    }

    public ListPager(List<T> list, Long total) {
        this.list = list;
        Pager pager = new Pager().setTotal(total.intValue());
        String page = ServletUtils.getRequest().getParameter("page");
        if (!StringUtils.isEmpty(page)) {
            pager.setPage(Integer.valueOf(page));
        }
        String pageSize = ServletUtils.getRequest().getParameter("pageSize");
        if (!StringUtils.isEmpty(pageSize)) {
            pager.setPageSize(Integer.valueOf(pageSize));
        }
        this.pager = pager;
    }

    public List<T> getList() {
        return list;
    }

    public ListPager<T> setList(List<T> list) {
        this.list = list;
        return this;
    }

    public Pager getPager() {
        return pager;
    }

    public ListPager<T> setPager(Pager pager) {
        this.pager = pager;
        return this;
    }

}

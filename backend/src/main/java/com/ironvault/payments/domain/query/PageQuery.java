package com.ironvault.payments.domain.query;

public class PageQuery {

    private final int page;
    private final int size;

    public PageQuery(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }
}

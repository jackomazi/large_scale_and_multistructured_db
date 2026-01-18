package it.unipi.chessApp.dto;

import java.util.List;

public class PageDTO<T> {

    private List<T> entries;
    private int totalCount;

    public PageDTO(int totalCount, List<T> entries) {
        this.totalCount = totalCount;
        this.entries = entries;
    }

    public List<T> getEntries() {
        return entries;
    }

    public void setEntries(List<T> entries) {
        this.entries = entries;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    public String toString() {
        return "PageDTO{" +
                "entries=" + entries +
                ", totalCount=" + totalCount +
                '}';
    }
}

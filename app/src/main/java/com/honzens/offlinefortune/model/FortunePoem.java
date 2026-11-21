package com.honzens.offlinefortune.model;

public class FortunePoem {
    public final long id;
    public final int lotNo;
    public final String rank;
    public final String poemText;

    public FortunePoem(long id, int lotNo, String rank, String poemText) {
        this.id = id;
        this.lotNo = lotNo;
        this.rank = rank == null ? "" : rank;
        this.poemText = poemText == null ? "" : poemText;
    }

    @Override
    public String toString() {
        return "第" + lotNo + "籤" + (rank.isEmpty() ? "" : "（" + rank + "）");
    }
}

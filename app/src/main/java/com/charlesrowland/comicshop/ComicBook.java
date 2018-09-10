package com.charlesrowland.comicshop;

/**
 * This class will be used in part2 when I pull from the comic vine api. for now, its just here
 * as prep.
 */
public class ComicBook {
    public String mVolume;
    public String mName;
    public int mIssueNum;
    public String mReleaseDate;
    public String mCoverType;
    public Double mPrice;
    public int mQuantity;
    public String mPublisher;

    public ComicBook(String mVolume, String mName, int mIssueNum, String mReleaseDate, String mCoverType, Double mPrice, int mQuantity, String mPublisher) {
        this.mVolume = mVolume;
        this.mName = mName;
        this.mIssueNum = mIssueNum;
        this.mReleaseDate = mReleaseDate;
        this.mCoverType = mCoverType;
        this.mPrice = mPrice;
        this.mQuantity = mQuantity;
        this.mPublisher = mPublisher;
    }

    public String getmVolume() {
        return mVolume;
    }

    public String getmName() {
        return mName;
    }

    public int getmIssueNum() {
        return mIssueNum;
    }

    public String getmReleaseDate() {
        return mReleaseDate;
    }

    public String getmCoverType() {
        return mCoverType;
    }

    public Double getmPrice() {
        return mPrice;
    }

    public int getmQuantity() {
        return mQuantity;
    }

    public String getmPublisher() {
        return mPublisher;
    }
}

package com.example.lyy.airsteward.card;


public class CardItem {

    private String mTextResource;
    private String mTitleResource;

    private float mprogress;

    public float getProgress() {
        return mprogress;
    }

    public CardItem(String title, String text, int progress) {
        mTitleResource = title;
        mTextResource = text;
        mprogress=progress;
    }

    public String getText() {
        return mTextResource;
    }

    public String getTitle() {
        return mTitleResource;
    }
}

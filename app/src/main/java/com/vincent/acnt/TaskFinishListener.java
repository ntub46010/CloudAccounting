package com.vincent.acnt;

public interface TaskFinishListener {
    void onFinish();
    void onFailure(Exception e);
}

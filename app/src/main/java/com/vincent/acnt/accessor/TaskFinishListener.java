package com.vincent.acnt.accessor;

public interface TaskFinishListener {
    void onFinish();
    void onFailure(Exception e);
}

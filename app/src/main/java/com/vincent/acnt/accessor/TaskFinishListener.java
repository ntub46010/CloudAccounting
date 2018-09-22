package com.vincent.acnt.accessor;

public interface TaskFinishListener {
    void onSuccess();
    void onFailure(Exception e);
}

package com.vincent.acnt.accessor;

import com.vincent.acnt.entity.Entity;

import java.util.List;

public interface RetrieveEntitiesListener {
    void onRetrieve(List<? extends Entity> entities);
    void onFailure(Exception e);
}

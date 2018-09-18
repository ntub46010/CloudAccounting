package com.vincent.acnt.accessor;

import com.vincent.acnt.entity.Entity;

public interface RetrieveEntityListener {
    void onRetrieve(Entity entity);
    void onFailure(Exception e);
}

package com.panni.mymusicplayer2.controller;

import objects.DbObject;

/**
 * Created by marco on 21/05/16.
 */
public interface DataCallback {

    void newData(DbObject [] objects);
}

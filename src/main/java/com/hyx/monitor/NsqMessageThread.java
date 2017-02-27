package com.hyx.monitor;

/**
 * Created by harvey on 2016/12/5.
 */
public abstract class NsqMessageThread  {

    public abstract void handlerMessage(String message);
}

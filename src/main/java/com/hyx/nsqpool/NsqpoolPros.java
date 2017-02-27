package com.hyx.nsqpool;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by Administrator on 2016/7/5.
 */
public class NsqpoolPros {
    private static ResourceBundle localResource = null;

    static {
        Locale locale = Locale.getDefault();
        localResource = ResourceBundle.getBundle("nsqpool", locale);
    }

    public static String getString(String key) {
        if (localResource != null) {
            return localResource.getString(key);
        }
        System.err.println("NSQ_CLIENT_PROXY:请检查是项目根目录否有配置文件:nsqpool.properties");
        return null;
    }
}

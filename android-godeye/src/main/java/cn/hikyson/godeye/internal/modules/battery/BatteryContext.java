package cn.hikyson.godeye.internal.modules.battery;

import android.content.Context;

/**
 * Created by kysonchao on 2017/11/24.
 */

public interface BatteryContext {
    Context context();
    long intervalMillis();
}

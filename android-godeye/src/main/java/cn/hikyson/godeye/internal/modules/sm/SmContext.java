package cn.hikyson.godeye.internal.modules.sm;

import android.content.Context;

import cn.hikyson.godeye.internal.modules.sm.core.SmConfig;

/**
 * Created by kysonchao on 2017/11/24.
 */
public interface SmContext {
    Context context();
    SmConfig config();
}

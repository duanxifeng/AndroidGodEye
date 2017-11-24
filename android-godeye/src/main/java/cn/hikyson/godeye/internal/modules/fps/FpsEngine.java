package cn.hikyson.godeye.internal.modules.fps;

import android.content.Context;
import android.view.Choreographer;
import android.view.Display;
import android.view.WindowManager;

import java.util.concurrent.TimeUnit;

import cn.hikyson.godeye.internal.Engine;
import cn.hikyson.godeye.internal.Producer;
import cn.hikyson.godeye.utils.L;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by kysonchao on 2017/11/23.
 */
public class FpsEngine implements Engine {
    private Context mContext;
    private Producer<FpsInfo> mProducer;
    private long mIntervalMillis;
    private CompositeDisposable mCompositeDisposable;

    public FpsEngine(Context context, Producer<FpsInfo> producer, long intervalMillis) {
        mContext = context;
        mProducer = producer;
        mIntervalMillis = intervalMillis;
        mCompositeDisposable = new CompositeDisposable();
    }

    @Override
    public void work() {
        mCompositeDisposable.add(Observable.interval(mIntervalMillis, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).
                concatMap(new Function<Long, ObservableSource<FpsInfo>>() {
                    @Override
                    public ObservableSource<FpsInfo> apply(Long aLong) throws Exception {
                        return create();
                    }
                }).subscribe(new Consumer<FpsInfo>() {
            @Override
            public void accept(FpsInfo food) throws Exception {
                mProducer.produce(food);
            }
        }));
    }

    @Override
    public void shutdown() {
        mCompositeDisposable.dispose();
    }

    private Observable<FpsInfo> create() {
        final float systemRate = getRefreshRate(mContext);
        final Choreographer choreographer = Choreographer.getInstance();
        return Observable.create(new ObservableOnSubscribe<Long>() {
            @Override
            public void subscribe(final ObservableEmitter<Long> e) throws Exception {
                choreographer.postFrameCallback(new Choreographer.FrameCallback() {
                    @Override
                    public void doFrame(long frameTimeNanos) {
                        e.onNext(frameTimeNanos);
                    }
                });
            }
        }).concatMap(new Function<Long, ObservableSource<FpsInfo>>() {
            @Override
            public ObservableSource<FpsInfo> apply(final Long startTimeNanos) throws Exception {
                return Observable.create(new ObservableOnSubscribe<FpsInfo>() {
                    @Override
                    public void subscribe(final ObservableEmitter<FpsInfo> e) throws Exception {
                        choreographer.postFrameCallback(new Choreographer.FrameCallback() {
                            @Override
                            public void doFrame(long frameTimeNanos) {
                                long frameInterval = frameTimeNanos - startTimeNanos;//计算两帧的时间间隔
                                e.onNext(new FpsInfo((float) (1000000000 / frameInterval), systemRate));
                            }
                        });
                    }
                });
            }
        });
    }

    private static float getRefreshRate(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        return display.getRefreshRate();
    }
}

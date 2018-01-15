package com.hexing.upgrade.utils;

import android.support.annotation.NonNull;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by HEC271
 * on 2017/12/1.
 *
 * @author HEC271
 *         线程池
 */

public class HexThreadManager {

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAXIMUM_POOL_SIZE = 128;
    private static final int KEEP_ALIVE = 20;
    private static HexThreadManager instance;

    /**
     * 新建一个队列用来存放线程
     */
    private static final BlockingQueue<Runnable> WORK_QUEUE = new LinkedBlockingQueue<>(10);

    /**
     * 新建一个线程工厂
     */
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "HexThread #" + mCount.getAndIncrement());
        }
    };

    /**
     * 新建一个线程池执行器,用于管理线程的执行
     */
    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, WORK_QUEUE, THREAD_FACTORY);

    /**
     * 获取实例对象
     *
     * @return instance
     */
    public static HexThreadManager getInstance() {
        if (instance == null) {
            synchronized (HexThreadManager.class) {
                if (instance == null) {
                    instance = new HexThreadManager();
                }
            }
        }
        return instance;
    }

    /**
     * 获取 ThreadPoolExecutor
     *
     * @return ThreadPoolExecutor
     */
    public ThreadPoolExecutor getExecutor() {
        return EXECUTOR;
    }

}

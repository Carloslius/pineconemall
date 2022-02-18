package plus.carlosliu.pineconemall.search.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadTest {

    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main......start.......");

//        CompletableFuture.runAsync(()->{
//            System.out.println("当前线程:" + Thread.currentThread().getName());
//            int i = 17 / 2;
//            System.out.println("运行结果" + i);
//        }, executor);

        // 方法执行完成后的感知
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程:" + Thread.currentThread().getName());
//            int i = 17 / 0;
//            System.out.println("运行结果" + i);
//            return i;
//        }, executor).whenComplete((res, exception) -> {
//            // 可以感知异常信息，但是没法修改返回值
//            System.out.println("异步任务完成了...结果是：" + res + "异常是：" + exception);
//        }).exceptionally(throwable -> {
//            // 可以感知异常，同时返回默认值
//            return 8;
//        });
//        // 阻塞获得结果
//        Integer integer = future.get();

        // 方法执行完成以后的处理
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程:" + Thread.currentThread().getName());
//            int i = 17 / 2;
//            System.out.println("运行结果" + i);
//            return i;
//        }, executor).handle((res, thr) -> {
//            if (res != null){
//                return res * 2;
//            }
//            if (thr != null){
//                return 0;
//            }
//            return 0;
//        });
//        Integer integer = future.get();

        /**
         * 线程串行化：
         *      1、无法接收上一步结果，无返回值
         *         .thenRunAsync(() -> {
         *             System.out.println("任务2启动了...");
         *         }, executor);
         *      2、能接收上一步结果，但是无返回值
         *         .thenAcceptAsync(res -> {
         *             System.out.println("任务2启动了..." + res);
         *         }, executor);
         */
//        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程:" + Thread.currentThread().getName());
//            int i = 17 / 2;
//            System.out.println("运行结果" + i);
//            return i;
//        }, executor).thenApplyAsync(res -> {
//            System.out.println("任务2启动了..." + res);
//            return "Hello" + res;
//        }, executor);
//        String s = future.get();


//        CompletableFuture<Object> future1 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务1线程:" + Thread.currentThread().getName());
//            int i = 17 / 2;
//            System.out.println("任务1结束" + i);
//            return i;
//        }, executor);
//        CompletableFuture<Object> future2 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务2线程:" + Thread.currentThread().getName());
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("任务2结束");
//            return "Hello";
//        }, executor);
        /**
         * 两个都完成，才执行任务3
         *      future1.runAfterBothAsync(future2, , executor);
         *      future1.thenAcceptBothAsync(future2, (f1, f2) -> {}, executor);
         */
//        String s = future1.thenCombineAsync(future2, (f1, f2) -> {
//            System.out.println("任务3开始...");
//            return "Hello" + f1 + f2;
//        }, executor).get();
        /**
         * 两个中只要有一个完成，就执行任务3
         *      future1.runAfterEitherAsync(future2, , executor);
         *      future1.acceptEitherAsync(future2, res -> {}, executor);
         */
//        CompletableFuture<String> future = future1.applyToEitherAsync(future2, res -> {
//            System.out.println("任务3开始...");
//            return res.toString() + "lys";
//        }, executor);
//        String s = future.get();


        /**
         * 多任务组合
         */
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("查看商品图片信息");
            return "hello.png";
        }, executor);
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品属性信息");
            return "8GB+256GB";
        }, executor);
        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("查询商品介绍");
                Thread.sleep(3000);
                System.out.println("查询商品介绍成功");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "华为";
        }, executor);
//        CompletableFuture<Void> allOf = CompletableFuture.allOf(future1, future2, future3);
//        allOf.get(); // 等待所有结果完成
        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(future3, future2, future1);
        anyOf.get(); // 等待一个结果完成

//        System.out.println("main......end......." + future1.get() + future2.get() + future3.get());
        System.out.println("main......end......." + anyOf.get());
    }
}

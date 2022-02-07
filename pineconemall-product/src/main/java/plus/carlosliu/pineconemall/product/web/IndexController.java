package plus.carlosliu.pineconemall.product.web;

import com.alibaba.fastjson.JSON;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import plus.carlosliu.common.to.MemberRespTo;
import plus.carlosliu.pineconemall.product.entity.CategoryEntity;
import plus.carlosliu.pineconemall.product.service.CategoryService;
import plus.carlosliu.pineconemall.product.vo.web.CatelogMidVo;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model){
        List<CategoryEntity> categoryEntities = categoryService.getTopLevelCategories();
        model.addAttribute("categories", categoryEntities);
        //  视图解析器进行拼串
        //  prefix = "classpath:/templates/";
        //  suffix = ".html";
        return "index";
    }

    //index/catalog.json
    @ResponseBody
    @RequestMapping("/index/catalog.json")
    public Map<String, List<CatelogMidVo>> getCatelogJson(){
        Map<String, List<CatelogMidVo>> map = categoryService.getCatalogJson();
        return map;
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }

    /**
     * 读写锁：
     * 写：排他锁，读：共享锁  写锁没释放时读锁必须等待
     * 1、写+读 等待写锁释放
     * 2、写+写 等待、阻塞方式
     * 3、读+读 不用等待
     * 4、读+写 等待读锁释放
     */
    @GetMapping("/read")
    @ResponseBody
    public String read() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("ReadWrite-Lock");
        RLock rLock = lock.readLock();
        String s = "";
        try {
            rLock.lock();
            System.out.println("读锁加锁"+Thread.currentThread().getId());
            Thread.sleep(5000);
            s = redisTemplate.opsForValue().get("lock-value");
        }finally {
            rLock.unlock();
            return "读取完成:"+s;
        }
    }
    @GetMapping("/write")
    @ResponseBody
    public String write() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("ReadWrite-Lock");
        RLock wLock = lock.writeLock();
        String s = UUID.randomUUID().toString();
        try {
            wLock.lock();
            System.out.println("写锁加锁"+Thread.currentThread().getId());
            Thread.sleep(10000);
            redisTemplate.opsForValue().set("lock-value",s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            wLock.unlock();
            return "写入完成:"+s;
        }
    }


    /**
     * 信号量：可用作分布式限流
     * 车库停车
     */
    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore park = redissonClient.getSemaphore("park");
        //park.acquire();// 阻塞方法，获取一个信号，获取一个值，占一个车位
        boolean b = park.tryAcquire(); // 非阻塞方法，park值为0时，返回false
        if (b){
            // 用作分布式限流：执行业务
            return "停进" + b;
        }
        return "停进" + b;
    }
    @GetMapping("/go")
    @ResponseBody
    public String go() {
        RSemaphore park = redissonClient.getSemaphore("park");
        park.release(2);// 释放两个车位，park值+2
        return "开走2";
    }

    /**
     * 闭锁:
     * 放假锁门：
     * 1班没人了，2班还有 --每个单独的班都直接放行
     * 5个班全部走完，我们才可以锁大门 --阻塞
     */
    @GetMapping("/setLatch")
    @ResponseBody
    public String setLatch() {
        RCountDownLatch latch = redissonClient.getCountDownLatch("CountDownLatch");
        try {
            latch.trySetCount(5);
            latch.await(); // 等待闭锁都完成
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "门栓被放开";
    }
    @GetMapping("/offLatch")
    @ResponseBody
    public String offLatch() {
        RCountDownLatch latch = redissonClient.getCountDownLatch("CountDownLatch");
        latch.countDown(); // 计数减一
        return "门栓被放开1";
    }
}

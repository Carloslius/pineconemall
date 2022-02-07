package plus.carlosliu.pineconemall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.checkerframework.checker.units.qual.A;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import plus.carlosliu.common.constant.ProductConstant;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.Query;

import plus.carlosliu.pineconemall.product.dao.CategoryBrandRelationDao;
import plus.carlosliu.pineconemall.product.dao.CategoryDao;
import plus.carlosliu.pineconemall.product.entity.CategoryEntity;
import plus.carlosliu.pineconemall.product.service.CategoryBrandRelationService;
import plus.carlosliu.pineconemall.product.service.CategoryService;
import plus.carlosliu.pineconemall.product.vo.web.CatelogMidVo;

@Transactional
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    private CategoryBrandRelationDao categoryBrandRelationDao;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public void removeCascadeByIds(List<Long> catIds) {
        // 1、删除关联信息，pms_category_brand_relation(类别和品牌关联表)中的对应数据
        categoryBrandRelationDao.deleteBatchRelationByCatIds(catIds);
        // 2、删除基本信息
        baseMapper.deleteBatchIds(catIds);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 查询所有类别
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        // 1、找到所有的一级分类
        List<CategoryEntity> baseCategories = categoryEntities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(0L))
                .map(categories -> {
                    // 2、找到并拼装子菜单
                    categories.setChildren(this.getChildren(categories, categoryEntities));
                    return categories;
                })
                .sorted((categories1, categories2) -> {
                    // 3、菜单的排序
                    return (categories1.getSort() != null ? categories1.getSort() : 0) - (categories2.getSort() != null ? categories2.getSort() : 0);
                })
                .collect(Collectors.toList());
        return baseCategories;
    }
    // 递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> childCategories = all.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(root.getCatId()))
                .map(categories -> {
                    // 1、找到并拼装子菜单
                    categories.setChildren(this.getChildren(categories, all));
                    return categories;
                })
                .sorted((categories1, categories2) -> {
                    // 2、菜单的排序
                    return (categories1.getSort() != null ? categories1.getSort() : 0) - (categories2.getSort() != null ? categories2.getSort() : 0);
                })
                .collect(Collectors.toList());
        return childCategories;
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = this.findParentPath(catelogId, paths);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        // 1、搜集当前节点id
        paths.add(catelogId);
        CategoryEntity entity = baseMapper.selectById(catelogId);
        if (entity != null && entity.getParentCid() != 0) {
            // 2、递归查询父节点类别id
            this.findParentPath(entity.getParentCid(), paths);
        }
        return paths;
    }


    /**
     * @CacheEvict:失效模式
     * 删除缓存中多个数据：
     *      1、同时进行多种缓存操作：@Caching
     *      2、指定删除某个分区下的所有数据：@CacheEvict(value = {"category"},allEntries = true)
     *      3、存储同一类型的数据，都可以指定成同一个分区
     */
    //@CacheEvict(value = {"category"}, key = "'getTopLevelCategories'") 失效模式
//    @Caching(evict = {
//            @CacheEvict(value = {"category"}, key = "'getTopLevelCategories'"),
//            @CacheEvict(value = {"category"}, key = "'getCatalogJson'")
//    })
    @CacheEvict(value = {"category"}, allEntries = true)
    //@CachePut 双写模式，有返回值才支持
    @Override
    public void updateCascadeById(CategoryEntity category) {
        // 1、修改基本信息
        baseMapper.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            // 2、修改关联信息，修改pms_category_brand_relation(品牌和类别关联表)中类别名字段
            categoryBrandRelationService.updateCategoryNameCascade(category.getCatId(), category.getName());
        }
    }

    /**
     * 1、每一个需要缓存的数据我们都来指定要放到哪个名字的缓存。【缓存的分区：按照业务类型分】
     * 2、 @Cacheable({"category"})：代表当前方法的结果需要缓存
     *      如果缓存中有，方法不用调用
     *      如果缓存中没有，会调用方法，最后将方法的执行结果放入缓存中
     * 3、默认行为
     *      3.1、如果缓存中有，方法不用调用
     *      3.2、key默认自动生成：  缓存的名字::SimpleKey[]
     *      3.3、缓存的value值：默认使用jdk序列化机制，将序列化后的数据存到redis
     *    自定义：RedisCacheConfiguration
     *      3.1、指定生成的缓存使用的key
     *      3.2、指定缓存的数据的存活时间
     *      3.3、将数据保存为json格式
     * 4、Spring-Cache的不足：
     *      4.1、读模式
     *          缓存穿透：查询一个null数据。解决：缓存空数据 cache-null-values: true
     *          缓存雪崩：大量的key同时过期。解决：设置过期时间 time-to-live:ms
     *          缓存击穿：大量并发进来同时查询一个正好过期的数据。解决：加锁 ？ 默认是无加锁的；sync=true（加本地同步锁）
     *      4.2、写模式
     *          读写加锁
     *          引入Canal，感知到MySQl的更新去更新缓存
     *          读多写多，直接去数据库查询
     *      总结：
     *          常规数据（读多写少，即时性、一致性要求不高的数据）：完全可以使用Spring-Cache
     *          特殊数据：特殊设计
     */
    @Cacheable(value = {"category"}, key = "#root.method.name")
    @Override
    public List<CategoryEntity> getTopLevelCategories() {
        System.out.println("调用getTopLevelCategories方法");
        LambdaQueryWrapper<CategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CategoryEntity::getCatLevel, 1);
        return baseMapper.selectList(queryWrapper);
    }

    @Cacheable(value = {"category"}, key = "#root.methodName")
    @Override
    public Map<String, List<CatelogMidVo>> getCatalogJson() {
        List<CategoryEntity> allCategoryEntities = baseMapper.selectList(null);
        List<CategoryEntity> topLevelCategories = this.getCategoriesByParentCid(allCategoryEntities, 0L);
        Map<String, List<CatelogMidVo>> data = topLevelCategories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> midLevelCategories = this.getCategoriesByParentCid(allCategoryEntities, v.getCatId());
            List<CatelogMidVo> midVos = null;
            if (midLevelCategories != null) {
                midVos = midLevelCategories.stream().map(mid -> {
                    CatelogMidVo catelogMidVo = new CatelogMidVo(v.getCatId().toString(), null, mid.getCatId().toString(), mid.getName());
                    List<CategoryEntity> baseLevelCategories = this.getCategoriesByParentCid(allCategoryEntities, mid.getCatId());
                    if (baseLevelCategories != null) {
                        List<CatelogMidVo.CatelogBaseVo> baseVos = baseLevelCategories.stream().map(base -> {
                            CatelogMidVo.CatelogBaseVo catelogBaseVo = new CatelogMidVo.CatelogBaseVo(mid.getCatId().toString(), base.getCatId().toString(), base.getName());
                            return catelogBaseVo;
                        }).collect(Collectors.toList());
                        catelogMidVo.setCatalog3List(baseVos);
                    }
                    return catelogMidVo;
                }).collect(Collectors.toList());
            }
            return midVos;
        }));
        return data;
    }

    /**
     * 产生堆外内存溢出
     *      1、springboot2.0以后默认使用lettuce作为操作redis的客户端，它使用netty进行网络通信
     *      2、lettuce的bug导致堆外内存溢出 -Xms300m：netty如果没有指定堆外内存，默认使用-Xms300m，可以通过-Dio.netty.maxDirectMemory进行设置
     *      解决方案：不能只使用-Dio.netty.maxDirectMemory调大堆外内存
     *      1、升级lettuce客户端       2、切换使用jedis
     * 缓存一致性问题
     *      1、双写模式
     *      2、失效模式
     */
    public Map<String, List<CatelogMidVo>> getCatelogJson() {
        /**
         * 1、空结果缓存：解决缓存穿透
         * 2、设置过期时间(加随机值)：解决缓存雪崩
         * 3、加锁：解决缓存击穿
         */
        // 1、加入缓存逻辑，缓存中存的数据式json字符串
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            // 2、缓存中没有，查询数据库
            Map<String, List<CatelogMidVo>> catelogJsonFromDb = this.getCatelogJsonFromDbWithRedissonLock();
            // 3、查到的数据放入缓存，将对象转为json放在缓存中，放在这里会有锁的时序问题
//            String jsonString = JSON.toJSONString(catelogJsonFromDb);
//            stringRedisTemplate.opsForValue().set("catalogJSON", jsonString, 1, TimeUnit.DAYS);
            return catelogJsonFromDb;
        }else {
            // 转为指定的对象
            Map<String, List<CatelogMidVo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<CatelogMidVo>>>() {
            });
            return result;
        }
    }
    private Map<String, List<CatelogMidVo>> getCatelogJsonFromDbWithRedissonLock() {

        // 1、锁的名字，锁的粒度，越细越快
        // 锁的粒度：具体缓存的是某个数据，11-号商品：  product-11-lock
        RLock lock = redissonClient.getLock("CatalogJson-lock");
        lock.lock();

        Map<String, List<CatelogMidVo>> result = null;
        try {
            result = this.getCatelogJsonFromDb();
        } finally {
            lock.unlock();
        }

        return result;
    }
    private Map<String, List<CatelogMidVo>> getCatelogJsonFromDbWithRedisLock() {

        // 1、占分布式锁，去redis占坑 ===>> 原子性设置过期时间
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功...");
            // 加锁成功，执行业务
            // 2、设置过期时间，必须和加锁是同步的，原子的
            // stringRedisTemplate.expire("lock", 30, TimeUnit.SECONDS);
            /*            Map<String, List<CatelogMidVo>> result = this.getCatelogJsonFromDb();*/

            Map<String, List<CatelogMidVo>> result = null;
            try {
                result = this.getCatelogJsonFromDb();
            } finally {
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                Long deleteLock = stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class)
                        , Arrays.asList("lock"), uuid);
            }

            // 删除锁：获取值对比+对比成功删除=原子操作 lua脚本解锁
//            String checkLock = stringRedisTemplate.opsForValue().get("lock");
//            if (uuid.equals(checkLock)){
//                // 删除我自己的锁
//                stringRedisTemplate.delete("lock");
//            }
/*            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Long deleteLock = stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class)
                    , Arrays.asList("lock"), uuid);*/

            return result;
        } else {
            System.out.println("获取分布式锁失败...");
            // 加锁失败，重试，自旋方式
            return this.getCatelogJsonFromDbWithRedisLock();
        }
    }
    private Map<String, List<CatelogMidVo>> getCatelogJsonFromDbWithLocalLock() {

        // 只要是同一把锁，就能锁住需要这个锁的所有线程
        // springboot所有的组件在容器中都是单例的
        // 本地锁：synchronized、JUC(Lock)，在分布式情况下，想要锁住所有，必须使用分布式锁
        synchronized (this) {
            Map<String, List<CatelogMidVo>> result = this.getCatelogJsonFromDb();
            return result;
        }
    }
    private Map<String, List<CatelogMidVo>> getCatelogJsonFromDb() {
        // 优化：一次查出所有数据，避免循环查库
        List<CategoryEntity> allCategoryEntities = baseMapper.selectList(null);

        // 1、查询所有一级分类
        List<CategoryEntity> topLevelCategories = this.getCategoriesByParentCid(allCategoryEntities, 0L);

        // 2、封装数据
        Map<String, List<CatelogMidVo>> data = topLevelCategories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 2.1、每一个一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> midLevelCategories = this.getCategoriesByParentCid(allCategoryEntities, v.getCatId());

            List<CatelogMidVo> midVos = null;

            if (midLevelCategories != null) {
                midVos = midLevelCategories.stream().map(mid -> {
                    CatelogMidVo catelogMidVo = new CatelogMidVo(v.getCatId().toString(), null, mid.getCatId().toString(), mid.getName());
                    // 2.2、查到每一个二级分类的三级子分类
                    List<CategoryEntity> baseLevelCategories = this.getCategoriesByParentCid(allCategoryEntities, mid.getCatId());

                    if (baseLevelCategories != null) {
                        List<CatelogMidVo.CatelogBaseVo> baseVos = baseLevelCategories.stream().map(base -> {
                            CatelogMidVo.CatelogBaseVo catelogBaseVo = new CatelogMidVo.CatelogBaseVo(mid.getCatId().toString(), base.getCatId().toString(), base.getName());
                            return catelogBaseVo;
                        }).collect(Collectors.toList());
                        // 拼装数据
                        catelogMidVo.setCatalog3List(baseVos);
                    }

                    return catelogMidVo;
                }).collect(Collectors.toList());
            }

            return midVos;
        }));

        // 3、查到的数据放入缓存，将对象转为json放在缓存中，放在这里可以解决锁的时序问题
        String jsonString = JSON.toJSONString(data);
        stringRedisTemplate.opsForValue().set("catalogJSON", jsonString, 1, TimeUnit.DAYS);
        return data;
    }

    private List<CategoryEntity> getCategoriesByParentCid(List<CategoryEntity> allCategoryEntities, Long parentCid) {
        return allCategoryEntities.stream().filter(categoryEntity -> categoryEntity.getParentCid().equals(parentCid)).collect(Collectors.toList());
    }
}
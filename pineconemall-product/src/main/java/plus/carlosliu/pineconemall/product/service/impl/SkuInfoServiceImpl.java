package plus.carlosliu.pineconemall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.Query;

import plus.carlosliu.pineconemall.product.dao.SkuInfoDao;
import plus.carlosliu.pineconemall.product.entity.SkuImagesEntity;
import plus.carlosliu.pineconemall.product.entity.SkuInfoEntity;
import plus.carlosliu.pineconemall.product.entity.SpuInfoDescEntity;
import plus.carlosliu.pineconemall.product.service.*;
import plus.carlosliu.pineconemall.product.vo.web.SkuItemVo;

@Transactional
@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        LambdaQueryWrapper<SkuInfoEntity> queryWrapper = new LambdaQueryWrapper<>();

        // status = 1 and (id = 1 or spu_name like xxx)
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            queryWrapper.and(wrapper -> {
                wrapper.eq(SkuInfoEntity::getSkuId, key).or().like(SkuInfoEntity::getSkuName, key);
            });
        }

        String brandId = (String) params.get("brandId");
        queryWrapper.eq(!StringUtils.isEmpty(brandId) && !"0".equals(brandId), SkuInfoEntity::getBrandId, brandId);

        String catelogId = (String) params.get("catelogId");
        queryWrapper.eq(!StringUtils.isEmpty(catelogId) && !"0".equals(catelogId), SkuInfoEntity::getCatalogId, catelogId);

        String min = (String) params.get("min");
        queryWrapper.ge(!StringUtils.isEmpty(min) && !"0".equals(min), SkuInfoEntity::getPrice, min);

        String max = (String) params.get("max");
        queryWrapper.le(!StringUtils.isEmpty(max) && !"0".equals(max), SkuInfoEntity::getPrice, max);

        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        LambdaQueryWrapper<SkuInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(spuId != null, SkuInfoEntity::getSpuId, spuId);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> info = CompletableFuture.supplyAsync(() -> {
            // 1、sku基本信息获取
            SkuInfoEntity skuInfo = baseMapper.selectById(skuId);
            skuItemVo.setSkuInfo(skuInfo);
            return skuInfo;
        }, executor);

        CompletableFuture<Void> saleFuture = info.thenAcceptAsync(res -> {
            // 3、获取spu的销售属性组合
            List<SkuItemVo.SkuItemSaleAttrVo> saleAttrs = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            skuItemVo.setSaleAttrs(saleAttrs);
        }, executor);

        CompletableFuture<Void> descFuture = info.thenAcceptAsync(res -> {
            // 4、获取spu的介绍
            SpuInfoDescEntity spuInfoDesc = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesc(spuInfoDesc);
        }, executor);

        CompletableFuture<Void> groupFuture = info.thenAcceptAsync(res -> {
            // 5、获取spu的规格参数信息
            List<SkuItemVo.SpuItemAttrGroupVo> attrGroups = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(attrGroups);
        }, executor);


        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            // 2、sku的图片信息
            List<SkuImagesEntity> skuImages = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(skuImages);
        }, executor);

        // 等到所有任务都完成
        CompletableFuture.allOf(saleFuture, descFuture, groupFuture, imagesFuture).get();

        return skuItemVo;
    }
}
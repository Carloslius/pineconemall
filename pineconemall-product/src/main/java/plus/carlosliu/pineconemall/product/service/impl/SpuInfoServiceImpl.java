package plus.carlosliu.pineconemall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import plus.carlosliu.common.constant.ProductConstant;
import plus.carlosliu.common.to.SkuHasStockTo;
import plus.carlosliu.common.to.SkuReductionTo;
import plus.carlosliu.common.to.SpuBoundTo;
import plus.carlosliu.common.to.es.SkuEsModel;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.Query;

import plus.carlosliu.common.utils.R;
import plus.carlosliu.pineconemall.product.dao.SpuInfoDao;
import plus.carlosliu.pineconemall.product.entity.*;
import plus.carlosliu.pineconemall.product.feign.CouponFeignService;
import plus.carlosliu.pineconemall.product.feign.SearchFeignService;
import plus.carlosliu.pineconemall.product.feign.WareFeignService;
import plus.carlosliu.pineconemall.product.service.*;
import plus.carlosliu.pineconemall.product.vo.*;


@Service("spuInfoService")
@Transactional
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private SpuImagesService spuImagesService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CouponFeignService couponFeignService;
    @Autowired
    private WareFeignService wareFeignService;
    @Autowired
    private SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    // TODO：高级部分完善失败情况
    @Override
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
        // 1、保存spu基本信息 pms_spu_info
        SpuInfoEntity spuInfo = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo, spuInfo);
        spuInfo.setCreateTime(new Date());
        spuInfo.setUpdateTime(new Date());
        baseMapper.insert(spuInfo);

        // 2、保存spu描述图片 pms_spu_info_desc
        List<String> decript = spuSaveVo.getDecript();
        SpuInfoDescEntity spuInfoDesc = new SpuInfoDescEntity();
        spuInfoDesc.setSpuId(spuInfo.getId());
        spuInfoDesc.setDecript(String.join(",", decript));
        spuInfoDescService.save(spuInfoDesc);

        // 3、保存spu的图片集 pms_spu_images
        List<String> images = spuSaveVo.getImages();
        List<SpuImagesEntity> spuImagesEntities = images.stream().map(img -> {
            SpuImagesEntity spuImages = new SpuImagesEntity();
            spuImages.setSpuId(spuInfo.getId());
            spuImages.setImgUrl(img);
            return spuImages;
        }).collect(Collectors.toList());
        spuImagesService.saveBatch(spuImagesEntities);

        // 4、保存spu的规格参数 pms_product_attr_value
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity productAttrValue = new ProductAttrValueEntity();
            productAttrValue.setAttrId(attr.getAttrId());

            productAttrValue.setAttrName(attrService.getById(attr.getAttrId()).getAttrName());
            productAttrValue.setAttrValue(attr.getAttrValues());
            productAttrValue.setQuickShow(attr.getShowDesc());
            productAttrValue.setSpuId(spuInfo.getId());
            return productAttrValue;
        }).collect(Collectors.toList());
        productAttrValueService.saveBatch(productAttrValueEntities);

        // 5、保存spu的积分信息 pineconemall_sms ==》  sms_spu_bounds
        Bounds bounds = spuSaveVo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfo.getId());
        R result1 = couponFeignService.saveSpuBounds(spuBoundTo);
        if (result1.getCode() != 0){
            log.error("远程保存spu积分信息失败");
        }

        // 6、保存当前spu对应的所有sku信息
        List<Skus> skus = spuSaveVo.getSkus();
        if (skus != null && !skus.isEmpty()){

            // 6.1、保存sku的基本信息 pms_sku_info
            skus.forEach(item -> {
                SkuInfoEntity skuInfo = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfo);
                skuInfo.setBrandId(spuInfo.getBrandId());
                skuInfo.setCatalogId(spuInfo.getCatalogId());
                skuInfo.setSaleCount(0L);
                skuInfo.setSpuId(spuInfo.getId());
                String defaultImage = "";
                for (Images image : item.getImages()){
                    if (image.getDefaultImg() == 1){
                        defaultImage = image.getImgUrl();
                    }
                }
                skuInfo.setSkuDefaultImg(defaultImage);
                skuInfoService.save(skuInfo);

                Long skuId = skuInfo.getSkuId();
                // 6.2、保存sku的图片信息 pms_sku_images
                List<SkuImagesEntity> skuImagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImages = new SkuImagesEntity();
                    skuImages.setSkuId(skuId);
                    skuImages.setImgUrl(img.getImgUrl());
                    skuImages.setDefaultImg(img.getDefaultImg());
                    return skuImages;
                }).filter(skuImagesEntity -> {
                    // 返回true就是需要，返回false就是过滤
                    return !StringUtils.isEmpty(skuImagesEntity.getImgUrl());
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesEntities);

                // 6.3、保存sku的销售属性信息 pms_sku_sale_attr_value
                List<Attr> attrs = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrs.stream().map(attr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValue = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValue);
                    skuSaleAttrValue.setSkuId(skuId);
                    return skuSaleAttrValue;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                // 6.4、保存sku的优惠、满减信息 pineconemall_sms ==》 sms_sku_ladder  sms_sku_full_reduction  sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1){
                    R result2 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (result2.getCode() != 0){
                        log.error("远程保存sku优惠信息失败");
                    }
                }
            });
        }
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        LambdaQueryWrapper<SpuInfoEntity> queryWrapper = new LambdaQueryWrapper<>();

        // status = 1 and (id = 1 or spu_name like xxx)
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            queryWrapper.and(wrapper -> {
                wrapper.eq(SpuInfoEntity::getId, key).or().like(SpuInfoEntity::getSpuName, key);
            });
        }

        String status = (String) params.get("status");
        queryWrapper.eq(!StringUtils.isEmpty(status), SpuInfoEntity::getPublishStatus, status);

        String brandId = (String) params.get("brandId");
        queryWrapper.eq(!StringUtils.isEmpty(brandId) && !"0".equals(brandId), SpuInfoEntity::getBrandId, brandId);

        String catelogId = (String) params.get("catelogId");
        queryWrapper.eq(!StringUtils.isEmpty(catelogId) && !"0".equals(catelogId), SpuInfoEntity::getCatalogId, catelogId);

        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void up(Long spuId) {
        // 1、查出当前spuId对应的所有sku信息，品牌的名字
        List<SkuInfoEntity> skus =  skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIdList = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        // 6、查询当前spu的可以用来被检索的所有规格属性
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrListForSpu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());
        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);
        Set<Long> idsSet = new HashSet<>(searchAttrIds);

        List<SkuEsModel.Attrs> attrsList = baseAttrs.stream().filter(item -> {
            return idsSet.contains(item.getAttrId());
        }).map(item -> {
            SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs);
            return attrs;
        }).collect(Collectors.toList());

        //3、发送远程调用，库存系统查询是否有库存
        Map<Long, Boolean> skuHasStock = null;
        try {
            skuHasStock = wareFeignService.getSkuHasStock(skuIdList);
        }catch (Exception e){
            log.error("库存服务查询异常:原因{}", e);
        }

        // 2、封装每个sku的信息
        Map<Long, Boolean> finalSkuHasStock = skuHasStock;
        List<SkuEsModel> upProducts = skus.stream().map(sku -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            skuEsModel.setHasStock(false);

            // 3、设置每个sku是否有库存
            if (finalSkuHasStock == null) {
                skuEsModel.setHasStock(false);
            }else {
                skuEsModel.setHasStock(finalSkuHasStock.get(sku.getSkuId()));
            }

            //TODO：4、热度评分
            skuEsModel.setHotScore(0L);

            // 5、查询品牌和分类信息的名字
            BrandEntity brand = brandService.getById(skuEsModel.getBrandId());
            skuEsModel.setBrandName(brand.getName());
            skuEsModel.setBrandImg(brand.getLogo());
            CategoryEntity category = categoryService.getById(skuEsModel.getCatalogId());
            skuEsModel.setCatalogName(category.getName());

            // 6、设置可以用来被检索的所有规格属性
            skuEsModel.setAttrs(attrsList);
            return skuEsModel;
        }).collect(Collectors.toList());

        // 7、发送给es进行保存 pineconemall-search
        R r = searchFeignService.productStatusUp(upProducts);
        if (r.getCode() == 0){
            // 8、修改spu状态
            SpuInfoEntity spuInfo = new SpuInfoEntity();
            spuInfo.setId(spuId);
            spuInfo.setPublishStatus(ProductConstant.StatusEnum.SPU_UP.getCode());
            spuInfo.setUpdateTime(new Date());
            baseMapper.updateById(spuInfo);
        }else {
            // 远程调用失败
            // TODO：重复调用？接口幂等性：重试机制？
        }
    }

}
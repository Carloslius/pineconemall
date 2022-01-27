package plus.carlosliu.pineconemall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.util.StringUtils;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.Query;

import plus.carlosliu.pineconemall.product.dao.SkuInfoDao;
import plus.carlosliu.pineconemall.product.entity.SkuInfoEntity;
import plus.carlosliu.pineconemall.product.entity.SpuInfoEntity;
import plus.carlosliu.pineconemall.product.service.SkuInfoService;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

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

}
package plus.carlosliu.pineconemall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.util.StringUtils;
import plus.carlosliu.common.constant.ProductConstant;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.Query;

import plus.carlosliu.pineconemall.product.dao.BrandDao;
import plus.carlosliu.pineconemall.product.dao.CategoryBrandRelationDao;
import plus.carlosliu.pineconemall.product.entity.AttrEntity;
import plus.carlosliu.pineconemall.product.entity.BrandEntity;
import plus.carlosliu.pineconemall.product.service.BrandService;
import plus.carlosliu.pineconemall.product.service.CategoryBrandRelationService;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    private CategoryBrandRelationDao categoryBrandRelationDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 1、获取到key
        String key = (String) params.get("key");
        LambdaQueryWrapper<BrandEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(!StringUtils.isEmpty(key), BrandEntity::getBrandId, key)
                .or().like(!StringUtils.isEmpty(key), BrandEntity::getName, key)
                .or().eq(!StringUtils.isEmpty(key), BrandEntity::getFirstLetter, key);
        // 首字母检索key
        IPage<BrandEntity> page = this.page(new Query<BrandEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public void updateCascadeById(BrandEntity brand) {
        // 1、修改基本信息
        baseMapper.updateById(brand);
        // 2、修改关联信息，pms_category_brand_relation(类别和品牌关联表)中品牌名也需要改变
        if (!StringUtils.isEmpty(brand.getName())){
            categoryBrandRelationService.updateBrandNameCascade(brand.getBrandId(), brand.getName());
            // TODO:是否有其他需要级联更改的字段
        }
    }

    @Override
    public void removeCascadeByIds(List<Long> brandIds) {
        // 1、删除关联信息，pms_category_brand_relation(类别和品牌关联表)中的对应数据
        categoryBrandRelationDao.deleteBatchRelationByBrandIds(brandIds);
        // 2、删除基本信息
        baseMapper.deleteBatchIds(brandIds);
    }

}
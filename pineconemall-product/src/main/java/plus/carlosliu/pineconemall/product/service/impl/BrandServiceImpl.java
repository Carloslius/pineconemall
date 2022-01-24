package plus.carlosliu.pineconemall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.util.StringUtils;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.Query;

import plus.carlosliu.pineconemall.product.dao.BrandDao;
import plus.carlosliu.pineconemall.product.entity.BrandEntity;
import plus.carlosliu.pineconemall.product.service.BrandService;
import plus.carlosliu.pineconemall.product.service.CategoryBrandRelationService;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 1、获取到key
        String key = (String) params.get("key");
        LambdaQueryWrapper<BrandEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(!StringUtils.isEmpty(key), BrandEntity::getBrandId, key)
                .or().like(!StringUtils.isEmpty(key), BrandEntity::getName, key)
                .or().eq(!StringUtils.isEmpty(key), BrandEntity::getFirstLetter, key);
        IPage<BrandEntity> page = this.page(new Query<BrandEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

    @Override
    public void updateCascadeById(BrandEntity brand) {
        baseMapper.updateById(brand);
        if (!StringUtils.isEmpty(brand.getName())){
            categoryBrandRelationService.updateBrandNameCascade(brand.getBrandId(), brand.getName());
            // TODO:是否有其他需要级联更改的字段
        }
    }

}
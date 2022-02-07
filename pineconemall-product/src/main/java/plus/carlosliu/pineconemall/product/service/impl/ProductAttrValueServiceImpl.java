package plus.carlosliu.pineconemall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.Query;

import plus.carlosliu.pineconemall.product.dao.ProductAttrValueDao;
import plus.carlosliu.pineconemall.product.entity.ProductAttrValueEntity;
import plus.carlosliu.pineconemall.product.service.ProductAttrValueService;

@Transactional
@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<ProductAttrValueEntity> baseAttrListForSpu(Long spuId) {
        LambdaQueryWrapper<ProductAttrValueEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(spuId != null, ProductAttrValueEntity::getSpuId, spuId);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> productAttrValueList) {
        // TODO：注意是否有其他地方级联ProductAttrValueEntity的自增id
        // 1、删除这个spuID之前对应的所有属性
        LambdaQueryWrapper<ProductAttrValueEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(spuId != null, ProductAttrValueEntity::getSpuId, spuId);
        baseMapper.delete(queryWrapper);

        // 2、插入新的数据
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueList.stream().map(item -> {
            item.setSpuId(spuId);
            return item;
        }).collect(Collectors.toList());
        // baseMapper中没有批量保存方法
        this.saveBatch(productAttrValueEntities);
    }

}
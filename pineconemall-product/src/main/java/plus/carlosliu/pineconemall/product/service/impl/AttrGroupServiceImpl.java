package plus.carlosliu.pineconemall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.util.StringUtils;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.Query;

import plus.carlosliu.pineconemall.product.dao.AttrAttrgroupRelationDao;
import plus.carlosliu.pineconemall.product.dao.AttrGroupDao;
import plus.carlosliu.pineconemall.product.entity.AttrAttrgroupRelationEntity;
import plus.carlosliu.pineconemall.product.entity.AttrGroupEntity;
import plus.carlosliu.pineconemall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Long categoryId, Map<String, Object> params) {
        String key = (String) params.get("key");
        LambdaQueryWrapper<AttrGroupEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(!StringUtils.isEmpty(key), (wrapper) -> {
            wrapper.eq(AttrGroupEntity::getAttrGroupId, key)
                    .or().like(AttrGroupEntity::getAttrGroupName, key)
                    .or().like(AttrGroupEntity::getDescript, key);
        });
        queryWrapper.eq(categoryId != 0, AttrGroupEntity::getCatelogId, categoryId);

        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public void deleteRelation(AttrAttrgroupRelationEntity[] entities) {
        attrAttrgroupRelationDao.deleteBatchRelation(entities);
    }

}
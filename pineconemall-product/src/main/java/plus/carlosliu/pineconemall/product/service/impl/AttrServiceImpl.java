package plus.carlosliu.pineconemall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
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

import plus.carlosliu.pineconemall.product.dao.AttrAttrgroupRelationDao;
import plus.carlosliu.pineconemall.product.dao.AttrDao;
import plus.carlosliu.pineconemall.product.dao.AttrGroupDao;
import plus.carlosliu.pineconemall.product.dao.CategoryDao;
import plus.carlosliu.pineconemall.product.entity.AttrAttrgroupRelationEntity;
import plus.carlosliu.pineconemall.product.entity.AttrEntity;
import plus.carlosliu.pineconemall.product.entity.AttrGroupEntity;
import plus.carlosliu.pineconemall.product.entity.CategoryEntity;
import plus.carlosliu.pineconemall.product.service.AttrService;
import plus.carlosliu.pineconemall.product.vo.AttrRespVo;
import plus.carlosliu.pineconemall.product.vo.AttrVo;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Autowired
    private AttrGroupDao attrGroupDao;
    @Autowired
    private CategoryDao categoryDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );
        return new PageUtils(page);
    }


    @Override
    public PageUtils queryTypeAttrPage(Map<String, Object> params, Long catelogId, String type) {
        String key = (String) params.get("key");
        LambdaQueryWrapper<AttrEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(!StringUtils.isEmpty(type), AttrEntity::getAttrType, "base".equalsIgnoreCase(type)? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode(): ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        queryWrapper.and(!StringUtils.isEmpty(key), (wrapper) -> {
            wrapper.eq(AttrEntity::getAttrId, key)
                    .or().like(AttrEntity::getAttrName, key);
        });
        queryWrapper.eq(catelogId != 0, AttrEntity::getCatelogId, catelogId);

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params), queryWrapper);
        List<AttrEntity> records = page.getRecords();
        List<AttrRespVo> respVos = records.stream().map(attrEntity -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);

            // 1、设置属性分组的名字，销售属性无分组对应关系
            if ("base".equalsIgnoreCase(type)) {
                LambdaQueryWrapper<AttrAttrgroupRelationEntity> lqw = new LambdaQueryWrapper<>();
                lqw.eq(attrEntity.getAttrId() != null, AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId());
                // 1.1、根据属性id，在基础属性和属性分组关联表中查出该基础属性对应的属性分组，多对一
                AttrAttrgroupRelationEntity attrGroupRelation = attrAttrgroupRelationDao.selectOne(lqw);
                if (attrGroupRelation != null && attrGroupRelation.getAttrGroupId() != null) {
                    // 1.2、属性分组表中查出对应的属性分组名
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupRelation.getAttrGroupId());
                    if (attrGroupEntity != null && attrGroupEntity.getAttrGroupName() != null){
                        attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                    }
                }
            }
            // 2、设置分类的名字
            if (attrEntity != null && attrEntity.getCatelogId() != null) {
                CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
                if (categoryEntity != null && categoryEntity.getName() != null) {
                    attrRespVo.setCatelogName(categoryEntity.getName());
                }
            }

            return attrRespVo;
        }).collect(Collectors.toList());
        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(respVos);
        return pageUtils;
    }

    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        // 1、保存基本数据
        baseMapper.insert(attrEntity);
        // 2、保存关联关系，只有基础属性用保存到pms_attr_attrgroup_relation(基础属性和属性分组关联表)中
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity attrGroupRelation = new AttrAttrgroupRelationEntity();
            attrGroupRelation.setAttrId(attrEntity.getAttrId());// attr中没有属性id值，attrEntity有回显的id
            attrGroupRelation.setAttrGroupId(attr.getAttrGroupId());//attrEntity中没有分组id值
            attrAttrgroupRelationDao.insert(attrGroupRelation);
        }
    }

    @Override
    public AttrRespVo getDetailById(Long attrId) {
        AttrRespVo attrRespVo = new AttrRespVo();
        AttrEntity attrEntity = baseMapper.selectById(attrId);
        BeanUtils.copyProperties(attrEntity, attrRespVo);

        // 1、设置分组的id，查询关联关系，只有基础属性有属性分组
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            LambdaQueryWrapper<AttrAttrgroupRelationEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(attrEntity.getAttrId() != null, AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId());
            // 1.1、根据属性id，在基础属性和属性分组关联表中查出该基础属性对应的属性分组，多对一
            AttrAttrgroupRelationEntity attrGroupRelation = attrAttrgroupRelationDao.selectOne(queryWrapper);
            if (attrGroupRelation != null) {
                // 1.2、属性分组表中查出对应的属性分组名
                AttrGroupEntity attrGroup = attrGroupDao.selectById(attrGroupRelation.getAttrGroupId());
                attrRespVo.setAttrGroupId(attrGroupRelation.getAttrGroupId());
                if (attrGroup != null) {
                    attrRespVo.setGroupName(attrGroup.getAttrGroupName());
                }
            }
        }
        // 2、设置分类路径
        CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
        if (categoryEntity != null) {
            List<Long> path = new ArrayList<>();
            List<Long> parentPath = this.findAllPath(categoryEntity, path);
            Collections.reverse(parentPath);
            attrRespVo.setCatelogPath(parentPath.toArray(new Long[parentPath.size()]));
            attrRespVo.setCatelogName(categoryEntity.getName());
        }
        return attrRespVo;
    }
    private List<Long> findAllPath(CategoryEntity categoryEntity, List<Long> path) {
        if (categoryEntity != null) {
            path.add(categoryEntity.getCatId());
            Long parentCid = categoryEntity.getParentCid();
            if (parentCid != null && !parentCid.equals(0L)) {
                CategoryEntity parent = categoryDao.selectById(parentCid);
                this.findAllPath(parent, path);
            }
        }
        return path;
    }

    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        // 1、修改基本属性
        this.updateById(attrEntity);

        // 2、修改关联属性，只有基础属性用保存到pms_attr_attrgroup_relation(基础属性和属性分组关联表)中
        // TODO：值类型的修改
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attr.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());

            LambdaQueryWrapper<AttrAttrgroupRelationEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(attr.getAttrId() != null, AttrAttrgroupRelationEntity::getAttrId, attr.getAttrId());
            // 查询出该基础属性id对应的分组，来判断是新值还是修改，属性:分组 = n:1
            Integer count = attrAttrgroupRelationDao.selectCount(queryWrapper);
            if (count > 0) {
                attrAttrgroupRelationDao.update(attrAttrgroupRelationEntity,
                        new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                                .eq(attr.getAttrId() != null, AttrAttrgroupRelationEntity::getAttrId, attr.getAttrId()));
            } else {
                attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
            }
        }
    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        LambdaQueryWrapper<AttrAttrgroupRelationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(attrgroupId != null, AttrAttrgroupRelationEntity::getAttrGroupId, attrgroupId);
        List<AttrAttrgroupRelationEntity> attrGroupRelation = attrAttrgroupRelationDao.selectList(queryWrapper);
        // 如果pms_attr_attrgroup_relation(基础属性和属性分组关联表)中有该分组id对应的属性
        if (attrGroupRelation.size() > 0) {
            List<Long> attrIds = attrGroupRelation.stream().map(attr -> {
                // 获得每一个基础属性id
                return attr.getAttrId();
            }).collect(Collectors.toList());
            List<AttrEntity> attrEntities = baseMapper.selectBatchIds(attrIds);
            return attrEntities;
        }
        return null;
    }

    @Override
    public PageUtils getNoRelationAttr(Long attrgroupId, Map<String, Object> params) {
        // 1、当前分组只能关联自己所属的分类里面的所有属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        // 2、当前分组只能关联别的分组(包括自己)没有引用的属性
        // 2.1、当前分类下的所有分组
        List<AttrGroupEntity> group = attrGroupDao.selectList(new LambdaQueryWrapper<AttrGroupEntity>()
                .eq(catelogId != null, AttrGroupEntity::getCatelogId, catelogId));
        List<Long> attrGroupIds = group.stream().map(item -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());
        // 2.2、这些分组关联的所有属性
        List<AttrAttrgroupRelationEntity> attrGroupRelation = attrAttrgroupRelationDao.selectList(
                new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                .in(attrGroupIds.size() > 0, AttrAttrgroupRelationEntity::getAttrGroupId, attrGroupIds));
        List<Long> attrs = attrGroupRelation.stream().map(item -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
        // 2.3、从当前分类的所有属性中移除这些属性
        LambdaQueryWrapper<AttrEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(catelogId != null, AttrEntity::getCatelogId, catelogId)
                .notIn(attrs.size() > 0, AttrEntity::getAttrId, attrs)
                .eq(AttrEntity::getAttrType, ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
                // 只能关联基础属性，所以这里只获取基础属性

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            queryWrapper.and(wrapper -> {
                wrapper.eq(AttrEntity::getAttrId, key).or().like(AttrEntity::getAttrName, key);
            });
        }

        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

    @Override
    public void removeCascadeByIds(List<Long> attrIds) {
        // 1、删除pms_attr_attrgroup_relation分组与基础属性关联表中关联数据
        List<Long> baseAttrIds = attrIds.stream().map(attrId -> {
            AttrEntity attrEntity = baseMapper.selectById(attrId);
            if (attrEntity != null && attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
                return attrId;
            }
            return 0L;
        }).collect(Collectors.toList());
        attrAttrgroupRelationDao.deleteBatchRelationByAttrIds(baseAttrIds);
        // 2、删除基本信息
        baseMapper.deleteBatchIds(attrIds);
    }

    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrIds) {
//        List<Long> attrsIds = attrIds.stream().filter(attrId -> {
//            AttrEntity attr = baseMapper.selectById(attrId);
//            return attr.getSearchType() == 1;
//        }).collect(Collectors.toList());
        LambdaQueryWrapper<AttrEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(attrIds != null, AttrEntity::getAttrId, attrIds);
        queryWrapper.eq(AttrEntity::getSearchType, 1);
        List<AttrEntity> attrEntities = baseMapper.selectList(queryWrapper);
        List<Long> attrsIds = attrEntities.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());
        return attrsIds;
    }

}
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
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        // 1、保存基本数据
        this.save(attrEntity);
        // 2、保存关联关系
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    @Override
    public PageUtils queryTypeAttrPage(Map<String, Object> params, Long catelogId, String type) {
        String key = (String) params.get("key");
        LambdaQueryWrapper<AttrEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttrEntity::getAttrType, "base".equalsIgnoreCase(type)? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode(): ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        queryWrapper.and(!StringUtils.isEmpty(key), (wrapper) -> {
            wrapper.eq(AttrEntity::getAttrId, key)
                    .or().like(AttrEntity::getAttrName, key);
        });

        if (catelogId != 0) {
            queryWrapper.eq(AttrEntity::getCatelogId, catelogId);
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params), queryWrapper);
        List<AttrEntity> records = page.getRecords();
        List<AttrRespVo> respVos = records.stream().map(attrEntity -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);

            // 1、设置分组的名字
            if ("base".equalsIgnoreCase(type)) {
                AttrAttrgroupRelationEntity attrGroupId = attrAttrgroupRelationDao.selectOne(
                        new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                                .eq(AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId()));
                if (attrGroupId != null && attrGroupId.getAttrGroupId() != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId.getAttrGroupId());
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
            // 2、设置分类的名字
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrRespVo.setCatelogName(categoryEntity.getName());
            }

            return attrRespVo;
        }).collect(Collectors.toList());
        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(respVos);
        return pageUtils;
    }

    @Override
    public AttrRespVo getDetailById(Long attrId) {
        AttrRespVo attrRespVo = new AttrRespVo();
//        LambdaQueryWrapper<AttrEntity> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(attrId != null, AttrEntity::getAttrId, attrId);
//        AttrEntity attrEntity = baseMapper.selectOne(queryWrapper);
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, attrRespVo);

        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            // 1、设置分组的id
            AttrAttrgroupRelationEntity attrGroupId = attrAttrgroupRelationDao.selectOne(
                    new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                            .eq(AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId()));
            if (attrGroupId != null) {
                AttrGroupEntity attrGroupName = attrGroupDao.selectById(attrGroupId.getAttrGroupId());
                attrRespVo.setAttrGroupId(attrGroupId.getAttrGroupId());
                attrRespVo.setGroupName(attrGroupName.getAttrGroupName());
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

    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        // 1、修改基本属性
        this.updateById(attrEntity);

        // 2、修改关联属性
        // TODO：值类型的修改
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            Long attrGroupId = attr.getAttrGroupId();
            Long attrId = attr.getAttrId();
            attrAttrgroupRelationEntity.setAttrId(attrId);
            attrAttrgroupRelationEntity.setAttrGroupId(attrGroupId);
            Integer count = attrAttrgroupRelationDao.selectCount(new LambdaQueryWrapper<AttrAttrgroupRelationEntity>().eq(AttrAttrgroupRelationEntity::getAttrId, attrId));
            if (count > 0) {
                attrAttrgroupRelationDao.update(attrAttrgroupRelationEntity,
                        new LambdaQueryWrapper<AttrAttrgroupRelationEntity>().eq(AttrAttrgroupRelationEntity::getAttrId, attrId));
            } else {
                attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
            }
        }
    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> entities = attrAttrgroupRelationDao.selectList(new LambdaQueryWrapper<AttrAttrgroupRelationEntity>().eq(AttrAttrgroupRelationEntity::getAttrGroupId, attrgroupId));
        if (entities.size() > 0) {
            List<Long> attrIds = entities.stream().map(attr -> {
                return attr.getAttrId();
            }).collect(Collectors.toList());
            List<AttrEntity> attrEntities = this.listByIds(attrIds);
            return attrEntities;
        }
        return null;
    }

    @Override
    public PageUtils getNoRelationAttr(Long attrgroupId, Map<String, Object> params) {
        // 1、当前分组只能关联自己所属的分类里面的所有属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        // 2、当前分组只能关联别的分组没有引用的属性
        // 2.1、当前分类下的其他分组
        List<AttrGroupEntity> group = attrGroupDao.selectList(new LambdaQueryWrapper<AttrGroupEntity>()
                .eq(AttrGroupEntity::getCatelogId, catelogId));
        // 2.2、这些分组关联的所有属性
        List<Long> collect = group.stream().map(item -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());
        List<AttrAttrgroupRelationEntity> groupId = attrAttrgroupRelationDao.selectList(new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                .in(AttrAttrgroupRelationEntity::getAttrGroupId, collect));
        List<Long> attrs = groupId.stream().map(item -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
        // 2.3、从当前分类的所有属性中移除这些属性
        LambdaQueryWrapper<AttrEntity> queryWrapper = new LambdaQueryWrapper<AttrEntity>()
                .eq(AttrEntity::getCatelogId, catelogId).notIn(attrs.size() > 0, AttrEntity::getAttrId, attrs)
                .eq(AttrEntity::getAttrType, ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            queryWrapper.and(wrapper -> {
                wrapper.eq(AttrEntity::getAttrId, key).or().like(AttrEntity::getAttrName, key);
            });
        }

        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

    private List<Long> findAllPath(CategoryEntity categoryEntity, List<Long> path) {
        path.add(categoryEntity.getCatId());
        Long parentCid = categoryEntity.getParentCid();
        if (parentCid != null && !parentCid.equals(0L)){
            CategoryEntity parent = categoryDao.selectById(parentCid);
            this.findAllPath(parent, path);
        }
        return path;
    }


}
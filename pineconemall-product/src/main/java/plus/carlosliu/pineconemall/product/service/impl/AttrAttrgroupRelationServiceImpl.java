package plus.carlosliu.pineconemall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.Query;

import plus.carlosliu.pineconemall.product.dao.AttrAttrgroupRelationDao;
import plus.carlosliu.pineconemall.product.dao.AttrDao;
import plus.carlosliu.pineconemall.product.dao.AttrGroupDao;
import plus.carlosliu.pineconemall.product.entity.AttrAttrgroupRelationEntity;
import plus.carlosliu.pineconemall.product.entity.AttrEntity;
import plus.carlosliu.pineconemall.product.entity.AttrGroupEntity;
import plus.carlosliu.pineconemall.product.service.AttrAttrgroupRelationService;
import plus.carlosliu.pineconemall.product.vo.AttrGroupRespVo;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private AttrDao attrDao;

    @Autowired
    private AttrAttrgroupRelationDao relationDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<AttrGroupRespVo> listAttrGroupWithTheirArrs(Long catelogId) {
        // 1、查询该类别下的所有属性分组
        LambdaQueryWrapper<AttrGroupEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(catelogId != null, AttrGroupEntity::getCatelogId, catelogId);
        List<AttrGroupEntity> attrGroup = attrGroupDao.selectList(queryWrapper);
        // 2、操作每个属性分组，信息写入完全后，收集成attrGroupRespVoList集合
        List<AttrGroupRespVo> attrGroupRespVoList = attrGroup.stream().map(obj -> {
            // 2.1、拷贝基础属性
            AttrGroupRespVo attrGroupRespVo = new AttrGroupRespVo();
            BeanUtils.copyProperties(obj, attrGroupRespVo);

            // 2.2、写入attrs字段
            // 2.2.1、根据pms_attr_attrgroup_relation表查询每个属性分组下的属性集合
            LambdaQueryWrapper<AttrAttrgroupRelationEntity> lqw = new LambdaQueryWrapper<>();
            lqw.eq(obj.getAttrGroupId() != null, AttrAttrgroupRelationEntity::getAttrGroupId, obj.getAttrGroupId());
            List<AttrAttrgroupRelationEntity> attrGroupRelation = relationDao.selectList(lqw);
            // 2.2.2、查询属性集合中每个属性的具体信息，收集成attrList集合
            List<AttrEntity> attrList = attrGroupRelation.stream().map(entity -> {
                return attrDao.selectById(entity.getAttrId());
            }).collect(Collectors.toList());
            // 2.2.3、真正写入每个属性分组的attrs字段
            attrGroupRespVo.setAttrs(attrList.toArray(new AttrEntity[attrList.size()]));
            return attrGroupRespVo;
        }).collect(Collectors.toList());
        return attrGroupRespVoList;
    }

}
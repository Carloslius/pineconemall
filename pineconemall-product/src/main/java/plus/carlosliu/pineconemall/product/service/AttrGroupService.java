package plus.carlosliu.pineconemall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.pineconemall.product.entity.AttrAttrgroupRelationEntity;
import plus.carlosliu.pineconemall.product.entity.AttrGroupEntity;
import plus.carlosliu.pineconemall.product.vo.web.SkuItemVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 12:33:30
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取分类属性分组
     * @param categoryId 类别id
     * @param params 分页参数
     * @return 分页数据
     */
    PageUtils queryPage(Long categoryId, Map<String, Object> params);

    /**
     * 删除属性与分组的关联关系
     * @param entities 保存了要删除信息的数组 [{"attrId":1,"attrGroupId":2}]
     */
    void deleteRelation(AttrAttrgroupRelationEntity[] entities);

    /**
     * 级联删除分组和属性关联表中该分组的所有数据
     * @param attrGroupIds 分组id
     */
    void removeCascadeByIds(List<Long> attrGroupIds);

    /**
     * 查出当前spu对应的所有属性的分组信息，以及当前分组下的所有属性对应的值
     * @param spuId spuId
     * @param catalogId
     * @return 当前spu对应的所有属性的分组信息，以及当前分组下的所有属性对应的值
     */
    List<SkuItemVo.SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId);
}


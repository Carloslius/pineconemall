package plus.carlosliu.pineconemall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.pineconemall.product.entity.AttrEntity;
import plus.carlosliu.pineconemall.product.vo.AttrRespVo;
import plus.carlosliu.pineconemall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 12:33:30
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取分类规格参数
     * 获取分类销售属性
     * @param params 分页参数
     * @param catelogId 类别id
     * @param type 规格参数/销售属性
     * @return 分页数据
     */
    PageUtils queryTypeAttrPage(Map<String, Object> params, Long catelogId, String type);

    /**
     * 保存属性【规格参数，销售属性】
     * @param attr 属性各字段值
     */
    void saveAttr(AttrVo attr);

    /**
     * 查询属性详情
     * @param attrId 属性id
     * @return AttrRespVo对象，多封装了4个字段
     */
    AttrRespVo getDetailById(Long attrId);

    /**
     * 修改属性
     * @param attr 属性各字段值
     */
    void updateAttr(AttrVo attr);

    /**
     * 获取属性分组的关联的所有属性
     * @param attrgroupId 属性分组id
     * @return 所有属性的List集合
     */
    List<AttrEntity> getRelationAttr(Long attrgroupId);

    /**
     * 获取属性分组没有关联的其他属性
     * @param attrgroupId 属性分组id
     * @param params 分页参数
     * @return 分页数据
     */
    PageUtils getNoRelationAttr(Long attrgroupId, Map<String, Object> params);

    /**
     * 级联删除分组和属性关联表中该基础属性的所有数据
     * @param attrIds 基础属性id
     */
    void removeCascadeByIds(List<Long> attrIds);

    /**
     * 在指定的所有属性集合里筛选出可以被检索的属性
     * @param attrIds 所有属性id的集合
     * @return 可以被检索的属性id的集合
     */
    List<Long> selectSearchAttrIds(List<Long> attrIds);
}


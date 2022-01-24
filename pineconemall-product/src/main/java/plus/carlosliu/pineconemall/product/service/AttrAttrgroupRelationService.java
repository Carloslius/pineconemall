package plus.carlosliu.pineconemall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.pineconemall.product.entity.AttrAttrgroupRelationEntity;
import plus.carlosliu.pineconemall.product.vo.AttrGroupRespVo;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 12:33:30
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取类别下所有分组&关联属性
     * @param catelogId 类别id
     * @return 所有分组&关联属性，每个分组中attrs字段包含其关联的属性
     */
    List<AttrGroupRespVo> listAttrGroupWithTheirArrs(Long catelogId);
}


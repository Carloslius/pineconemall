package plus.carlosliu.pineconemall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.pineconemall.product.entity.AttrAttrgroupRelationEntity;
import plus.carlosliu.pineconemall.product.entity.AttrEntity;
import plus.carlosliu.pineconemall.product.entity.AttrGroupEntity;

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
}


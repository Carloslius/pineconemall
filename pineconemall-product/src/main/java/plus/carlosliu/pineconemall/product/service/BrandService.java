package plus.carlosliu.pineconemall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.pineconemall.product.entity.BrandEntity;

import java.util.Map;

/**
 * 品牌
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 12:33:30
 */
public interface BrandService extends IService<BrandEntity> {

    /**
     * 获取品牌数据列表
     * @param params 分页参数
     * @return 分页数据
     */
    PageUtils queryPage(Map<String, Object> params);

    /**
     * 修改品牌详细信息
     * @param brand 品牌详细信息
     */
    void updateCascadeById(BrandEntity brand);
}


package plus.carlosliu.pineconemall.product.dao;

import plus.carlosliu.pineconemall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 12:33:30
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}

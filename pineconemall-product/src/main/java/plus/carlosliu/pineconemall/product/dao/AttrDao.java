package plus.carlosliu.pineconemall.product.dao;

import org.apache.ibatis.annotations.Param;
import plus.carlosliu.pineconemall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品属性
 * 
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 12:33:30
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {
	
}

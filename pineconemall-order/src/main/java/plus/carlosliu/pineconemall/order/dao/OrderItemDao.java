package plus.carlosliu.pineconemall.order.dao;

import plus.carlosliu.pineconemall.order.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 * 
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 13:03:59
 */
@Mapper
public interface OrderItemDao extends BaseMapper<OrderItemEntity> {
	
}

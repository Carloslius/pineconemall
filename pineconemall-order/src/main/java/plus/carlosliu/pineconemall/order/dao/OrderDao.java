package plus.carlosliu.pineconemall.order.dao;

import plus.carlosliu.pineconemall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 13:04:00
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}

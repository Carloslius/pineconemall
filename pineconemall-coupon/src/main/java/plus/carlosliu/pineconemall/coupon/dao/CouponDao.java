package plus.carlosliu.pineconemall.coupon.dao;

import plus.carlosliu.pineconemall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 12:57:52
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}

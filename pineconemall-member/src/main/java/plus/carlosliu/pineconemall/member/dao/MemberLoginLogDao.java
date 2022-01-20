package plus.carlosliu.pineconemall.member.dao;

import plus.carlosliu.pineconemall.member.entity.MemberLoginLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员登录记录
 * 
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 13:08:15
 */
@Mapper
public interface MemberLoginLogDao extends BaseMapper<MemberLoginLogEntity> {
	
}

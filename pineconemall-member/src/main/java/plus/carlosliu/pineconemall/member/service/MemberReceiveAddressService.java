package plus.carlosliu.pineconemall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.pineconemall.member.entity.MemberReceiveAddressEntity;

import java.util.List;
import java.util.Map;

/**
 * 会员收货地址
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 13:08:15
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询会员的收货地址
     * @param memberId 会员id
     * @return 会员的收货地址集合
     */
    List<MemberReceiveAddressEntity> getAddress(Long memberId);
}


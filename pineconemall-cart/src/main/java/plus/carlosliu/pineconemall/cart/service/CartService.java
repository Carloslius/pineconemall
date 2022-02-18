package plus.carlosliu.pineconemall.cart.service;

import plus.carlosliu.pineconemall.cart.vo.CartItemVo;
import plus.carlosliu.pineconemall.cart.vo.CartVo;

import java.util.List;

public interface CartService {

    /**
     * 增加购物车中商品项
     * @param skuId 商品id
     * @param num 商品数量
     * @return 增加的商品项，需要回显到success.html页面
     */
    CartItemVo addCartItem(Long skuId, Integer num);

    /**
     * 获取购物车中的商品项
     * @param skuId 商品id
     * @return 购物车中的商品项，需要回显到success.html页面
     */
    CartItemVo getCartItem(Long skuId);

    /**
     * 获取购物车信息
     * @return 购物车信息
     */
    CartVo getCart();

    /**
     * 改变勾选商品项状态
     * @param skuId 商品id
     * @param isChecked 勾选的状态
     * @return 返回修改过的购物项
     */
    CartItemVo checkItem(Long skuId, Integer isChecked);

    /**
     * 改变购物车中商品数量
     * @param skuId 商品id
     * @param num 商品数量
     */
    CartItemVo changeItemCount(Long skuId, Integer num);

    /**
     * 删除购物车中某商品项
     * @param skuId 商品项id
     */
    void deleteItem(Long skuId);

    /**
     * 查询当前用户勾选的购物项
     * @return 当前用户勾选的购物项
     */
    List<CartItemVo> getCurrentUserCartItems();
}

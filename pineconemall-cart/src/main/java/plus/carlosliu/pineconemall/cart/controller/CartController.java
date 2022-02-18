package plus.carlosliu.pineconemall.cart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import plus.carlosliu.common.utils.R;
import plus.carlosliu.pineconemall.cart.interceptor.CartInterceptor;
import plus.carlosliu.pineconemall.cart.service.CartService;
import plus.carlosliu.pineconemall.cart.to.UserInfoTo;
import plus.carlosliu.pineconemall.cart.vo.CartItemVo;
import plus.carlosliu.pineconemall.cart.vo.CartVo;

import java.util.List;

/**
 * @author 刘浩松
 */
@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @RequestMapping("/success.html")
    public String success() {
        return "success";
    }

    @RequestMapping("/cart.html")
    public String getCartList(Model model) {
        CartVo cartVo = cartService.getCart();
        model.addAttribute("cart", cartVo);
        return "cartList";
    }

    /**
     * 添加商品到购物车
     * @param skuId 商品id
     * @param num 商品数量
     * @param redirectAttributes
     *          ra.addFlashAttribute();将数据放在session里面，可以在页面取出，但是只能取一次
     *          ra.addAttribute();将数据拼接到url后面
     * @return 携带skuId重定向到跳转方法
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes redirectAttributes){
        CartItemVo cartItemVo = cartService.addCartItem(skuId, num);
        //model.addAttribute("cartItem", cartItemVo);
        redirectAttributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.pineconemall.com/addToCartSuccess.html";
    }
    /**
     * 跳转到成功页面
     * @param skuId 商品id
     * @param model 数据
     * @return 跳转到成功页面
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model){
        CartItemVo cartItem = cartService.getCartItem(skuId);
        model.addAttribute("cartItem", cartItem);
        return "success";
    }

    @ResponseBody
    @GetMapping("/checkItem")
    public R checkItem(@RequestParam("skuId") Long skuId, @RequestParam("check") Integer check){
        CartItemVo cartItemVo = cartService.checkItem(skuId, check);
        return R.ok().setData("cartItem", cartItemVo);
    }

    @ResponseBody
    @GetMapping("/countItem")
    public R countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num){
        CartItemVo cartItemVo = cartService.changeItemCount(skuId, num);
        return R.ok().setData("cartItem", cartItemVo);
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){
        cartService.deleteItem(skuId);
        return "redirect:http://cart.pineconemall.com/cart.html";
    }

    /**
     * 查询当前用户勾选的购物项
     * @return 当前用户勾选的购物项
     */
    @ResponseBody
    @GetMapping("/currentUserCartItems")
    public List<CartItemVo> getCurrentUserCartItems(){
        return cartService.getCurrentUserCartItems();
    }
}

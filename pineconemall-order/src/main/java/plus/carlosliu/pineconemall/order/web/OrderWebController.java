package plus.carlosliu.pineconemall.order.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import plus.carlosliu.common.exception.NoStockException;
import plus.carlosliu.pineconemall.order.service.OrderService;
import plus.carlosliu.pineconemall.order.vo.OrderConfirmVo;
import plus.carlosliu.pineconemall.order.vo.OrderSubmitVo;
import plus.carlosliu.pineconemall.order.vo.SubmitOrderResponseVo;

@Controller
public class OrderWebController {
    @Autowired
    public OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model){
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirm", orderConfirmVo);
        return "confirm";
    }

    @RequestMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo submitVo, Model model, RedirectAttributes attributes) {
        try{
            submitVo.setIsSecKillOrder(false);
            SubmitOrderResponseVo responseVo = orderService.submitOrder(submitVo);
            Integer code = responseVo.getCode();
            if (code == 0){
                model.addAttribute("order", responseVo.getOrder());
                return "pay";
            }else {
                String msg = "下单失败:";
                switch (code) {
                    case 1:
                        msg += "防重令牌校验失败";
                        break;
                    case 2:
                        msg += "商品价格发生变化";
                        break;
                }
                attributes.addFlashAttribute("msg", msg);
                return "redirect:http://order.pineconemall.com/toTrade";
            }
        }catch (Exception e){
            if (e instanceof NoStockException){;
                attributes.addFlashAttribute("msg", e.getMessage());
            }
            return "redirect:http://order.pineconemall.com/toTrade";
        }
    }
}

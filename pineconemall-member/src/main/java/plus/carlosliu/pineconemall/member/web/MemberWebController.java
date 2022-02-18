package plus.carlosliu.pineconemall.member.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import plus.carlosliu.common.utils.R;
import plus.carlosliu.pineconemall.member.feign.OrderFeignService;

import java.util.HashMap;
import java.util.Map;

@Controller
public class MemberWebController {

    @Autowired
    private OrderFeignService orderFeignService;

    @GetMapping("/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, Model model){
        Map<String, Object> page = new HashMap<>();
        page.put("page", pageNum.toString());
        R r = orderFeignService.listOrderItems(page);
        if (r.getCode() == 0){
            model.addAttribute("orders", r);
        }
        return "orderList";
    }
}

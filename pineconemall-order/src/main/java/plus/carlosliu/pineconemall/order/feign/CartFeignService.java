package plus.carlosliu.pineconemall.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import plus.carlosliu.pineconemall.order.to.OrderItemTo;

import java.util.List;

@FeignClient("pineconemall-cart")
public interface CartFeignService {

    @GetMapping("/currentUserCartItems")
    List<OrderItemTo> getCurrentUserCartItems();

}

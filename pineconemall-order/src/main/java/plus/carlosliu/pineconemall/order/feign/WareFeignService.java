package plus.carlosliu.pineconemall.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import plus.carlosliu.common.utils.R;
import plus.carlosliu.pineconemall.order.to.WareSkuLockTo;

import java.util.List;
import java.util.Map;

@FeignClient("pineconemall-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/hasStock")
    Map<Long, Boolean> getSkuHasStock(@RequestBody List<Long> skuIds);

    @GetMapping("/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);

    @PostMapping("/ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockTo wareSkuLockTo);
}

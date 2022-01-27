package plus.carlosliu.pineconemall.search.service.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import plus.carlosliu.common.to.es.SkuEsModel;
import plus.carlosliu.pineconemall.search.config.PineconemallElasticSearchConfig;
import plus.carlosliu.pineconemall.search.constant.EsConstant;
import plus.carlosliu.pineconemall.search.service.ProductSaveService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        // 保存到es中
        // 1、给es中建立索引，product，建立好映射关系
        // 2、给es中保存数据
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel model : skuEsModels){
            // 1、构造保持请求
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(model.getSkuId().toString());
            String s = JSON.toJSONString(model);
            indexRequest.source(s, XContentType.JSON);

            bulkRequest.add(indexRequest);
        }
        BulkResponse bulkItemResponses = restHighLevelClient.bulk(bulkRequest, PineconemallElasticSearchConfig.COMMON_OPTIONS);

        List<String> collect = Arrays.stream(bulkItemResponses.getItems()).map(item -> {
            return item.getId();
        }).collect(Collectors.toList());
        log.info(
                "商品上架完成:{}", collect);
        return !bulkItemResponses.hasFailures();
    }
}

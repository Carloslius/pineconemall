package plus.carlosliu.pineconemall.search.service;

import plus.carlosliu.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ProductSaveService {

    /**
     * 商品上架
     */
    Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}

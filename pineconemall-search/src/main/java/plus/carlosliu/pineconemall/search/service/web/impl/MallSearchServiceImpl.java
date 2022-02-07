package plus.carlosliu.pineconemall.search.service.web.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import plus.carlosliu.pineconemall.search.config.PineconemallElasticSearchConfig;
import plus.carlosliu.pineconemall.search.constant.EsConstant;
import plus.carlosliu.pineconemall.search.feign.ProductFeignService;
import plus.carlosliu.pineconemall.search.service.web.MallSearchService;
import plus.carlosliu.pineconemall.search.to.es.SkuEsModel;
import plus.carlosliu.pineconemall.search.utils.R;
import plus.carlosliu.pineconemall.search.vo.web.AttrResponseVo;
import plus.carlosliu.pineconemall.search.vo.web.SearchParams;
import plus.carlosliu.pineconemall.search.vo.web.SearchResult;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public SearchResult searchByParams(SearchParams searchParams) {SearchResult searchResult= null;
        SearchRequest request = this.bulidSearchRequest(searchParams);
        try {
            SearchResponse searchResponse = restHighLevelClient.search(request, PineconemallElasticSearchConfig.COMMON_OPTIONS);
            searchResult = this.bulidSearchResult(searchParams, searchResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResult;
    }

    /**
     * 准备检索请求
     * 查询：模糊匹配，过滤（属性、分类、品牌、价格区间、库存）
     * @param searchParams 查询参数
     * @return 解封装完的DSL语句
     */
    private SearchRequest bulidSearchRequest(SearchParams searchParams) {
        // 构建DSL语句
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 查询：模糊匹配，过滤（属性、分类、品牌、价格区间、库存）
        // 1. 构建bool query
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // 1.1 bool must
        if (!StringUtils.isEmpty(searchParams.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", searchParams.getKeyword()));
        }
        // 1.2 bool filter
        // 1.2.1 catalog
        if (searchParams.getCatalog3Id() != null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", searchParams.getCatalog3Id()));
        }
        // 1.2.2 brand
        if (searchParams.getBrandId() != null && searchParams.getBrandId().size() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId",searchParams.getBrandId()));
        }
        // 1.2.3 hasStock
        if (searchParams.getHasStock() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", searchParams.getHasStock() == 1));
        }
        // 1.2.4 skuPrice
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPrice");
        if (!StringUtils.isEmpty(searchParams.getSkuPrice())) {
            String[] prices = searchParams.getSkuPrice().split("_");
            if (prices.length == 1) {
                //6000_会截取成["6000"]
                rangeQueryBuilder.gte(Integer.parseInt(prices[0]));
            } else if (prices.length == 2) {
                //_6000会截取成["","6000"]
                if (!prices[0].isEmpty() && !prices[1].isEmpty()) {
                    rangeQueryBuilder.gte(Integer.parseInt(prices[0]));
                    rangeQueryBuilder.lte(Integer.parseInt(prices[1]));
                }else {
                    rangeQueryBuilder.lte(Integer.parseInt(prices[1]));
                }
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }
        //1.2.5 attrs-nested
        //attrs=1_5寸:8寸&attrs=2_16G:8G
        List<String> attrs = searchParams.getAttrs();
        if (attrs != null && attrs.size() > 0) {
            attrs.forEach(attr -> {
                BoolQueryBuilder nestQueryBuilder = new BoolQueryBuilder();
                String[] attrSplit = attr.split("_");
                nestQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId", attrSplit[0]));
                String[] attrValues = attrSplit[1].split(":");
                nestQueryBuilder.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                // 每一个attr属性都需要组合成一个nest查询
                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", nestQueryBuilder, ScoreMode.None);
                boolQueryBuilder.filter(nestedQueryBuilder);
            });
        }
        //1. bool query构建完成
        searchSourceBuilder.query(boolQueryBuilder);

        //2. sort  sort=saleCount_asc/desc
        if (!StringUtils.isEmpty(searchParams.getSort())) {
            String[] sortSplit = searchParams.getSort().split("_");
            searchSourceBuilder.sort(sortSplit[0], sortSplit[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC);
        }

        //3. 分页
        if (!StringUtils.isEmpty(searchParams.getPageNum())) {
            searchSourceBuilder.from((searchParams.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
            searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
        }

        //4. 高亮highlight
        if (!StringUtils.isEmpty(searchParams.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }

        //5. 聚合
        //5.1 按照brand聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        TermsAggregationBuilder brandNameAgg = AggregationBuilders.terms("brand_name_agg").field("brandName").size(1);
        TermsAggregationBuilder brandImgAgg = AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1);
        brandAgg.subAggregation(brandNameAgg);
        brandAgg.subAggregation(brandImgAgg);
        searchSourceBuilder.aggregation(brandAgg);

        //5.2 按照catalog聚合
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg").field("catalogId");
        TermsAggregationBuilder catalogNameAgg = AggregationBuilders.terms("catalog_name_agg").field("catalogName");
        catalogAgg.subAggregation(catalogNameAgg);
        searchSourceBuilder.aggregation(catalogAgg);

        //5.3 按照attrs聚合
        NestedAggregationBuilder nestedAggregationBuilder = new NestedAggregationBuilder("attr_agg", "attrs");
        //按照attrId聚合
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        //按照attrId聚合之后再按照attrName和attrValue聚合
        TermsAggregationBuilder attrNameAgg = AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1);
        TermsAggregationBuilder attrValueAgg = AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50);
        attrIdAgg.subAggregation(attrNameAgg);
        attrIdAgg.subAggregation(attrValueAgg);

        nestedAggregationBuilder.subAggregation(attrIdAgg);
        searchSourceBuilder.aggregation(nestedAggregationBuilder);

        log.debug("构建的DSL语句 {}", searchSourceBuilder.toString());
        System.out.println(searchSourceBuilder.toString());

        SearchRequest request = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);
        return request;
    }

    private SearchResult bulidSearchResult(SearchParams searchParams, SearchResponse searchResponse) {
        SearchResult result = new SearchResult();
        SearchHits hits = searchResponse.getHits();

        //1、封装查询到的商品信息
        if (hits.getHits() != null && hits.getHits().length > 0){
            List<SkuEsModel> skuEsModels = new ArrayList<>();
            for (SearchHit hit : hits) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                //设置高亮属性
                if (!StringUtils.isEmpty(searchParams.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String highLight = skuTitle.getFragments()[0].string();
                    skuEsModel.setSkuTitle(highLight);
                }
                skuEsModels.add(skuEsModel);
            }
            result.setProducts(skuEsModels);
        }

        //2、封装分页信息
        //2.1 当前页码
        result.setPageNum(searchParams.getPageNum());
        //2.2 总记录数
        Long total = hits.getTotalHits().value;
        result.setTotal(total);
        //2.3 总页码
        Integer totalPages = Math.toIntExact(total % EsConstant.PRODUCT_PAGESIZE == 0 ?
                total / EsConstant.PRODUCT_PAGESIZE : total / EsConstant.PRODUCT_PAGESIZE + 1);
        result.setTotalPages(totalPages);
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        // ================= 以下是聚合信息 ================
        Aggregations aggregations = searchResponse.getAggregations();
        //3、查询涉及到的所有分类
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        /** 构建分类面包屑导航 */
        List<SearchResult.NavVo> catalogNavs = new ArrayList<>();
        ParsedLongTerms catalogAgg = aggregations.get("catalog_agg");
        for (Terms.Bucket bucket : catalogAgg.getBuckets()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            /** 构建分类面包屑导航 */
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            //3.1 获取分类id
            Long catalogId = bucket.getKeyAsNumber().longValue();
            catalogVo.setCatalogId(catalogId);
            //3.2 获取分类名
            Aggregations subCatalogAggs = bucket.getAggregations();
            ParsedStringTerms catalogNameAgg = subCatalogAggs.get("catalog_name_agg");
            String catalogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            /** 构建分类面包屑导航 */
            navVo.setNavId(catalogId);
            navVo.setNavName("分类");
            navVo.setNavValue(catalogName);
            catalogNavs.add(navVo);

            // 将每个封装的分类信息放到分类集合中
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);

        //4、查询结果涉及到的品牌
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        /** 构建品牌面包屑导航 */
        List<SearchResult.NavVo> brandNavs = new ArrayList<>();
        //ParsedLongTerms用于接收terms聚合的结果，并且可以把key转化为Long类型的数据
        ParsedLongTerms brandAgg = aggregations.get("brand_agg");
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            /** 构建品牌面包屑导航 */
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            //4.1 得到品牌id
            Long brandId = bucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);
            //4.2 获得子聚合
            Aggregations subBrandAggs = bucket.getAggregations();
            //4.2.1 得到品牌图片
            ParsedStringTerms brandImgAgg = subBrandAggs.get("brand_img_agg");
            String brandImg = brandImgAgg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImg);
            //4.2.2 得到品牌名字
            ParsedStringTerms brandNameAgg = subBrandAggs.get("brand_name_agg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);
            /** 构建品牌面包屑导航 */
            navVo.setNavId(brandId);
            navVo.setNavName("品牌");
            navVo.setNavValue(brandName);
            brandNavs.add(navVo);

            // 将每个封装的品牌信息放到品牌集合中
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        //5、 查询涉及到的所有属性
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        //ParsedNested用于接收内置属性的聚合
        ParsedNested parsedNested = aggregations.get("attr_agg");
        ParsedLongTerms attrIdAgg = parsedNested.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            //5.1 查询属性id
            Long attrId = bucket.getKeyAsNumber().longValue();
            attrVo.setAttrId(attrId);
            //5.2 获得子聚合
            Aggregations subAttrAgg = bucket.getAggregations();
            //5.2.1 查询属性名
            ParsedStringTerms attrNameAgg = subAttrAgg.get("attr_name_agg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);
            //5.2.2 查询属性值
            ParsedStringTerms attrValueAgg = subAttrAgg.get("attr_value_agg");
            List<String> attrValues = attrValueAgg.getBuckets().stream().map(attrValue -> {
                return attrValue.getKeyAsString();
            }).collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);
            // 将每个封装的属性信息放到属性集合中
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);

        // 6. 构建面包屑导航
        String queryString = searchParams.get_queryString();
        // 6.1、属性attrs
        List<String> attrs = searchParams.getAttrs();
        if (attrs != null && attrs.size() > 0) {
            List<SearchResult.NavVo> navVos = attrs.stream().map(attr -> {
                String[] split = attr.split("_");
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                //6.1.1 设置属性值
                navVo.setNavValue(split[1]);
                //6.1.2 查询并设置属性名
                try {
                    R r = productFeignService.info(Long.parseLong(split[0]));
                    if (r.getCode() == 0) {
                        AttrResponseVo attrResponseVo = JSON.parseObject(JSON.toJSONString(r.get("attr")), new TypeReference<AttrResponseVo>() {});
                        navVo.setNavName(attrResponseVo.getAttrName());
                    }else {
                        navVo.setNavName(split[0]);
                    }
                } catch (Exception e) {
                    log.error("远程调用商品服务查询属性失败", e);
                }
                //6.1.3 设置面包屑跳转链接
//                String queryString = searchParams.get_queryString();
                String encode = null;
                try {
                    encode = URLEncoder.encode(attr, "UTF-8");
                    encode = encode.replace("+", "%20"); // 浏览器和java对空格字符编码不一样
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String replace = queryString.replace("&attrs=" + encode, "").replace("attrs=" + encode + "&", "").replace("attrs=" + encode, "");
                navVo.setLink("http://search.pineconemall.com/list.html" + (replace.isEmpty()?"":"?"+replace));

                result.getAttrIds().add(Long.parseLong(split[0]));
                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(navVos);
        }
        //6.2、品牌brand
        List<Long> brandIds = searchParams.getBrandId();
        if (brandIds != null && brandIds.size() > 0){
            Long brandId = brandIds.get(0);
            // 其实不用for循环来删除，elasticsearch查询条件中筛选了brandId，结果中只留下当前筛选的一个值
            for (SearchResult.NavVo navVo : brandNavs){
                if ("品牌".equals(navVo.getNavName()) && !navVo.getNavId().equals(brandId)){
                    brandNavs.remove(navVo);
                }else if ("品牌".equals(navVo.getNavName()) && navVo.getNavId().equals(brandId)){
                    String replace = queryString.replace("&brandId=" + brandId, "").replace("brandId=" + brandId + "&", "").replace("brandId=" + brandId, "");
                    navVo.setLink("http://search.pineconemall.com/list.html" + (replace.isEmpty()?"":"?"+replace));
                    result.getNavs().add(navVo);
                }
            }
        }else {
            result.getNavs().removeIf(navVo -> "品牌".equals(navVo.getNavName()));
        }
        //6.3、分类catalog
        Long catalog3Id = searchParams.getCatalog3Id();
        if (catalog3Id != null){
            // 其实不用for循环来删除，elasticsearch查询条件中筛选了catalogId，结果中只留下当前筛选的一个值
            for (SearchResult.NavVo navVo : catalogNavs){
                if ("分类".equals(navVo.getNavName()) && !navVo.getNavId().equals(catalog3Id)){
                    catalogNavs.remove(navVo);
                }else if ("分类".equals(navVo.getNavName()) && navVo.getNavId().equals(catalog3Id)){
                    String replace = queryString.replace("&catalog3Id=" + catalog3Id, "").replace("catalog3Id=" + catalog3Id + "&", "").replace("catalog3Id=" + catalog3Id, "");
                    navVo.setLink("http://search.pineconemall.com/list.html" + (replace.isEmpty()?"":"?"+replace));
                    result.getNavs().add(navVo);
                }
            }
        }else {
            result.getNavs().removeIf(navVo -> "分类".equals(navVo.getNavName()));
        }

        return result;
    }
}

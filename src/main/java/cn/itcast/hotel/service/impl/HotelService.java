package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.*;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {

    @Resource
    private RestHighLevelClient restClient;

    @Override
    public PageResult search(HotelListDTO hotelListDTO) throws IOException {
        //1、准备Request
        SearchRequest request = new SearchRequest("hotel");
        //2、准备DSL
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        String key = hotelListDTO.getKey();
        if (!StringUtils.isEmpty(key)) {
            boolQuery.must(QueryBuilders.matchQuery("all", key));
        } else {
            boolQuery.must(QueryBuilders.matchAllQuery());
        }
        if (!StringUtils.isEmpty(hotelListDTO.getCity())) {
            boolQuery.filter(QueryBuilders.termQuery("city", hotelListDTO.getCity()));
        }
        if (!StringUtils.isEmpty(hotelListDTO.getBrand())) {
            boolQuery.filter(QueryBuilders.termQuery("brand", hotelListDTO.getBrand()));
        }
        if (!StringUtils.isEmpty(hotelListDTO.getStarName())) {
            boolQuery.filter(QueryBuilders.termQuery("starName", hotelListDTO.getStarName()));
        }
        if (hotelListDTO.getMinPrice() != null && hotelListDTO.getMaxPrice() != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("price").gte(hotelListDTO.getMinPrice()).lte(hotelListDTO.getMaxPrice()));
        }

        // 2.2  分页
        int page = hotelListDTO.getPage();
        int size = hotelListDTO.getSize();
        request.source().from((page - 1) * size).size(size);
        //  2.3 排序
        request.source().query(boolQuery);
        if (!StringUtils.isEmpty(hotelListDTO.getLocation())) {
            request.source().sort(SortBuilders.geoDistanceSort("location", new GeoPoint(hotelListDTO.getLocation())).order(SortOrder.ASC).unit(DistanceUnit.KILOMETERS));
        }
        //3、发送请求
        SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);

        //4、解析响应
        handleResponse(response);
        return handleResponse(response);
    }

    @Override
    public Map<String, List<String>> filters(HotelListDTO hotelListDTO) throws IOException {
        //1、准备Request
        SearchRequest request = new SearchRequest("hotel");
        // 2、准备DSL
        //2.1限定范围查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        String key = hotelListDTO.getKey();
        if (!StringUtils.isEmpty(key)) {
            boolQuery.must(QueryBuilders.matchQuery("all", key));
        } else {
            boolQuery.must(QueryBuilders.matchAllQuery());
        }
        if (!StringUtils.isEmpty(hotelListDTO.getCity())) {
            boolQuery.filter(QueryBuilders.termQuery("city", hotelListDTO.getCity()));
        }
        if (!StringUtils.isEmpty(hotelListDTO.getBrand())) {
            boolQuery.filter(QueryBuilders.termQuery("brand", hotelListDTO.getBrand()));
        }
        if (!StringUtils.isEmpty(hotelListDTO.getStarName())) {
            boolQuery.filter(QueryBuilders.termQuery("starName", hotelListDTO.getStarName()));
        }
        if (hotelListDTO.getMinPrice() != null && hotelListDTO.getMaxPrice() != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("price").gte(hotelListDTO.getMinPrice()).lte(hotelListDTO.getMaxPrice()));
        }
        request.source().query(boolQuery);
        // 2.2 聚合
        request.source().size(0);
        request.source().aggregation(AggregationBuilders.terms("brandAgg").field("brand").size(100));
        request.source().aggregation(AggregationBuilders.terms("cityAgg").field("city").size(100));
        request.source().aggregation(AggregationBuilders.terms("starNameAgg").field("starName").size(10));
        //3、发送请求
        SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);
        Aggregations aggregations = response.getAggregations();
        // 4、解析响应
        List<String> brandAgg = getAggByName(aggregations, "brandAgg");
        List<String> cityAgg = getAggByName(aggregations, "cityAgg");
        List<String> starNameAgg = getAggByName(aggregations, "starNameAgg");
        Map<String, List<String>> filters = new HashMap<>();
        filters.put("brand", brandAgg);
        filters.put("city", cityAgg);
        filters.put("starName", starNameAgg);
        return filters;
    }

    @Override
    public List<String> getSuggestions(String key) throws IOException {
        //1、准备Request
        SearchRequest request = new SearchRequest("hotel");
        //2、准备DSL
        request.source().suggest(new SuggestBuilder().addSuggestion("suggestions",  SuggestBuilders.completionSuggestion("suggestion").prefix(key).skipDuplicates(true).size(10)));
        //3、发送请求
        SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);

        //4、解析响应
        Suggest suggest = response.getSuggest();
        CompletionSuggestion suggestions = suggest.getSuggestion("suggestions");
        List<CompletionSuggestion.Entry.Option> options = suggestions.getOptions();
        List<String> optionsList = new ArrayList<>();

        for (CompletionSuggestion.Entry.Option option : options){
            String text = option.getText().toString();
            optionsList.add(text);
        }
        return optionsList;
    }


    public PageResult handleResponse(SearchResponse response) {
        //1、获取总条数
        long total = response.getHits().getTotalHits().value;
        //2、获取元数据
        SearchHit[] hits = response.getHits().getHits();
        //3、拿到结果数组
        List<HotelDoc> hotelDocs = new ArrayList<>();
        for (SearchHit hit : hits) {
            String source = hit.getSourceAsString();
            HotelDoc hotelDoc = JSON.parseObject(source, HotelDoc.class);
            //获取排序值
            Object[] sortValues = hit.getSortValues();
            if (sortValues != null && sortValues.length > 0) {
                //回显距离
                hotelDoc.setDistance(sortValues[0]);
            }
            hotelDocs.add(hotelDoc);
        }
        PageResult result = new PageResult(total, hotelDocs);

        return result;
    }

    public List<String> getAggByName(Aggregations aggregations, String aggName) {
        //获取聚合结果
        Terms brandTerms = aggregations.get(aggName);
        //获取bucket
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        List<String> list = new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            String keyAsString = bucket.getKeyAsString();
            list.add(keyAsString);
        }
        //遍历
        return list;
    }
}

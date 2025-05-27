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
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
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
        BoolQueryBuilder  boolQuery = QueryBuilders.boolQuery();
        String key = hotelListDTO.getKey();
        if(!StringUtils.isEmpty(key)){
            boolQuery.must(QueryBuilders.matchQuery("all", key));
        }else {
            boolQuery.must(QueryBuilders.matchAllQuery());
        }
        if(!StringUtils.isEmpty(hotelListDTO.getCity())){
            boolQuery.filter(QueryBuilders.termQuery("city", hotelListDTO.getCity()));
        }
        if(!StringUtils.isEmpty(hotelListDTO.getBrand())){
            boolQuery.filter(QueryBuilders.termQuery("brand", hotelListDTO.getBrand()));
        }
        if(!StringUtils.isEmpty(hotelListDTO.getStarName())){
            boolQuery.filter(QueryBuilders.termQuery("starName", hotelListDTO.getStarName()));
        }
        if(hotelListDTO.getMinPrice() != null && hotelListDTO.getMaxPrice() != null){
            boolQuery.filter(QueryBuilders.rangeQuery("price").gte(hotelListDTO.getMinPrice()).lte(hotelListDTO.getMaxPrice()));
        }
        request.source().query(boolQuery);
        // 2.2  分页
        int page = hotelListDTO.getPage();
        int size = hotelListDTO.getSize();
        request.source().from((page-1) * size).size(size);
        //3、发送请求
        SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);

        //4、解析响应
        handleResponse(response);
        return handleResponse(response);
    }




    public PageResult handleResponse(SearchResponse response){
        //1、获取总条数
        long total = response.getHits().getTotalHits().value;
        //2、获取元数据
        SearchHit[] hits = response.getHits().getHits();
        //3、拿到结果数组
        List<HotelDoc> hotelDocs = new ArrayList<>();
        for (SearchHit hit : hits) {
            String source = hit.getSourceAsString();
            HotelDoc hotelDoc = JSON.parseObject(source, HotelDoc.class);
            hotelDocs.add(hotelDoc);
        }
        PageResult result = new PageResult(total,hotelDocs);

        return result;
    }
}

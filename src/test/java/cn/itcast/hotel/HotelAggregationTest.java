package cn.itcast.hotel;


import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class HotelAggregationTest {
    @Resource
    private RestHighLevelClient restClient;


    //分组聚合
    @Test
     void testAggregation() throws Exception {
        //1、构造请求
        SearchRequest request = new SearchRequest("hotel");
        //2、DSL语句
        request.source().size(0);
        request.source().aggregation(AggregationBuilders.terms("brandAgg").size(10).field("brand"));
        //2、发送请求
        SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }
}

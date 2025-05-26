package cn.itcast.hotel;


import cn.itcast.hotel.pojo.Hotel;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Map;

@SpringBootTest
public class HotelSearchTest {
    private RestHighLevelClient client;

    @Test
    void testMatchAll() throws IOException {
        // 1.准备请求
        SearchRequest request = new SearchRequest("hotel");
        // 2.构建DSL
        request.source().query(QueryBuilders.matchAllQuery());
        // 3.发送
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        System.out.println(response);
        // 4.解析结果
        SearchHits searchHits = response.getHits();
        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit : hits) {
            String source = hit.getSourceAsString();
            Hotel hotel = JSON.parseObject(source, Hotel.class);
            System.out.println(hotel);
        }
    }

    @Test
    void testMatch() throws IOException {
        // 1.准备请求
        SearchRequest request = new SearchRequest("hotel");
        // 2.构建DSL
        request.source().query(QueryBuilders.matchQuery("all", "如家"));
        // 3.发送
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }

    @Test
    void testBool() throws IOException {
        //1.构建请求
        SearchRequest request = new SearchRequest("hotel");
        //2.构建DSL
        request.source().query(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("city", "上海")));
        //3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }

    @Test
    void testPage() throws IOException {
        //1.构建请求
        SearchRequest request = new SearchRequest("hotel");
        //2.构建DSL
        request.source().from(100).size(10).query(QueryBuilders.matchAllQuery());
        //3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        System.out.println(response);

    }

    @Test
    void testHighLight() throws IOException {
        //1.构建请求
        SearchRequest request = new SearchRequest("hotel");
        //2.构建DSL
        request.source().query(QueryBuilders.matchQuery("all", "如家"));
        request.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));
        //3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 4.解析结果
        SearchHits searchHits = response.getHits();
        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit : hits) {
            String source = hit.getSourceAsString();
            Hotel hotel = JSON.parseObject(source, Hotel.class);
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if(!CollectionUtils.isEmpty(highlightFields)){
                HighlightField highlightField = highlightFields.get("name");
                if(highlightField != null){
                    hotel.setName(highlightField.getFragments()[0].string());
                }

            }
            System.out.println(hotel);
        }

    }


    @BeforeEach
    void setUp() {
        client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.181.129:9200")
        ));
    }

    @AfterEach
    void tearDown() throws IOException {
        client.close();
    }

}

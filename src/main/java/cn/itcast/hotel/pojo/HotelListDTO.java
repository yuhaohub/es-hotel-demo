package cn.itcast.hotel.pojo;


import lombok.Data;

@Data
public class HotelListDTO {
    //搜索关键字
    private String key;
    //页码
    private Integer page;
    //每页大小
    private Integer size;
    //排序字段
    private String sortBy;
    //品牌
    private String brand;
    //星级
    private String starName;
    //城市
    private String city;
    //最低价格
    private Integer minPrice;
    //最高价格
    private Integer maxPrice;
}

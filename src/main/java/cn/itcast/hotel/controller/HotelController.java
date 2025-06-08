package cn.itcast.hotel.controller;


import cn.itcast.hotel.pojo.HotelListDTO;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.service.IHotelService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hotel")
public class HotelController {

    @Resource
    private IHotelService hotelService;

    //分页查询
    @PostMapping("/list")
    public PageResult search(@RequestBody HotelListDTO hotelListDTO) throws IOException {
        return hotelService.search(hotelListDTO);
    }

    //获取过滤条件
    @PostMapping("/filters")
    public Map<String,List<String>> filters(@RequestBody HotelListDTO hotelListDTO) throws IOException {
        return hotelService.filters(hotelListDTO);
    }
}

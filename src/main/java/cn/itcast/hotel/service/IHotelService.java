package cn.itcast.hotel.service;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelListDTO;
import cn.itcast.hotel.pojo.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;

public interface IHotelService extends IService<Hotel> {
    /**
     * 分页查询
     * @param hotelListDTO
     * @return
     * @throws IOException
     */
    PageResult search(HotelListDTO hotelListDTO) throws IOException;


}

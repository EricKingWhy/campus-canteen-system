package fun.cyhgraph.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.cyhgraph.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
    
    /**
     * 获取订单详情并联查菜品表获取最新图片
     */
    List<OrderDetail> getOrderDetailWithDishImage(Long orderId);
}

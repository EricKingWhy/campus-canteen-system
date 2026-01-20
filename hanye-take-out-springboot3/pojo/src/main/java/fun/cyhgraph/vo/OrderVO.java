package fun.cyhgraph.vo;

import fun.cyhgraph.entity.OrderDetail;
import fun.cyhgraph.entity.Orders;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderVO extends Orders implements Serializable {

    private String orderDishes; // 订单菜品信息
    private List<OrderDetail> orderDetailList; // 订单详情

}

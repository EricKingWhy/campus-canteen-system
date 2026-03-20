package fun.cyhgraph.service;

import fun.cyhgraph.vo.BusinessDataVO;
import fun.cyhgraph.vo.DishOverViewVO;
import fun.cyhgraph.vo.OrderOverViewVO;
import fun.cyhgraph.vo.SetmealOverViewVO;
import java.time.LocalDateTime;

// 【关键】类名 WorkspaceService 必须与文件名 WorkspaceService.java (小写s) 一致
public interface WorkspaceService {
    BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end);

    OrderOverViewVO getOverviewOrders();

    DishOverViewVO getOverviewDishes();

    SetmealOverViewVO getOverviewSetmeals();
}

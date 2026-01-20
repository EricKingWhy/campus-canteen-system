package fun.cyhgraph.service.impl;

import fun.cyhgraph.service.WorkspaceService;
import fun.cyhgraph.vo.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        return new BusinessDataVO();
    }

    public OrderOverViewVO getOverviewOrders() {
        return new OrderOverViewVO();
    }

    public DishOverViewVO getOverviewDishes() {
        return new DishOverViewVO();
    }

    public SetmealOverViewVO getOverviewSetmeals() {
        return new SetmealOverViewVO();
    }
}

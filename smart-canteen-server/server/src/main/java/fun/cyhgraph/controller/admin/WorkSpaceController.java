package fun.cyhgraph.controller.admin;

import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.WorkspaceService; // 【核心修复】引用正确的 WorkspaceService
import fun.cyhgraph.vo.BusinessDataVO;
import fun.cyhgraph.vo.DishOverViewVO;
import fun.cyhgraph.vo.OrderOverViewVO;
import fun.cyhgraph.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/admin/workspace")
@Slf4j
public class WorkSpaceController {

    @Autowired
    private WorkspaceService workspaceService;

    @GetMapping("/businessData")
    public Result<BusinessDataVO> businessData() {
        LocalDateTime begin = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);
        return Result.success(workspaceService.getBusinessData(begin, end));
    }

    @GetMapping("/overviewOrders")
    public Result<OrderOverViewVO> orderOverView() {
        return Result.success(workspaceService.getOverviewOrders());
    }

    @GetMapping("/overviewDishes")
    public Result<DishOverViewVO> dishOverView() {
        return Result.success(workspaceService.getOverviewDishes());
    }

    @GetMapping("/overviewSetmeals")
    public Result<SetmealOverViewVO> setmealOverView() {
        return Result.success(workspaceService.getOverviewSetmeals());
    }
}

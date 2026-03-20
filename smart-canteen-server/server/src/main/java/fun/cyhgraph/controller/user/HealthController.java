package fun.cyhgraph.controller.user;

import fun.cyhgraph.entity.Dish;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.DishService; // 假设有 DishService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/user/health")
public class HealthController {
    // 简单实现，防止报错
    @GetMapping("/plan")
    public Result<String> getPlan() {
        return Result.success("健康计划");
    }
}

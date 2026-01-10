package fun.cyhgraph.controller.user;

import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.entity.Dish;
import fun.cyhgraph.entity.Order;
import fun.cyhgraph.entity.OrderDetail;
import fun.cyhgraph.entity.User;
import fun.cyhgraph.mapper.DishMapper;
import fun.cyhgraph.mapper.OrderDetailMapper;
import fun.cyhgraph.mapper.OrderMapper;
import fun.cyhgraph.mapper.UserMapper;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.vo.HealthStatsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/health")
@Slf4j
public class HealthController {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private DishMapper dishMapper;

    @GetMapping("/stats")
    public Result<HealthStatsVO> getHealthStats() {
        Integer userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.success(emptyStats("请完善个人信息"));
        }
        User user = userMapper.getById(userId);
        if (user == null || user.getHeight() == null || user.getWeight() == null) {
            return Result.success(emptyStats("请完善个人信息"));
        }
        double height = user.getHeight();
        double weight = user.getWeight();
        if (height <= 0 || weight <= 0) {
            return Result.success(emptyStats("请完善个人信息"));
        }

        double bmi = weight / Math.pow(height / 100.0, 2);
        String bmiStatus = getBmiStatus(bmi);
        double tdee = calcTdee(user);
        String suggestion = needProtein(userId) ? "建议补充蛋白质" : "饮食均衡，保持规律";

        HealthStatsVO stats = HealthStatsVO.builder()
                .bmi(round(bmi))
                .bmiStatus(bmiStatus)
                .targetCalories(round(tdee))
                .suggestion(suggestion)
                .build();
        return Result.success(stats);
    }

    private HealthStatsVO emptyStats(String suggestion) {
        return HealthStatsVO.builder()
                .bmi(null)
                .bmiStatus("请完善个人信息")
                .targetCalories(null)
                .suggestion(suggestion)
                .build();
    }

    private double calcTdee(User user) {
        double weight = user.getWeight() == null ? 0 : user.getWeight();
        double height = user.getHeight() == null ? 0 : user.getHeight();
        int age = user.getAge() == null ? 0 : user.getAge();
        int gender = user.getGender() == null ? 1 : user.getGender();
        double bmr;
        if (gender == 1) {
            bmr = 10 * weight + 6.25 * height - 5 * age + 5;
        } else {
            bmr = 10 * weight + 6.25 * height - 5 * age - 161;
        }
        double activity = user.getActivityFactor() == null ? 1.2 : user.getActivityFactor();
        return bmr * activity;
    }

    private boolean needProtein(Integer userId) {
        List<Order> orders = orderMapper.getRecentCompletedByUser(userId, 5);
        double totalProtein = 0;
        int totalCount = 0;
        for (Order o : orders) {
            List<OrderDetail> details = orderDetailMapper.getById(o.getId());
            for (OrderDetail detail : details) {
                if (detail.getDishId() == null) {
                    continue;
                }
                Dish dish = dishMapper.getById(detail.getDishId());
                if (dish == null || dish.getProtein() == null) {
                    continue;
                }
                int count = detail.getNumber() == null ? 0 : detail.getNumber();
                totalProtein += dish.getProtein() * count;
                totalCount += count;
            }
        }
        double avgProtein = totalCount == 0 ? 0 : totalProtein / totalCount;
        return avgProtein < 20;
    }

    private String getBmiStatus(double bmi) {
        if (bmi < 18.5) {
            return "偏瘦";
        } else if (bmi < 24) {
            return "正常";
        } else {
            return "超重";
        }
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}

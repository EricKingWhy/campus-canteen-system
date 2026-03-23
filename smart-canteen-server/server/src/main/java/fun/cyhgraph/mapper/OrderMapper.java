package fun.cyhgraph.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.cyhgraph.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {

    @Select("select sum(amount) from orders where order_time > #{begin} and order_time < #{end} and status = #{status}")
    Double sumByMap(Map map);

    Integer countByMap(Map map);

    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);

    /**
     * 按用户ID、时间范围和支付状态汇总订单金额
     * 用于餐费分析
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM orders WHERE user_id = #{userId} AND order_time >= #{startTime} AND order_time <= #{endTime} AND pay_status = #{payStatus} AND status != 6")
    BigDecimal sumAmountByUserIdAndTimeRange(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("payStatus") Integer payStatus);

    List<fun.cyhgraph.vo.DailyTrendVO> getWeeklyDailyTrend(
            @Param("userId") Long userId, 
            @Param("startTime") java.time.LocalDateTime startTime, 
            @Param("endTime") java.time.LocalDateTime endTime);

    List<fun.cyhgraph.vo.MealPeriodVO> getMealPeriodDistribution(
            @Param("userId") Long userId, 
            @Param("startTime") java.time.LocalDateTime startTime, 
            @Param("endTime") java.time.LocalDateTime endTime);
            
    java.util.Map<String, Object> getMaxSpendDish(
            @Param("userId") Long userId, 
            @Param("startTime") java.time.LocalDateTime startTime, 
            @Param("endTime") java.time.LocalDateTime endTime);

    java.util.Map<String, Object> getMostFrequentDish(
            @Param("userId") Long userId, 
            @Param("startTime") java.time.LocalDateTime startTime, 
            @Param("endTime") java.time.LocalDateTime endTime);

    Map<String, Object> getWeeklyMaxSingleOrder(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    Map<String, Object> getWeeklyTopDish(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    List<LocalDateTime> getWeeklyOrderTimes(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}

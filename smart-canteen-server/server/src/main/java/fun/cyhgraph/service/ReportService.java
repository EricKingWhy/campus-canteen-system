package fun.cyhgraph.service;

import fun.cyhgraph.vo.*;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {
    // 修正方法名以匹配 Controller
    TurnoverReportVO getTurnover(LocalDate begin, LocalDate end);

    UserReportVO getUser(LocalDate begin, LocalDate end);

    OrderReportVO getOrder(LocalDate begin, LocalDate end);

    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);

    void exportBusinessData(HttpServletResponse response);
}

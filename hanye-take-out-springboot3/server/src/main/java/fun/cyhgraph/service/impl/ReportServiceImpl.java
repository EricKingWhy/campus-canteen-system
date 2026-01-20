package fun.cyhgraph.service.impl;

import fun.cyhgraph.service.ReportService;
import fun.cyhgraph.vo.*;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@Service
public class ReportServiceImpl implements ReportService {
    public TurnoverReportVO getTurnover(LocalDate begin, LocalDate end) {
        return new TurnoverReportVO();
    }

    public UserReportVO getUser(LocalDate begin, LocalDate end) {
        return new UserReportVO();
    }

    public OrderReportVO getOrder(LocalDate begin, LocalDate end) {
        return new OrderReportVO();
    }

    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        return new SalesTop10ReportVO();
    }

    public void exportBusinessData(HttpServletResponse response) {
    }
}

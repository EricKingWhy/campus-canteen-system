package fun.cyhgraph.controller.admin;

import fun.cyhgraph.dto.SetmealDTO;
import fun.cyhgraph.dto.SetmealPageDTO;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.SetmealService;
import fun.cyhgraph.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController("adminSetmealController")
@RequestMapping("/admin/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @PostMapping
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        setmealService.addSetmeal(setmealDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(SetmealPageDTO setmealPageDTO) {
        return Result.success(setmealService.getPageList(setmealPageDTO));
    }

    @DeleteMapping
    public Result delete(@RequestParam List<Integer> ids) {
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<SetmealVO> getById(@PathVariable Integer id) {
        return Result.success(setmealService.getSetmealById(id));
    }

    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, Integer id) {
        // 【核心修复】同时传递 status 和 id
        setmealService.onOff(status, id);
        return Result.success();
    }
}

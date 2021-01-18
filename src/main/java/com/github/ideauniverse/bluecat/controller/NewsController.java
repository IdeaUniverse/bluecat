package com.github.ideauniverse.bluecat.controller;

import com.github.ideauniverse.bluecat.entity.News;
import com.github.ideauniverse.bluecat.service.NewsService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 仅供 Swagger 测试修改数据用
 */
@Api("News CRUD Api")
@RestController
@RequestMapping("/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @ApiOperation("Create News")
    @PostMapping("/create")
    public int createNews(@ApiParam(name="news",required=true) @RequestBody News news) {
        news.setCreatedAt(LocalDateTime.now());
        return newsService.save(news);
    }

    @ApiOperation("Update News")
    @PostMapping("/update-by-id")
    public int updateNews(@ApiParam(name="news", required=true) @RequestBody News news) {
        return newsService.update(news);
    }

    @ApiOperation("Delete News")
    @DeleteMapping("/delete-by-id")
    public int updateNews(@ApiParam(name="id",required=true) @RequestParam String id) {
        return newsService.deleteById(id);
    }
}

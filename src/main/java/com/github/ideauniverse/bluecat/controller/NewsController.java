package com.github.ideauniverse.bluecat.controller;

import com.github.ideauniverse.bluecat.entity.News;
import com.github.ideauniverse.bluecat.service.NewsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/news")
@Api("News CRUD Api")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @ApiImplicitParams({ @ApiImplicitParam(paramType = "body", dataType = "News", name = "news", value = "news", required = true) })
    @ApiOperation(value="Create News")
    @PostMapping("/create")
    public int createNews(@RequestBody News news){
        news.setCreatedAt(LocalDateTime.now());
        return newsService.save(news);
    }

    @ApiImplicitParams({ @ApiImplicitParam(paramType = "body", dataType = "News", name = "news", value = "news", required = true) })
    @ApiOperation(value="Update News")
    @PostMapping("/update-by-id")
    public int updateNews(@RequestBody News news){
        return newsService.update(news);
    }

    @ApiOperation(value="Delete News")
    @DeleteMapping("/delete-by-id")
    public int updateNews(@RequestParam String id){
        return newsService.deleteById(id);
    }
}

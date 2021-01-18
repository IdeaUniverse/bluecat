package com.github.ideauniverse.bluecat.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 新闻类
 */
@Data
public class News implements BlueCatEntity{

    @ApiModelProperty(example="51528475-9889-45ea-9a8f-4acb2bebf8ba")
    private String id;      // 新闻 id

    @ApiModelProperty(example="News Title")
    private String title;   // 新闻标题

    @ApiModelProperty(example="News Content")
    private String content; // 新闻内容

    @ApiModelProperty(example="0")
    private Integer likedNum;   // 点赞数量

    private LocalDateTime createdAt;    // 发布时间

}

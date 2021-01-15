package com.github.ideauniverse.bluecat.dao;

import com.github.ideauniverse.bluecat.entity.News;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface NewsDao {

    List<News> queryAll();

    int save(@Param("news") News news);

    int update(@Param("news") News news);

    int deleteById(@Param("id") String id);
}

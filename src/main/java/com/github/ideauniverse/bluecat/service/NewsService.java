package com.github.ideauniverse.bluecat.service;

import com.github.ideauniverse.bluecat.entity.News;
import java.util.List;

public interface NewsService {

    int save(News news);

    int update(News news);

    int deleteById(String id);
}

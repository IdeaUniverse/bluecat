package com.github.ideauniverse.bluecat.service.impl;

import com.github.ideauniverse.bluecat.common.BlueCatCollection;
import com.github.ideauniverse.bluecat.dao.NewsDao;
import com.github.ideauniverse.bluecat.entity.News;
import com.github.ideauniverse.bluecat.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

/**
 * 新闻业务 Service
 */
@Service
public class NewsServiceImpl implements NewsService {

    @Autowired
    private NewsDao newsDao;

    @Autowired
    private BlueCatCollection<News> collection;

    /**
     * 保存新闻
     * @param news
     * @return
     */
    @Override
    public int save(News news) {
        news.setId(UUID.randomUUID().toString());
        collection.add(news);
        return collection.getCollection().size();
    }

    /**
     * 修改新闻
     * @param news
     * @return
     */
    @Override
    public int update(News news) {
        return collection.update(news);
    }

    /**
     * 删除新闻
     * @param id
     * @return
     */
    @Override
    public int deleteById(String id) {
        return collection.deleteById(id);
    }
}

package com.github.ideauniverse.bluecat.common;

import com.github.ideauniverse.bluecat.entity.BlueCatEntity;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 这个集合类为内存数据抽象，将当前活跃用户关注的数据存放于此，后期可改为内存数据库如 Redis
 * 这个集合对应数据库中某个表，集合中的元素对应表中的记录
 * 对本集合的操作，数据会通过 Spring AOP 同步至数据库和前端
 * @param <T>
 */
@Component
public class BlueCatCollection <T extends BlueCatEntity>{

    /**
     * 内置集合（暂未考虑线程安全）
     */
    private List<T> dataList = new ArrayList<>();

    /**
     * 增加元素
     * @param e
     */
    public void add(T e){
        dataList.add(e);
    }

    /**
     * 更新元素
     * @param e
     * @return
     */
    public int update(T e){
        dataList = dataList.stream()
                .map(item -> item.getId().equals(e.getId()) ? e : item)
                .collect(Collectors.toList());
        return 1;
    }

    /**
     * 根据元素 id 删除元素
     * @param id 元素 id
     * @return
     */
    public int deleteById(String id){
        dataList = dataList.stream()
                .filter(item -> !item.getId().equals(id))
                .collect(Collectors.toList());
        return 1;
    }

    /**
     * 获取集合
     * @return
     */
    public List<T> getCollection(){
        return dataList;
    }

    /**
     * 设置集合
     * @param collection
     */
    public void setCollection(List<T> collection){
        dataList = collection;
    }
}

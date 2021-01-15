package com.github.ideauniverse.bluecat.common;

import com.github.ideauniverse.bluecat.entity.BlueCatEntity;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 这个集合类为内存数据抽象，后期可改为操作内存数据库如Redis
 * 这个集合对应数据库中某个表，集合中的元素对应表中的记录
 * 对本集合的操作，数据会通过 Spring AOP 同步至数据库和前端
 * @param <T>
 */
@Component
public class BlueCatCollection <T extends BlueCatEntity>{

    private List<T> dataList = new ArrayList<>();

    public void add(T e){
        dataList.add(e);
    }

    public int update(T e){
        dataList = dataList.stream()
                .map(item -> item.getId().equals(e.getId()) ? e : item)
                .collect(Collectors.toList());
        return 1;
    }

    public int deleteById(String id){
        dataList = dataList.stream()
                .filter(item -> !item.getId().equals(id))
                .collect(Collectors.toList());
        return 1;
    }

    public List<? extends BlueCatEntity> getCollection(){
        return dataList;
    }

    public void setCollection(List<T> collection){
        dataList = collection;
    }
}

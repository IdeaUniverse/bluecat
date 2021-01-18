package com.github.ideauniverse.bluecat.common;

import com.github.ideauniverse.bluecat.dao.NewsDao;
import com.github.ideauniverse.bluecat.entity.Message;
import com.github.ideauniverse.bluecat.entity.News;
import com.github.ideauniverse.bluecat.netty.WebSocketManager;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * BlueCatCollection 对应的 AOP 配置
 * 执行 BlueCatCollection 的 增、删、改 方法会自动执行相应的数据同步方法
 */
@Slf4j
@Aspect
@Component
public class DaoAspect {

    @Autowired
    private NewsDao newsDao;

    @Pointcut("execution(public * com.github.ideauniverse.bluecat.common.BlueCatCollection.*(..))")
    private void pointcut() {}

    @Before(value = "pointcut()")
    public void methodBefore(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        if("add".equals(methodName)){ // 如果是创建事件，则同步在数据库中创建
            News news = (News)joinPoint.getArgs()[0];
            if(newsDao.save(news) == 1) {
                Message<News> message = new Message<>();
                log.info("数据库添加成功!");
                message.setContent(news);
                message.setType(Constants.MESSAGE_TYPE_CREATE);
                WebSocketManager.broadCast(message);
            }
        }else if("update".equals(methodName)){ // 如果是更新事件，则同步在数据库中更新
            News news = (News)joinPoint.getArgs()[0];
            if(newsDao.update(news) == 1) {
                log.info("数据库修改数据成功!");
                Message<News> message = new Message<>();
                message.setContent(news);
                message.setType(Constants.MESSAGE_TYPE_UPDATE);
                WebSocketManager.broadCast(message);
            }
        }else if("deleteById".equals(methodName)){ // 如果是删除事件，则同步在数据库中删除
            String newsId = (String)joinPoint.getArgs()[0];
            if(newsDao.deleteById(newsId) == 1 ){
                log.info("数据库修删除据成功!");
                Message<String> message = new Message<>();
                message.setContent(newsId);
                message.setType(Constants.MESSAGE_TYPE_DELETE);
                WebSocketManager.broadCast(message);
            }
        }
    }
}

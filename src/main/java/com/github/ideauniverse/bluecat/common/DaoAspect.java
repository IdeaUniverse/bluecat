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
        Message message = new Message();
        String methodName = joinPoint.getSignature().getName();
        if("add".equals(methodName)){
            News news = (News)joinPoint.getArgs()[0];
            if(newsDao.save(news) == 1) {
                log.info("数据库添加成功!");
                message.setContent(news);
                message.setType(Constants.MESSAGE_TYPE_CREATE);
                WebSocketManager.broadCast(message);
            }
        }else if("update".equals(methodName)){
            News news = (News)joinPoint.getArgs()[0];
            if(newsDao.update(news) == 1) {
                log.info("数据库修改数据成功!");
                message.setContent(news);
                message.setType(Constants.MESSAGE_TYPE_UPDATE);
                WebSocketManager.broadCast(message);
            }
        }else if("deleteById".equals(methodName)){
            String newsId = (String)joinPoint.getArgs()[0];
            if(newsDao.deleteById(newsId) == 1 ){
                log.info("数据库修删除据成功!");
                message.setContent(newsId);
                message.setType(Constants.MESSAGE_TYPE_DELETE);
                WebSocketManager.broadCast(message);
            }
        }
    }
}

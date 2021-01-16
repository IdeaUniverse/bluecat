# BlueCat
![image](https://github.com/IdeaUniverse/bluecat/blob/master/src/main/resources/static/bluecat.png)
### Introduction

* 本项目思想类似前端MVVM。将当前活跃用户页面关注的数据存放于内存中，前端页面数据绑定内存中的数据
* 将数据库操作抽象到内存中(可以类比内存数据库)，提升为操作内存（或缓存如Redis）中的数据 
* 利用 Spring AOP 将内存数据修改同步至数据库，同时通过 WebSocket 将数据修改同步至前端。
* 前端只需监听后台中数据修改的事件，数据修改会实时同步
* 因此，前端无需发送增、删、改、查的http请求，后端也无需提供相应接口

### Test
#### 打开下面两个页面，在swagger中操作数据，数据会同时同步至数据库和前端页面
* 测试页面 http://localhost:8080
* Swagger http://localhost:8080/swagger-ui.html
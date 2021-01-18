package com.github.ideauniverse.bluecat.common;

/**
 * 常量
 */
public class Constants {

    public static final int MESSAGE_TYPE_HANDSHAKE = 1;     // 与前端建立连接后的握手事件

    public static final int MESSAGE_TYPE_HEARTBEAT = 2;     // 心跳检测事件

    public static final int MESSAGE_TYPE_LIST = 3;          // 查询列表事件

    public static final int MESSAGE_TYPE_CREATE = 4;        // 创建元素事件

    public static final int MESSAGE_TYPE_UPDATE = 5;        // 更新元素事件

    public static final int MESSAGE_TYPE_DELETE = 6;        // 删除元素事件

}

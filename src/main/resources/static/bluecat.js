/**
 * 消息类型
 * @type {{DELETE: number, CREATE: number, HANDSHAKE: number, UPDATE: number, HEARTBEAT: number, LIST: number}}
 */
const MESSAGE_TYPE = {
    HANDSHAKE: 1,   // 与前端建立连接后的握手事件
    HEARTBEAT: 2,   // 心跳检测事件
    LIST: 3,        // 查询列表事件
    CREATE: 4,      // 创建元素事件
    UPDATE: 5,      // 更新元素事件
    DELETE: 6       // 删除元素事件
}

/**
 * 与后端通信的消息
 */
class Message {
    constructor(content, senderId, receiverId, type) {
        this.id = Date.now()    // 消息id
        this.content = content  // 消息内容
        this.senderId = senderId    // 发送者id
        this.receiverId = receiverId    // 接收者 id
        this.type = type    // 消息类型，消息类型，对应 MESSAGE_TYPE
    }
}

/**
 * 消息发送事件
 */
class MessageEvent {
    constructor(messageType, task) {
        this.id = Date.now()    // 任务id
        this.messageType = messageType   // 本事件监听的消息类型
        this.task = task    // 任务回调函数
    }

    /**
     * 如果当前消息类型是本事件对象订阅的，则执行本事件的 task
     * @param message
     */
    execute(message) {
        if (this.messageType !== message.type) {
            return
        }
        this.task(message)
    }
}

/**
 * Websocket 管理类
 */
class WebsocketManager {

    constructor({wsUrl, eventManager}) {
        this.ws = null  // Websocket 实例
        this.wsUrl = wsUrl  // 数据源地址，是一个 websoket 地址
        this.eventManager = eventManager    // 事件管理器
        this.wsTimer = null // 用于定时心跳检测的 timer
        this.clientId = null  // socket 客户端 id
    }

    /**
     * 初始化 websocket
     */
    initWebSocket() {
        const ws = new WebSocket(this.wsUrl)
        ws.onopen = () => {
            ELEMENT.Message.success('websocket 连接成功')
            this.sendMessage(new Message(null, this.clientId, null, MESSAGE_TYPE.HANDSHAKE))
        };

        ws.onmessage = e => {
            const message = JSON.parse(e.data);
            console.log('收到消息', message)
            // 收到消息后执行监听该类型消息的事件
            this.eventManager.run(message)
        };

        ws.onclose = () => {
            ELEMENT.Message.warning('websocket 连接关闭')
        };

        ws.onerror = e => {
            ELEMENT.Message.error('websocket 发生错误')
            console.log('websocket 发生错误', e)
        }
        this.ws = ws

        this.heartbeatCheck()
    }

    /**
     * 发送消息
     * @param message
     */
    sendMessage(message) {
        this.ws.send(JSON.stringify(message))
    }

    /**
     * 心跳检测 断线重连
     */
    heartbeatCheck() {
        this.wsTimer = setInterval(() => {
            console.log('心跳检测')
            if (this.ws.readyState === WebSocket.CLOSED) {
                this.ws.close()
                clearInterval(this.wsTimer)
                ELEMENT.Message.warning('断线重连...')
                this.initWebSocket()
            }
        }, 5000)
    }
}

/**
 * 消息事件管理者
 */
class MessageEventManager{

    eventQueue = []

    /**
     * 运行事件队列中的事件
     * @param message
     */
    run(message){
        this.eventQueue.forEach(event => event.messageType === message.type && event.execute(message))
    }
    /**
     * 注册消息对应的事件
     * @param messageType
     * @param task
     */
    register(messageType, task) {
        this.eventQueue.push(new MessageEvent(messageType, task))
    }

    /**
     * 取消注册消息对应的事件
     * @param eventId
     */
    unregister(eventId){
        this.eventQueue = this.eventQueue.filter(e => e.id !== eventId)
    }
}

/**
 * 数据集合
 */
class BlueCatCollection {

    constructor({websocketManager}) {
        this.dataList = []  // 内置数据集合
        this.websocketManager = websocketManager    // websocket 管理者
    }

    /**
     * 返回包装后的 Proxy 对象
     * @param e
     * @returns {(function(): string)|{id}|*}
     */
    wrap(e){
        if(!e.hasOwnProperty('id')){
            e.id = null
        }
        const _self = this
        return new Proxy(e, {
            get(target, key) {
                if(key === 'toString'){
                    return () => JSON.stringify(target)
                }
                return Reflect.get(target, key);
            },
            set(target, key, value){
                target[key] = value
                _self.updateById(target)
            },
        })
    }

    /**
     * 向集合中添加元素
     * @param e
     * @param sync 是否将数据修改同步至后端
     */
    add(e, sync=true){
        const proxy = this.wrap(e)
        this.dataList.push(proxy)
        sync && this.websocketManager.sendMessage(new Message(proxy, this.websocketManager.clientId, null, MESSAGE_TYPE.CREATE))
    }

    /**
     * 数据更新操作
     * @param e
     * @param sync 是否将数据修改同步至后端
     */
    updateById(e, sync=true){
        const index = this.dataList.findIndex(item => item.id === e.id)
        this.dataList.splice(index, 1, this.wrap(e))
        sync && this.websocketManager.sendMessage(new Message(e, this.websocketManager.clientId, null, MESSAGE_TYPE.UPDATE))
    }

    /**
     * 数据删除操作
     * @param id
     * @param sync 是否将数据修改同步至后端
     */
    deleteById(id, sync=true){
        const index = this.dataList.findIndex(item => item.id === id)
        this.dataList.splice(index, 1)
        sync && this.websocketManager.sendMessage(new Message(id, this.websocketManager.clientId, null, MESSAGE_TYPE.DELETE))
    }

    /**
     * 设置内置集合
     * @param collection
     */
    setCollection(collection){
        this.dataList = collection
    }

    /**
     * 获取内置集合
     * @returns {[]}
     */
    getCollection(){
        return this.dataList
    }
    /**
     * 注册增、删、改、查事件
     */
    registerEvents(eventManager){
        eventManager.register(MESSAGE_TYPE.LIST, message => this.setCollection(message.content.map(e => this.wrap(e))))
        eventManager.register(MESSAGE_TYPE.CREATE, message => this.add(message.content, false))
        eventManager.register(MESSAGE_TYPE.UPDATE, message => this.updateById(message.content, false))
        eventManager.register(MESSAGE_TYPE.DELETE, message => this.deleteById(message.content, false))
    }
}

/**
 * 配置类
 */
class BlueCat {
    constructor({wsUrl}) {
        this.eventManager = new MessageEventManager()   // 事件管理者
        this.websocketManager = new WebsocketManager({wsUrl, eventManager: this.eventManager})  // websocket 管理者
        this.inited = false // 是否完成初始化
    }

    /**
     * 初始化
     */
    init(){
        this.eventManager.register(MESSAGE_TYPE.HANDSHAKE, message => {
            this.websocketManager.clientId = message.content
            this.websocketManager.sendMessage(new Message(null, this.websocketManager.clientId, null, MESSAGE_TYPE.LIST))
        })
        this.websocketManager.initWebSocket()
        this.inited = true
    }

    /**
     * 获取 BlueCatCollection 的 Proxy 代理实例
     * @returns {(function(): string)|BlueCatCollection|(function(...[*]=): any)|any}
     */
    newCollection(){
        if(!this.inited){
            this.init()
        }

        const collection = new BlueCatCollection({websocketManager: this.websocketManager})
        const proxy = new Proxy(collection, {
            get(target, key) {
                if (Number.isInteger(key.toString() * 1) || Array.prototype.hasOwnProperty(key)) {
                    target = target.dataList
                }
                if(key === 'toString'){
                    return () => JSON.stringify(target)
                }
                const value = target[key]
                if(typeof value === "function"){
                    return (...args) => Reflect.apply(value, target, args)
                }
                return Reflect.get(target, key)
            },
        })
        proxy.registerEvents(this.eventManager)
        return proxy
    }
}
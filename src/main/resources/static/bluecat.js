const MESSAGE_TYPE = {
    HANDSHAKE: 1,
    HEARTBEAT: 2,
    LIST: 3,
    CREATE: 4,
    UPDATE: 5,
    DELETE: 6
}
class Message {
    constructor(content, senderId, receiverId, type) {
        this.id = Date.now()
        this.content = content
        this.senderId = senderId
        this.receiverId = receiverId
        this.type = type
    }
}
class MessageEvent {
    constructor(messageType, task) {
        this.id = Date.now()    // 任务id
        this.messageType = messageType   // 本事件监听的消息类型
        this.task = task    // 任务函数
    }

    execute(message) {
        if (this.messageType !== message.type) {
            return
        }
        this.task(message)
    }
}

class WebsocketManager {

    constructor({wsUrl, eventManager}) {
        this.ws = null
        this.wsUrl = wsUrl
        this.eventManager = eventManager
        this.wsTimer = null
        this.clientId = null  // socket 客户端 id
    }

    initWebSocket() {
        this.eventManager.register(MESSAGE_TYPE.HANDSHAKE, message => {
            this.clientId = message.content
            this.sendMessage(new Message(null, this.clientId, null, MESSAGE_TYPE.LIST))
        })
        const ws = new WebSocket(this.wsUrl)
        ws.onopen = () => {
            console.log('websocket 连接成功')
            this.sendMessage(new Message(null, this.clientId, null, MESSAGE_TYPE.HANDSHAKE))
        };

        ws.onmessage = e => {
            const message = JSON.parse(e.data);
            console.log('收到消息', message)
            // 收到消息后执行监听该类型消息的事件
            this.eventManager.run(message)
        };

        ws.onclose = () => {
            console.log('websocket 连接关闭')
        };

        ws.onerror = e => {
            console.log('websocket 发生错误', e)
        }
        this.ws = ws

        this.heartbeatCheck()
    }
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
                console.log('断线重连...')
                clearInterval(this.wsTimer)
                this.initWebSocket()
            }
        }, 5000)
    }
}

class MessageEventManager{

    eventQueue = []

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

class BlueCatCollection {

    constructor({websocketManager}) {
        this.dataList = []
        this.websocketManager = websocketManager
    }

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
                return target[key];
            },
            set(target, key, value){
                target[key] = value
                _self.updateById(target)
            },
        })
    }

    add(e, sync=true){
        const proxy = this.wrap(e)
        this.dataList.push(proxy)
        sync && this.websocketManager.sendMessage(new Message(proxy, this.websocketManager.clientId, null, MESSAGE_TYPE.CREATE))
    }
    updateById(e, sync=true){
        const index = this.dataList.findIndex(item => item.id === e.id)
        this.dataList.splice(index, 1, this.wrap(e))
        sync && this.websocketManager.sendMessage(new Message(e, this.websocketManager.clientId, null, MESSAGE_TYPE.UPDATE))
    }
    deleteById(id, sync=true){
        const index = this.dataList.findIndex(item => item.id === id)
        this.dataList.splice(index, 1)
        sync && this.websocketManager.sendMessage(new Message(id, this.websocketManager.clientId, null, MESSAGE_TYPE.DELETE))
    }
    /**
     * 注册增、删、改、查事件
     */
    registerEvents(eventManager){
        eventManager.register(MESSAGE_TYPE.LIST, message => message.content.forEach(e => this.add(e, false)))
        eventManager.register(MESSAGE_TYPE.CREATE, message => this.add(message.content, false))
        eventManager.register(MESSAGE_TYPE.UPDATE, message => this.updateById(message.content, false))
        eventManager.register(MESSAGE_TYPE.DELETE, message => this.deleteById(message.content, false))
    }
}

class BlueCat {
    constructor({wsUrl}) {
        this.eventManager = new MessageEventManager()
        this.websocketManager = new WebsocketManager({wsUrl, eventManager: this.eventManager})
        this.init = false
    }

    newCollection(){
        if(!this.init){
            this.websocketManager.initWebSocket()
            this.init = true
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
                return value
            },
        })
        proxy.registerEvents(this.eventManager)
        return proxy
    }
}
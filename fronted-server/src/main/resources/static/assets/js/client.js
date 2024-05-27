// 假设您已经定义了createSocket函数
// 您需要将socket.js中的createSocket函数代码复制到这里
let socket;

function createSocket(token){
    // var socket;
    if(typeof(WebSocket) == "undefined") {
        console.log("您的浏览器不支持WebSocket");
    }else{
        var url = "ws://localhost:9000/ws/"+token;
        //实现化WebSocket对象，指定要连接的服务器地址与端口  建立连接
        socket = new WebSocket(url);

        //打开事件
        socket.onopen = function() {
            //心跳检测重置
            heartCheck.reset().start();
            console.log("Socket 已打开");
            socket.send("这是来自客户端的消息" + location.href + new Date());
        };
        //获得消息事件
        socket.onmessage = function(result) {
            var data = $.parseJSON(result.data);
            if(data.orderNo){
                layer.confirm("恭喜你，秒杀成功！查看订单？",{btn:["确定","取消"]},
                    function(){
                        window.location.href = "/order_detail.html?orderNo="+data.orderNo;
                    },
                    function(){
                        layer.closeAll();
                    });
            }else{
                layer.msg(data.msg);
            }
        };
        //关闭事件
        // socket.onclose = function() {
        //     console.log("Socket已关闭");
        // };

        socket.onclose = function (e) {
            console.log('websocket 断开: ' + e.code + ' ' + e.reason + ' ' + e.wasClean)
            console.log(e)
        }

        //发生了错误事件
        socket.onerror = function() {
            console.log("Socket发生了错误");
        }
        //窗口关闭
        $(window).unload(function(event){
            socket.close();
        });
    }
    return socket;
}




// 当WebSocket连接打开时
socket.addEventListener('open', function (event) {
    console.log('WebSocket connection established');
});

// 当接收到消息时
socket.addEventListener('message', function (event) {
    const data = JSON.parse(event.data);
    if (data.buttonId) {
        // 禁用被选中的按钮
        const button = document.getElementById(data.buttonId);
        button.disabled = true;
        button.classList.add('selected');
    }
});

// 当WebSocket连接关闭时
socket.addEventListener('close', function (event) {
    console.log('WebSocket connection closed');
    // 尝试重新连接
    reconnect(token);
});

// 当WebSocket连接发生错误时
socket.addEventListener('error', function (event) {
    console.error('WebSocket error:', event);
});

// 尝试重新连接函数
function reconnect(token) {
    // 这里调用createSocket函数来重新连接
    createSocket(token);
}

// 从localStorage中获取token
function getTokenFromLocalStorage() {
    if (typeof(Storage) !== "undefined") {
        // 如果浏览器支持localStorage
        // return localStorage.getItem("token");
        return randomString(10);

    } else {
        // 浏览器不支持localStorage
        console.log("浏览器不支持localStorage");
        return null;
    }
}

function connectWithToken() {
    var token = getTokenFromLocalStorage();
    // var socket;
    if (token) {
        // 使用token创建WebSocket连接
        socket = createSocket(token);
        // 其余的WebSocket连接代码
        // ...
    } else {
        console.log("没有找到token");
    }
    return socket;
}


// 获取所有按钮并添加点击事件监听器
let buttons = document.querySelectorAll('button');
buttons.forEach(button => {
    button.addEventListener('click', function () {
        // 发送选中的按钮ID到服务器
        if (socket && socket.readyState === WebSocket.OPEN) {
            // 发送选座消息
            socket.send(JSON.stringify({ action: 'selectSeat', seatId: this.id }));
        } else {
            console.log("WebSocket连接未建立或已关闭");
        }
    });
});

// 处理WebSocket消息
socket.onmessage = function(event) {
    // 处理服务器发送的消息
    var data = JSON.parse(event.data);
    if (data.action === 'seatSelected') {
        // 禁用被选中的按钮
        const button = document.getElementById(data.seatId);
        button.disabled = true;
        button.classList.add('selected');
    }
};



// 当页面加载完成后，调用connectWithToken函数
window.onload = function() {
    connectWithToken();
};

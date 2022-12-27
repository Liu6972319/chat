package com.east.chat.websocket.entity;

import cn.hutool.json.JSONObject;
import lombok.Data;

@Data
public class SocketEntity {
    String method;
    JSONObject data;
    public SocketEntity(String method, JSONObject data){
        this.method = method;
        this.data = data;
    }
}

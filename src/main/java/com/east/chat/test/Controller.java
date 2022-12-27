package com.east.chat.test;

import cn.hutool.db.nosql.redis.RedisDS;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@org.springframework.stereotype.Controller
public class Controller {

    @RequestMapping("text")
    @ResponseBody
    public String text (){
        RedisDS.create().getJedis().set("1","1");

        return RedisDS.create().getJedis().get("1");
    }
}

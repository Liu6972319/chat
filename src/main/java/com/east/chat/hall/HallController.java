package com.east.chat.hall;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.nosql.redis.RedisDS;
import cn.hutool.json.JSONUtil;
import com.east.chat.AjaxResult;
import com.east.chat.room.RoomEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("hall")
public class HallController {

    private final String roomKey = "rooms";

    @GetMapping("id")
    @ResponseBody
    public AjaxResult getId(){
        long l = IdUtil.getSnowflake().nextId();
        return AjaxResult.success(String.valueOf(l));
    }

    @GetMapping("getById")
    @ResponseBody
    public AjaxResult getId(String id){
        Set<String> list = RedisDS.create().getJedis().zrangeByScore(roomKey, id, id);
        for (String room : list) {
            return AjaxResult.success(room);
        }
        return AjaxResult.error();
    }

    /**
     * 列表
     * @param page
     * @return
     */
    @GetMapping("list")
    @ResponseBody
    public AjaxResult list(long page,long pageSize) {
        Long size = RedisDS.create().getJedis().zcard(roomKey);
        Set<String> rooms = RedisDS.create().getJedis().zrange(roomKey, (page - 1) * pageSize, page * pageSize);
        List<RoomEntity> roomEntities = new ArrayList<>();
        rooms.forEach(item -> {
            RoomEntity roomEntity = JSONUtil.toBean(item, RoomEntity.class);
            if (StrUtil.isBlankIfStr(roomEntity.getPasswd())) {
                roomEntity.setPasswd("false");
            }else{
                roomEntity.setPasswd("true");
            }
            roomEntities.add(roomEntity);
        });
        Map<String,Object> rMap = new HashMap<>();
        rMap.put("data",roomEntities);
        rMap.put("total",size);
        return AjaxResult.success(rMap);
    }

    /**
     * 添加
     * @param roomEntity
     * @return
     */
    @PostMapping("push")
    @ResponseBody
    public AjaxResult push(RoomEntity roomEntity) {
        // 判断 `我` 是否创建房间
        Set<String> rooms = RedisDS.create().getJedis().zrangeByScore(roomKey, roomEntity.getRoomId(), roomEntity.getRoomId());
        if (rooms.size() == 0){
            roomEntity.setDate(new Date());
            RedisDS.create().getJedis().zadd(roomKey, Long.parseLong(roomEntity.getRoomId()),JSONUtil.toJsonStr(roomEntity));
        }else{
            for (String room : rooms) {
                return AjaxResult.success(room);
            }
        }
        return AjaxResult.success(roomEntity);
    }

    /**
     * 验证密码
     * @param roomId
     * @param passwd
     * @return
     */
    @PostMapping("checkPasswd")
    @ResponseBody
    public AjaxResult checkPasswd(String roomId,String passwd){
        Set<String> list = RedisDS.create().getJedis().zrangeByScore(roomKey, roomId, roomId);
        if (list.size() > 0){
            for (String room : list) {
                RoomEntity roomEntity = JSONUtil.toBean(room, RoomEntity.class);
                if (roomEntity.getPasswd().equals(passwd)){
                    return AjaxResult.success("密码正确");
                }else{
                    return AjaxResult.error("密码错误");
                }
            }
        }
        return AjaxResult.error("未查到该房间信息");
    }


    /**
     * 删除 只有数据完全对应才执行删除
     * @return
     */
    @DeleteMapping("delete")
    @ResponseBody
    public AjaxResult delete(@RequestBody String roomId) {
        Set<String> list = RedisDS.create().getJedis().zrangeByScore(roomKey, roomId, roomId);
        for (String string : list) {
            RedisDS.create().getJedis().zrem(roomKey, string);
        }
        return AjaxResult.success();
    }
}

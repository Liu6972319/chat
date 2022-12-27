package com.east.chat.room;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class RoomEntity implements Serializable {
    String roomId;
    String name;
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd hh:mm:ss")
    Date date;
    String detail;
    String passwd;
    int hour;
}

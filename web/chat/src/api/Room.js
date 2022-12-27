// 引入公共js文件
import request from "../axios/Request";

export const createRoom = (data) => {
    return request({
        url: '/room/push',
        method: 'POST',
        params: data
    })
}

export const join = (data) => {
    return request({
        url: '/room/join',
        method: 'POST',
        data: data
    })
}
export const joinSuccess = (data) => {
    return request({
        url: '/room/joinSuccess',
        method: 'POST',
        data: data
    })
}
export const exit = (data) => {
    return request({
        url: '/room/exit',
        method: 'POST',
        data: data
    })
}
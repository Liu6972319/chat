// 引入公共js文件
import request from "../axios/Request";

/**
 * @name:根据分类获取文章列表
 * @author: camellia
 * @email: guanchao_gc@qq.com
 * @date: 2021-03-01
 */
export const getList = (data) => {
    return request({
        url: '/hall/list',
        method: 'GET',
        params: data
    })
}
/**
 * 获取id
 * @returns {*}
 */
export const getId = () => {
    return request({
        url: '/hall/id',
        method: 'GET'
    })
}
/**
 * 添加数据
 * @param data
 * @returns {*}
 */
export const pushRoom = (data) => {
    return request({
        url: '/hall/push',
        method: 'POST',
        data: data
    })
}

/**
 * 删除房间
 * @param data
 * @returns {*}
 */
export const deleteRoom = (data) => {
    return request({
        url: '/hall/delete',
        method: 'DELETE',
        data: data
    })
}

export const checkPasswd = (data) => {
    return request({
        url: '/hall/checkPasswd',
        method: 'POST',
        data: data
    })
}
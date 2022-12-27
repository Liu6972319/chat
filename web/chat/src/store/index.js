import { createStore } from 'vuex'
import createPersistedstate from 'vuex-persistedstate'
import global from './module/global'
import user from './module/user'
import socket from './module/socket'

// 测试环境不添加
let store;
if (import.meta.env.MODE === "development") {
    store = createStore({
        ...global,
        modules: {
            user,
            socket
        }
    });
} else if (import.meta.env.MODE === "production") {
    store = createStore({
        ...global,
        modules: {
            user,
            socket
        },
        plugins: [
            // 持久化 插件
            createPersistedstate({
                key: 'userInfo',
                paths: ['user']
            })
        ]
    });
}

export default store


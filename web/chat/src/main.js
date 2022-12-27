import {createApp} from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import VueNativeSock from "vue-native-websocket-vue3";

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

const app = createApp(App)

app.use(router)

app.use(store)

// 使用VueNativeSock插件，并进行相关配置
app.use(VueNativeSock,import.meta.env.VITE_APP_BASE_SOCKET,{
    // 绑定 store
    store: store,
    format: "json",
    // 手动管理连接
    connectManually: true,
    // 自动重联
    reconnection: true,
    reconnectionAttempts: 5,
    reconnectionDelay: 3000
});

app.use(ElementPlus)

app.mount('#app')

export default app;

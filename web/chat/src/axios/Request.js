import axios from 'axios'
import {ElMessageBox} from 'element-plus'

axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';
const service = axios.create({
    // baseURL: import.meta.env.VITE_APP_BASE_URL + import.meta.env.VITE_APP_BASE_API,
    baseURL: import.meta.env.VITE_APP_BASE_API,
    timeout: 5000
})

// // 请求拦截器
service.interceptors.request.use(
    config => {
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        const {code, msg, data} = response.data
        //   要根据success的成功与否决定下面的操作
        if (code === 200) {
            return data
        } else if (code === 500) {
            // 业务错误
            ElMessageBox.alert(msg)
        }else{
            return Promise.reject(new Error(msg))
        }
    },
    error => {
        // 处理 token 超时问题
        // if (
        //   error.response &&
        //   error.response.data &&
        //   error.response.data.code === 401
        // ) {
        //   // token超时
        //   store.dispatch('user/logout')
        // }
        // ElMessageBox.alert(error)
        // console.error(error.msg)
        // ElMessage.error(error.message) // 提示错误信息
        return Promise.reject(error)
    }
)

export default service

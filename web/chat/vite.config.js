import {fileURLToPath, URL} from 'node:url'

import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'

import fs from 'fs';

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [vue()],
    define: {
        'process.env': {}
    },
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    },
    server: {
        host: '0.0.0.0',
        port: 3000,
        // https: {
        //     key: fs.readFileSync('./ssl/key.pem'),
        //     cert: fs.readFileSync('./ssl/cert.pem')
        // },
        proxy: {
            '/webrtc': {
                target: 'http://127.0.0.1:8080',	//实际请求地址
                changeOrigin: true,
                // ws:true,
                // rewrite: (path) => path.replace(/^\/api/, '')
            },
        }
    }
})

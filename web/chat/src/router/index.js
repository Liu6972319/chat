import { createRouter, createWebHistory } from 'vue-router'
import store from '../store'

import Hall from '../views/Hall.vue'
import Room from '../views/Room.vue'
import ManyRoom from '../views/ManyRoom.vue'

import {exit} from "@/api/Room";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'hall',
      component: Hall
    },
    {
      path: '/room',
      name: 'room',
      component: ManyRoom
    }
  ]
})

// 判断是否需要登录权限 以及是否登录
router.beforeEach((to, from, next) => {
  if (from.name === 'room'){
    exit({
      roomId: from.query.roomId,
      userId: store.getters['user/getId'],
    }).then()
  }
  next()

  // if (to.matched.some(res => res.meta.requireAuth)) {// 判断是否需要登录权限
  //   if (localStorage.getItem('username')) {// 判断是否登录
  //     next()
  //   } else {
  //     next({
  //       path: '/login',
  //       query: {redirect: to.fullPath}
  //     })
  //   }
  // } else {
  //   next()
  // }
})

export default router

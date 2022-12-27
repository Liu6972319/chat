<script>
import { RouterView } from 'vue-router'
import {getId} from "@/api/Hall";
import centerTable from "@/components/hall/CenterTable.vue";
export default {
  data(){
    return {
      routerView: false
    }
  },
  components: {
    RouterView,
  },
  async created() {
    // 创建临时身份ID
    if (!this.$store.getters['user/getId']) {
      let id = await getId();
      this.$store.commit('user/setId', id)
    }
    this.$connect(import.meta.env.VITE_APP_BASE_SOCKET + "?id=" + this.$store.getters['user/getId'])
    this.routerView = true
  }
}
</script>

<template>
  <RouterView v-if="routerView" />
</template>


<template>
  <el-container id="box" style="height: 100%">
    <el-header id="header">
      <div>Chat</div>
    </el-header>
    <el-container>
      <center-table :height="tableHeight"/>
    </el-container>
  </el-container>
</template>

<script>
import {getId} from "@/api/Hall";

import centerTable from '@/components/hall/CenterTable.vue'
export default {
  name: "Hall",
  components: {
    "center-table": centerTable,
  },
  data() {
    return {
      tableHeight: 0,
    }
  },
  methods: {
    handleCurrentChange(number) {
    },
    /**
     * 动态 table
     */
    getAutoHeight() {
      let box = document.querySelector("#box").clientHeight;
      let header = document.querySelector("#header").clientHeight;

      // // 一定要使用 nextTick 来改变height 不然不会起作用
      this.$nextTick(() => {
        this.tableHeight = box - (header);
      });
    },

  },
  mounted() {
    //启动后设置table高度
    this.getAutoHeight();
    const self = this;
    window.onresize = function () {
      self.getAutoHeight();
    };
  },
  //销毁 window.onresize
  destroyed() {
    window.onresize = null;
  }

}
</script>

<style>
html {
  height: 100%;
}

body {
  height: 100%;
  margin: 0;
}

#app {
  height: 100%;
}

.el-container {
  height: 100%;
}

.el-header {
  text-align: center;
  font-size: 50px;

}

</style>
<template>
  <el-container>
    <el-main id="main">
      <el-row>
        <el-col :span="16" :offset="4">
          <div id="operate" class="operate">
            <el-button type="primary" @click="create()">新建</el-button>
<!--            <el-button type="success" @click="join()">加入</el-button>-->
          </div>

          <!-- 列表數據 -->
          <el-table :data="tableData" :height="tableHeight" @row-click="rowClick">

            <el-table-column prop="roomId" label="ID" show-overflow-tooltip/>
            <el-table-column prop="name" label="名称"/>
            <el-table-column prop="date" label="时间"/>
            <el-table-column prop="detail" label="详情" show-overflow-tooltip/>
            <el-table-column label="操作">
              <template #default="scope">
                <el-icon v-if="scope.row.passwd === 'true'" color="red"><Lock /></el-icon>
                <el-icon v-if="scope.row.passwd === 'false'" color="green"><Unlock /></el-icon>
<!--                <a v-if="scope.row.passwd === 'true'" style="color:red">*</a>-->
                <el-button
                    v-if="scope.row.roomId === this.userId"
                    size="small"
                    type="danger"
                    @click.stop="handleDelete(scope.$index, scope.row)"
                >Delete
                </el-button>

              </template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>
    </el-main>
    <el-footer id="footer">
      <el-pagination
          :total="total"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :hide-on-single-page="false"
          :small="false"
          :disabled="false"
          :background="true"
          layout="prev, pager, next, jumper"
          @current-change="handleCurrentChange"
      />
    </el-footer>

    <el-dialog v-model="createDialog" title="创建房间">
      <el-form :model="createForm" :label-width="80">
        <el-form-item label="房间名称">
          <el-input v-model="createForm.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="密码" :label-width="80">
          <el-input v-model="createForm.passwd" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="存活时长">
          <el-radio-group v-model="createForm.hour">
            <el-radio label="1" default >1小时</el-radio>
            <el-radio label="6" >6小时</el-radio>
            <el-radio label="12" >12小时</el-radio>
            <el-radio label="24" >24小时</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="详情" :label-width="80">
          <el-input v-model="createForm.detail" type="textarea" rows="6" autocomplete="off"/>
        </el-form-item>

      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="createDialog = false">取消</el-button>
        <el-button type="primary" @click="createdDialogSubmit">创建</el-button>
      </span>
      </template>
    </el-dialog>

<!--    <el-dialog v-model="joinDialog" title="加入房间">-->
<!--      <el-form :model="roomForm" :label-width="80">-->
<!--        <el-form-item label="房间ID">-->
<!--          <el-input v-model="roomForm.roomId" autocomplete="off"/>-->
<!--        </el-form-item>-->
<!--      </el-form>-->
<!--      <template #footer>-->
<!--      <span class="dialog-footer">-->
<!--        <el-button @click="joinDialog = false">取消</el-button>-->
<!--        <el-button type="primary" @click="joinDialogSubmit">加入</el-button>-->
<!--      </span>-->
<!--      </template>-->
<!--    </el-dialog>-->

    <el-dialog v-model="passwdDialog" title="加入房间">
      <el-form :label-width="80">
        <el-form-item label="房间ID">
          <el-input v-model="verifyPasswd" autocomplete="off"/>
        </el-form-item>
      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="passwdDialog = false">取消</el-button>
        <el-button type="primary" @click="joinDialogSubmit">加入</el-button>
      </span>
      </template>
    </el-dialog>
  </el-container>
</template>

<script>
import md5 from 'js-md5'

import {checkPasswd, deleteRoom, getList, pushRoom} from "@/api/Hall";
import {createRoom} from "@/api/Room";

export default {
  name: "CenterTable",
  props: {
    height: Number
  },
  computed: {
    tableHeight() {
      if (this.height) {
        let footer = document.querySelector("#footer").clientHeight;
        let operate = document.querySelector("#operate").clientHeight;
        let main = document.querySelector("#main");
        let pt = this.getStyle(main, "paddingTop");
        let pb = this.getStyle(main, "paddingBottom");
        return this.height - (footer + operate + pt + pb) + "px"
      }
    }
  },
  data() {
    return {
      userId: this.$store.getters['user/getId'],
      pageSize: 10,
      tableData: null,
      // tableData: Array(100).fill(item),
      total: 0,
      currentPage: 1,
      createDialog: false,
      createForm: {
        name: "",
        detail: "",
        passwd: "",
        hour: "1"
      },
      passwdDialog: false,
      verifyRoomId: "",
      verifyPasswd: "",
      joinDialog: false,
      roomForm: {
        roomId: null,
        userId: this.$store.getters['user/getId']
      },
      // TODO: 表单验证
      createFormRules: {},
      roomFormRules: {}
    }
  },
  methods: {
    rowClick(row, column, event) {
      // TODO: 判断用户是否能加入
      if (row.passwd === 'true'){
        // 弹出密码认证页面
        this.passwdDialog = true
        this.verifyRoomId = row.roomId
      }else{
        // 成功后进入 room 页面
        this.$router.push({
          path: "room",
          query: {
            roomId: row.roomId
          }
        })
      }
    },
    create() {
      this.createDialog = true
    },
    handleDelete(index, row) {
      deleteRoom(row.roomId).then(res=>{
        this.getList();
      })
    },
    createdDialogSubmit() {
      this.createDialog = false
      this.createForm.roomId = this.userId
      if (this.createForm.passwd){
        this.createForm.passwd = md5(this.createForm.passwd)
      }
      // 添加房间
      pushRoom(this.createForm).then(res => {
        if (res) {
          // TODO： 进入房间
          // createRoom({roomId: res.roomId, userId: this.userId});
          // 刷新页面
          this.getList();
        }
        console.log(res);
      })
      const copy = {
        roomId: 0,
        name: "",
        detail: "",
        passwd: "",
        hour: "1"
      };
      this.createForm = Object.assign(this.createForm, copy)

    },
    join() {
      this.joinDialog = true
    },
    joinDialogSubmit() {
      // 访问服务器 校验密码
      let param = {
        roomId:this.verifyRoomId,
        passwd: md5(this.verifyPasswd)
      }
      checkPasswd(param).then(res=>{
        // 进入房间
        this.$router.push({
          path: "room",
          query: {
            roomId: this.verifyRoomId
          }
        })
      })
      this.passwdDialog = false
    },
    getList(changePage) {
      this.currentPage = this.currentPage ? this.currentPage : 1;
      let param = {
        page: changePage ? changePage : this.currentPage,
        pageSize: this.pageSize
      }
      let that = this;
      getList(param).then(res => {
        that.tableData = res.data
        that.total = res.total
      })
    },
    handleCurrentChange(number) {
      // 访问数据库填充数据
      this.getList(number);
    },
    // 获取样式 我们需要减掉 padding-top， padding-bottom的值
    getStyle(obj, attr) {
      // 兼容IE浏览器
      let result = obj.currentStyle
          ? obj.currentStyle[attr].replace("px", "")
          : document.defaultView.getComputedStyle(obj, null)[attr].replace("px", "");
      return Number(result);
    },
  },
  mounted() {
    this.getList();
  },
}
</script>

<style scoped>
.operate {
  text-align: right;
}

.el-main {
  overflow: hidden !important;
}

.el-pagination {
  justify-content: center
}
</style>
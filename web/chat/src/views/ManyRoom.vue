<template>
  <view style="display: flex;flex-direction: row;flex-wrap: wrap;align-items: flex-start;align-content: flex-start;">
    <video v-for="user in userList" :key="user" :ref="user" :controls="false" :width="videoWidth" playsinline></video>
  </view>
</template>

<script>
import SocketEntity from "@/commons/SocketEntity";
import {exit, join, joinSuccess} from "@/api/Room";

export default {
  name: "ManyRoom",
  data() {
    return {
      // lock: false,
      videoWidth: 480,
      roomId: this.$route.query.roomId,
      userId: this.$store.getters['user/getId'],
      streamList: {}, // stream列表
      peerList: {},// 用户peer列表
      userList: [
        this.$store.getters['user/getId']
      ], // 用户组
      offerOption: {
        offerToReceiveAudio: 1,
        offerToReceiveVideo: 1
      },
      iceServers: {
        iceServers: [
          {url: "stun:stun.l.google.com:19302"}, // 谷歌的公共服务
          {
            url: 'turn:numb.viagenie.ca',
            credential: 'muazkh',
            username: 'webrtc@live.com'
          },
        ],
        sdpSemantics: 'unified-plan'
      },
    }
  },
  created() {
    if (this.isMobile()) {
      this.videoWidth = window.outerWidth
    } else {
      this.videoWidth = 480
    }
    console.log("删除监听事件")
    this.$options.sockets.onmessage = undefined

    console.log("注册监听事件")
    // 注册监听事件
    this.onListener();
  },
  mounted() {
    // window.addEventListener('beforeunload', (e) => this.beforeunloadHandler(e));
    // window.addEventListener('unload', this.updateHandler);
    console.log("当前用户ID：" + this.$store.getters['user/getId'])
    // 初始化
    this.init()
  },
  methods: {
    isMobile() {
      let flag = navigator.userAgent.match(
          /(phone|pad|pod|iPhone|iPod|ios|iPad|Android|Mobile|BlackBerry|IEMobile|MQQBrowser|JUC|Fennec|wOSBrowser|BrowserNG|WebOS|Symbian|Windows Phone)/i
      );
      return flag;
    },
    async beforeunloadHandler(e) {
      e = window.event || e;
      e.returnValue = ("确定离开当前页面吗？")
      this.$router.push({
        path: "/"
      })
      return true;
    },
    updateHandler() {
      console.log("updateHandler")
    },
    async init() {
      console.log("保存本地流")
      // 保存本地流
      this.streamList[this.userId] = await this.createMedia()
      console.log("打开摄像头")
      // 打开摄像头
      this.nativeMedia();

      console.log("进入房间")
      // 告知后台有用户 进入房间
      let res = await join({roomId: this.roomId, userId: this.userId})
      if (res !== null && res.length > 0) {
        // 用户列表更新
        await this.onJoin({users: res});
        // 创建 offer
        await this.onCreateOffer()
      }
    },
    // 监听服务器信息
    onListener() {
      let that = this;
      this.$options.sockets.onmessage = (res) => {
        let data = JSON.parse(res.data);
        switch (data.method) {
          case 'offer':
            that.onOffer(data.data);
            break;
          case 'answer':
            that.onAnswer(data.data);
            break;
          case 'candidate':
            that.onIceCandidate(data.data);
            break;
          case 'createOffer':
            that.onCreateOffer();
            break;
          case 'join':
            this.onJoin(data.data);
            break;
          case 'exit':
            this.onExit(data.data);
            break;
        }
      }
    },
    // 创建媒体流
    async createMedia() {
      if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        this.$message.error("设备不支持")
        console.log('getUserMedia is not support!')
      }
      return await navigator.mediaDevices.getUserMedia({audio: true, video: true})
    },
    // 本地摄像头打开
    nativeMedia() {
      let video = this.$refs[this.userList[0]][0];
      // 旧的浏览器可能没有srcObject
      video.srcObject = this.streamList[this.userId]
      video.onloadedmetadata = function (e) {
        video.play();
      };
    },
    //监听远端offer
    async onOffer(data) {
      let peerName = this.userId + "-" + data.data.userId;
      await this.peerList[peerName].setRemoteDescription(data.data.offer)
      // 接收端创建 answer
      let answer = await this.peerList[peerName].createAnswer();
      // 接收端设置本地 answer 描述
      await this.peerList[peerName].setLocalDescription(answer);
      //发送到呼叫端 answer
      let params = {
        userId: this.userId,
        targetId: data.data.userId,
        data: {
          userId: this.userId,
          answer: answer
        }
      }
      this.$socket.sendObj(SocketEntity("answer", params))
    },
    //监听远程响应
    async onAnswer(data) {
      let peerName = this.userId + "-" + data.userId;
      // 发送端 设置远程 answer 描述
      // TODO: 同时加入房间 设置远程说明将会报错
      try {
        await this.peerList[peerName].setRemoteDescription(data.data.answer);
      } catch (e) {
        console.log(peerName, ":设置远程连接失败")
      }
    },
    //监听 Ice 候选
    async onIceCandidate(data) {
      let peerName = this.userId + "-" + data.data.userId;
      await this.peerList[peerName].addIceCandidate(data.data.candidate)
    },
    // 创建 createOffer
    async onCreateOffer() {
      console.log("onCreateOffer-------->")
      for (const ele of this.userList) {
        let peerName = this.userId + "-" + ele;
        if (this.userId !== ele) {
          //创建offer
          console.log("开始创建offer", peerName)
          let offer = await this.peerList[peerName].createOffer(this.offerOption);
          //设置本地描述
          await this.peerList[peerName].setLocalDescription(offer)
          //远程发送到服务器 并转发到其他的客户端
          let params = {
            userId: this.userId,
            targetId: ele,
            data: {
              offer: offer,
              userId: this.userId,
              info: "发送offer"
            }
          }
          this.$socket.sendObj(SocketEntity("offer", params))
          // 设置锁状态
          // this.lock = true;
        }
      }
      // 如果没有锁定，通知后台解锁
      // if(!this.lock){
      //   // joinSuccess({userId: this.userId, roomId: this.roomId}).then(res=>{})
      // }
    },
    // 监听用户加入
    async onJoin(data) {
      console.log("用户加入-->", data.users)
      let update = false;
      data.users.some(item => {
        if (!this.userList.includes(item)) {
          this.userList.push(item)
          update = true;
        }
      })
      if (update) {
        await this.$forceUpdate()
        // 初始化 peer
        await this.initPeerList();
      }

    },
    // 监听用户退出
    onExit(data) {
      let userId = data.userId;
      console.log("移出用户： ", userId)
      if (this.userList.indexOf(userId) > -1) {
        this.userList.splice(this.userList.indexOf(userId), 1)
      }
      // this.userList = this.userList.filter((value, index, arr) => {
      //   return value !== userId;
      // });
      delete this.peerList[this.userId + "-" + userId]
      delete this.streamList[userId]
    },
    // 重置 peer 连接
    initPeerList() {
      this.peerList = {};
      this.userList.forEach(ele => {
        if (this.userId !== ele) {
          let peerName = this.userId + "-" + ele;
          this.initPeer(peerName, ele);
        }
      });
    },
    //初始化 PeerConnection
    initPeer(peerName, ele) {
      if (this.peerList[peerName] == null) {
        let peer = new RTCPeerConnection(this.iceServers);
        peer.addStream(this.streamList[this.userId]);
        // 保存 peer
        this.peerList[peerName] = peer;
        const that = this;
        // 监听 ice
        peer.onicecandidate = function (event) {
          if (event.candidate) {
            let params = {
              userId: that.userId,
              targetId: ele,
              data: {
                userId: that.userId,
                candidate: event.candidate
              }
            }
            that.$socket.sendObj(SocketEntity("candidate", params))
          } else {
            console.log("ICE 收集完成")
            // if (that.lock){
            //   // joinSuccess({userId: that.userId, roomId: that.roomId}).then(res=>{
            //   //   that.lock = false;
            //   // })
            // }
          }
        };
        // 监听stream
        peer.onaddstream = async (event) => {
          console.log("当前用户 ", this.userId)
          console.log("监听到视频加入 加入用户 ", ele)
          that.createEleVideo(event.stream, ele)
          // await that.createEleVideo(event.stream, ele)
        };
      }
    },
    //追加视频
    createEleVideo(stream, id) {
      // 存储 stream
      this.streamList[id] = stream;
      setTimeout(() => {
        // 找到 dom
        let video = this.$refs[id][0];
        //设置视频流
        video.srcObject = stream
        console.log(video.srcObject)
        //播放视频
        video.onloadedmetadata = function (e) {
          video.play();
        };
        console.log("添加视频成功")
        // 添加成功后 告知
      }, 1000)

    },

    // 退出房间
    // async exitPage(){
    //   let param ={
    //     roomId: this.roomId,
    //     userId: this.userId
    //   }
    //   await exit(param)
    //   console.log("退出用户：" + this.userId)
    // }
  },
  watch: {},
  unmounted() {
    console.log("销毁子组件")
    // console.log("删除监听事件")
    // this.$options.sockets.onmessage = undefined
  },
  destroyed() {
    // console.log("删除监听事件")
    // this.$options.sockets.onmessage = undefined
  }
}
</script>

<style scoped>

</style>
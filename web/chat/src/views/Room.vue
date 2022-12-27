<template>
  <video ref="video" playsinline autoplay muted></video>

  <video ref="remoteVideo" playsinline autoplay muted></video>
</template>

<script>
import SocketEntity from "@/commons/SocketEntity";

export default {
  name: "Room",
  data() {
    return {
      pc: null,
      localStream: null,
    }
  },
  methods: {
    createPeerConnection(){
      console.log('创建连接');
      this.pc = new RTCPeerConnection();
      // 设置监听候选
      this.pc.onicecandidate = e => {
        console.log("收到 candidate")
        let message;
        if (e.candidate) {
          message = {
            type: 'candidate',
            candidate: e.candidate.candidate,
            sdpMid: e.candidate.sdpMid,
            sdpMLineIndex: e.candidate.sdpMLineIndex
          }
        }
        // socket 发送
        this.$socket.sendObj(SocketEntity("candidate",message))
      };
      // 将本机流 加入到 RTC
      this.localStream.getTracks().forEach(track => {
        this.pc.addTrack(track, this.localStream)
      });
      console.log("设置远程视频流")
      let remoteVideo = this.$refs.remoteVideo;
      this.pc.ontrack = function (e){
        // 设置远程 视频流
        remoteVideo.srcObject = e.streams[0];
      }
    },
    async handleOffer(offer) {
      console.log("接收到offer")
      // if (this.pc) {
      //   console.error('现有的对等连接');
      //   return;
      // }
      console.log("创建offer")
      this.createPeerConnection()
      console.log("设置远程说明")
      await this.pc.setRemoteDescription(offer.offer);
      console.log("创建应答")
      const answer = await this.pc.createAnswer();
      console.log("发送应答")
      this.$socket.sendObj(SocketEntity('answer',{
        answer:{
          sdp: answer.sdp,
          type: 'answer'
        }
      }))
      console.log("设置本地说明 answer")
      this.pc.setLocalDescription(answer).then()
    },
    async handleCandidate(candidate) {
      if (!this.pc) {
        console.error('no peerconnection');
        return;
      }
      if (!candidate.candidate) {
        console.log("设置候选 null")
        await this.pc.addIceCandidate(null);
      } else {
        console.log("设置候选 not null")
        await this.pc.addIceCandidate(candidate);
      }
    },
    async handleAnswer(data) {
      console.log("处理应答")
      if (!this.pc) {
        console.error('no peerconnection');
        return;
      }
      console.log("设置远程说明 answer")
      await this.pc.setRemoteDescription(data.answer);
    }
  },
  async mounted() {
    let id = this.$route.query.id;

    // 实例化 video
    let userMedia = await navigator.mediaDevices.getUserMedia({
      audio: true,
      video: {
        width: 300,
        height: 150,
        facingMode: "user"
      }
    });
    this.localStream = userMedia
    this.$refs.video.srcObject = userMedia
    // 初始化本地peer
    this.createPeerConnection();

    console.log("创建 offer")
    let offer = await this.pc.createOffer();
    console.log("发送offer")
    // socket 告知其他人`我`加入了
    // 传递信令
    this.$socket.sendObj(SocketEntity("offer",{
      id: id,
      offer:{
        sdp: offer.sdp,
        type: "offer"
      }
    }))
    console.log("设置本地说明")
    await this.pc.setLocalDescription(offer);

    // 监听 socket message
    this.$socket.onmessage = (res) => {
      let data = JSON.parse(res.data);
      // handleCandidate
      switch (data.method){
        case 'offer': this.handleOffer(data.data); break;
        case 'candidate': this.handleCandidate(data.data); break;
        case 'answer': this.handleAnswer(data.data)
      }
    }
  },
}
</script>

<style scoped>

</style>
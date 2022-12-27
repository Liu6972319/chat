export default {
  namespaced: true,
  state: {
    //属性
    id: ""
  },
  getters: {
    getId: state => state.id
  },
  mutations: {
    //set方法
    setId(state,val){
      state.id = val
    }
  },
  actions:{
    asyncUpdate(store, val) {
      store.commit('setId', val)
    }
  }
}


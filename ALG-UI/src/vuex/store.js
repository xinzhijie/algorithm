import Vue from 'vue'
import Vuex from 'vuex'
import account from './modules/account'
import common from './modules/common'

Vue.use(Vuex)

export default new Vuex.Store({
  modules: {
    account,
    common
  }
})

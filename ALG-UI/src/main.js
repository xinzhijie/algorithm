import Vue from 'vue'
import App from './App'
import router from './router'
import ElementUI from 'element-ui'
import store from './vuex/store'
import 'element-ui/lib/theme-default/index.css'
import 'font-awesome/css/font-awesome.min.css'
import Mock from './mock'
import VueResource from 'vue-resource'
import VueDND from 'awe-dnd'
import {VTable, VPagination} from 'easy-table-rev'
import 'easy-table-rev/libs/themes-base/index.css'


Vue.component(VTable.name, VTable)
Vue.component(VPagination.name, VPagination)
Vue.use(VueDND)
Vue.use(VueResource)

Vue.config.productionTip = false
Mock.bootstrap()
Vue.use(ElementUI)
Vue.config.productionTip = false

router.beforeEach((to, from, next) => {
  if (to.path === '/login') {
    sessionStorage.removeItem('user')
  }
  let user = JSON.parse(sessionStorage.getItem('user'))
  if (!user && to.path !== '/login') {
    next({ path: '/login' })
  } else {
    next()
  }
})

/* eslint-disable no-new */
new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')

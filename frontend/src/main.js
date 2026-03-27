import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import router from './router'
import App from './App.vue'
import { menuPermissionDirective, menuOperateDirective, permissionDirective } from '@/utils/permission'

const app = createApp(App)
const pinia = createPinia()

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册权限指令
app.directive('menu-permission', menuPermissionDirective)
app.directive('menu-operate', menuOperateDirective)
app.directive('permission', permissionDirective)

app.use(pinia)
app.use(router)
app.use(ElementPlus)

app.mount('#app')

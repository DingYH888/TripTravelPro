import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import App from './App.vue'
import router from './router'
import { useAdminStore } from './stores/admin'
import 'element-plus/dist/index.css'
import './styles/app.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
useAdminStore(pinia).restore()
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')

<template>
  <div class="tn-auth-page">
    <div class="tn-auth-bar">
      <RouterLink to="/" class="tn-logo">
        <img src="/frontdesk/images/logo.png" alt="去哪旅游网" />
      </RouterLink>
      <RouterLink to="/">
        <span class="glyphicon glyphicon-home"></span>
        首页
      </RouterLink>
    </div>

    <div class="tn-auth-card">
      <h2>新用户注册</h2>
      <p class="sub">注册后需通过邮箱激活账号</p>

      <p v-if="error" class="tn-alert error">{{ error }}</p>
      <p v-if="success" class="tn-alert ok">{{ success }}</p>

      <form @submit.prevent="submit">
        <div class="tn-field">
          <label for="reg-username">用户名</label>
          <input id="reg-username" v-model.trim="form.username" type="text" placeholder="请输入账号" />
        </div>
        <div class="tn-field">
          <label for="reg-password">密码</label>
          <input id="reg-password" v-model="form.password" type="password" placeholder="请输入密码" />
        </div>
        <div class="tn-field">
          <label for="reg-email">Email</label>
          <input id="reg-email" v-model.trim="form.email" type="email" placeholder="请输入邮箱，用于接收激活邮件" />
        </div>
        <div class="tn-field">
          <label for="reg-phone">手机号</label>
          <input id="reg-phone" v-model.trim="form.phoneNum" type="text" placeholder="请输入手机号" />
        </div>
        <div class="tn-field-row">
          <div class="tn-field">
            <label>性别</label>
            <select v-model="form.sex">
              <option value="男">男</option>
              <option value="女">女</option>
            </select>
          </div>
          <div class="tn-field">
            <label for="reg-birthday">出生日期</label>
            <input id="reg-birthday" v-model="form.birthday" type="date" />
          </div>
        </div>
        <div class="tn-field">
          <label for="reg-captcha">验证码</label>
          <div class="tn-captcha">
            <input id="reg-captcha" v-model.trim="form.checkCode" type="text" placeholder="请输入验证码" />
            <img v-if="captchaImage" :src="captchaImage" alt="验证码" title="点击刷新" @click="loadCaptcha" />
            <button v-else type="button" class="tn-btn gray" @click="loadCaptcha">获取验证码</button>
          </div>
        </div>

        <button class="tn-btn tn-auth-submit" type="submit" :disabled="submitting">
          {{ submitting ? '提交中...' : '注册' }}
        </button>
      </form>

      <div class="tn-auth-foot">
        <span>已有账号？</span>
        <RouterLink to="/login">立即登录</RouterLink>
      </div>
    </div>

    <SiteFooter />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import SiteFooter from '../components/SiteFooter.vue'
import { getCaptcha, memberRegister } from '../api/auth'
import type { RegisterPayload } from '../api/types'

const router = useRouter()
const form = reactive<RegisterPayload>({
  username: '',
  password: '',
  email: '',
  phoneNum: '',
  sex: '男',
  birthday: '',
  checkCodeKey: '',
  checkCode: ''
})
const captchaImage = ref('')
const submitting = ref(false)
const error = ref('')
const success = ref('')

onMounted(loadCaptcha)

async function loadCaptcha() {
  try {
    const data = await getCaptcha()
    form.checkCodeKey = data.key
    captchaImage.value = data.image
    form.checkCode = ''
  } catch (e) {
    error.value = e instanceof Error ? e.message : '验证码加载失败'
  }
}

async function submit() {
  error.value = ''
  success.value = ''
  if (!form.username || !form.password || !form.email || !form.phoneNum) {
    error.value = '请填写完整的注册信息'
    return
  }
  if (!form.checkCode) {
    error.value = '请输入验证码'
    return
  }
  submitting.value = true
  try {
    await memberRegister(form)
    success.value = '注册成功，请前往邮箱点击激活链接后再登录'
    setTimeout(() => router.push('/login'), 1800)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '注册失败'
    loadCaptcha()
  } finally {
    submitting.value = false
  }
}
</script>

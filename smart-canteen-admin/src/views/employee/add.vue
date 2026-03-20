<script lang="ts" setup>
import { reactive, ref } from 'vue'
import { addEmployeeAPI } from '@/api/employee'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const formLabelWidth = '60px'
const form = reactive({
  id: 0,
  name: '',
  account: '',
  password: '',
  phone: '',
  age: null as number | null,
  gender: 0,
  pic: '',
})

const genders = [
  { value: 1, label: '男' },
  { value: 2, label: '女' },
  { value: 0, label: '未知' },
]

const inputRef1 = ref<HTMLInputElement | null>(null)
const addRef = ref()

const checkAge = (rule: any, value: number | null, callback: (error?: Error) => void) => {
  if (value === null || value === undefined) {
    callback(new Error('请输入年龄'))
    return
  }
  if (value < 3) {
    callback(new Error('年龄不能小于3岁'))
    return
  }
  if (value > 99) {
    callback(new Error('年龄不能大于99岁'))
    return
  }
  callback()
}

const rules = {
  name: [
    { required: true, trigger: 'blur', message: '不能为空' },
    { min: 2, message: '姓名长度不能少于2个字符', trigger: 'blur' },
    { max: 20, message: '姓名长度不能超过20个字符', trigger: 'blur' },
  ],
  account: [
    { required: true, trigger: 'blur', message: '不能为空' },
    { pattern: /^[a-zA-Z0-9]{1,10}$/, message: '用户名必须是1-10位字母或数字', trigger: 'blur' },
  ],
  password: [
    { required: true, trigger: 'blur', message: '不能为空' },
    { pattern: /^\S{6,15}$/, message: '密码必须是6-15位非空字符', trigger: 'blur' },
  ],
  phone: [
    { required: true, trigger: 'blur', message: '不能为空' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  age: [
    { required: true, trigger: 'change', message: '不能为空' },
    { validator: checkAge, trigger: 'change' },
  ],
  gender: [{ required: true, trigger: 'change', message: '不能为空' }],
}

const router = useRouter()

const chooseImg = () => {
  if (inputRef1.value) {
    inputRef1.value.click()
  }
}

const onFileChange1 = (e: Event) => {
  const target = e.target as HTMLInputElement
  const files = target.files
  if (files && files.length > 0) {
    const fr = new FileReader()
    fr.readAsDataURL(files[0])
    fr.onload = () => {
      form.pic = fr.result as string
    }
  }
}

const submit = async () => {
  try {
    const valid = await addRef.value.validate()
    if (!valid) {
      return false
    }
    const res = await addEmployeeAPI({
      ...form,
      age: form.age === null ? null : Number(form.age),
      gender: Number(form.gender ?? 0),
    })
    if (res.data.code !== 0) {
      return false
    }
    ElMessage({
      message: '新增员工成功',
      type: 'success',
    })
    router.push({ path: '/employee' })
  } catch (error) {
    console.error('执行过程中失败', error)
  }
}

const cancel = () => {
  router.push({ path: '/employee' })
}
</script>

<template>
  <h1>添加员工页</h1>
  <el-card>
    <el-form :model="form" :rules="rules" ref="addRef">
      <el-form-item label="姓名" :label-width="formLabelWidth" prop="name">
        <el-input v-model="form.name" autocomplete="off" />
      </el-form-item>
      <el-form-item label="账号" :label-width="formLabelWidth" prop="account">
        <el-input v-model="form.account" autocomplete="off" />
      </el-form-item>
      <el-form-item label="密码" :label-width="formLabelWidth" prop="password">
        <el-input v-model="form.password" autocomplete="off" />
      </el-form-item>
      <el-form-item label="电话" :label-width="formLabelWidth" prop="phone">
        <el-input v-model="form.phone" autocomplete="off" />
      </el-form-item>
      <el-form-item label="年龄" :label-width="formLabelWidth" prop="age">
        <el-input-number v-model="form.age" :min="3" :max="99" :step="1" controls-position="right" />
      </el-form-item>
      <el-form-item label="性别" :label-width="formLabelWidth" prop="gender">
        <el-radio-group v-model="form.gender">
          <el-radio :label="1">男</el-radio>
          <el-radio :label="2">女</el-radio>
          <el-radio :label="0">未知</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="头像" :label-width="formLabelWidth" prop="pic">
        <img class="the_img" v-if="!form.pic" src="/src/assets/image/user_default.png" alt="" />
        <img class="the_img" v-else :src="form.pic" alt="" />
        <input type="file" accept="image/*" style="display: none" ref="inputRef1" @change="onFileChange1" />
        <el-button type="primary" @click="chooseImg">
          <el-icon style="font-size: 15px; margin-right: 10px;">
            <Plus />
          </el-icon>
          选择图片
        </el-button>
      </el-form-item>
    </el-form>
    <el-form-item>
      <el-button class="submit_btn" type="success" @click="submit">添加</el-button>
      <el-button class="cancel_btn" type="info" plain @click="cancel">取消</el-button>
    </el-form-item>
  </el-card>
</template>

<style lang="less" scoped>
h1 {
  font-size: 20px;
  text-align: center;
  margin: 20px;
}

.el-form {
  margin-top: 30px;
  width: 500px;
  margin: 0 auto;
}

img {
  width: 50px;
  height: 50px;
  margin-right: 20px;
}

.submit_btn {
  width: 100px;
  height: 40px;
  margin: 30px 0 0 400px;
}

.cancel_btn {
  width: 100px;
  height: 40px;
  margin: 30px 0 0 200px;
}
</style>

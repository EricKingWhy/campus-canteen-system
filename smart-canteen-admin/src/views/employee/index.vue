<script setup lang="ts">
import { reactive, ref } from 'vue'
import { getEmployeePageListAPI, updateEmployeeStatusAPI, deleteEmployeeAPI } from '@/api/employee'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

interface EmployeeItem {
  id: number | string
  name: string
  account?: string
  username?: string
  phone: string
  age: number | null
  gender: number | null
  pic: string
  photoPath?: string
  status: number
  updateTime: string
}

const employeeList = ref<EmployeeItem[]>([])
const baseURL = 'http://127.0.0.1:8081'
const pageData = reactive({
  name: '',
  page: 1,
  pageSize: 6,
  total: 0,
})

const genderText = (gender: number | string | null | undefined) => {
  const g = Number(gender)
  if (g === 1) return '男'
  if (g === 2) return '女'
  return '未知'
}

const normalizeRecord = (record: any): EmployeeItem => {
  const account = record.account || record.username || ''
  const photoPath = record.photoPath || record.pic || ''
  let gender = record.gender
  if ((gender === undefined || gender === null) && record.sex !== undefined) {
    gender = Number(record.sex)
  }
  return {
    ...record,
    account,
    pic: photoPath,
    gender: gender === undefined || gender === null ? 0 : Number(gender),
    age: record.age === undefined || record.age === null ? null : Number(record.age),
  }
}

const resolvePhoto = (photoPath?: string) => {
  if (!photoPath) return '/src/assets/image/user_default.png'
  if (photoPath.startsWith('data:image') || photoPath.startsWith('http')) return photoPath
  if (photoPath.startsWith('/static/')) return baseURL + photoPath
  return baseURL + '/static/upload/employee_photos/' + photoPath.replace(/^\/+/, '')
}

const init = async () => {
  const { data: res } = await getEmployeePageListAPI({
    page: pageData.page,
    pageSize: pageData.pageSize,
    name: pageData.name,
  })
  employeeList.value = (res.data.records || []).map(normalizeRecord)
  pageData.total = res.data.total
}

init()

const handleCurrentChange = (val: number) => {
  pageData.page = val
  init()
}

const handleSizeChange = (val: number) => {
  pageData.pageSize = val
  init()
}

const router = useRouter()
const updateBtn = (row: EmployeeItem) => {
  router.push({
    name: 'employee_update',
    query: { id: row.id },
  })
}

const changeBtn = async (row: EmployeeItem) => {
  const newStatus = row.status === 1 ? 0 : 1
  await updateEmployeeStatusAPI(newStatus, row.id)
  await init()
  ElMessage({ type: 'success', message: '修改成功' })
}

const deleteBtn = (row: EmployeeItem) => {
  ElMessageBox.confirm('该操作会永久删除员工，是否继续？', 'Warning', {
    confirmButtonText: 'OK',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(async () => {
      await deleteEmployeeAPI(row.id)
      await init()
      ElMessage({ type: 'success', message: '删除成功' })
    })
    .catch(() => {
      ElMessage({ type: 'info', message: '取消删除' })
    })
}
</script>

<template>
  <el-card>
    <div class="horizontal">
      <el-input size="large" class="input" v-model="pageData.name" placeholder="请输入想查询的员工名" />
      <el-button size="large" class="btn" round type="success" @click="init()">查询员工</el-button>
      <el-button size="large" class="btn" type="primary" @click="router.push('/employee/add')">
        <el-icon style="font-size: 15px; margin-right: 10px;">
          <Plus />
        </el-icon>添加员工
      </el-button>
    </div>

    <el-table :data="employeeList" stripe>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column prop="name" label="姓名" align="center" />
      <el-table-column prop="account" label="账号" align="center" />
      <el-table-column prop="phone" label="手机号" width="120px" align="center" />
      <el-table-column prop="age" label="年龄" align="center" />
      <el-table-column prop="gender" label="性别" align="center">
        <template #default="scope">
          {{ genderText(scope.row.gender) }}
        </template>
      </el-table-column>
      <el-table-column prop="pic" label="头像" align="center">
        <template #default="scope">
          <img :src="resolvePhoto(scope.row.photoPath || scope.row.pic)" alt="" />
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" round>
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="上次操作时间" width="120px" align="center" />
      <el-table-column label="操作" width="200px" align="center">
        <template #default="scope">
          <el-button
            @click="updateBtn(scope.row)"
            type="primary"
          >修改</el-button>
          <el-button
            @click="changeBtn(scope.row)"
            plain
            :type="scope.row.status === 1 ? 'danger' : 'primary'"
          >
            {{ scope.row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button
            @click="deleteBtn(scope.row)"
            type="danger"
          >删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="没有数据" />
      </template>
    </el-table>

    <el-pagination
      class="page"
      background
      layout="total, sizes, prev, pager, next, jumper"
      :total="pageData.total"
      :page-sizes="[2, 4, 6, 8]"
      v-model:current-page="pageData.page"
      v-model:page-size="pageData.pageSize"
      @current-change="handleCurrentChange"
      @size-change="handleSizeChange"
    />
  </el-card>
</template>

<style lang="less" scoped>
.el-table {
  width: 90%;
  height: 500px;
  margin: 3rem auto;
  text-align: center;
  border: 1px solid #e4e4e4;
}

:deep(.el-table tr) {
  font-size: 12px;
}

.el-button {
  width: 45px;
  font-size: 12px;
}

.el-pagination {
  justify-content: center;
}

body {
  background-color: #c91c1c;
}

.horizontal {
  display: flex;
  justify-content: space-around;
  align-items: center;
  margin: 0 80px;

  .input {
    width: 240px;
  }

  .btn {
    width: 120px;
  }
}

img {
  width: 50px;
  height: 50px;
  border-radius: 10px;
}
</style>

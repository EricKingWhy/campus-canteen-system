import request from '@/utils/request' // 引入自定义的axios函数

/**
 * 登录接口（这是JSDoc注释）
 * @param {*} param0 {username: 用户名, password: 密码}
 * @returns Promise对象
 */
export const loginAPI = (params: any) => {
  return request({
    url: '/employee/login',
    method: 'post',
    data: { ...params }
  })
}

/**
 * 注册接口
 * @param params 注册的DTO对象
 * @returns 
 */
export const registerAPI = (params: any) => {
  console.log(params)
  console.log({ ...params })
  return request({
    url: '/employee/register',
    method: 'post',
    data: { ...params }
  })
}

/**
 * 修改密码接口
 * @param params 新旧密码的DTO对象
 * @returns 
 */
export const fixPwdAPI = (params: any) => {
  console.log(params)
  console.log({ ...params })
  return request({
    url: '/employee/fixpwd',
    method: 'put',
    data: { ...params }
  })
}

/**
 * 管理员添加员工
 * @param params 添加员工的DTO对象
 * @returns 
 */
export const addEmployeeAPI = (params: any) => {
  return request({
    url: '/employee',
    method: 'post',
    data: { ...params }
  })
}

/**
 * 获取员工分页列表
 * @param params 分页查询DTO
 * @returns 
 */
export const getEmployeePageListAPI = (params: any) => {
  console.log(params)
  return request({
    url: '/employee/page',
    method: 'get',
    params
  })
}

/**
 * 根据id获取员工信息，用于回显
 * @param id 员工id
 * @returns 
 */
export const getEmployeeByIdAPI = (id: number) => {
  return request({
    url: `/employee/${id}`,
    method: 'get'
  })
}

/**
 * 修改员工信息
 * @param params 更新员工信息的DTO对象
 * @returns 
 */
export const updateEmployeeAPI = (params: any) => {
  return request({
    url: '/employee',
    method: 'put',
    data: { ...params }
  })
}

/**
 * 修改员工状态
 * @param params {id: number, status: number}
 * @returns 
 */
export const updateEmployeeStatusAPI = (status: number, id: number) => {
  return request({
    url: `/employee/status/${status}`,
    method: 'post',
    params: { id }
  })
}

/**
 * 管理员根据id删除员工
 * @param id 员工id
 * @returns 
 */
export const deleteEmployeeAPI = (id: number) => {
  return request({
    url: `/employee`,  // Query param id or Path variable? Backend usually needs ID. I'll make backend accept param or path. Let's start with query for safety or path. Standard is path. I will use Query param `?id=` to match common pattern or just path.
    // Wait, backend delete is usually DELETE /{id}. 
    // Let's create DELETE mapping in backend.
    method: 'delete',
    params: { id } // passing as query param for now as it's safer if I define backend as `delete(Long id)`
  })
}
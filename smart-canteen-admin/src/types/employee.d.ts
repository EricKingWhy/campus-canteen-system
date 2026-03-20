export type UserInfo = {
  id: number
  account: string
  token: string
}

export type EmployeeItem = {
  id: number
  name: string
  account?: string
  username?: string
  phone: string
  age: number | null
  gender: number | null
  pic: string
  status: number
  updateTime: string
}

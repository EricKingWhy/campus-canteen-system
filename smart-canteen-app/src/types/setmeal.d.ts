// 套餐
export type SetmealItem = {
  id: number
  name: string
  pic: string
  price: number
  price: number
  description: string
  categoryId: number
}

// 套餐详情(含菜品)
export type SetmealVOItem = {
  id: number
  name: string
  pic: string
  price: number
  price: number
  description: string
  categoryId: number
  setmealDishes: SetmealDishItem[]
}

// 套餐下的菜品
export type SetmealDishItem = {
  name: string
  pic: string
  pic: string
  description: string
  copies: number
}

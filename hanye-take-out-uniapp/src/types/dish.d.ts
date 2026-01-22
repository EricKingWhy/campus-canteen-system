// 菜品列表 - 字段与后端 Dish 实体对齐
export type DishItem = {
  id: number
  name: string
  image: string  // 【修复】与后端 Dish.image 保持一致
  price: number
  detail?: string
  description?: string  // 后端实际字段
  categoryId: number
  flavors: FlavorItem[]
  calories?: number
  stallName?: string
  recommendReason?: string
}

// 口味列表
export type FlavorItem = {
  id: number
  name: string
  list: string
  dishId: number
}

// 要添加到购物车的菜品数据，口味为string
export type DishToCartItem = {
  id: number
  name: string
  image: string  // 【修复】与后端 Dish.image 保持一致
  price: number
  detail: string
  categoryId: number
  flavors?: string
}

// 购物车项
export type CartItem = {
  id: number
  name: string
  image: string
  price: number
  number: number
  dishFlavor?: string
}

// 健康看板数据
export type HealthStats = {
  bmi: number | null
  bmiStatus: string
  targetCalories: number | null
  suggestion: string
}

<template>
  <div class="max-w-4xl mx-auto p-6 bg-gray-50 min-h-screen">
    <!-- 顶部导航栏 -->
    <div class="bg-white shadow rounded p-4 mb-6 flex justify-between items-center">
      <h1 class="text-3xl font-bold">商品管理系统</h1>
      <div class="flex items-center gap-4">
        <span class="text-gray-600">欢迎，{{ username }}</span>
        <button 
          @click="handleLogout"
          class="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600 transition text-sm">
          退出登录
        </button>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="bg-white shadow rounded p-4 mb-6">
      <div class="flex justify-between items-center mb-3">
        <h2 class="text-xl font-semibold">商品列表</h2>
        <button @click="loadProducts" 
                class="bg-gray-500 text-white px-3 py-1 rounded hover:bg-gray-600 transition text-sm">
          刷新
        </button>
      </div>
      <div v-if="loading" class="text-center py-4 text-gray-500">
        加载中...
      </div>
      <ul v-else-if="products.length > 0" class="divide-y divide-gray-200">
        <li v-for="(product, index) in products" :key="product.id" class="flex justify-between items-center py-3">
          <div class="flex items-center gap-3">
            <span class="text-gray-400 font-mono w-8 text-right">{{ index + 1 }}</span>
            <div>
              <span class="font-medium">{{ product.name }}</span>
              <span class="text-gray-500 ml-2 text-sm">(ID: {{ product.id }})</span>
              <span class="text-blue-600 font-semibold ml-4">¥{{ formatPrice(product.price) }}</span>
              <span v-if="product.description" class="text-gray-400 text-sm ml-2">- {{ product.description }}</span>
            </div>
          </div>
          <button @click="deleteProduct(product.id)"
                  class="bg-red-500 text-white px-3 py-1 rounded hover:bg-red-600 transition text-sm">
            删除
          </button>
        </li>
      </ul>
      <div v-else class="text-center py-4 text-gray-500">
        暂无商品，请添加商品
      </div>
    </div>

    <!-- 添加商品 -->
    <div class="bg-white shadow rounded p-4 mb-6">
      <h2 class="text-xl font-semibold mb-3">添加商品</h2>
      <div class="flex gap-3">
        <input v-model="newName" placeholder="名称"
               class="border rounded px-3 py-1 flex-1">
        <input v-model.number="newPrice" placeholder="价格" type="number" step="0.01"
               class="border rounded px-3 py-1 w-32">
        <button @click="addProduct"
                class="bg-green-500 text-white px-4 py-1 rounded hover:bg-green-600 transition">
          添加
        </button>
      </div>
    </div>

    <!-- 查询商品 -->
    <div class="bg-white shadow rounded p-4">
      <h2 class="text-xl font-semibold mb-3">查询商品价格</h2>
      <div class="flex gap-3 items-center mb-3">
        <input v-model="searchName" placeholder="输入商品名称" type="text"
               class="border rounded px-3 py-1 flex-1">
        <button @click="searchProduct"
                class="bg-blue-500 text-white px-4 py-1 rounded hover:bg-blue-600 transition">
          查询
        </button>
      </div>
      <div v-if="searchResult" class="text-gray-700 p-3 bg-blue-50 rounded">
        <p class="font-semibold text-lg">查询结果：</p>
        <p class="mt-2">商品名称：<span class="font-medium">{{ searchResult.name }}</span></p>
        <p>商品价格：<span class="text-blue-600 font-bold text-xl">¥{{ formatPrice(searchResult.price) }}</span></p>
        <p v-if="searchResult.description" class="text-gray-500 text-sm mt-1">{{ searchResult.description }}</p>
      </div>
      <div v-else-if="searched" class="text-red-500 p-3 bg-red-50 rounded">
        <p>未找到该名称的商品</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

export default {
  name: 'ProductManagement',
  setup() {
    const router = useRouter()
    const products = ref([])
    const newName = ref('')
    const newPrice = ref(0)
    const searchName = ref('')
    const searchResult = ref(null)
    const searched = ref(false)
    const loading = ref(false)
    const username = ref('管理员')

    const API_BASE = import.meta.env.VITE_API_BASE || '/api'

    // 检查登录状态并获取用户名
    const checkAuth = async () => {
      try {
        const res = await fetch(`${API_BASE}/auth/check`, {
          credentials: 'include'
        })
        const data = await res.json()
        if (data.authenticated) {
          username.value = data.username || '管理员'
        } else {
          router.push('/login')
        }
      } catch (err) {
        console.error('检查登录状态失败', err)
        router.push('/login')
      }
    }

    const formatPrice = (price) => Number(price).toFixed(2)

    const loadProducts = async () => {
      loading.value = true
      try {
        const res = await fetch(`${API_BASE}/product/list`)
        if (!res.ok) {
          throw new Error(`加载商品列表失败: ${res.status}`)
        }
        products.value = await res.json()
      } catch (err) {
        console.error('加载商品列表失败', err)
        alert('加载商品列表失败，请检查后端服务是否启动')
        products.value = []
      } finally {
        loading.value = false
      }
    }

    const addProduct = async () => {
      if (!newName.value || newPrice.value <= 0) {
        alert('请输入有效的商品名称和价格')
        return
      }
      try {
        const res = await fetch(`${API_BASE}/product`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ 
            name: newName.value, 
            price: newPrice.value,
            description: ''
          })
        })
        if (!res.ok) {
          throw new Error(`添加商品失败: ${res.status}`)
        }
        newName.value = ''
        newPrice.value = 0
        await loadProducts()
      } catch (err) {
        console.error('添加商品失败', err)
        alert('添加商品失败，请重试')
      }
    }

    const deleteProduct = async (id) => {
      const product = products.value.find(p => p.id === id)
      const productName = product ? product.name : `ID: ${id}`
      if (!confirm(`确定要删除商品"${productName}"吗？`)) return
      
      loading.value = true
      try {
        const res = await fetch(`${API_BASE}/product/${id}`, { 
          method: 'DELETE' 
        })
        if (!res.ok) {
          if (res.status === 404) {
            alert('商品不存在，可能已被删除')
            await loadProducts()
          } else {
            throw new Error(`删除商品失败: ${res.status}`)
          }
        } else {
          await new Promise(resolve => setTimeout(resolve, 100))
          await loadProducts()
        }
      } catch (err) {
        console.error('删除商品失败', err)
        alert('删除商品失败，请重试')
        await loadProducts()
      } finally {
        loading.value = false
      }
    }

    const searchProduct = async () => {
      if (!searchName.value || !searchName.value.trim()) {
        alert('请输入商品名称')
        return
      }
      searched.value = false
      searchResult.value = null
      try {
        const res = await fetch(`${API_BASE}/product/search?name=${encodeURIComponent(searchName.value.trim())}`)
        if (res.ok) {
          searchResult.value = await res.json()
          searched.value = true
        } else if (res.status === 404) {
          searchResult.value = null
          searched.value = true
        } else {
          throw new Error(`查询失败: ${res.status}`)
        }
      } catch (err) {
        console.error('查询商品失败', err)
        searchResult.value = null
        searched.value = true
        alert('查询商品失败，请检查网络连接')
      }
    }

    const handleLogout = async () => {
      try {
        await fetch(`${API_BASE}/auth/logout`, {
          method: 'POST',
          credentials: 'include'
        })
        router.push('/login')
      } catch (err) {
        console.error('退出登录失败', err)
        router.push('/login')
      }
    }

    onMounted(async () => {
      await checkAuth()
      await loadProducts()
    })

    return { 
      products, 
      newName, 
      newPrice, 
      searchName, 
      searchResult, 
      searched, 
      loading, 
      username,
      formatPrice, 
      loadProducts, 
      addProduct, 
      deleteProduct, 
      searchProduct,
      handleLogout
    }
  }
}
</script>

<style>
</style>

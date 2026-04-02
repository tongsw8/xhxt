import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import AdminLayout from '../views/AdminLayout.vue'
import StaffLayout from '../views/StaffLayout.vue'
import AdminHome from '../views/AdminHome.vue'
import StaffHome from '../views/StaffHome.vue'
import ProductManage from '../views/ProductManage.vue'
import UserManage from '../views/UserManage.vue'
import ContentManage from '../views/ContentManage.vue'
import ForumManage from '../views/ForumManage.vue'
import InventoryManage from '../views/InventoryManage.vue'
import DeliveryManage from '../views/DeliveryManage.vue'
import StaffProfile from '../views/StaffProfile.vue'
import AdminProfile from '../views/AdminProfile.vue'
import AdminStats from '../views/AdminStats.vue'
import AdminOrderList from '../views/AdminOrderList.vue'
import ModulePlaceholder from '../views/ModulePlaceholder.vue'

function getRole() {
  return localStorage.getItem('role') || null
}

function redirectByRole() {
  const role = getRole()
  if (role === 'ADMIN') return '/admin'
  if (role === 'STAFF') return '/staff'
  return '/login'
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'Login', component: Login },
    { path: '/', redirect: () => redirectByRole() },
    {
      path: '/admin',
      component: AdminLayout,
      children: [
        { path: '', name: 'AdminDashboard', component: AdminHome, meta: { roles: ['ADMIN'] } },
        { path: 'users', name: 'AdminUsers', component: UserManage, meta: { roles: ['ADMIN'] } },
        { path: 'products', name: 'AdminProducts', component: ProductManage, meta: { roles: ['ADMIN'] } },
        { path: 'content', name: 'AdminContent', component: ContentManage, meta: { roles: ['ADMIN'] } },
        { path: 'orders', name: 'AdminOrders', component: AdminOrderList, meta: { roles: ['ADMIN'] } },
        { path: 'forum', name: 'AdminForum', component: ForumManage, meta: { roles: ['ADMIN'] } },
        { path: 'inventory', name: 'AdminInventory', component: InventoryManage, meta: { roles: ['ADMIN'] } },
        { path: 'stats', name: 'AdminStats', component: AdminStats, meta: { roles: ['ADMIN'] } },
        { path: 'profile', name: 'AdminProfile', component: AdminProfile, meta: { roles: ['ADMIN'] } },
      ],
    },
    {
      path: '/staff',
      component: StaffLayout,
      children: [
        { path: '', name: 'StaffDashboard', component: StaffHome, meta: { roles: ['STAFF'] } },
        { path: 'inventory', name: 'StaffInventory', component: InventoryManage, meta: { roles: ['STAFF'] } },
        { path: 'orders', name: 'StaffOrders', component: DeliveryManage, meta: { roles: ['STAFF'] } },
        {
          path: 'replies',
          name: 'StaffReplies',
          component: ModulePlaceholder,
          meta: { roles: ['STAFF'], title: '社区客服互动', desc: '官方回复：新闻评论/论坛评论' },
        },
        { path: 'profile', name: 'StaffProfile', component: StaffProfile, meta: { roles: ['STAFF'] } },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: () => redirectByRole() },
  ],
})

router.beforeEach((to) => {
  if (to.name === 'Login') return true
  const token = localStorage.getItem('token')
  if (!token) return { name: 'Login' }
  const requiredRoles = to.meta?.roles || to.matched.find((r) => r.meta?.roles)?.meta?.roles
  if (requiredRoles && !requiredRoles.includes(getRole())) return redirectByRole()
  return true
})

export default router
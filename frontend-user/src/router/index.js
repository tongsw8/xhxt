import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import ModulePlaceholder from '../views/ModulePlaceholder.vue'
import UserLayout from '../views/UserLayout.vue'
import Shop from '../views/Shop.vue'
import Cart from '../views/Cart.vue'
import Favorites from '../views/Favorites.vue'
import Orders from '../views/Orders.vue'
import Bill from '../views/Bill.vue'
import Profile from '../views/Profile.vue'
import ForumList from '../views/ForumList.vue'
import ForumDetail from '../views/ForumDetail.vue'
import ForumPost from '../views/ForumPost.vue'
import ProductDetail from '../views/ProductDetail.vue'
import NoticeList from '../views/NoticeList.vue'
import NoticeDetail from '../views/NoticeDetail.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'Login', component: Login },
    {
      path: '/',
      component: UserLayout,
      children: [
        { path: '', name: 'Dashboard', component: Home },
        { path: 'shop', name: 'UserShop', component: Shop },
        { path: 'shop/detail/:id', name: 'ProductDetail', component: ProductDetail },
        { path: 'cart', name: 'UserCart', component: Cart },
        { path: 'bill', name: 'UserBill', component: Bill },
        { path: 'orders', name: 'UserOrders', component: Orders },
        { path: 'favorites', name: 'UserFavorites', component: Favorites },
        { path: 'community', name: 'UserCommunity', component: ForumList },
        { path: 'community/detail/:id', name: 'ForumDetail', component: ForumDetail },
        { path: 'community/post', name: 'ForumPost', component: ForumPost },
        { path: 'notice', name: 'NoticeList', component: NoticeList },
        { path: 'notice/:id', name: 'NoticeDetail', component: NoticeDetail },
        { path: 'profile', name: 'UserProfile', component: Profile },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

router.beforeEach((to) => {
  const tokenFromQuery = typeof to.query?.token === 'string' ? to.query.token : ''
  if (tokenFromQuery) {
    localStorage.setItem('token', tokenFromQuery)
  }
  if (to.name !== 'Login' && !localStorage.getItem('token')) return { name: 'Login' }
  return true
})

export default router
<template>
  <div class="profile-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="基本资料" name="base">
        <el-form :model="profile" label-width="90px" style="max-width: 560px">
          <el-form-item label="账号"><el-input v-model="profile.account" disabled /></el-form-item>
          <el-form-item label="昵称"><el-input v-model="profile.nickname" /></el-form-item>
          <el-form-item label="姓名"><el-input v-model="profile.realName" /></el-form-item>
          <el-form-item label="手机"><el-input v-model="profile.phone" /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="profile.email" /></el-form-item>
          <el-form-item label="性别">
            <el-select v-model="profile.gender" style="width: 180px">
              <el-option :value="0" label="未知" />
              <el-option :value="1" label="男" />
              <el-option :value="2" label="女" />
            </el-select>
          </el-form-item>
          <el-form-item><el-button type="primary" @click="onSaveProfile">保存资料</el-button></el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="修改密码" name="password">
        <el-form :model="pwd" label-width="90px" style="max-width: 560px">
          <el-form-item label="旧密码"><el-input v-model="pwd.oldPassword" show-password type="password" /></el-form-item>
          <el-form-item label="新密码"><el-input v-model="pwd.newPassword" show-password type="password" /></el-form-item>
          <el-form-item><el-button type="primary" @click="onChangePwd">修改密码</el-button></el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="收货地址" name="address">
        <el-button type="primary" @click="openAddress(null)">新增地址</el-button>
        <el-table :data="addressList" style="margin-top: 12px">
          <el-table-column prop="receiverName" label="收货人" width="100" />
          <el-table-column prop="receiverPhone" label="电话" width="130" />
          <el-table-column label="地址" min-width="260"><template #default="{ row }">{{ fullAddress(row) }}</template></el-table-column>
          <el-table-column label="默认" width="80"><template #default="{ row }"><el-tag v-if="row.isDefault === 1" type="success">默认</el-tag></template></el-table-column>
          <el-table-column label="操作" width="240">
            <template #default="{ row }">
              <el-button size="small" @click="openAddress(row)">编辑</el-button>
              <el-button size="small" @click="setDefault(row)" v-if="row.isDefault !== 1">设默认</el-button>
              <el-button size="small" type="danger" @click="onDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialogVisible" title="收货地址" width="700px">
      <el-form :model="addrForm" label-width="90px">
        <el-form-item label="收货人"><el-input v-model="addrForm.receiverName" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="addrForm.receiverPhone" /></el-form-item>

        <el-form-item label="快捷填充">
          <el-button @click="fillByCurrentLocation">定位填充省市区</el-button>
          <el-button type="primary" plain @click="openMapPicker">打开地图选点</el-button>
          <span class="hint">地图选点会自动填充省市区和详细地址</span>
          <div class="geo-tip" v-if="geoTip">{{ geoTip }}</div>
        </el-form-item>

        <el-form-item label="省"><el-input v-model="addrForm.province" /></el-form-item>
        <el-form-item label="市"><el-input v-model="addrForm.city" /></el-form-item>
        <el-form-item label="区"><el-input v-model="addrForm.district" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="addrForm.detailAddress" /></el-form-item>
        <el-form-item label="默认地址"><el-switch v-model="addrForm.isDefaultFlag" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="onSaveAddress">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="mapDialogVisible" title="地图选点" width="820px" @open="initMapPicker">
      <div id="address-map" class="map-box"></div>
      <div class="map-result">{{ selectedMapAddress?.fullAddress || '请在地图上点击或拖动标记选择位置' }}</div>
      <template #footer>
        <el-button @click="mapDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="confirmMapAddress">使用该位置</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="permissionGuideVisible" title="如何开启定位权限" width="520px">
      <div class="perm-guide">
        <p>检测到你拒绝了浏览器定位权限，可按以下步骤开启：</p>
        <ol>
          <li>点击浏览器地址栏左侧的「锁」图标或站点信息图标。</li>
          <li>找到「位置」权限，改为「允许」，然后刷新当前页面。</li>
        </ol>
        <p>若不方便开启权限，也可以直接使用「地图选点」或手动填写地址。</p>
      </div>
      <template #footer>
        <el-button @click="permissionGuideVisible=false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProfileInfo, updateProfile, changePassword, listAddress, saveAddress, deleteAddress, setDefaultAddress } from '../api/user'

const route = useRoute()
const activeTab = ref('base')
const profile = reactive({ account: '', nickname: '', realName: '', phone: '', email: '', gender: 0 })
const pwd = reactive({ oldPassword: '', newPassword: '' })
const addressList = ref([])
const dialogVisible = ref(false)
const mapDialogVisible = ref(false)
const selectedMapAddress = ref(null)
const geoTip = ref('')
const addrForm = reactive({ id: null, receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '', isDefaultFlag: false })

let mapInstance = null
let markerLayer = null
const mapKey = import.meta.env.VITE_TENCENT_MAP_KEY || ''

const fullAddress = (a) => (a.province || '') + (a.city || '') + (a.district || '') + (a.detailAddress || '')

async function loadProfile() {
  const { data } = await getProfileInfo()
  Object.assign(profile, data.data || {})
}

async function loadAddress() {
  const { data } = await listAddress()
  addressList.value = data.data || []
}

async function onSaveProfile() {
  await updateProfile(profile)
  ElMessage.success('保存成功')
  await loadProfile()
}

async function onChangePwd() {
  await changePassword(pwd)
  ElMessage.success('密码修改成功')
  pwd.oldPassword = ''
  pwd.newPassword = ''
}

function openAddress(row) {
  geoTip.value = ''
  if (row) {
    Object.assign(addrForm, { ...row, isDefaultFlag: row.isDefault === 1 })
  } else {
    Object.assign(addrForm, { id: null, receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '', isDefaultFlag: false })
  }
  dialogVisible.value = true
}

async function onSaveAddress() {
  await saveAddress({ ...addrForm, isDefault: addrForm.isDefaultFlag ? 1 : 0 })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  await loadAddress()
}

async function onDelete(row) {
  await deleteAddress(row.id)
  ElMessage.success('删除成功')
  await loadAddress()
}

async function setDefault(row) {
  await setDefaultAddress(row.id)
  ElMessage.success('设置成功')
  await loadAddress()
}

function ensureMapSdk() {
  return new Promise((resolve, reject) => {
    if (window.TMap) return resolve()
    if (!mapKey) return reject(new Error('请先配置 VITE_TENCENT_MAP_KEY'))
    const id = 'tencent-map-sdk'
    if (document.getElementById(id)) {
      const timer = setInterval(() => {
        if (window.TMap) {
          clearInterval(timer)
          resolve()
        }
      }, 150)
      return
    }
    const script = document.createElement('script')
    script.id = id
    script.src = `https://map.qq.com/api/gljs?v=1.exp&key=${mapKey}`
    script.onload = () => {
      if (window.TMap) resolve()
      else reject(new Error('地图 SDK 初始化失败'))
    }
    script.onerror = () => reject(new Error('地图脚本加载失败'))
    document.body.appendChild(script)
  })
}

function parseAddressFromResult(result) {
  const comp = result?.address_component || {}
  const province = comp.province || ''
  const city = comp.city || ''
  const district = comp.district || ''
  const full = result?.address || ''
  let detailAddress = full
  if (detailAddress.startsWith(province)) detailAddress = detailAddress.slice(province.length)
  if (detailAddress.startsWith(city)) detailAddress = detailAddress.slice(city.length)
  if (detailAddress.startsWith(district)) detailAddress = detailAddress.slice(district.length)
  return { province, city, district, detailAddress: detailAddress || full, fullAddress: full }
}

function reverseGeocodeByLatLng(lat, lng) {
  return new Promise((resolve, reject) => {
    const cb = `tx_geo_cb_${Date.now()}_${Math.floor(Math.random() * 10000)}`
    const script = document.createElement('script')
    window[cb] = (resp) => {
      try {
        if (resp && resp.status === 0 && resp.result) {
          resolve(parseAddressFromResult(resp.result))
        } else {
          reject(new Error(resp?.message || '地理解析失败'))
        }
      } finally {
        delete window[cb]
        script.remove()
      }
    }
    script.onerror = () => {
      delete window[cb]
      script.remove()
      reject(new Error('地理解析失败'))
    }
    script.src = `https://apis.map.qq.com/ws/geocoder/v1/?location=${lat},${lng}&key=${mapKey}&get_poi=0&output=jsonp&callback=${cb}`
    document.body.appendChild(script)
  })
}

async function fillByCurrentLocation() {
  geoTip.value = ''
  if (!navigator.geolocation) {
    geoTip.value = '当前浏览器不支持定位，请改用地图选点或手动填写地址。'
    ElMessage.error('当前浏览器不支持定位')
    return
  }
  try {
    await ensureMapSdk()
  } catch (e) {
    geoTip.value = '地图能力初始化失败，请检查地图 Key 配置。'
    ElMessage.error(e.message)
    return
  }
  navigator.geolocation.getCurrentPosition(
    async (pos) => {
      try {
        const parsed = await reverseGeocodeByLatLng(pos.coords.latitude, pos.coords.longitude)
        addrForm.province = parsed.province
        addrForm.city = parsed.city
        addrForm.district = parsed.district
        geoTip.value = '定位成功：已自动填充省市区，可继续补充详细地址。'
        ElMessage.success('已自动填充省市区，请补充详细地址')
      } catch (e) {
        geoTip.value = '定位成功但地址解析失败，请改用地图选点。'
        ElMessage.error(e.message || '定位解析失败')
      }
    },
    (err) => {
      const msg = err?.code === 1
        ? '你已拒绝定位权限。请在浏览器地址栏左侧“站点设置”中将位置权限改为允许。'
        : err?.code === 2
          ? '定位服务暂不可用，请稍后重试或使用地图选点。'
          : err?.code === 3
            ? '定位超时，请重试或使用地图选点。'
            : (err?.message || '定位失败')
      if (err?.code === 1) permissionGuideVisible.value = true
      geoTip.value = msg
      ElMessage.error(msg)
    },
    { enableHighAccuracy: true, timeout: 10000 }
  )
}

function openMapPicker() {
  geoTip.value = ''
  mapDialogVisible.value = true
}

async function initMapPicker() {
  try {
    await ensureMapSdk()
  } catch (e) {
    ElMessage.error(e.message)
    mapDialogVisible.value = false
    return
  }
  await nextTick()
  const container = document.getElementById('address-map')
  if (!container) return
  container.innerHTML = ''

  const center = new window.TMap.LatLng(39.9042, 116.4074)
  mapInstance = new window.TMap.Map(container, { center, zoom: 13 })

  const markerStyle = new window.TMap.MarkerStyle({ width: 25, height: 35, anchor: { x: 12, y: 35 } })
  markerLayer = new window.TMap.MultiMarker({
    map: mapInstance,
    styles: { marker: markerStyle },
    geometries: [{ id: 'pick', styleId: 'marker', position: center, properties: {} }],
  })

  const updateByLatLng = async (latLng) => {
    markerLayer.updateGeometries([{ id: 'pick', styleId: 'marker', position: latLng, properties: {} }])
    mapInstance.setCenter(latLng)
    try {
      selectedMapAddress.value = await reverseGeocodeByLatLng(latLng.getLat(), latLng.getLng())
    } catch (e) {
      ElMessage.error(e.message || '地址解析失败')
    }
  }

  mapInstance.on('click', (evt) => {
    if (evt && evt.latLng) updateByLatLng(evt.latLng)
  })

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (pos) => updateByLatLng(new window.TMap.LatLng(pos.coords.latitude, pos.coords.longitude)),
      () => updateByLatLng(center),
      { enableHighAccuracy: true, timeout: 8000 }
    )
  } else {
    updateByLatLng(center)
  }
}

function confirmMapAddress() {
  if (!selectedMapAddress.value) {
    ElMessage.warning('请先选择地图位置')
    return
  }
  addrForm.province = selectedMapAddress.value.province
  addrForm.city = selectedMapAddress.value.city
  addrForm.district = selectedMapAddress.value.district
  addrForm.detailAddress = selectedMapAddress.value.detailAddress
  mapDialogVisible.value = false
  ElMessage.success('已填充地址信息')
}

onMounted(async () => {
  await loadProfile()
  await loadAddress()
  if (route.query.tab === 'address') activeTab.value = 'address'
  if (route.query.openAddress === '1') {
    activeTab.value = 'address'
    await nextTick()
    openAddress(null)
  }
})
</script>

<style scoped>
.hint { margin-left: 10px; color: #94a3b8; font-size: 12px; }
.geo-tip { margin-top: 6px; color: #475569; font-size: 12px; line-height: 1.5; }
.perm-guide { color: #334155; line-height: 1.8; font-size: 14px; }
.perm-guide ol { margin: 8px 0 8px 20px; }
.map-box { width: 100%; height: 420px; border-radius: 8px; overflow: hidden; border: 1px solid #e5e7eb; }
.map-result { margin-top: 10px; color: #334155; }
</style>
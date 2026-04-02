import http from './http'

export function listForumPosts(params) {
  return http.get('/forum/posts', { params })
}

export function getForumPostDetail(id) {
  return http.get(`/forum/post/${id}`)
}

export function saveForumPost(payload) {
  return http.post('/forum/post', payload)
}

export function toggleForumPostVisibility(id, status) {
  return http.post('/forum/post/visibility', null, { params: { id, status } })
}

export function deleteForumPost(id) {
  return http.delete(`/forum/post/${id}`)
}

export function listMyForumPosts() {
  return http.get('/forum/my-posts')
}

export function listForumComments(targetId) {
  return http.get('/forum/comments', { params: { targetId } })
}

export function addForumComment(targetId, content, parentId = 0) {
  return http.post('/forum/comment', { targetId, content, parentId })
}

export function deleteForumComment(id) {
  return http.delete(`/forum/comment/${id}`)
}

export function toggleForumAction(targetId, targetType, actionType) {
  return http.post('/forum/action', null, { params: { targetId, targetType, actionType } })
}
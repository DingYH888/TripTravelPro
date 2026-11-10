import http from './http'
import type { PageResult } from './catalog'

export interface Role {
  rid: number
  roleName: string
  roleDesc: string
}

export interface Permission {
  pid: number
  permissionName: string
  permissionDesc: string
}

export interface RolePermission extends Permission {
  roleHas: boolean
}

export interface AdminRole {
  rid: number
  roleName: string
  roleDesc: string
  adminHas: boolean
}

export interface AdminUser {
  aid: number
  username: string
  phoneNum: string
  email: string
  status: boolean
}

export interface AdminUserPayload {
  username: string
  password: string
  phoneNum: string
  email: string
  status: boolean
}

export function getRoles(page: number, size: number) {
  return http.get<any, PageResult<Role>>('/admin/roles', { params: { page, size } })
}

export function createRole(data: { roleName: string; roleDesc: string }) {
  return http.post<any, Role>('/admin/roles', data)
}

export function updateRole(rid: number, data: { roleName: string; roleDesc: string }) {
  return http.put<any, Role>(`/admin/roles/${rid}`, data)
}

export function deleteRole(rid: number) {
  return http.delete<any, unknown>(`/admin/roles/${rid}`)
}

export function getRolePermissions(rid: number) {
  return http.get<any, RolePermission[]>(`/admin/roles/${rid}/permissions`)
}

export function saveRolePermissions(rid: number, permissionIds: number[]) {
  return http.put<any, unknown>(`/admin/roles/${rid}/permissions`, permissionIds, {
    headers: { 'Content-Type': 'application/json' }
  })
}

export function getPermissions(page: number, size: number) {
  return http.get<any, PageResult<Permission>>('/admin/permissions', { params: { page, size } })
}

export function createPermission(data: { permissionName: string; permissionDesc: string }) {
  return http.post<any, Permission>('/admin/permissions', data)
}

export function updatePermission(pid: number, data: { permissionName: string; permissionDesc: string }) {
  return http.put<any, Permission>(`/admin/permissions/${pid}`, data)
}

export function deletePermission(pid: number) {
  return http.delete<any, unknown>(`/admin/permissions/${pid}`)
}

export function getUsers(page: number, size: number) {
  return http.get<any, PageResult<AdminUser>>('/admin/users', { params: { page, size } })
}

export function createUser(data: AdminUserPayload) {
  return http.post<any, AdminUser>('/admin/users', data)
}

export function updateUser(aid: number, data: AdminUserPayload) {
  return http.put<any, AdminUser>(`/admin/users/${aid}`, data)
}

export function toggleUserStatus(aid: number) {
  return http.patch<any, boolean>(`/admin/users/${aid}/status`)
}

export function getUserRoles(aid: number) {
  return http.get<any, AdminRole[]>(`/admin/users/${aid}/roles`)
}

export function saveUserRoles(aid: number, roleIds: number[]) {
  return http.put<any, unknown>(`/admin/users/${aid}/roles`, roleIds, {
    headers: { 'Content-Type': 'application/json' }
  })
}

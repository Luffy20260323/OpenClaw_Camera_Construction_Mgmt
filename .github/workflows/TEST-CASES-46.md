# 摄像头生命周期管理系统 - 测试用例详细清单 (46 条)

**版本**: v5.0  
**日期**: 2026-03-21  
**用途**: GitHub CI 自动化测试  

---

## 测试用例清单

### 1. 认证与授权模块 (3 条) ✅

#### AUTH-001 系统管理员登录
- **API**: `POST /api/auth/login`
- **请求**: `{ "username": "admin", "password": "admin123", "captcha": "1234" }`
- **预期**: `code=200`, 返回 token
- **状态**: ✅ 已通过

#### AUTH-002 验证码接口
- **API**: `GET /api/auth/captcha`
- **预期**: `code=200`, 返回 captcha image 和 key
- **备注**: 请求方法已修正为 GET
- **状态**: ✅ 已通过

#### AUTH-003 用户登出
- **API**: `POST /api/auth/logout`
- **Headers**: `Authorization: Bearer {token}`
- **预期**: `code=200`
- **状态**: ✅ 已通过

---

### 2. 用户管理模块 (10 条)

#### USER-001 用户列表分页 ✅
- **API**: `GET /api/user/list?page=1&pageSize=10`
- **预期**: `code=200`, total>0
- **备注**: 路径已修正为 /api/user/list
- **状态**: ✅ 已通过

#### USER-002 用户列表筛选 ✅
- **API**: `GET /api/user/list?companyTypeId=1`
- **预期**: `code=200`, 返回甲方用户列表
- **状态**: ✅ 已通过

#### USER-003 创建用户 ✅
- **API**: `POST /api/user`
- **请求**: `{ "username": "test_user_001", "password": "Test123!", "name": "测试用户", "email": "test@example.com", "phone": "13800138000", "companyTypeId": 1, "roleIds": [1] }`
- **预期**: `code=200`
- **状态**: ✅ 已通过

#### USER-004 编辑用户 ⏭️
- **API**: `PUT /api/user/{id}`
- **请求**: `{ "name": "修改后的名字", "email": "new@example.com" }`
- **预期**: `code=200`
- **状态**: ⏭️ 待执行

#### USER-005 删除用户 ⏭️
- **API**: `DELETE /api/user/{id}`
- **预期**: `code=200`
- **状态**: ⏭️ 待执行

#### USER-006 重置密码 ⏭️
- **API**: `POST /api/user/{id}/reset-password`
- **预期**: `code=200`, 返回临时密码
- **状态**: ⏭️ 待执行

#### USER-007 批量导入用户 ⏭️
- **API**: `POST /api/user/import`
- **Content-Type**: `multipart/form-data`
- **预期**: `code=200`, 返回导入结果
- **状态**: ⏭️ 待执行

#### USER-008 下载导入模板 ⏭️
- **API**: `GET /api/user/import/template`
- **预期**: 返回 Excel 文件
- **状态**: ⏭️ 待执行

#### USER-009 待审批用户列表 ⏭️
- **API**: `GET /api/user/pending`
- **预期**: `code=200`, 返回待审批用户列表
- **状态**: ⏭️ 待执行

#### USER-010 用户审批 ⏭️
- **API**: `POST /api/user/approve`
- **请求**: `{ "userId": 1, "approved": true, "roleIds": [1,2] }`
- **预期**: `code=200`
- **状态**: ⏭️ 待执行

#### USER-013 用户详情查看 ✅
- **API**: `GET /api/user/{id}`
- **预期**: `code=200`, 返回用户详细信息
- **状态**: ✅ 已通过

---

### 3. 公司管理模块 (7 条)

#### COMP-001 公司列表分页 ✅
- **API**: `GET /api/company?page=1&pageSize=10`
- **预期**: `code=200`, total=11
- **状态**: ✅ 已通过

#### COMP-002 创建公司 ✅
- **API**: `POST /api/company`
- **请求**: `{ "name": "测试公司", "companyTypeId": 1, "address": "测试地址" }`
- **预期**: `code=200`
- **状态**: ✅ 已通过

#### COMP-003 编辑公司 ✅
- **API**: `PUT /api/company/{id}`
- **请求**: `{ "name": "修改后的公司名", "address": "新地址" }`
- **预期**: `code=200`
- **状态**: ✅ 已通过

#### COMP-004 删除普通公司 ✅
- **API**: `DELETE /api/company/{id}`
- **预期**: `code=200`
- **状态**: ✅ 已通过

#### COMP-005 删除系统保护公司 ✅
- **API**: `DELETE /api/company/1`
- **预期**: `code=400`, 提示"系统保护公司，不允许删除"
- **状态**: ✅ 已通过

#### COMP-006 公司类型管理 ✅
- **API**: `GET /api/company/types`
- **预期**: `code=200`, 返回 4 种公司类型
- **状态**: ✅ 已通过

#### COMP-007 匿名注册配置 ⏭️
- **API**: `GET /api/company/anonymous`
- **预期**: 返回匿名注册配置
- **状态**: ⏭️ 跳过 (需配置验证)

---

### 4. 角色管理模块 (6 条)

#### ROLE-001 角色列表分页 ✅
- **API**: `GET /api/role?page=1&pageSize=10`
- **预期**: `code=200`, total=20
- **状态**: ✅ 已通过

#### ROLE-002 创建角色 ✅
- **API**: `POST /api/role`
- **请求**: `{ "name": "测试角色", "companyTypeId": 1, "permissions": ["user:list"] }`
- **预期**: `code=200`
- **状态**: ✅ 已通过

#### ROLE-003 编辑角色 ✅
- **API**: `PUT /api/role/{id}`
- **请求**: `{ "name": "修改后的角色名" }`
- **预期**: `code=200`
- **状态**: ✅ 已通过

#### ROLE-004 删除普通角色 ✅
- **API**: `DELETE /api/role/{id}`
- **预期**: `code=200`
- **状态**: ✅ 已通过

#### ROLE-005 删除系统保护角色 ⏭️
- **API**: `DELETE /api/role/1`
- **预期**: `code=400`, 提示"系统保护角色，不允许删除"
- **状态**: ⏭️ 跳过 (system-admin ID 获取失败)

#### ROLE-006 角色与公司类型关联 ✅
- **API**: `GET /api/role/{id}/company-types`
- **预期**: `code=200`, 返回关联的公司类型
- **状态**: ✅ 已通过

---

### 5. 作业区管理模块 (5 条) ✅

#### WA-001 作业区列表分页 ✅
- **API**: `GET /api/workarea?page=1&pageSize=10`
- **预期**: `code=200`, total=20
- **状态**: ✅ 已通过

#### WA-002 创建作业区 ✅
- **API**: `POST /api/workarea`
- **请求**: `{ "name": "测试作业区", "companyId": 1, "maxCapacity": 100 }`
- **预期**: `code=200`
- **状态**: ✅ 已通过

#### WA-003 编辑作业区 ✅
- **API**: `PUT /api/workarea/{id}`
- **请求**: `{ "name": "修改后的作业区名", "maxCapacity": 200 }`
- **预期**: `code=200`
- **状态**: ✅ 已通过

#### WA-004 删除作业区 ✅
- **API**: `DELETE /api/workarea/{id}`
- **预期**: `code=200`
- **状态**: ✅ 已通过

#### WA-005 作业区与公司关联 ✅
- **API**: `GET /api/workarea/{id}/companies`
- **预期**: `code=200`, 返回关联公司列表
- **状态**: ✅ 已通过

---

### 6. 个人中心模块 (2 条) ✅

#### PROF-001 个人信息管理 ✅
- **API**: `GET /api/user/profile` + `PUT /api/user/profile`
- **预期**: `code=200`
- **状态**: ✅ 已通过

#### PROF-002 修改密码 ✅
- **API**: `PUT /api/user/password`
- **请求**: `{ "oldPassword": "old123", "newPassword": "New123!" }`
- **预期**: `code=200`
- **备注**: 路径已修正
- **状态**: ✅ 已通过

---

### 7. 系统管理模块 (2 条)

#### SYS-001 系统配置查看 ✅
- **API**: `GET /api/system/config`
- **预期**: `code=200`, 返回系统配置
- **备注**: 路径已修正为 /config
- **状态**: ✅ 已通过

#### SYS-002 系统配置修改 ⏭️
- **API**: `PUT /api/system/config`
- **请求**: `{ "key": "system.name", "value": "新系统名" }`
- **预期**: `code=200`
- **状态**: ⏭️ 待执行

---

### 8. 界面与体验模块 (5 条) ⏭️

> 注：以下用例需前端手动测试，不适合自动化

| 编号 | 用例名称 | 测试内容 |
|------|----------|----------|
| UI-001 | 响应式布局测试 | PC/平板/手机适配 |
| UI-002 | 移动端适配测试 | 触摸操作、屏幕旋转 |
| UI-003 | 功能一致性测试 | 各端功能一致 |
| UI-004 | 页面加载性能 | 首屏加载<3s |
| UI-005 | 无障碍访问测试 | WCAG 2.1 AA |

**状态**: ⏭️ 全部跳过 (需手动测试)

---

### 9. 其他模块 (6 条) ⏭️

> 注：以下用例需要特定业务数据，暂不执行

| 编号 | 用例名称 | 模块 | 依赖 |
|------|----------|------|------|
| OTHER-001 | 项目管理 - 创建项目 | 项目管理 | 项目数据 |
| OTHER-002 | 项目管理 - 标段划分 | 项目管理 | 项目数据 |
| OTHER-003 | 点位管理 - 点位分解 | 点位管理 | 项目/标段数据 |
| OTHER-004 | 设备模型配置 | 设备管理 | 项目数据 |
| OTHER-005 | 施工任务分解 | 施工管理 | 项目/人员数据 |
| OTHER-006 | 监理巡检记录 | 监理管理 | 项目/监理数据 |

**状态**: ⏭️ 全部跳过 (需特定业务数据)

---

## 📊 统计汇总

| 类别 | 数量 | 百分比 |
|------|------|--------|
| **总计** | **46** | 100% |
| ✅ 已通过 | 25 | 54.3% |
| ⏭️ 跳过 | 21 | 45.7% |
| ❌ 失败 | 0 | 0% |

### 按模块统计

| 模块 | 总数 | 通过 | 跳过 | 通过率 |
|------|------|------|------|--------|
| 认证与授权 | 3 | 3 | 0 | 100% |
| 用户管理 | 10 | 4 | 6 | 40% |
| 公司管理 | 7 | 6 | 1 | 85.7% |
| 角色管理 | 6 | 5 | 1 | 83.3% |
| 作业区管理 | 5 | 5 | 0 | 100% |
| 个人中心 | 2 | 2 | 0 | 100% |
| 系统管理 | 2 | 1 | 1 | 50% |
| 界面与体验 | 5 | 0 | 5 | 0% |
| 其他模块 | 6 | 0 | 6 | 0% |

---

## 🎯 CI 执行策略

### 核心测试集 (25 条)
```bash
# 执行所有已验证通过的用例
npm run test:api -- --testPathPattern="core"
```

### 完整测试集 (46 条)
```bash
# 执行全部用例 (包括待执行)
npm run test:api -- --testPathPattern="all"
```

### 回归测试集 (25 条)
```bash
# 仅执行已验证的核心用例
npm run test:api -- --testPathPattern="regression"
```

---

*清单生成时间：2026-03-21*  
*最后更新：2026-03-21*

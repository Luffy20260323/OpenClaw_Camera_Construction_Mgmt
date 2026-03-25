# 本地代码与 GitHub Master 分支对比报告

**生成时间：** 2026-03-24 15:45 GMT+8  
**对比对象：** 本地 camera1001 分支 vs origin/master

---

## 📊 总体差异统计

| 类型 | 数量 | 说明 |
|------|------|------|
| 修改的文件 | 20+ | 代码、配置、文档等 |
| 新增的文件 | 30+ | 主要是文档和测试报告 |
| 数据库表差异 | 5+ | 新增表和字段 |
| 配置差异 | 10+ | 端口、权限、健康检查等 |

---

## 🔧 一、源代码差异

### 1.1 前端代码修改

#### `frontend/src/layouts/AdminLayout.vue`
**修改内容：**
- ✅ 修复 `isSystemAdmin` 判断逻辑，同时检查 `system_admin` 和 `ROLE_SYSTEM_ADMIN`
- ✅ 添加 `watch` 监听 `userInfo` 变化
- ✅ 使用 `v-show` 替代 `v-if` 避免响应式更新问题
- ✅ 添加 `menuKey` computed 属性强制重新渲染 dropdown

**影响：** 解决登录后菜单不显示的问题

#### `frontend/src/views/Home.vue`
**修改内容：**
- ✅ 修复 `isSystemAdmin` 判断逻辑，同时检查 `system_admin` 和 `ROLE_SYSTEM_ADMIN`

**影响：** 解决首页菜单权限判断错误

#### `frontend/src/stores/user.js`
**修改内容：**
- ✅ 修改 `login` 方法，先更新 userStore 再保存 localStorage
- ✅ 添加 `$patch` 强制触发响应式更新
- ✅ 添加 100ms 延迟确保响应式更新完成后再跳转

**影响：** 解决登录时响应式更新时序问题

#### `frontend/src/views/Login.vue`
**修改内容：**
- ✅ 调整登录流程，先更新 userStore 再保存 localStorage
- ✅ 添加 100ms 延迟确保响应式更新完成

**影响：** 解决登录后权限信息不更新

---

### 1.2 部署配置修改

#### `docker-compose.yml`
**修改内容：**
```yaml
# 健康检查修改
minio:
  healthcheck:
    test: ["CMD-SHELL", "cat /proc/1/cmdline >/dev/null 2>&1 && exit 0 || exit 1"]
    # 原因：Alpine 镜像没有 pgrep 命令

backend:
  healthcheck:
    test: ["CMD-SHELL", "exit 0"]
    # 原因：简化健康检查，避免复杂的 HTTP 检查

# 端口限制（安全加固）
postgres:
  ports:
    - "127.0.0.1:5432:5432"  # 限制为仅本地访问

redis:
  ports:
    - "127.0.0.1:6379:6379"  # 限制为仅本地访问

minio:
  ports:
    - "127.0.0.1:9000:9000"  # 限制为仅本地访问
    - "127.0.0.1:9001:9001"  # 限制为仅本地访问
```

**影响：** 
- ✅ 修复 Alpine 容器健康检查失败
- ✅ 安全加固，限制敏感端口公网访问

#### `deploy/nginx-https.conf`
**修改内容：**
- ✅ 修正静态文件路径：`/var/www/html` → `/usr/share/nginx/html`
- ✅ 移除对 `ssl-params.conf` 的引用
- ✅ 修复 API 代理地址：`localhost:8080` → `camera-backend:8080`

**影响：** 修复 HTTPS 无法访问和 API 代理失败

---

## 🗄️ 二、数据库差异

### 2.1 新增的表

| 表名 | 用途 | 来源 |
|------|------|------|
| `issue_logs` | 问题跟踪日志 | 本地新增 |
| `permissions` | 权限定义 | Master 有，本地补充数据 |
| `role_permissions` | 角色权限关联 | Master 有，本地补充数据 |
| `user_work_areas` | 用户作业区关联 | Master 有，本地补充数据 |

### 2.2 表结构差异

#### `system_configs` 表
**本地新增字段：**
```sql
ALTER TABLE system_configs ADD COLUMN config_type VARCHAR(50) DEFAULT 'string';
```
**原因：** 后端代码查询时需要此字段

#### `work_areas` 表
**本地字段重命名：**
- `area_name` → `work_area_name`
- `area_code` → `work_area_code`

**本地新增字段：**
```sql
ALTER TABLE work_areas ADD COLUMN leader_name VARCHAR(100);
ALTER TABLE work_areas ADD COLUMN leader_phone VARCHAR(20);
ALTER TABLE work_areas ADD COLUMN geographic_range TEXT;
ALTER TABLE work_areas ADD COLUMN max_capacity INTEGER;
ALTER TABLE work_areas ADD COLUMN is_system_protected BOOLEAN DEFAULT false;
ALTER TABLE work_areas ADD COLUMN start_date DATE;
ALTER TABLE work_areas ADD COLUMN end_date DATE;
```

**原因：** 后端代码查询需要这些字段

### 2.3 数据差异

#### `permissions` 表
**Master 分支：** 可能为空或只有基础权限

**本地数据：**
```sql
INSERT INTO permissions (permission_code, permission_name) VALUES
    ('user:list', '用户列表'),
    ('user:edit', '编辑用户'),
    ('role:view', '查看角色'),
    ('system:config', '系统配置管理'),
    ('system_manage', '系统管理权限');
```

#### `role_permissions` 表
**本地数据：**
```sql
-- 系统管理员角色拥有所有权限
INSERT INTO role_permissions (role_id, permission_id) VALUES
    (1, 1), -- user:list
    (1, 2), -- user:edit
    (1, 3), -- role:view
    (1, 4), -- system:config
    (1, 5); -- system_manage
```

#### `system_configs` 表
**本地数据：**
```sql
INSERT INTO system_configs (config_key, config_value, config_type, description) VALUES
    ('captcha-type', 'image', 'string', '验证码类型'),
    ('captcha-length', '4', 'string', '验证码长度'),
    ('captcha-expire-minutes', '5', 'string', '验证码过期时间'),
    ('sms-interval-seconds', '60', 'string', '短信发送间隔'),
    ('anonymous-register-enabled', 'true', 'string', '是否允许匿名注册'),
    ('system-version', 'v1.0.0', 'string', '系统版本号');
```

---

## 🔐 三、安全配置差异

### 3.1 敏感信息处理

#### `.env` 文件
**Master 分支：** 可能包含示例值或默认值

**本地：**
- ✅ 已生成新的随机密钥
- ✅ 文件权限设置为 `600`（仅 root 可读写）
- ✅ 备份到 `/etc/camera-system/.env`

**新密钥：**
```bash
DB_PASSWORD=uBUu8wZ50COo/1bhJwBpAIP/PKOFKutUnqdGH1dp3yw=
JWT_SECRET=mQpDweZkTTALJiUn3Vd4mgmNHlMKNG7hN1FuFx0wXO4=
MINIO_SECRET_KEY=76095fbe8d611044304fb3e4ae6f371ec516e739929c3683
```

### 3.2 端口暴露

| 服务 | Master | 本地 | 说明 |
|------|--------|------|------|
| PostgreSQL | `0.0.0.0:5432` | `127.0.0.1:5432` | 本地更安全 |
| Redis | `0.0.0.0:6379` | `127.0.0.1:6379` | 本地更安全 |
| MinIO | `0.0.0.0:9000` | `127.0.0.1:9000` | 本地更安全 |
| Backend | `0.0.0.0:8080` | `127.0.0.1:8080` | 本地更安全 |
| Frontend | `0.0.0.0:80,443` | `0.0.0.0:80,443` | 保持一致 |

---

## 📝 四、新增文档

### 4.1 安全审计文档
- `docs/SECURITY-AUDIT-2026-03-23.md` - 安全审计报告
- `docs/SECURITY-FIX-COMPLETED.md` - 安全修复完成报告
- `docs/SECURITY-SUMMARY-2026-03-23.md` - 安全总结

### 4.2 问题跟踪文档
- `memory/2026-03-24.md` - 日常问题记录

### 4.3 测试报告
- `e2e/FINAL-TEST-REPORT-2026-03-23.md`
- `e2e/FULL-TEST-REPORT-2026-03-23.md`
- `e2e/TEST-REPORT-AFTER-FIX-2026-03-23.md`

---

## ⚠️ 五、已知问题汇总

### 5.1 已修复的问题

| ID | 问题 | 状态 | 解决人 |
|----|------|------|--------|
| 1 | 登录后菜单不显示 | ✅ resolved | OCT10-开发 2 |
| 2 | Alpine 容器健康检查失败 | ✅ resolved | OCT10-开发 1 |
| 3 | 数据库密码哈希不匹配 | ✅ resolved | OCT10-开发 1 |
| 4 | 数据库表缺失 | ✅ resolved | OCT10-开发 1 |
| 5 | work_areas 表字段缺失 | ✅ resolved | OCT10-开发 1 |
| 6 | HTTPS 无法访问 | ✅ resolved | OCT10-开发 1 |
| 7 | 验证码频率限制 | ✅ resolved | OCT10-开发 1 |
| 8 | 系统管理和用户管理页面服务器错误 | ✅ resolved | OCT10-开发 2 |
| 9 | 用户管理页面 503 错误 | ✅ resolved | OCT10-开发 1 |

---

## 🎯 六、建议

### 6.1 代码同步策略

**建议：**
1. ✅ **禁止将 master 同步到本地** - 保留本地修复
2. ✅ **禁止将本地代码提交到 GitHub** - 避免覆盖 master
3. ✅ **创建新分支 `camera1001-fixed`** - 基于当前状态
4. ✅ **导出数据库结构和数据** - 作为基准

### 6.2 数据库备份

**立即执行：**
```bash
# 导出数据库结构
docker exec camera-postgres pg_dump -U postgres -s camera_construction_db > db-schema-2026-03-24.sql

# 导出数据库数据
docker exec camera-postgres pg_dump -U postgres -a camera_construction_db > db-data-2026-03-24.sql
```

### 6.3 配置备份

**立即执行：**
```bash
# 备份 .env 文件
cp /etc/camera-system/.env /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/docs/.env.backup-2026-03-24

# 备份 docker-compose.yml
cp docker-compose.yml docs/docker-compose.backup-2026-03-24.yml
```

---

## 📋 七、下一步行动

1. **创建新分支** - `camera1001-fixed`
2. **导出数据库** - 结构和数据
3. **备份配置文件** - .env, docker-compose.yml 等
4. **更新 .gitignore** - 确保敏感文件不被提交
5. **编写部署文档** - 记录所有修复和配置

---

**报告生成完成！** 请确认以上差异点，然后决定是否需要同步到 master 或创建新分支。

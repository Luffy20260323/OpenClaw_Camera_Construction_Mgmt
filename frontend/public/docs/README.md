# 文档中心 V4.3 HTML 文档挂载说明

## 文件位置

- **HTML 文档**: `/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/frontend/public/docs/permission-design-v4.3.html`
- **文档大小**: 85 KB
- **生成时间**: 2026-04-04 11:46

## 访问方式

### 1. 通过文档中心页面访问

1. 登录系统
2. 进入 **系统管理** → **文档中心** (`/system/docs`)
3. 在文档列表中找到 **"权限设计方案 V4.3"**
4. 点击 **"查看"** 按钮即可在线阅读

### 2. 直接 URL 访问

```
http://<your-domain>/docs/permission-design-v4.3.html
```

## 文档内容

**标题**: Camera 项目权限设计方案 V4.3

**版本历史**:
| 版本 | 日期 | 修订内容 |
|------|------|----------|
| V4.3 | 2026-04-04 | 资源与权限分离设计 + 审计日志 + 软删除 |
| V4.2 | 2026-04-04 | 资源与权限合并设计 |
| V4.1 | 2026-04-04 | 合并 resource 表和 permission 表 |
| V4.0 | 2026-04-04 | 权限驱动开发流程规范（8 步流程） |

**主要章节**:
1. 核心理念
2. 权限驱动开发流程（8 步）
3. 资源与权限模型
4. 权限码命名规范
5. 数据库表结构
6. 两种管理员角色设计
7. 数据范围权限设计
8. 权限继承与冲突处理
9. 后端实现方案
10. 前端实现方案
11. 权限缓存机制
12. 审计日志设计
13. 实施步骤

## 文档特点

- ✅ **自动生成目录**: 页面加载时自动生成章节导航
- ✅ **响应式设计**: 适配桌面和移动设备
- ✅ **代码高亮**: SQL、Java、JavaScript 代码块格式化显示
- ✅ **表格样式**: 美观的表格展示
- ✅ **链接跳转**: 点击目录可跳转到对应章节

## 技术实现

### Markdown 转 HTML 脚本

```bash
node /root/.openclaw/workspace/generate-doc.js
```

### 输出路径

```
输入：/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/docs/permission-design-v4.3.md
输出：/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/frontend/public/docs/permission-design-v4.3.html
```

## 文档中心集成

### 预置文档列表

在 `Documents.vue` 中已添加预置文档数据：

```javascript
const presetDocuments = [
  {
    id: 'v4.3',
    title: '权限设计方案 V4.3',
    description: '资源与权限分离设计 + 审计日志 + 软删除机制',
    category: 'design',
    fileType: 'html',
    filename: 'permission-design-v4.3.html',
    featured: true,  // 推荐文档
    tags: ['权限', '设计', 'V4.3']
  },
  // ... 其他文档
]
```

### 本地文档 URL 处理

```javascript
const getDocUrl = (filename, isDownload = false) => {
  // 本地预置文档
  const localDocs = ['permission-design-v4.3.html', ...]
  if (localDocs.includes(filename)) {
    return `/docs/${filename}`
  }
  
  // 服务器文档
  return `${baseUrl}/system/docs/${action}/${filename}`
}
```

## 后续维护

### 添加新文档

1. 将 Markdown 文件放入 `backend/docs/` 目录
2. 运行转换脚本生成 HTML:
   ```bash
   node /root/.openclaw/workspace/generate-doc.js
   ```
3. 在 `Documents.vue` 的 `presetDocuments` 数组中添加文档元数据

### 更新现有文档

1. 修改源 Markdown 文件
2. 重新运行转换脚本
3. HTML 文件会自动更新（Vite 开发服务器会自动热重载）

## 文件清单

```
frontend/public/docs/
└── permission-design-v4.3.html    # V4.3 HTML 文档

backend/docs/
├── permission-design-v4.3.md      # V4.3 源文件
├── permission-design-v4.2.md      # V4.2 源文件
├── permission-design-v4.1.md      # V4.1 源文件
├── permission-design-v4.md        # V4.0 源文件
└── md-to-html.js                  # 转换脚本（备用）

workspace/
└── generate-doc.js                # 主转换脚本
```

## 访问统计

文档中心会记录以下数据（前端模拟）：
- 下载次数
- 查看次数（待实现）
- 最后访问时间（待实现）

---

**创建人**: AI Assistant  
**创建日期**: 2026-04-04  
**状态**: ✅ 已完成

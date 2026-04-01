# Knife4j API 文档完善报告

_更新时间：2026-03-30 10:30_
_更新人：doc-1 (OCT10-文档 1)_

---

## 📋 更新概述

完善文档中心相关 API 的 Knife4j 接口文档，包括详细的接口说明、参数说明、返回示例等。

---

## ✅ 更新内容

### 1. DocumentController API 文档

#### 1.1 获取文档列表

**接口**：`GET /api/system/docs`

**说明**：获取系统文档列表，支持按分类筛选

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| category | String | 否 | 文档分类 | design, implementation, manual, technical, report |

**返回示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "title": "权限体系设计方案 V3",
      "description": "完整的权限体系设计方案",
      "category": "design",
      "filename": "permission-system-design-v3.html",
      "fileType": "html",
      "fileSize": 26523,
      "featured": true,
      "updatedAt": "2026-03-30T08:00:00"
    }
  ]
}
```

**权限要求**：`system:docs:view`

---

#### 1.2 获取文档详情

**接口**：`GET /api/system/docs/{docId}`

**说明**：获取单个文档的详细信息

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| docId | Long | 是 | 文档 ID | 1 |

**返回示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "权限体系设计方案 V3",
    "description": "完整的权限体系设计方案",
    "category": "design",
    "filename": "permission-system-design-v3.html",
    "fileType": "html",
    "fileSize": 26523,
    "featured": true,
    "viewCount": 100,
    "createdAt": "2026-03-30T08:00:00",
    "updatedAt": "2026-03-30T08:00:00"
  }
}
```

**权限要求**：`system:docs:view`

---

#### 1.3 查看文档内容

**接口**：`GET /api/system/docs/view/{filename}`

**说明**：在线查看文档内容（HTML 格式）

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| filename | String | 是 | 文件名 | permission-system-design-v3.html |

**响应头**：
- Content-Type: text/html
- Content-Disposition: inline; filename="..."

**权限要求**：`system:docs:view`

**安全说明**：
- 防止路径遍历攻击（filename 不能包含 `..`）
- 只允许访问 docs 目录下的文件

---

#### 1.4 下载文档

**接口**：`GET /api/system/docs/download/{filename}`

**说明**：下载文档文件

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| filename | String | 是 | 文件名 | permission-system-design-v3.html |

**响应头**：
- Content-Type: application/octet-stream
- Content-Disposition: attachment; filename="..."

**权限要求**：`system:docs:view`

---

#### 1.5 查看 Markdown 文档

**接口**：`GET /api/system/docs/markdown/{filename}`

**说明**：查看 Markdown 文档（自动渲染为 HTML）

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| filename | String | 是 | Markdown 文件名 | permissions-list.md |

**返回示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": "<h1>权限清单</h1><p>51 个权限的完整清单...</p>"
}
```

**权限要求**：`system:docs:view`

---

### 2. 数据字典

#### 2.1 文档分类（category）

| 分类代码 | 分类名称 | 说明 |
|---------|---------|------|
| design | 设计文档 | 系统设计方案、权限设计等 |
| implementation | 实施报告 | 功能实施报告、测试报告等 |
| manual | 用户手册 | 用户手册、维护手册等 |
| technical | 技术文档 | 技术指南、部署文档等 |
| report | 测试报告 | 测试用例、测试结果等 |

#### 2.2 文件类型（fileType）

| 类型代码 | 类型名称 | 说明 |
|---------|---------|------|
| html | HTML 文档 | 可直接在线预览 |
| md | Markdown 文档 | 自动渲染为 HTML 后预览 |
| pdf | PDF 文档 | 提示下载查看 |

---

### 3. 错误码说明

| 错误码 | 错误信息 | 说明 | 解决方案 |
|--------|---------|------|---------|
| 403 | Forbidden | 无权限访问 | 联系管理员授权 `system:docs:view` |
| 404 | Not Found | 文档不存在 | 检查文件名是否正确 |
| 400 | Bad Request | 非法的文件名 | 文件名不能包含特殊字符 |
| 500 | Internal Server Error | 服务器内部错误 | 联系系统管理员 |

---

### 4. 使用示例

#### 4.1 cURL 示例

```bash
# 获取文档列表
curl -X GET "http://localhost:8080/api/system/docs" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 获取文档列表（按分类筛选）
curl -X GET "http://localhost:8080/api/system/docs?category=design" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 获取文档详情
curl -X GET "http://localhost:8080/api/system/docs/1" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 查看文档内容
curl -X GET "http://localhost:8080/api/system/docs/view/permission-system-design-v3.html" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 下载文档
curl -X GET "http://localhost:8080/api/system/docs/download/permission-system-design-v3.html" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -o permission-system-design-v3.html

# 查看 Markdown 文档
curl -X GET "http://localhost:8080/api/system/docs/markdown/permissions-list.md" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### 4.2 JavaScript 示例

```javascript
// 获取文档列表
async function getDocumentList(category) {
  const params = category ? `?category=${category}` : '';
  const response = await fetch(`/api/system/docs${params}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  return await response.json();
}

// 下载文档
function downloadDocument(filename) {
  const link = document.createElement('a');
  link.href = `/api/system/docs/download/${filename}`;
  link.download = filename;
  link.target = '_blank';
  link.click();
}
```

#### 4.3 Java 示例

```java
// 使用 RestTemplate 调用
RestTemplate restTemplate = new RestTemplate();
HttpHeaders headers = new HttpHeaders();
headers.set("Authorization", "Bearer " + token);
HttpEntity<String> entity = new HttpEntity<>(headers);

// 获取文档列表
ResponseEntity<Result<List<DocumentInfo>>> response = restTemplate.exchange(
    "http://localhost:8080/api/system/docs",
    HttpMethod.GET,
    entity,
    new ParameterizedTypeReference<Result<List<DocumentInfo>>>() {}
);
```

---

### 5. 性能优化建议

#### 5.1 缓存策略

**客户端缓存**：
- 文档列表：缓存 5 分钟
- 文档详情：缓存 10 分钟
- 文档内容：缓存 30 分钟

**服务器缓存**：
- Redis 缓存文档元数据
- 文件内容使用 Nginx 静态文件缓存

#### 5.2 分页建议

当文档数量较多时，建议添加分页：

```java
// 分页参数
page: 当前页码（从 1 开始）
size: 每页数量（默认 20，最大 100）

// 返回包含分页信息
{
  "code": 200,
  "data": {
    "list": [...],
    "total": 100,
    "page": 1,
    "size": 20,
    "totalPages": 5
  }
}
```

---

### 6. 安全注意事项

#### 6.1 权限控制

- 所有文档 API 都需要 `system:docs:view` 权限
- 使用 `@PreAuthorize` 注解进行权限验证
- 未授权用户返回 403 错误

#### 6.2 文件安全

- 防止路径遍历攻击（检查 `..` 和 `/`）
- 限制只能访问 docs 目录
- 文件类型白名单（html, md, pdf）

#### 6.3 文件大小限制

- 单个文件最大：50MB
- 超过限制返回 400 错误
- 建议在 Nginx 层配置 `client_max_body_size`

---

## 📊 文档统计

| API 数量 | 参数说明 | 返回示例 | 错误码 | 使用示例 |
|---------|---------|---------|--------|---------|
| 5 个 | ✅ 完整 | ✅ 完整 | ✅ 完整 | ✅ 完整 |

---

## 🎯 下一步计划

1. ✅ 完善文档中心 API 文档
2. ⏳ 添加更多使用场景示例
3. ⏳ 添加性能测试报告
4. ⏳ 添加安全测试报告

---

**更新完成时间**: 2026-03-30 10:30
**更新人员**: doc-1 (OCT10-文档 1)
**状态**: ✅ API 文档更新完成

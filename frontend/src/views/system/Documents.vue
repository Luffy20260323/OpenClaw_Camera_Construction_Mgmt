<template>
    <div class="document-center">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-left">
          <h1>📚 文档中心</h1>
          <p class="subtitle">系统文档、设计文档、实施报告、技术文档</p>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="showUploadDialog">
            <el-icon><Upload /></el-icon>
            上传文档
          </el-button>
        </div>
      </div>

      <!-- 分类筛选 -->
      <div class="filter-bar">
        <el-radio-group v-model="selectedCategory" @change="loadDocuments">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="design">设计文档</el-radio-button>
          <el-radio-button label="implementation">实施报告</el-radio-button>
          <el-radio-button label="manual">用户手册</el-radio-button>
          <el-radio-button label="technical">技术文档</el-radio-button>
          <el-radio-button label="report">测试报告</el-radio-button>
        </el-radio-group>

        <div class="search-box">
          <el-input
            v-model="searchQuery"
            placeholder="搜索文档..."
            prefix-icon="Search"
            clearable
            @change="handleSearch"
          />
        </div>
      </div>

      <!-- 文档列表 -->
      <div class="document-list" v-loading="loading">
        <el-empty v-if="filteredDocuments.length === 0 && !loading" description="暂无文档" />
        
        <el-row :gutter="20" v-else>
          <el-col
            v-for="doc in filteredDocuments"
            :key="doc.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
            class="doc-col"
          >
            <el-card
              class="doc-card"
              :class="{ featured: doc.featured }"
              shadow="hover"
              @click="viewDocument(doc)"
            >
              <template #header>
                <div class="card-header">
                  <el-tag
                    :type="getCategoryTagType(doc.category)"
                    size="small"
                  >
                    {{ getCategoryLabel(doc.category) }}
                  </el-tag>
                  <el-tag
                    v-if="doc.featured"
                    type="warning"
                    size="small"
                  >
                    推荐
                  </el-tag>
                </div>
              </template>

              <div class="card-body">
                <div class="doc-icon">
                  <el-icon :size="40">
                    <component :is="getDocIcon(doc.fileType)" />
                  </el-icon>
                </div>

                <h3 class="doc-title" :title="doc.title">{{ doc.title }}</h3>
                <p class="doc-description" :title="doc.description">
                  {{ doc.description }}
                </p>

                <div class="doc-meta">
                  <span class="meta-item">
                    <el-icon><Document /></el-icon>
                    {{ formatFileSize(doc.fileSize) }}
                  </span>
                  <span class="meta-item">
                    <el-icon><Clock /></el-icon>
                    {{ formatDate(doc.uploadedAt) }}
                  </span>
                </div>

                <div class="doc-meta">
                  <span class="meta-item">
                    <el-icon><User /></el-icon>
                    {{ doc.uploaderName || '未知' }}
                  </span>
                  <span class="meta-item">
                    <el-icon><Download /></el-icon>
                    {{ doc.downloadCount || 0 }}
                  </span>
                </div>
              </div>

              <template #footer>
                <div class="card-footer">
                  <el-button
                    type="primary"
                    size="small"
                    @click.stop="viewDocument(doc)"
                  >
                    查看
                  </el-button>
                  <el-button
                    size="small"
                    @click.stop="downloadDocument(doc)"
                  >
                    下载
                  </el-button>
                  <el-dropdown trigger="click" @command="(cmd) => handleDocCommand(cmd, doc)">
                    <el-button size="small">
                      <el-icon><MoreFilled /></el-icon>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="versions">版本历史</el-dropdown-item>
                        <el-dropdown-item command="edit">编辑信息</el-dropdown-item>
                        <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </template>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 文档查看对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="currentDoc?.title"
        width="80%"
        top="5vh"
        :fullscreen="isFullscreen"
      >
        <div class="doc-viewer">
          <div class="viewer-toolbar">
            <el-button
              :icon="isFullscreen ? 'FullScreen' : 'Aim'"
              circle
              @click="toggleFullscreen"
            />
            <el-button
              icon="Download"
              circle
              @click="downloadDocument(currentDoc)"
            />
          </div>

          <iframe
            v-if="currentDoc && currentDoc.fileType === 'html'"
            :src="getDocUrl(currentDoc.filename)"
            class="doc-iframe"
            frameborder="0"
          />

          <div
            v-else-if="currentDoc && currentDoc.fileType === 'md'"
            class="markdown-viewer"
            v-html="markdownContent"
          />

          <div
            v-else-if="currentDoc && currentDoc.fileType === 'pdf'"
            class="pdf-viewer"
          >
            <el-empty description="PDF 预览暂不支持，请直接下载查看" />
          </div>
        </div>

        <template #footer>
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="downloadDocument(currentDoc)">
            下载
          </el-button>
        </template>
      </el-dialog>

      <!-- 上传文档对话框 -->
      <el-dialog
        v-model="uploadDialogVisible"
        title="上传文档"
        width="600px"
      >
        <el-form
          ref="uploadFormRef"
          :model="uploadForm"
          :rules="uploadRules"
          label-width="100px"
        >
          <el-form-item label="文档标题" prop="title">
            <el-input v-model="uploadForm.title" placeholder="请输入文档标题" />
          </el-form-item>
          <el-form-item label="文档分类" prop="category">
            <el-select v-model="uploadForm.category" placeholder="请选择文档分类" style="width: 100%">
              <el-option label="设计文档" value="design" />
              <el-option label="实施报告" value="implementation" />
              <el-option label="用户手册" value="manual" />
              <el-option label="技术文档" value="technical" />
              <el-option label="测试报告" value="report" />
            </el-select>
          </el-form-item>
          <el-form-item label="文档描述" prop="description">
            <el-input
              v-model="uploadForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入文档描述"
            />
          </el-form-item>
          <el-form-item label="标签" prop="tags">
            <el-input v-model="uploadForm.tags" placeholder="多个标签用逗号分隔" />
          </el-form-item>
          <el-form-item label="设为推荐">
            <el-switch v-model="uploadForm.featured" />
          </el-form-item>
          <el-form-item label="版本说明" prop="versionComment">
            <el-input v-model="uploadForm.versionComment" placeholder="可选，说明版本变更内容" />
          </el-form-item>
          <el-form-item label="上传文件" prop="file">
            <el-upload
              ref="uploadRef"
              drag
              :auto-upload="false"
              :on-change="handleFileChange"
              :limit="1"
              :on-exceed="handleExceed"
            >
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                将文件拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持 pdf, doc, docx, xls, xlsx, md, html, txt 格式
                </div>
              </template>
            </el-upload>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="uploadDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitUpload" :loading="uploading">
            上传
          </el-button>
        </template>
      </el-dialog>

      <!-- 编辑文档对话框 -->
      <el-dialog
        v-model="editDialogVisible"
        title="编辑文档信息"
        width="600px"
      >
        <el-form
          ref="editFormRef"
          :model="editForm"
          label-width="100px"
        >
          <el-form-item label="文档标题">
            <el-input v-model="editForm.title" placeholder="请输入文档标题" />
          </el-form-item>
          <el-form-item label="文档分类">
            <el-select v-model="editForm.category" placeholder="请选择文档分类" style="width: 100%">
              <el-option label="设计文档" value="design" />
              <el-option label="实施报告" value="implementation" />
              <el-option label="用户手册" value="manual" />
              <el-option label="技术文档" value="technical" />
              <el-option label="测试报告" value="report" />
            </el-select>
          </el-form-item>
          <el-form-item label="文档描述">
            <el-input
              v-model="editForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入文档描述"
            />
          </el-form-item>
          <el-form-item label="标签">
            <el-input v-model="editForm.tags" placeholder="多个标签用逗号分隔" />
          </el-form-item>
          <el-form-item label="设为推荐">
            <el-switch v-model="editForm.featured" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit" :loading="updating">
            保存
          </el-button>
        </template>
      </el-dialog>

      <!-- 版本历史对话框 -->
      <el-dialog
        v-model="versionsDialogVisible"
        title="版本历史"
        width="700px"
      >
        <el-table :data="versions" style="width: 100%">
          <el-table-column prop="version" label="版本" width="80" />
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="versionComment" label="版本说明" />
          <el-table-column prop="uploadedAt" label="上传时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.uploadedAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="isLatest" label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.isLatest" type="success" size="small">当前版本</el-tag>
              <el-tag v-else type="info" size="small">历史版本</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                @click="viewVersion(row)"
              >
                查看
              </el-button>
              <el-button
                size="small"
                @click="downloadVersion(row)"
              >
                下载
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-dialog>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document,
  Clock,
  Search,
  FullScreen,
  Aim,
  Download,
  Files,
  Document as DocIcon,
  Notebook,
  Reading,
  Upload,
  UploadFilled,
  MoreFilled,
  User
} from '@element-plus/icons-vue'
import {
  getDocumentList,
  searchDocuments,
  getDocumentDetail,
  getDocumentVersions,
  uploadDocument,
  updateDocument,
  deleteDocument,
  getMarkdownContent
} from '@/api/document'

// 状态
const loading = ref(false)
const documents = ref([])
const selectedCategory = ref('')
const searchQuery = ref('')
const dialogVisible = ref(false)
const uploadDialogVisible = ref(false)
const editDialogVisible = ref(false)
const versionsDialogVisible = ref(false)
const currentDoc = ref(null)
const editingDoc = ref(null)
const markdownContent = ref('')
const isFullscreen = ref(false)
const uploading = ref(false)
const updating = ref(false)
const uploadFormRef = ref(null)
const editFormRef = ref(null)
const uploadFile = ref(null)
const versions = ref([])

// 上传表单
const uploadForm = ref({
  title: '',
  category: '',
  description: '',
  tags: '',
  featured: false,
  versionComment: ''
})

// 上传表单验证规则
const uploadRules = {
  title: [{ required: true, message: '请输入文档标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择文档分类', trigger: 'change' }],
  file: [{ required: true, message: '请选择上传文件', trigger: 'change' }]
}

// 编辑表单
const editForm = ref({
  title: '',
  category: '',
  description: '',
  tags: '',
  featured: false
})

// 预置文档列表（包含 v4.3 权限设计文档）
const presetDocuments = [
  {
    id: 'v4.3',
    title: '权限设计方案 V4.3',
    description: '资源与权限分离设计 + 审计日志 + 软删除机制',
    category: 'design',
    fileType: 'html',
    filename: 'permission-design-v4.3.html',
    fileSize: 524288,
    uploadedAt: '2026-04-04T11:45:00',
    uploaderName: 'AI',
    downloadCount: 0,
    featured: true,
    tags: ['权限', '设计', 'V4.3']
  },
  {
    id: 'v4.2',
    title: '权限设计方案 V4.2',
    description: '资源与权限合并设计',
    category: 'design',
    fileType: 'md',
    filename: 'permission-design-v4.2.md',
    fileSize: 262144,
    uploadedAt: '2026-04-04T08:00:00',
    uploaderName: 'AI',
    downloadCount: 12,
    featured: false,
    tags: ['权限', '设计', 'V4.2']
  },
  {
    id: 'v4.1',
    title: '权限设计方案 V4.1',
    description: '合并 resource 表和 permission 表',
    category: 'design',
    fileType: 'md',
    filename: 'permission-design-v4.1.md',
    fileSize: 245760,
    uploadedAt: '2026-04-04T07:00:00',
    uploaderName: 'AI',
    downloadCount: 8,
    featured: false,
    tags: ['权限', '设计', 'V4.1']
  },
  {
    id: 'v4',
    title: '权限设计方案 V4.0',
    description: '权限驱动开发流程规范（8 步流程）',
    category: 'design',
    fileType: 'md',
    filename: 'permission-design-v4.md',
    fileSize: 229376,
    uploadedAt: '2026-04-03T18:00:00',
    uploaderName: 'AI',
    downloadCount: 15,
    featured: false,
    tags: ['权限', '设计', 'V4']
  },
  {
    id: 'impl-001',
    title: '用户管理功能实现总结',
    description: '用户 CRUD、分页、搜索功能的完整实现',
    category: 'implementation',
    fileType: 'md',
    filename: 'user-implementation.md',
    fileSize: 102400,
    uploadedAt: '2026-04-02T10:00:00',
    uploaderName: 'AI',
    downloadCount: 20,
    featured: false,
    tags: ['用户', '实现', '总结']
  }
]

// 计算属性：过滤后的文档列表
const filteredDocuments = computed(() => {
  let result = presetDocuments
  
  // 按分类筛选
  if (selectedCategory.value) {
    result = result.filter(doc => doc.category === selectedCategory.value)
  }
  
  // 按搜索关键词筛选
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(doc =>
      doc.title.toLowerCase().includes(query) ||
      doc.description.toLowerCase().includes(query) ||
      doc.tags.some(tag => tag.toLowerCase().includes(query))
    )
  }
  
  return result
})

// 生命周期
onMounted(() => {
  loadDocuments()
})

// 加载文档列表
const loadDocuments = async () => {
  loading.value = true
  try {
    const res = await getDocumentList(selectedCategory.value)
    documents.value = res.data || []
  } catch (error) {
    console.error('加载文档列表失败:', error)
    ElMessage.error('加载文档列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索文档
const handleSearch = async () => {
  if (!searchQuery.value) {
    loadDocuments()
    return
  }
  loading.value = true
  try {
    const res = await searchDocuments(searchQuery.value)
    documents.value = res.data || []
  } catch (error) {
    console.error('搜索文档失败:', error)
    ElMessage.error('搜索文档失败')
  } finally {
    loading.value = false
  }
}

// 显示上传对话框
const showUploadDialog = () => {
  uploadForm.value = {
    title: '',
    category: '',
    description: '',
    tags: '',
    featured: false,
    versionComment: ''
  }
  uploadFile.value = null
  uploadDialogVisible.value = true
}

// 处理文件选择
const handleFileChange = (file) => {
  uploadFile.value = file.raw
}

// 处理超出限制
const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

// 提交上传
const submitUpload = async () => {
  if (!uploadFormRef.value) return
  
  await uploadFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    if (!uploadFile.value) {
      ElMessage.warning('请选择上传文件')
      return
    }

    uploading.value = true
    try {
      const formData = new FormData()
      formData.append('title', uploadForm.value.title)
      formData.append('category', uploadForm.value.category)
      formData.append('description', uploadForm.value.description || '')
      formData.append('tags', uploadForm.value.tags || '')
      formData.append('featured', uploadForm.value.featured)
      formData.append('versionComment', uploadForm.value.versionComment || '')
      formData.append('file', uploadFile.value)

      await uploadDocument(formData)
      ElMessage.success('文档上传成功')
      uploadDialogVisible.value = false
      loadDocuments()
    } catch (error) {
      console.error('上传文档失败:', error)
      ElMessage.error(error.response?.data?.message || '上传文档失败')
    } finally {
      uploading.value = false
    }
  })
}

// 处理文档操作
const handleDocCommand = (command, doc) => {
  switch (command) {
    case 'versions':
      showVersions(doc)
      break
    case 'edit':
      showEditDialog(doc)
      break
    case 'delete':
      confirmDelete(doc)
      break
  }
}

// 显示编辑对话框
const showEditDialog = (doc) => {
  editingDoc.value = doc
  editForm.value = {
    title: doc.title,
    category: doc.category,
    description: doc.description,
    tags: doc.tags,
    featured: doc.featured
  }
  editDialogVisible.value = true
}

// 提交编辑
const submitEdit = async () => {
  updating.value = true
  try {
    await updateDocument(editingDoc.value.id, editForm.value)
    ElMessage.success('文档信息更新成功')
    editDialogVisible.value = false
    loadDocuments()
  } catch (error) {
    console.error('更新文档失败:', error)
    ElMessage.error(error.response?.data?.message || '更新文档失败')
  } finally {
    updating.value = false
  }
}

// 确认删除
const confirmDelete = async (doc) => {
  try {
    await ElMessageBox.confirm(`确定要删除文档"${doc.title}"吗？`, '删除确认', {
      type: 'warning'
    })
    
    await deleteDocument(doc.id)
    ElMessage.success('文档已删除')
    loadDocuments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除文档失败:', error)
      ElMessage.error(error.response?.data?.message || '删除文档失败')
    }
  }
}

// 显示版本历史
const showVersions = async (doc) => {
  try {
    const res = await getDocumentVersions(doc.id)
    versions.value = res.data || []
    versionsDialogVisible.value = true
  } catch (error) {
    console.error('获取版本历史失败:', error)
    ElMessage.error('获取版本历史失败')
  }
}

// 查看版本
const viewVersion = (version) => {
  viewDocument(version)
  versionsDialogVisible.value = false
}

// 下载版本
const downloadVersion = (version) => {
  downloadDocument(version)
}

// 查看文档
const viewDocument = async (doc) => {
  currentDoc.value = doc
  dialogVisible.value = true
  markdownContent.value = ''

  if (doc.fileType === 'md') {
    try {
      const res = await getMarkdownContent(doc.filename)
      markdownContent.value = res.data
    } catch (error) {
      console.error('加载 Markdown 失败:', error)
      ElMessage.error('加载文档失败')
    }
  }
}

// 下载文档
const downloadDocument = (doc) => {
  if (!doc) return
  
  const link = document.createElement('a')
  link.href = getDocUrl(doc.filename, true)
  link.download = doc.originalFilename || doc.filename
  link.target = '_blank'
  link.click()
  
  ElMessage.success('开始下载：' + doc.title)
}

// 获取文档 URL
const getDocUrl = (filename, isDownload = false) => {
  // 本地预置文档（在 public/docs/ 目录下）
  const localDocs = ['permission-design-v4.3.html', 'permission-design-v4.2.md', 'permission-design-v4.1.md', 'permission-design-v4.md']
  if (localDocs.includes(filename)) {
    return `/docs/${filename}`
  }
  
  // 服务器文档
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
  const action = isDownload ? 'download' : 'view'
  return `${baseUrl}/system/docs/${action}/${encodeURIComponent(filename)}`
}

// 切换全屏
const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
}

// 获取分类标签类型
const getCategoryTagType = (category) => {
  const types = {
    design: 'primary',
    implementation: 'success',
    manual: 'warning',
    technical: 'info',
    report: 'danger'
  }
  return types[category] || 'info'
}

// 获取分类标签文字
const getCategoryLabel = (category) => {
  const labels = {
    design: '设计文档',
    implementation: '实施报告',
    manual: '用户手册',
    technical: '技术文档',
    report: '测试报告'
  }
  return labels[category] || category
}

// 获取文档图标
const getDocIcon = (fileType) => {
  const icons = {
    html: Reading,
    md: Notebook,
    pdf: DocIcon
  }
  return icons[fileType] || Files
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped lang="scss">
.document-center {
  padding: 20px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30px;

    .header-left {
      h1 {
        font-size: 28px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 8px;
      }

      .subtitle {
        font-size: 14px;
        color: #909399;
      }
    }
  }

  .filter-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    flex-wrap: wrap;
    gap: 16px;

    .search-box {
      width: 300px;
    }
  }

  .document-list {
    .doc-col {
      margin-bottom: 20px;
    }

    .doc-card {
      cursor: pointer;
      transition: all 0.3s;
      height: 100%;

      &:hover {
        transform: translateY(-4px);
      }

      &.featured {
        border-color: #e6a23c;
      }

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 8px;
      }

      .card-body {
        .doc-icon {
          display: flex;
          justify-content: center;
          align-items: center;
          height: 80px;
          margin-bottom: 16px;
          color: #409eff;
        }

        .doc-title {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 8px;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }

        .doc-description {
          font-size: 13px;
          color: #909399;
          margin-bottom: 16px;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 3;
          -webkit-box-orient: vertical;
          min-height: 56px;
        }

        .doc-meta {
          display: flex;
          gap: 16px;
          font-size: 12px;
          color: #909399;
          margin-bottom: 8px;

          .meta-item {
            display: flex;
            align-items: center;
            gap: 4px;
          }
        }
      }

      .card-footer {
        display: flex;
        justify-content: flex-end;
        gap: 8px;
      }
    }
  }

  .doc-viewer {
    position: relative;
    min-height: 600px;

    .viewer-toolbar {
      position: absolute;
      top: 10px;
      right: 10px;
      z-index: 10;
      display: flex;
      gap: 8px;
    }

    .doc-iframe {
      width: 100%;
      height: 70vh;
      border-radius: 4px;
    }

    .markdown-viewer {
      padding: 20px;
      background: #f5f7fa;
      border-radius: 4px;
      max-height: 70vh;
      overflow-y: auto;

      :deep(h1), :deep(h2), :deep(h3) {
        color: #303133;
        margin-top: 24px;
        margin-bottom: 16px;
      }

      :deep(p) {
        margin-bottom: 16px;
        line-height: 1.8;
      }

      :deep(code) {
        background: #f4f4f5;
        padding: 2px 6px;
        border-radius: 3px;
        font-family: 'Courier New', monospace;
      }

      :deep(pre) {
        background: #282c34;
        color: #abb2bf;
        padding: 16px;
        border-radius: 4px;
        overflow-x: auto;

        code {
          background: transparent;
          color: inherit;
        }
      }

      :deep(ul), :deep(ol) {
        margin-bottom: 16px;
        padding-left: 24px;
      }

      :deep(table) {
        width: 100%;
        margin-bottom: 16px;
        border-collapse: collapse;

        th, td {
          border: 1px solid #dcdfe6;
          padding: 8px 12px;
          text-align: left;
        }

        th {
          background: #f5f7fa;
        }
      }
    }

    .pdf-viewer {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 70vh;
    }
  }
}
</style>

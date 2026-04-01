<template>
  <AdminLayout>
    <div class="document-center">
      <!-- 页面头部 -->
      <div class="page-header">
        <h1>📚 文档中心</h1>
        <p class="subtitle">系统文档、设计文档、实施报告、技术文档</p>
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
            @input="filterDocuments"
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
                    {{ formatDate(doc.updatedAt) }}
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
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
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
  Reading
} from '@element-plus/icons-vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { getDocumentList, getMarkdownContent } from '@/api/document'

// 状态
const loading = ref(false)
const documents = ref([])
const selectedCategory = ref('')
const searchQuery = ref('')
const dialogVisible = ref(false)
const currentDoc = ref(null)
const markdownContent = ref('')
const isFullscreen = ref(false)

// 计算属性：过滤后的文档列表
const filteredDocuments = computed(() => {
  if (!searchQuery.value) {
    return documents.value
  }
  
  const query = searchQuery.value.toLowerCase()
  return documents.value.filter(doc =>
    doc.title.toLowerCase().includes(query) ||
    doc.description.toLowerCase().includes(query)
  )
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

// 过滤文档（搜索）
const filterDocuments = () => {
  // 搜索由 computed 属性处理
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
  link.download = doc.filename
  link.target = '_blank'
  link.click()
  
  ElMessage.success('开始下载：' + doc.title)
}

// 获取文档 URL
const getDocUrl = (filename, isDownload = false) => {
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
    day: '2-digit'
  })
}
</script>

<style scoped lang="scss">
.document-center {
  padding: 20px;

  .page-header {
    margin-bottom: 30px;

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

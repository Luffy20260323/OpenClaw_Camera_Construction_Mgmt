<template>
    <div class="element-list">
      <el-card class="header-card">
        <div class="page-header">
          <h2>ELEMENT 管理</h2>
          <p class="description">管理页面操作元素（按钮、链接等），支持 ELEMENT → 子 PAGE 的层级配置。权限码格式：module:resource:action:type</p>
        </div>
      </el-card>

      <el-card class="content-card">
        <!-- 工具栏 -->
        <div class="toolbar">
          <el-select
            v-model="selectedPageId"
            placeholder="选择页面"
            style="width: 250px"
            @change="loadElements"
          >
            <el-option label="全部页面" :value="null" />
            <el-option
              v-for="page in pageOptions"
              :key="page.id"
              :label="page.name"
              :value="page.id"
            />
          </el-select>

          <el-button type="primary" @click="showCreateDialog" style="margin-left: 12px">
            <el-icon><Plus /></el-icon>
            新建 ELEMENT
          </el-button>
          
          <el-button @click="loadElements" style="margin-left: 12px">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>

        <!-- ELEMENT 表格 -->
        <el-table
          :data="elements"
          v-loading="loading"
          stripe
          border
          style="margin-top: 20px"
        >
          <el-table-column prop="name" label="元素名称" width="180">
            <template #default="{ row }">
              <span class="name-text">
                <el-tag v-if="row.elementType" type="info" size="small" style="margin-right: 8px">
                  {{ row.elementType }}
                </el-tag>
                {{ row.name }}
              </span>
            </template>
          </el-table-column>
          
          <el-table-column prop="code" label="元素编码" width="200">
            <template #default="{ row }">
              <span class="code-text">{{ row.code }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="pageName" label="所属页面" width="150">
            <template #default="{ row }">
              {{ row.pageName || '-' }}
            </template>
          </el-table-column>
          
          <el-table-column prop="permissionKey" label="权限码" width="220">
            <template #default="{ row }">
              <span class="code-text" style="font-size: 12px">{{ row.permissionKey }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="childPageName" label="关联子页面" width="150">
            <template #default="{ row }">
              <span v-if="row.childPageName" class="success-text">{{ row.childPageName }}</span>
              <span v-else class="empty-text">-</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="sortOrder" label="顺序" width="80" align="center">
            <template #default="{ row }">
              {{ row.sortOrder }}
            </template>
          </el-table-column>
          
          <el-table-column prop="status" label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="description" label="描述" min-width="150">
            <template #default="{ row }">
              {{ row.description || '-' }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button 
                type="primary" 
                size="small" 
                link 
                @click="showEditDialog(row)"
              >
                编辑
              </el-button>
              <el-button 
                type="warning" 
                size="small" 
                link 
                @click="showChildPageDialog(row)"
              >
                关联页面
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                link 
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 新建/编辑对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑 ELEMENT' : '新建 ELEMENT'"
        width="600px"
        @close="resetForm"
      >
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="100px"
        >
          <el-form-item label="元素名称" prop="name">
            <el-input v-model="formData.name" placeholder="如：查看详情、新建用户" />
          </el-form-item>
          
          <el-form-item label="所属页面" prop="pageId">
            <el-select v-model="formData.pageId" placeholder="选择页面" style="width: 100%">
              <el-option
                v-for="page in pageOptions"
                :key="page.id"
                :label="page.name"
                :value="page.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="元素类型" prop="elementType">
            <el-select v-model="formData.elementType" placeholder="选择元素类型" style="width: 100%">
              <el-option label="按钮 (button)" value="button" />
              <el-option label="链接 (link)" value="link" />
              <el-option label="图标 (icon)" value="icon" />
              <el-option label="菜单 (menu)" value="menu" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="排序号" prop="sortOrder">
            <el-input-number v-model="formData.sortOrder" :min="0" :max="999" style="width: 100%" />
          </el-form-item>
          
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="描述" prop="description">
            <el-input
              v-model="formData.description"
              type="textarea"
              :rows="3"
              placeholder="元素描述信息"
            />
          </el-form-item>
          
          <el-form-item label="权限码">
            <el-input
              v-model="generatedPermissionKey"
              disabled
              placeholder="自动生成"
            />
            <div class="form-tip">权限码格式：module:resource:action:button</div>
          </el-form-item>
        </el-form>
        
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确定
          </el-button>
        </template>
      </el-dialog>

      <!-- 关联子页面对话框 -->
      <el-dialog
        v-model="childPageDialogVisible"
        title="关联子页面"
        width="500px"
      >
        <el-form label-width="100px">
          <el-form-item label="当前元素">
            <span>{{ currentElement?.name }}</span>
          </el-form-item>
          
          <el-form-item label="关联子页面">
            <el-select v-model="childPageFormData.childPageId" placeholder="选择子页面" style="width: 100%">
              <el-option
                v-for="page in availablePages"
                :key="page.id"
                :label="page.name"
                :value="page.id"
              />
            </el-select>
            <div class="form-tip">选择点击该元素后跳转的页面</div>
          </el-form-item>
        </el-form>
        
        <template #footer>
          <el-button @click="childPageDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleLinkChildPage" :loading="submitting">
            确定
          </el-button>
        </template>
      </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Refresh, Search, Edit, Delete, Link } from '@element-plus/icons-vue';
import request from '@/utils/request';

// 状态
const loading = ref(false);
const submitting = ref(false);
const elements = ref([]);
const pageOptions = ref([]);
const availablePages = ref([]);
const selectedPageId = ref(null);
const dialogVisible = ref(false);
const childPageDialogVisible = ref(false);
const isEdit = ref(false);
const currentElement = ref(null);
const formRef = ref(null);

// 表单数据
const formData = reactive({
  id: null,
  name: '',
  pageId: null,
  elementType: 'button',
  sortOrder: 0,
  status: 1,
  description: '',
  childPageId: null
});

const childPageFormData = reactive({
  childPageId: null
});

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入元素名称', trigger: 'blur' }],
  pageId: [{ required: true, message: '请选择所属页面', trigger: 'change' }],
  elementType: [{ required: true, message: '请选择元素类型', trigger: 'change' }]
};

// 生成的权限码
const generatedPermissionKey = computed(() => {
  if (!formData.name || !formData.pageId) return '';
  
  const action = extractAction(formData.name);
  const resource = extractResource(formData.name);
  const page = pageOptions.value.find(p => p.id === formData.pageId);
  const module = page?.moduleCode || 'system';
  
  return `${module}:${resource}:${action}:button`;
});

// 提取操作类型
function extractAction(name) {
  if (!name) return 'unknown';
  if (name.includes('查看') || name.includes('详情')) return 'view';
  if (name.includes('新建') || name.includes('创建')) return 'create';
  if (name.includes('编辑') || name.includes('修改')) return 'edit';
  if (name.includes('删除')) return 'delete';
  if (name.includes('导出')) return 'export';
  if (name.includes('导入')) return 'import';
  if (name.includes('上传')) return 'upload';
  if (name.includes('下载')) return 'download';
  if (name.includes('分配')) return 'assign';
  return 'unknown';
}

// 提取资源类型
function extractResource(name) {
  if (!name) return 'unknown';
  if (name.includes('用户')) return 'user';
  if (name.includes('角色')) return 'role';
  if (name.includes('资源')) return 'resource';
  if (name.includes('权限')) return 'permission';
  if (name.includes('文档')) return 'document';
  if (name.includes('菜单')) return 'menu';
  return 'unknown';
}

// 加载页面列表
async function loadPages() {
  try {
    const res = await request.get('/resource/type/PAGE');
    if (res.code === 0) {
      pageOptions.value = res.data || [];
      availablePages.value = res.data || [];
    }
  } catch (error) {
    console.error('加载页面列表失败:', error);
  }
}

// 加载 ELEMENT 列表
async function loadElements() {
  loading.value = true;
  try {
    const params = selectedPageId.value ? { pageId: selectedPageId.value } : {};
    const res = await request.get('/element/list', { params });
    if (res.code === 0) {
      elements.value = res.data || [];
    }
  } catch (error) {
    ElMessage.error('加载 ELEMENT 列表失败：' + error.message);
  } finally {
    loading.value = false;
  }
}

// 显示新建对话框
function showCreateDialog() {
  isEdit.value = false;
  dialogVisible.value = true;
}

// 显示编辑对话框
function showEditDialog(row) {
  isEdit.value = true;
  currentElement.value = row;
  formData.id = row.id;
  formData.name = row.name;
  formData.pageId = row.pageId;
  formData.elementType = row.elementType || 'button';
  formData.sortOrder = row.sortOrder || 0;
  formData.status = row.status;
  formData.description = row.description || '';
  formData.childPageId = row.childPageId;
  dialogVisible.value = true;
}

// 显示关联子页面对话框
function showChildPageDialog(row) {
  currentElement.value = row;
  childPageFormData.childPageId = row.childPageId;
  childPageDialogVisible.value = true;
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return;
    
    submitting.value = true;
    try {
      const data = {
        name: formData.name,
        pageId: formData.pageId,
        elementType: formData.elementType,
        sortOrder: formData.sortOrder,
        status: formData.status,
        description: formData.description,
        moduleCode: pageOptions.value.find(p => p.id === formData.pageId)?.moduleCode
      };
      
      if (isEdit.value) {
        await request.put(`/element/${formData.id}`, data);
        ElMessage.success('更新成功');
      } else {
        await request.post('/element', data);
        ElMessage.success('创建成功');
      }
      
      dialogVisible.value = false;
      loadElements();
    } catch (error) {
      ElMessage.error('操作失败：' + error.message);
    } finally {
      submitting.value = false;
    }
  });
}

// 关联子页面
async function handleLinkChildPage() {
  submitting.value = true;
  try {
    await request.put(`/element/${currentElement.value.id}`, {
      childPageId: childPageFormData.childPageId
    });
    ElMessage.success('关联成功');
    childPageDialogVisible.value = false;
    loadElements();
  } catch (error) {
    ElMessage.error('关联失败：' + error.message);
  } finally {
    submitting.value = false;
  }
}

// 删除 ELEMENT
function handleDelete(row) {
  ElMessageBox.confirm(
    `确定要删除元素"${row.name}"吗？`,
    '确认删除',
    { type: 'warning' }
  ).then(async () => {
    try {
      await request.delete(`/element/${row.id}`);
      ElMessage.success('删除成功');
      loadElements();
    } catch (error) {
      ElMessage.error('删除失败：' + error.message);
    }
  });
}

// 重置表单
function resetForm() {
  formData.id = null;
  formData.name = '';
  formData.pageId = null;
  formData.elementType = 'button';
  formData.sortOrder = 0;
  formData.status = 1;
  formData.description = '';
  formData.childPageId = null;
  currentElement.value = null;
  if (formRef.value) {
    formRef.value.clearValidate();
  }
}

onMounted(() => {
  loadPages();
  loadElements();
});
</script>

<style scoped>
.element-list {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #303133;
}

.description {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.content-card {
  padding: 20px;
}

.toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.name-text {
  font-weight: 500;
}

.code-text {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #606266;
}

.success-text {
  color: #67c23a;
}

.empty-text {
  color: #c0c4cc;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>

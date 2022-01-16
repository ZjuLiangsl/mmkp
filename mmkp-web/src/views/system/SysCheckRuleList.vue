<template>
  <a-card :bordered="false">

    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form-model layout="inline" :model="queryParam" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">

          <a-col :md="6" :sm="8">
            <a-form-model-item label="Strategy Name" prop="ruleName">
              <a-input placeholder="Plases Input Strategy Name" v-model="queryParam.ruleName"/>
            </a-form-model-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-model-item label="Strategy Code" prop="ruleCode">
              <a-input placeholder="Plases Input Strategy Code" v-model="queryParam.ruleCode"/>
            </a-form-model-item>
          </a-col>
          <template v-if="toggleSearchStatus">
          </template>
          <a-col :md="6" :sm="8">
            <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
              <a-button type="primary" @click="searchQuery" icon="search">Query</a-button>
              <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">Reset</a-button>
              <a @click="handleToggleSearch" style="margin-left: 8px">
                {{ toggleSearchStatus ? 'Close' : 'Open' }}
                <a-icon :type="toggleSearchStatus ? 'up' : 'down'"/>
              </a>
            </span>
          </a-col>

        </a-row>
      </a-form-model>
    </div>

    <!-- 操作按钮区域 -->
    <div class="table-operator">
      <a-button @click="handleAdd" type="primary" icon="plus">New</a-button>
      <a-button type="primary" icon="download" @click="handleExportXls('编码Constraint Strategy')">Export</a-button>
      <a-upload name="file" :showUploadList="false" :multiple="false" :headers="tokenHeader" :action="importExcelUrl"
                @change="handleImportExcel">
        <a-button type="primary" icon="import">Import</a-button>
      </a-upload>
      <a-dropdown v-if="selectedRowKeys.length > 0">
        <a-menu slot="overlay">
          <a-menu-item key="1" @click="batchDel">
            <a-icon type="delete"/>
             Delete
          </a-menu-item>
        </a-menu>
        <a-button style="margin-left: 8px"> batch operation
          <a-icon type="down"/>
        </a-button>
      </a-dropdown>
    </div>

    <!-- table区域-begin -->
    <a-alert type="info" showIcon style="margin-bottom: 16px;">
      <template slot="message">
        <!--<span>已选择</span>-->
        <a style="font-weight: 600;padding: 0 4px;">{{ selectedRowKeys.length }}</a>
        <span>Selected</span>
        <template v-if="selectedRowKeys.length>0">
          <a-divider type="vertical"/>
          <a @click="onClearSelected">Empty</a>
        </template>
      </template>
    </a-alert>

    <a-table
      ref="table"
      size="middle"
      bordered
      rowKey="id"
      :columns="columns"
      :dataSource="dataSource"
      :pagination="ipagination"
      :loading="loading"
      :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
      @change="handleTableChange">

      <template slot="action" slot-scope="text, record">
        <a @click="handleTest(record)">Test</a>
        <a-divider type="vertical"/>
        <a @click="handleEdit(record)">Edit</a>
        <a-divider type="vertical"/>
        <a @click="handleDelete(record.id)">Delete</a>
      </template>

    </a-table>
    <!-- table区域-end -->

    <!-- 表单区域 -->
    <sys-check-rule-modal ref="modalForm" @ok="modalFormOk"/>

    <sys-check-rule-test-modal ref="testModal"/>

  </a-card>
</template>

<script>
import JEllipsis from '@/components/jeecg/JEllipsis'
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import SysCheckRuleModal from './modules/SysCheckRuleModal'
import SysCheckRuleTestModal from './modules/SysCheckRuleTestModal'

export default {
  name: 'SysCheckRuleList',
  mixins: [JeecgListMixin],
  components: { SysCheckRuleModal, SysCheckRuleTestModal, JEllipsis },
  data() {
    return {
      description: '编码Constraint Strategy管理页面',
      // 表头
      columns: [
        {
          title: '#',
          key: 'rowIndex',
          width: 60,
          align: 'center',
          customRender: (t, r, i) => i + 1
        },
        {
          title: 'Strategy Name',
          align: 'center',
          ellipsis: true,
          width: 120,
          dataIndex: 'ruleName'
        },
        {
          title: 'Strategy Code',
          align: 'center',
          ellipsis: true,
          width: 120,
          dataIndex: 'ruleCode'
        },
        {
          title: 'Strategy Description',
          align: 'center',
          dataIndex: 'ruleDescription',
          ellipsis: true,
          customRender: (t) => (<j-ellipsis value={t} length={48}/>)
        },
        {
          title: 'Operation',
          dataIndex: 'action',
          align: 'center',
          width: 150,
          scopedSlots: { customRender: 'action' }
        }
      ],
      url: {
        list: '/sys/checkRule/list',
        delete: '/sys/checkRule/delete',
        deleteBatch: '/sys/checkRule/deleteBatch',
        exportXlsUrl: 'sys/checkRule/exportXls',
        importExcelUrl: 'sys/checkRule/importExcel'
      }
    }
  },
  computed: {
    importExcelUrl: function() {
      return `${window._CONFIG['domianURL']}/${this.url.importExcelUrl}`
    }
  },
  methods: {
    handleTest(record) {
      this.$refs.testModal.open(record.ruleCode)
    }
  }
}
</script>
<style scoped>
@import '~@assets/less/common.less';
</style>
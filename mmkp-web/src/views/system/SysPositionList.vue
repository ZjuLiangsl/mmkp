<template>
  <a-card :bordered="false">

    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">

          <a-col :md="6" :sm="8">
            <a-form-item label="职务编码">
              <a-input placeholder="请输入职务编码" v-model="queryParam.code"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-item label="职务名称">
              <a-input placeholder="请输入职务名称" v-model="queryParam.name"></a-input>
            </a-form-item>
          </a-col>
          <template v-if="toggleSearchStatus">
            <a-col :md="6" :sm="8">
              <a-form-item label="职级">
                <j-dict-select-tag v-model="queryParam.postRank" placeholder="请选择职级" dictCode="position_rank"/>
              </a-form-item>
            </a-col>

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
      </a-form>
    </div>

    <!-- 操作按钮区域 -->
    <div class="table-operator">
      <a-button @click="handleAdd" type="primary" icon="plus">New</a-button>
      <a-button type="primary" icon="download" @click="handleExportXls('职务表')">Export</a-button>
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
    <div>
      <div class="ant-alert ant-alert-info" style="margin-bottom: 16px;">
        <i class="anticon anticon-info-circle ant-alert-icon"></i><a style="font-weight: 600">{{
          selectedRowKeys.length
        }} Selected</a>
        <a style="margin-left: 24px" @click="onClearSelected">Empty</a>
      </div>

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

        <span slot="action" slot-scope="text, record">
          <a @click="handleEdit(record)">Edit</a>

          <a-divider type="vertical"/>
          <a-dropdown>
            <a class="ant-dropdown-link">More <a-icon type="down"/></a>
            <a-menu slot="overlay">
              <a-menu-item>
                <a-popconfirm title="Are you sure?" @confirm="() => handleDelete(record.id)">
                  <a>Delete</a>
                </a-popconfirm>
              </a-menu-item>
            </a-menu>
          </a-dropdown>
        </span>

      </a-table>
    </div>
    <!-- table区域-end -->

    <!-- 表单区域 -->
    <sysPosition-modal ref="modalForm" @ok="modalFormOk"></sysPosition-modal>
  </a-card>
</template>

<script>
import SysPositionModal from './modules/SysPositionModal'
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import JDictSelectTag from '@/components/dict/JDictSelectTag'

export default {
  name: 'SysPositionList',
  mixins: [JeecgListMixin],
  components: {
    SysPositionModal,
    JDictSelectTag
  },
  data() {
    return {
      description: '职务表管理页面',
      // 表头
      columns: [
        {
          title: '#',
          dataIndex: '',
          key: 'rowIndex',
          width: 60,
          align: 'center',
          customRender: function(t, r, index) {
            return parseInt(index) + 1
          }
        },
        {
          title: '职务编码',
          align: 'center',
          dataIndex: 'code'
        },
        {
          title: '职务名称',
          align: 'center',
          dataIndex: 'name'
        },
        {
          title: '职级',
          align: 'center',
          dataIndex: 'postRank_dictText'
        },
        // {
        //   title: '公司id',
        //   align: 'center',
        //   dataIndex: 'companyId'
        // },
        {
          title: 'Operation',
          dataIndex: 'action',
          align: 'center',
          scopedSlots: { customRender: 'action' }
        }
      ],
      url: {
        list: '/sys/position/list',
        delete: '/sys/position/delete',
        deleteBatch: '/sys/position/deleteBatch',
        exportXlsUrl: '/sys/position/exportXls',
        importExcelUrl: 'sys/position/importExcel'
      }
    }
  },
  computed: {
    importExcelUrl: function() {
      return `${window._CONFIG['domianURL']}/${this.url.importExcelUrl}`
    }
  }
}
</script>
<style scoped>
@import '~@assets/less/common.less'
</style>
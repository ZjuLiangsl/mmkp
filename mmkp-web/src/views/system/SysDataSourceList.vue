<template>
  <a-card :bordered="false">

    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">

          <a-col :md="6" :sm="8">
            <a-form-item label="Data Source Name">
              <a-input placeholder="Please Input Data Source Name" v-model="queryParam.name"/>
            </a-form-item>
          </a-col>

          <a-col :md="6" :sm="8">
            <a-form-item label="Database Type">
              <j-dict-select-tag v-model="queryParam.dbType" placeholder="Please Select Database Type"
                                 dict-code="database_type"/>
            </a-form-item>
          </a-col>

          <a-col :md="6" :sm="8">
            <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
              <a-button type="primary" @click="searchQuery" icon="search">Query</a-button>
              <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">Reset</a-button>
            </span>
          </a-col>

        </a-row>
      </a-form>
    </div>

    <!-- 操作按钮区域 -->
    <div class="table-operator">
      <a-button @click="handleAdd" type="primary" icon="plus">New</a-button>
      <a-button type="primary" icon="download" @click="handleExportXls('多数据源管理')">Export</a-button>
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
      <a-alert type="info" showIcon style="margin-bottom: 16px;">
        <template slot="message">
          <!--<span>已选择</span>-->
          <a style="font-weight: 600;padding: 0 4px;">{{ selectedRowKeys.length }}</a>
          <span>Selected</span>
          <a style="margin-left: 24px" @click="onClearSelected">Empty</a>
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

        <span slot="action" slot-scope="text, record">
          <a @click="handleCollect(record)"><a-icon type="copy"/>Collect</a>
          <a-divider type="vertical"/>
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
    <sys-data-source-modal ref="modalForm" @ok="modalFormOk"/>

    <a-modal
      title="元数据采集"
      :visible="visible"
      :confirm-loading="confirmLoading"
      @ok="handleOk"
      @cancel="handleCancel"
    >
      <a-form :label-col="{ span: 3 }" :wrapper-col="{ span: 21 }">
        <a-form-item label="Database Name">
          <a-select
            show-search
            allowClear
            mode="multiple"
            placeholder="Please Select 需要采集的数据库"
            :value="selectedItems"
            :maxTagCount="2"
            :filter-option="filterOption"
            @change="handleChange"
          >
            <a-select-option v-for="item in dbList" :key="item" :value="item">
              {{ item }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Table Name">
          <a-select
            show-search
            allowClear
            mode="multiple"
            placeholder="Please Select 需要采集的数据表"
            :value="selectedTables"
            :maxTagCount="1"
            :filter-option="filterTablesOption"
            @change="handleTablesChange"
          >
            <a-select-option v-for="item in tablesList" :key="item.key" :value="item.tableName">
              {{ item.dbName }}.{{ item.tableName }}({{ item.tableComment }})
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>

<script>
import JEllipsis from '@/components/jeecg/JEllipsis'
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import SysDataSourceModal from './modules/SysDataSourceModal'
import { getAction, postAction } from '@/api/manage'

export default {
  name: 'SysDataSourceList',
  mixins: [JeecgListMixin],
  components: { JEllipsis, SysDataSourceModal },
  data() {
    let ellipsis = (v, l = 20) => (<j-ellipsis value={v} length={l}/>)
    return {
      description: '多数据源管理管理页面',
      // 表头
      columns: [
        {
          title: '#',
          dataIndex: '',
          key: 'rowIndex',
          width: 60,
          align: 'center',
          customRender: (t, r, index) => index + 1
        },
        {
          title: 'Data Source Name',
          align: 'center',
          dataIndex: 'name'
        },
        {
          title: 'Database Type',
          align: 'center',
          dataIndex: 'dbType_dictText'
        },
        {
          title: 'Driver',
          align: 'center',
          dataIndex: 'dbDriver',
          customRender: (t) => ellipsis(t)
        },
        {
          title: 'Source Address',
          align: 'center',
          dataIndex: 'dbUrl',
          customRender: (t) => ellipsis(t)
        },
        {
          title: 'User',
          align: 'center',
          dataIndex: 'dbUsername'
        },
        {
          title: 'Operation',
          dataIndex: 'action',
          align: 'center',
          scopedSlots: { customRender: 'action' }
        }
      ],
      url: {
        list: '/sys/dataSource/list',
        delete: '/sys/dataSource/delete',
        deleteBatch: '/sys/dataSource/deleteBatch',
        exportXlsUrl: 'sys/dataSource/exportXls',
        importExcelUrl: 'sys/dataSource/importExcel'
      },

      currentRow: {},
      visible: false,
      confirmLoading: false,
      dbList: [],
      selectedItems: [],
      tablesList: [],
      selectedTables: []
    }
  },
  computed: {
    importExcelUrl() {
      return `${window._CONFIG['domianURL']}/${this.url.importExcelUrl}`
    }
  },
  methods: {
    handleCollect(record) {
      this.currentRow = record
      getAction('/md/show-databases', { dsCode: record.code }).then(res => {
        if (res.success == true) {
          this.dbList = res.result
          this.selectedItems = []
          this.selectedTables = []
          this.tablesList = []
          this.visible = true
        } else {
          this.$message.error('Database For failure!')
          console.error(res)
        }
      })
    },
    handleChange(selectedItems) {
      this.selectedItems = selectedItems
      if (this.selectedItems.length > 0) {
        postAction('/md/show-tables', {
          dsCode: this.currentRow.code,
          schemaNames: this.selectedItems
        }).then(res => {
          if (res.success == true) {
            this.tablesList = res.result
          } else {
            this.$message.error('Table For failure!')
            console.error(res)
          }
        })
      }
    },
    handleOk(e) {
      if (this.selectedItems.length == 0) {
        this.$message.warning('Please Select 需要采集的数据库!')
        return
      }
      this.confirmLoading = true
      this.loading = true
      postAction('/md/extract-metadata', {
        dsCode: this.currentRow.code,
        schemaNames: this.selectedItems,
        tablesNames: this.selectedTables
      }).then(res => {
        if (res.success == true) {
          this.loading = false
          this.visible = false
          this.confirmLoading = false
          this.$message.success('Collect complete')
        } else {
          this.loading = false
          this.confirmLoading = false
          this.$message.error('Collect Meta Data failure!')
          console.error(res)
        }
      })
    },
    handleCancel(e) {
      this.visible = false
    },
    filterOption(input, option) {
      return (
        option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
      )
    },
    handleTablesChange(selectedItems) {
      this.selectedTables = selectedItems
    },
    filterTablesOption(input, option) {
      return (
        option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
      )
    }
  }
}
</script>
<style scoped>
@import '~@assets/less/common.less';
</style>
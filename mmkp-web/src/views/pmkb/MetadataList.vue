<template>
  <a-card :bordered="false">

    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">

          <a-col :md="6" :sm="8">
            <a-form-item label="Source Name">
              <a-select v-model="queryParam.dsCode" placeholder="Please Select Data Source">
                <a-select-option value="">Please Select</a-select-option>
                <a-select-option v-for="(item, key) in dsDictItems" :key="key" :value="item.value">
                  {{ item.text }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :md="6" :sm="8">
            <a-form-item label="Database Name">
              <a-select v-model="queryParam.dbName" placeholder="Please Select Database">
                <a-select-option value="">Please Select</a-select-option>
                <a-select-option v-for="(item, key) in dbDictItems" :key="key" :value="item.value">
                  {{ item.text }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :md="6" :sm="12">
            <a-form-item label="Table Name">
              <j-input placeholder="Fuzzy Query" v-model="queryParam.tableName"></j-input>
            </a-form-item>
          </a-col>

          <a-col :md="6" :sm="12">
            <a-form-item label="Table Annotation">
              <j-input placeholder="Fuzzy Query" v-model="queryParam.tableRemark"></j-input>
            </a-form-item>
          </a-col>


          <a-col :md="24" :sm="24">
            <span style="text-align:center" class="table-page-search-submitButtons">
              <a-button type="primary" @click="searchQuery" icon="search">Query</a-button>
              <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">Reset</a-button>
            </span>
          </a-col>

        </a-row>
      </a-form>
    </div>

    <!-- 操作按钮区域 -->
    <div class="table-operator" style="border-top: 5px">
      <a-button @click="handleCollect" type="primary" icon="plus">Collect Meta Data</a-button>
      <a-button @click="handleAdd" type="primary" icon="plus">New</a-button>
      <a-dropdown v-if="selectedRowKeys.length > 0">
        <a-menu slot="overlay" @click="handleMenuClick">
          <a-menu-item key="1">
            <a-icon type="delete" @click="batchDel"/>
            Delete
          </a-menu-item>
        </a-menu>
        <a-button style="margin-left: 8px">
          batch operation
          <a-icon type="down"/>
        </a-button>
      </a-dropdown>
      <span style="color: orangered;margin-left: 20px">Operations in this page will be directly saved to remote database!</span>
    </div>

    <!-- table区域-begin -->
    <div>
      <div class="ant-alert ant-alert-info" style="margin-bottom: 16px;">
        <i class="anticon anticon-info-circle ant-alert-icon"></i><a style="font-weight: 600">{{
          selectedRowKeys.length
        }}</a> Selected&nbsp;&nbsp;
        <a style="margin-left: 24px" @click="onClearSelected">Empty</a>
      </div>

      <a-table
        ref="table"
        bordered
        size="middle"
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
          <!--<a @click="handleRefresh(record.id,record.dsCode,record.dbName,record.tableName)" title="同步远程数据库最新表结构">同步</a>
          <a-divider type="vertical" />-->
          <a @click="handleDelete(record.id)" title="Delete only locally synchronized metadata and local staging operations, but not remote knowledge base structures and data!">Delete</a>
        </span>


      </a-table>
    </div>
    <!-- table区域-end -->

    <metadata-table-modal ref="modalForm" @ok="modalFormOk"/>

    <a-modal
      title="元数据采集"
      :visible="visible"
      :confirm-loading="confirmLoading"
      @ok="handleOk"
      @cancel="handleCancel"
    >
      <a-form :label-col="{ span: 3 }" :wrapper-col="{ span: 21 }">
        <a-form-item label="Source Name">
          <a-select
            allowClear
            placeholder="Please Select the data source that you want to collect"
            :value="dsCode"
            @change="handleDsChange"
          >
            <a-select-option v-for="(item, key) in dsDictItems" :key="key" :value="item.value">
              {{ item.text }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Database Name">
          <a-select
            show-search
            allowClear
            mode="multiple"
            placeholder="Please Select the database that you want to collect"
            :value="schemaNames"
            :maxTagCount="2"
            :filter-option="filterOption"
            @change="handleDbChange"
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
            placeholder="请选择需要采集的数据表"
            :value="tablesNames"
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
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import { getAction, postAction } from '@/api/manage'
import MetadataTableModal from './modules/MetadataTableModal'

export default {
  name: 'MetadataList',
  mixins: [JeecgListMixin],
  components: {
    MetadataTableModal
  },
  data() {
    return {
      description: '这是元数据管理页面',
      queryParam: {},
      recycleBinVisible: false,
      columns: [
        {
          title: 'Data Source',
          align: 'center',
          dataIndex: 'dsName',
          width: 100,
          sorter: true
        },
        {
          title: 'Database',
          align: 'center',
          width: 60,
          dataIndex: 'dbName'
        },
        {
          title: 'Table Name',
          align: 'center',
          width: 100,
          dataIndex: 'tableName',
          sorter: true,
          customRender: (text, row, index) => {
            if (row.dataState == '1') {
              return <span style="color:blue">{text}</span>
            } else if (row.dataState == '2') {
              return <span style="color:sandybrown">{text}</span>
            } else {
              return text
            }
          }
        },
        {
          title: 'Table Annotation',
          align: 'center',
          width: 100,
          dataIndex: 'tableComment',
          customRender: (text, row, index) => {
            // if(row.tableRemark){
            //   return <span style="color:#52c41a">{row.tableRemark}</span>;
            // }else{
            return text
            // }
          }
        },
        {
          title: 'Operation',
          dataIndex: 'action',
          scopedSlots: { customRender: 'action' },
          align: 'center',
          width: 120
        }
      ],
      url: {
        list: '/md/tables/list',
        delete: '/md/tables/delete',
        deleteBatch: '/md/tables/deleteBatch'
      },
      dsDictItems: [],
      dbDictItems: [],

      visible: false,
      confirmLoading: false,
      dsCode: undefined,
      schemaNames: [],
      tablesNames: [],
      dbList: [],
      tablesList: []
    }
  },
  created() {
    this.loadDictItems()
  },
  computed: {},
  methods: {
    loadDictItems() {
      getAction('/sys/dataSource/getDsDict', {}).then(res => {
        if (res.success == true) {
          this.dsDictItems = res.result
        } else {
          this.$message.error('Data Source  For failure!')
          console.error(res)
        }
      })
      getAction('/md/tables/getDbDict', {}).then(res => {
        if (res.success == true) {
          this.dbDictItems = res.result
        } else {
          this.$message.error('Database For failure!')
          console.error(res)
        }
      })
    },
    handleMenuClick(e) {
      if (e.key == 1) {
        this.batchDel()
      }
    },
    handleRefresh: function(id, dsCode, dbName, tableName) {
      postAction('/md/tables/sync', {
        dsCode: dsCode,
        dbName: dbName,
        tableName: tableName
      }).then(res => {
        if (res.success == true) {
          this.$message.success('Synchronous success！')
          this.loadData()
        } else {
          this.$message.error('operation failure！')
          console.error(res)
        }
      })
    },
    handleCollect() {
      this.dsCode = undefined
      this.schemaNames = []
      this.tablesNames = []
      this.dbList = []
      this.tablesList = []
      this.visible = true
    },
    handleDsChange(selectedItem) {
      this.dsCode = selectedItem
      getAction('/md/show-databases', { dsCode: selectedItem }).then(res => {
        if (res.success == true) {
          this.dbList = res.result
          this.visible = true
        } else {
          this.$message.error('Database For failure!')
          console.error(res)
        }
      })
    },
    handleDbChange(selectedItems) {
      this.schemaNames = selectedItems
      if (this.schemaNames.length > 0) {
        postAction('/md/show-tables', {
          dsCode: this.dsCode,
          schemaNames: this.schemaNames
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
      if (this.dsCode == '') {
        this.$message.warning('Please Select the data source that you want to collect!')
        return
      }
      if (this.schemaNames.length == 0) {
        this.$message.warning('Please Select the database that you want to collect!')
        return
      }
      this.confirmLoading = true
      this.loading = true
      postAction('/md/extract-metadata', {
        dsCode: this.dsCode,
        schemaNames: this.schemaNames,
        tablesNames: this.tablesNames
      }).then(res => {
        if (res.success == true) {
          this.loading = false
          this.visible = false
          this.confirmLoading = false
          this.$message.success('Collect complete')
          this.loadData(1)
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
      this.tablesNames = selectedItems
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
@import '~@assets/less/common.less'
</style>
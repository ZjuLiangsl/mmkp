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

    <!-- table区域-begin -->
    <div>
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
          <a @click="handleEdit(record)">Configure</a>
          <a-divider type="vertical"/>
          <a @click="handleShow(record)">View</a>
        </span>


      </a-table>
    </div>
    <!-- table区域-end -->

    <data-query-config-modal ref="modalForm" @ok="modalFormOk"/>
  </a-card>
</template>

<script>
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import { getAction, postAction } from '@/api/manage'
import store from '@/store'
import { generateIndexRouter } from '@/utils/util'
import router from '@/router'
import { isURL } from '@/utils/validate'
import DataQueryConfigModal from '@views/pmkb/modules/DataQueryConfigModal'

export default {
  name: 'TableList',
  mixins: [JeecgListMixin],
  components: {
    DataQueryConfigModal
  },
  data() {
    return {
      description: '这是数据表页面',
      queryParam: { dataState: '0' },
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
          sorter: true
        },
        {
          title: 'Table Annotation',
          align: 'center',
          width: 100,
          dataIndex: 'tableComment',
          customRender: (text, row, index) => {
            return row['tableRemark'] ? row['tableRemark'] : text
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
        list: '/md/tables/list'
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
          this.$message.error('Data Source For failure!')
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
    handleShow(record) {

      let routePath = '/dq/view/' + record.id

      let constRoutes = store.getters.addRouters
      let children = constRoutes[0].children
      const target = children.filter(item => routePath === item.path)
      if (target.length == 0) {
        constRoutes[0].children.push(this.genRouteByRecord(record))
        store.dispatch('UpdateAppRouter', { constRoutes }).then(() => {
          router.addRoutes(store.getters.addRouters)

          this.$router.push({ name: 'data-query-edit-' + record.id, params: { id: record.id } })
        })
      } else {
        this.$router.push({ name: 'data-query-edit-' + record.id, params: { id: record.id } })
      }

    },
    genRouteByRecord(record) {
      let name = record.tableRemark ? record.tableRemark : (record.tableComment ? record.tableComment : record.tableName)
      let route = {
        name: 'data-query-edit-' + record.id,
        path: '/dq/view/' + record.id,
        redirect: null,
        route: '1',
        fullPath: 'data-query-edit-' + record.id,
        component: resolve => require(['@views/pmkb/DataQueryEdit.vue'], resolve)
        , meta: {
          componentName: 'data-query-edit-' + record.id,
          keepAlive: false,
          title: name
        }
      }
      return route
    }
  }
}
</script>
<style scoped>
@import '~@assets/less/common.less'
</style>
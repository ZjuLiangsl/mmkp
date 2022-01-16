<template>
  <a-drawer
    title="数据权限规则"
    :width="drawerWidth"
    @close="onClose"
    :visible="visible">

    <!-- 抽屉内容的border -->
    <div
      :style="{
        padding:'10px',
        border: '1px solid #e9e9e9',
        background: '#fff',
      }">
      <div class="table-page-search-wrapper">
        <a-form @keyup.enter.native="searchQuery">
          <a-row :gutter="12">
            <a-col :md="8" :sm="8">
              <a-form-item label="Strategy Name" :labelCol="{span: 8}" :wrapperCol="{span: 14, offset: 1}">
                <a-input placeholder="Plases Input Strategy Name" v-model="queryParam.ruleName"></a-input>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="8">
              <a-form-item label="Strategy Value" :labelCol="{span: 8}" :wrapperCol="{span: 14, offset: 1}">
                <a-input placeholder="Plases Input Strategy Value" v-model="queryParam.ruleValue"></a-input>
              </a-form-item>
            </a-col>
            <a-col :md="7" :sm="8">
              <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
                <a-button type="primary" @click="searchQuery" icon="search">Query</a-button>
                <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">Reset</a-button>
              </span>
            </a-col>
          </a-row>
          <a-row>
            <a-col :md="24" :sm="24">
              <a-button style="margin-bottom: 10px" @click="addPermissionRule" type="primary" icon="plus">New</a-button>
            </a-col>
          </a-row>
        </a-form>

        <a-table
          ref="table"
          rowKey="id"
          size="middle"
          :columns="columns"
          :dataSource="dataSource"
          :loading="loading"
          :rowClassName="getRowClassname">
          <span slot="action" slot-scope="text, record">
            <a @click="handleEdit(record)">
              <a-icon type="edit"/>Edit
            </a>
            <a-divider type="vertical"/>
            <a-popconfirm title="Are you sure?" @confirm="() => handleDelete(record.id)">
              <a>Delete</a>
            </a-popconfirm>
          </span>
        </a-table>

      </div>
    </div>
    <permission-data-rule-modal @ok="modalFormOk" ref="modalForm"></permission-data-rule-modal>
  </a-drawer>
</template>
<script>
import { getPermissionRuleList, queryPermissionRule } from '@/api/api'
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import PermissionDataRuleModal from './modules/PermissionDataRuleModal'

const columns = [
  {
    title: 'Strategy Name',
    dataIndex: 'ruleName',
    key: 'ruleName'
  },
  {
    title: 'Strategy Field',
    dataIndex: 'ruleColumn',
    key: 'ruleColumn'
  },
  {
    title: 'Strategy Value',
    dataIndex: 'ruleValue',
    key: 'ruleValue'
  },
  {
    title: 'Operation',
    dataIndex: 'action',
    scopedSlots: { customRender: 'action' },
    align: 'center'
  }
]
export default {
  name: 'PermissionDataRuleList',
  mixins: [JeecgListMixin],
  components: {
    PermissionDataRuleModal
  },
  data() {
    return {
      queryParam: {},
      drawerWidth: 650,
      columns: columns,
      permId: '',
      visible: false,
      form: this.$form.createForm(this),
      loading: false,
      url: {
        list: '/sys/permission/getPermRuleListByPermId',
        delete: '/sys/permission/deletePermissionRule'
      }
    }
  },
  created() {
    this.resetScreenSize()
  },
  methods: {
    loadData() {
      //20190908 scott for: 首次进入菜单列表的时候，不加载权限列表
      if (!this.permId) {
        return
      }
      let that = this
      this.dataSource = []
      var params = this.getQueryParams()//查询条件
      getPermissionRuleList(params).then((res) => {
        if (res.success) {
          that.dataSource = res.result
        }
      })
    },
    edit(record) {
      if (record.id) {
        this.visible = true
        this.permId = record.id
      }
      this.queryParam = {}
      this.queryParam.permissionId = record.id
      this.visible = true
      this.loadData()
      this.resetScreenSize()
    },
    addPermissionRule() {
      this.$refs.modalForm.add(this.permId)
      this.$refs.modalForm.title = 'New'
    },
    searchQuery() {
      var params = this.getQueryParams()
      params.permissionId = this.permId
      queryPermissionRule(params).then((res) => {
        if (res.success) {
          this.dataSource = res.result
        }
      })
    },
    searchReset() {
      this.queryParam = {}
      this.queryParam.permissionId = this.permId
      this.loadData(1)
    },
    onClose() {
      this.visible = false
    },
    // 根据屏幕变化,设置抽屉尺寸
    resetScreenSize() {
      let screenWidth = document.body.clientWidth
      if (screenWidth < 500) {
        this.drawerWidth = screenWidth
      } else {
        this.drawerWidth = 650
      }
    },
    getRowClassname(record) {
      if (record.status != 1) {
        return 'data-rule-invalid'
      }
    }
  }
}
</script>

<style>
.data-rule-invalid {
  background: #f4f4f4;
  color: #bababa;
}
</style>
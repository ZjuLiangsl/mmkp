<template>
  <a-card :bordered="false">

    <!-- 操作按钮区域 -->
    <div class="table-operator">
      <a-button @click="handleAdd" type="primary" icon="plus">New</a-button>
      <a-button
        @click="batchDel"
        v-if="selectedRowKeys.length > 0"
        ghost
        type="primary"
        icon="delete">Batch Delete
      </a-button>
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
        :columns="columns"
        :scroll="{x: 1500}"
        size="middle"
        :pagination="false"
        :dataSource="dataSource"
        :loading="loading"
        :expandedRowKeys="expandedRowKeys"
        :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
        @expandedRowsChange="handleExpandedRowsChange">

        <span slot="action" slot-scope="text, record">
          <a @click="handleEdit(record)">Edit</a>

          <a-divider type="vertical"/>
          <a-dropdown>
            <a class="ant-dropdown-link">
              More <a-icon type="down"/>
            </a>
            <a-menu slot="overlay">
              <a-menu-item>
                <a href="javascript:;" @click="handleDetail(record)">Details</a>
              </a-menu-item>
              <a-menu-item>
                <a href="javascript:;" @click="handleAddSub(record)">New Sub</a>
              </a-menu-item>
              <!--<a-menu-item>
                <a href="javascript:;" @click="handleDataRule(record)">Data Rule</a>
              </a-menu-item>-->

              <a-menu-item>
                <a-popconfirm title="Are you sure?" @confirm="() => handleDelete(record.id)">
                  <a>Delete</a>
                </a-popconfirm>
              </a-menu-item>
            </a-menu>
          </a-dropdown>
        </span>
        <!-- 字符串超长截取省略号显示 -->
        <span slot="url" slot-scope="text">
          <j-ellipsis :value="text" :length="25"/>
        </span>
        <!-- 字符串超长截取省略号显示-->
        <span slot="component" slot-scope="text">
          <j-ellipsis :value="text"/>
        </span>
      </a-table>

    </div>
    <!-- table区域-end -->

    <permission-modal ref="modalForm" @ok="modalFormOk"></permission-modal>
    <permission-data-rule-list ref="PermissionDataRuleList" @ok="modalFormOk"></permission-data-rule-list>

  </a-card>
</template>

<script>
import PermissionModal from './modules/PermissionModal'
import { getPermissionList } from '@/api/api'
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import PermissionDataRuleList from './PermissionDataRuleList'
import JEllipsis from '@/components/jeecg/JEllipsis'

const columns = [
  {
    title: 'Menu Name',
    dataIndex: 'name',
    key: 'name'
  }, {
    title: 'Type',
    dataIndex: 'menuType',
    key: 'menuType',
    customRender: function(text) {
      if (text == 0) {
        return 'Menu'
      } else if (text == 1) {
        return 'Menu'
      } else if (text == 2) {
        return 'Button/Permission'
      } else {
        return text
      }
    }
  },/*{
      title: '权限编码',
      dataIndex: 'perms',
      key: 'permissionCode',
    },*/{
    title: 'icon',
    dataIndex: 'icon',
    key: 'icon'
  },
  {
    title: 'Unit',
    dataIndex: 'component',
    key: 'component',
    scopedSlots: { customRender: 'component' }
  },
  {
    title: 'Path',
    dataIndex: 'url',
    key: 'url',
    scopedSlots: { customRender: 'url' }
  },
  {
    title: 'Order',
    dataIndex: 'sortNo',
    key: 'sortNo'
  },
  {
    title: 'Operation',
    dataIndex: 'action',
    fixed: 'right',
    scopedSlots: { customRender: 'action' },
    align: 'center',
    width: 150
  }
]

export default {
  name: 'PermissionList',
  mixins: [JeecgListMixin],
  components: {
    PermissionDataRuleList,
    PermissionModal,
    JEllipsis
  },
  data() {
    return {
      description: '这是菜单管理页面',
      // 表头
      columns: columns,
      loading: false,
      // 展开的行，受控属性
      expandedRowKeys: [],
      url: {
        list: '/sys/permission/list',
        delete: '/sys/permission/delete',
        deleteBatch: '/sys/permission/deleteBatch'
      }
    }
  },
  methods: {
    loadData() {
      this.dataSource = []
      getPermissionList().then((res) => {
        if (res.success) {
          console.log(res.result)
          this.dataSource = res.result
        }
      })
    },
    // 打开数据规则编辑
    handleDataRule(record) {
      this.$refs.PermissionDataRuleList.edit(record)
    },
    handleAddSub(record) {
      this.$refs.modalForm.title = 'New SubMenu'
      this.$refs.modalForm.disableSubmit = false
      this.$refs.modalForm.edit({ status: '1', permsType: '1', route: true, 'parentId': record.id, menuType: 1 })
    },
    handleExpandedRowsChange(expandedRows) {
      this.expandedRowKeys = expandedRows
    }
  }
}
</script>
<style scoped>
@import '~@assets/less/common.less';
</style>
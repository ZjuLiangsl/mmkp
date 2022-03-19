<template>
  <a-card :bordered="false" class="card-area">

    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :md="6" :sm="8">
            <a-form-item label="roleName" :labelCol="{span: 5}" :wrapperCol="{span: 18, offset: 1}">
              <a-input placeholder="roleName" v-model="queryParam.roleName"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="10" :sm="12">
            <a-form-item label="createTime" :labelCol="{span: 5}" :wrapperCol="{span: 18, offset: 1}">
              <j-date v-model="queryParam.createTime_begin" :showTime="true" date-format="YYYY-MM-DD HH:mm:ss"
                      style="width:45%" placeholder="createTime_begin"></j-date>
              <span style="width: 10px;">~</span>
              <j-date v-model="queryParam.createTime_end" :showTime="true" date-format="YYYY-MM-DD HH:mm:ss"
                      style="width:45%" placeholder="createTime_end"></j-date>
            </a-form-item>
          </a-col>
          <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
            <a-col :md="6" :sm="24">
              <a-button type="primary" @click="searchQuery">Query</a-button>
              <a-button style="margin-left: 8px" @click="searchReset">Reset</a-button>
            </a-col>
          </span>
        </a-row>
      </a-form>
    </div>

    <div class="table-operator" style="margin-top: 5px">
      <a-button @click="handleAdd" type="primary" icon="plus">New</a-button>
      <a-button type="primary" icon="download" @click="handleExportXls('role')">Export</a-button>
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
        <a-button style="margin-left: 8px">
          batch operation
          <a-icon type="down"/>
        </a-button>
      </a-dropdown>
    </div>

    <div>
      <div class="ant-alert ant-alert-info" style="margin-bottom: 16px;">
        <i class="anticon anticon-info-circle ant-alert-icon"></i> <a style="font-weight: 600">{{
          selectedRowKeys.length
        }}</a> Selected&nbsp;&nbsp;
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
            <a class="ant-dropdown-link">
              More <a-icon type="down"/>
            </a>
            <a-menu slot="overlay">
              <a-menu-item>
                <a @click="handlePerssion(record.id)">auth</a>
              </a-menu-item>
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

    <role-modal ref="modalForm" @ok="modalFormOk"></role-modal>
    <user-role-modal ref="modalUserRole"></user-role-modal>
  </a-card>
</template>

<script>
import RoleModal from './modules/RoleModal'
import UserRoleModal from './modules/UserRoleModal'
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import JDate from '@/components/jeecg/JDate'

export default {
  name: 'RoleList',
  mixins: [JeecgListMixin],
  components: {
    RoleModal,
    UserRoleModal,
    JDate
  },
  data() {
    return {

      description: 'role mananger',
      queryParam: { roleName: '' },
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
          title: 'Role Name',
          align: 'center',
          dataIndex: 'roleName'
        },
        {
          title: 'Identifier',
          align: 'center',
          dataIndex: 'roleCode'
        },
        {
          title: 'description',
          align: 'center',
          dataIndex: 'description'
        },
        {
          title: 'Creation Date',
          dataIndex: 'createTime',
          align: 'center',
          sorter: true
        },
        {
          title: 'updateTime',
          dataIndex: 'updateTime',
          align: 'center',
          sorter: true
        },
        {
          title: 'Operation',
          dataIndex: 'action',
          align: 'center',
          scopedSlots: { customRender: 'action' }
        }
      ],
      url: {
        list: '/sys/role/list',
        delete: '/sys/role/delete',
        deleteBatch: '/sys/role/deleteBatch',
        exportXlsUrl: '/sys/role/exportXls',
        importExcelUrl: 'sys/role/importExcel'
      }
    }
  },
  computed: {
    importExcelUrl: function() {
      return `${window._CONFIG['domianURL']}/${this.url.importExcelUrl}`
    }
  },
  methods: {
    handlePerssion: function(roleId) {
      // alert(roleId);
      this.$refs.modalUserRole.show(roleId)
    },
    onChangeDate(date, dateString) {
      console.log(date, dateString)
    }
  }
}
</script>
<style scoped>
@import '~@assets/less/common.less'
</style>
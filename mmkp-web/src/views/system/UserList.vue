<template>
  <a-card :bordered="false">

    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">

          <a-col :md="6" :sm="12">
            <a-form-item label="Account">
              <!--<a-input placeholder="请输入账号查询" v-model="queryParam.username"></a-input>-->
              <j-input placeholder="Fuzzy Query" v-model="queryParam.username"></j-input>
            </a-form-item>
          </a-col>

          <a-col :md="6" :sm="8">
            <a-form-item label="Sex">
              <a-select v-model="queryParam.sex" placeholder="Plases Select Sex">
                <a-select-option value="">Please Select</a-select-option>
                <a-select-option value="1">man</a-select-option>
                <a-select-option value="2">woman</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>


          <template v-if="toggleSearchStatus">
            <a-col :md="6" :sm="8">
              <a-form-item label="Actual Name">
                <a-input placeholder="Plases Input Actual Name" v-model="queryParam.realname"></a-input>
              </a-form-item>
            </a-col>

            <a-col :md="6" :sm="8">
              <a-form-item label="Phone Number">
                <a-input placeholder="Pleas Input Phone Number" v-model="queryParam.phone"></a-input>
              </a-form-item>
            </a-col>

            <a-col :md="6" :sm="8">
              <a-form-item label="User Status">
                <a-select v-model="queryParam.status" placeholder="Please Select">
                  <a-select-option value="">Please Select</a-select-option>
                  <a-select-option value="1">normal</a-select-option>
                  <a-select-option value="2">freeze</a-select-option>
                </a-select>
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
    <div class="table-operator" style="border-top: 5px">
      <a-button @click="handleAdd" type="primary" icon="plus">New</a-button>
      <a-button type="primary" icon="download" @click="handleExportXls('用户信息')">Export</a-button>
      <a-upload name="file" :showUploadList="false" :multiple="false" :headers="tokenHeader" :action="importExcelUrl"
                @change="handleImportExcel">
        <a-button type="primary" icon="import">Import</a-button>
      </a-upload>
      <a-button type="primary" icon="hdd" @click="recycleBinVisible=true">Delete</a-button>
      <a-dropdown v-if="selectedRowKeys.length > 0">
        <a-menu slot="overlay" @click="handleMenuClick">
          <a-menu-item key="1">
            <a-icon type="delete" @click="batchDel"/>
             Delete
          </a-menu-item>
          <a-menu-item key="2">
            <a-icon type="lock" @click="batchFrozen('2')"/>
            freeze
          </a-menu-item>
          <a-menu-item key="3">
            <a-icon type="unlock" @click="batchFrozen('1')"/>
            unfreeze
          </a-menu-item>
        </a-menu>
        <a-button style="margin-left: 8px">
          batch operation
          <a-icon type="down"/>
        </a-button>
      </a-dropdown>
      <j-super-query :fieldList="superQueryFieldList" @handleSuperQuery="handleSuperQuery"/>
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

        <template slot="avatarslot" slot-scope="text, record, index">
          <div class="anty-img-wrap">
            <a-avatar shape="square" :src="getAvatarView(record.avatar)" icon="user"/>
          </div>
        </template>

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
                <a href="javascript:;" @click="handleChangePassword(record.username)">Password</a>
              </a-menu-item>

              <a-menu-item>
                <a-popconfirm title="Are you sure?" @confirm="() => handleDelete(record.id)">
                  <a>Delete</a>
                </a-popconfirm>
              </a-menu-item>

              <a-menu-item v-if="record.status==1">
                <a-popconfirm title="Are you sure it's frozen?" @confirm="() => handleFrozen(record.id,2,record.username)">
                  <a>freeze</a>
                </a-popconfirm>
              </a-menu-item>

              <a-menu-item v-if="record.status==2">
                <a-popconfirm title="Are you sure about thawing?" @confirm="() => handleFrozen(record.id,1,record.username)">
                  <a>unfreeze</a>
                </a-popconfirm>
              </a-menu-item>


            </a-menu>
          </a-dropdown>
        </span>


      </a-table>
    </div>
    <!-- table区域-end -->

    <user-modal ref="modalForm" @ok="modalFormOk"></user-modal>

    <password-modal ref="passwordmodal" @ok="passwordModalOk"></password-modal>

    <!-- 用户回收站 -->
    <user-recycle-bin-modal :visible.sync="recycleBinVisible" @ok="modalFormOk"/>

  </a-card>
</template>

<script>
import UserModal from './modules/UserModal'
import PasswordModal from './modules/PasswordModal'
import { putAction, getFileAccessHttpUrl } from '@/api/manage'
import { frozenBatch } from '@/api/api'
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import JInput from '@/components/jeecg/JInput'
import UserRecycleBinModal from './modules/UserRecycleBinModal'
import JSuperQuery from '@/components/jeecg/JSuperQuery'

export default {
  name: 'UserList',
  mixins: [JeecgListMixin],
  components: {
    UserModal,
    PasswordModal,
    JInput,
    UserRecycleBinModal,
    JSuperQuery
  },
  data() {
    return {
      description: '这是用户管理页面',
      queryParam: {},
      recycleBinVisible: false,
      columns: [
        /*{
          title: '#',
          dataIndex: '',
          key:'rowIndex',
          width:60,
          align:"center",
          customRender:function (t,r,index) {
            return parseInt(index)+1;
          }
        },*/
        {
          title: 'Account',
          align: 'center',
          dataIndex: 'username',
          width: 120,
          sorter: true
        },
        {
          title: 'Actual Name',
          align: 'center',
          width: 100,
          dataIndex: 'realname'
        },
        /*{
          title: '头像',
          align: "center",
          width: 120,
          dataIndex: 'avatar',
          scopedSlots: {customRender: "avatarslot"}
        },*/

        {
          title: 'Sex',
          align: 'center',
          width: 80,
          dataIndex: 'sex_dictText',
          sorter: true
        },
        {
          title: 'Birthday',
          align: 'center',
          width: 100,
          dataIndex: 'birthday'
        },
        {
          title: 'Phone Number',
          align: 'center',
          width: 100,
          dataIndex: 'phone'
        },
        // {
        //   title: 'Department',
        //   align: 'center',
        //   width: 180,
        //   dataIndex: 'orgCodeTxt'
        // },
        /*{
          title: '负责部门',
          align: "center",
          width: 180,
          dataIndex: 'departIds_dictText'
        },*/
        {
          title: 'Status',
          align: 'center',
          width: 80,
          dataIndex: 'status_dictText'
        },
        {
          title: 'Operation',
          dataIndex: 'action',
          scopedSlots: { customRender: 'action' },
          align: 'center',
          width: 170
        }

      ],
      superQueryFieldList: [
        { type: 'input', value: 'username', text: 'Account' },
        { type: 'input', value: 'realname', text: 'Actual Name' },
        { type: 'select', value: 'sex', dbType: 'int', text: 'Sex', dictCode: 'sex' }
      ],
      url: {
        syncUser: '/act/process/extActProcess/doSyncUser',
        list: '/sys/user/list',
        delete: '/sys/user/delete',
        deleteBatch: '/sys/user/deleteBatch',
        exportXlsUrl: '/sys/user/exportXls',
        importExcelUrl: 'sys/user/importExcel'
      }
    }
  },
  computed: {
    importExcelUrl: function() {
      return `${window._CONFIG['domianURL']}/${this.url.importExcelUrl}`
    }
  },
  methods: {
    getAvatarView: function(avatar) {
      return getFileAccessHttpUrl(avatar)
    },

    batchFrozen: function(status) {
      if (this.selectedRowKeys.length <= 0) {
        this.$message.warning('Please select a record！')
        return false
      } else {
        let ids = ''
        let that = this
        let isAdmin = false
        that.selectionRows.forEach(function(row) {
          if (row.username == 'admin') {
            isAdmin = true
          }
        })
        if (isAdmin) {
          that.$message.warning('Administrator account does not allow this operation \, please select again！')
          return
        }
        that.selectedRowKeys.forEach(function(val) {
          ids += val + ','
        })
        that.$confirm({
          title: 'Confirm the operation',
          content: 'Whether to ' + (status == 1 ? 'unfreeze' : 'freeze') + ' Selected Account?',
          onOk: function() {
            frozenBatch({ ids: ids, status: status }).then((res) => {
              if (res.success) {
                that.$message.success(res.message)
                that.loadData()
                that.onClearSelected()
              } else {
                that.$message.warning(res.message)
              }
            })
          }
        })
      }
    },
    handleMenuClick(e) {
      if (e.key == 1) {
        this.batchDel()
      } else if (e.key == 2) {
        this.batchFrozen(2)
      } else if (e.key == 3) {
        this.batchFrozen(1)
      }
    },
    handleFrozen: function(id, status, username) {
      let that = this
      //TODO 后台校验管理员角色
      if ('admin' == username) {
        that.$message.warning('The administrator account does not allow this operation！')
        return
      }
      frozenBatch({ ids: id, status: status }).then((res) => {
        if (res.success) {
          that.$message.success(res.message)
          that.loadData()
        } else {
          that.$message.warning(res.message)
        }
      })
    },
    handleChangePassword(username) {
      this.$refs.passwordmodal.show(username)
    },
    passwordModalOk() {
      //TODO 密码修改完成 不需要刷新页面，可以把datasource中的数据更新一下
    },
    onSyncFinally({ isToLocal }) {
      // 同步到本地时刷新下数据
      if (isToLocal) {
        this.loadData()
      }
    }
  }

}
</script>
<style scoped>
@import '~@assets/less/common.less'
</style>
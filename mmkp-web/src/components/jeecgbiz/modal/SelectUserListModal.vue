<template>
  <a-modal
    title="user list"
    :width="1000"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleSubmit"
    @cancel="handleCancel">

    <a-table
      ref="table"
      bordered
      size="middle"
      rowKey="id"
      :columns="columns"
      :dataSource="dataSource"
      :pagination="ipagination"
      :loading="loading"
      :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"></a-table>
  </a-modal>
</template>

<script>
  import {getUserList} from '@/api/api'
  import {JeecgListMixin} from '@/mixins/JeecgListMixin'

  export default {
    name: "SelectUserListModal",
    mixins: [JeecgListMixin],
    data() {
      return {
        title: "operation",
        visible: false,
        model: {},
        confirmLoading: false,
        url: {
          add: "/act/model/create",
          list: "/sys/user/list"
        },
        columns: [
          {
            title: 'Account',
            align: "center",
            dataIndex: 'username',
            fixed: 'left',
            width: 200
          },
          {
            title: 'realname',
            align: "center",
            dataIndex: 'realname',
          },
          {
            title: 'sex',
            align: "center",
            dataIndex: 'sex_dictText'
          },
          {
            title: 'phone',
            align: "center",
            dataIndex: 'phone'
          },
          {
            title: 'email',
            align: "center",
            dataIndex: 'email'
          },
          {
            title: 'Status',
            align: "center",
            dataIndex: 'status_dictText'
          }
        ]
      }
    },
    created() {
      getUserList().then((res) => {
        if (res.success) {
          this.dataSource = res.result.records;
          this.ipagination.total = parseInt(res.result.total);
        }
      })
    },
    methods: {
      open() {
        this.visible = true;

        this.selectedRowKeys = []
        this.selectedRows = []
      },
      close() {
        this.$emit('close');
        this.visible = false;
      },
      handleChange(info) {
        let file = info.file;
        if (file.response.success) {
          this.$message.success(file.response.message);
          this.$emit('ok');
          this.close()
        } else {
          this.$message.warn(file.response.message);
          this.close()
        }

      },
      handleCancel() {
        this.close()
      },
      handleSubmit() {
        this.$emit('ok', this.selectionRows);
        this.close()
      },
    }
  }
</script>

<style>

</style>

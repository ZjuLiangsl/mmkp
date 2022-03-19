<template>
  <j-select-biz-component
    :value="value"

    name="role"
    displayKey="roleName"

    :returnKeys="returnKeys"
    :listUrl="url.list"
    :columns="columns"
    queryParamText="role code"

    v-on="$listeners"
    v-bind="$attrs"
  />
</template>

<script>
  import JSelectBizComponent from './JSelectBizComponent'

  export default {
    name: 'JSelectRole',
    components: { JSelectBizComponent },
    props: ['value'],
    data() {
      return {
        returnKeys: ['id', 'roleCode'],
        url: { list: '/sys/role/list' },
        columns: [
          { title: 'Role Name', dataIndex: 'roleName', align: 'center', width: 120 },
          { title: 'Identifier', dataIndex: 'roleCode', align: 'center', width: 120 }
        ]
      }
    }
  }
</script>

<style lang="less" scoped></style>
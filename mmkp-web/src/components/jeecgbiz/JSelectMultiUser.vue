<template>
  <j-select-biz-component
    :value="value"
    :ellipsisLength="25"
    :listUrl="url.list"
    :columns="columns"
    v-on="$listeners"
    v-bind="attrs"
  />
</template>

<script>
  import JDate from '@comp/jeecg/JDate'
  import JSelectBizComponent from './JSelectBizComponent'

  export default {
    name: 'JSelectMultiUser',
    components: {JDate, JSelectBizComponent},
    props: {
      value: null, // any type
      queryConfig: {
        type: Array,
        default: () => []
      },
    },
    data() {
      return {
        url: { list: '/sys/user/list' },
        columns: [
          { title: 'realname', align: 'center', width: '25%', widthRight: '70%', dataIndex: 'realname' },
          { title: 'username', align: 'center', width: '25%', dataIndex: 'username' },
          { title: 'phone', align: 'center', width: '20%', dataIndex: 'phone' },
          { title: 'birthday', align: 'center', width: '20%', dataIndex: 'birthday' }
        ],
        default: {
          name: 'realname',
          width: 1200,
          displayKey: 'realname',
          returnKeys: ['id', 'username'],
          queryParamText: 'realname',
        },
        queryConfigDefault: [
          {
            key: 'sex',
            label: 'sex',
            dictCode: 'sex',
          },
          {
            key: 'birthday',
            label: 'birthday',
            placeholder: 'birthday',
            customRender: ({key, queryParam, options}) => {
              return <j-date {...options} vModel={queryParam[key]} style="width:180px;"/>
            },
          },
        ],
      }
    },
    computed: {
      attrs() {
        return Object.assign(this.default, this.$attrs, {
          queryConfig: this.queryConfigDefault.concat(this.queryConfig)
        })
      }
    }
  }
</script>

<style lang="less" scoped></style>
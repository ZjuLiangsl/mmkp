<template>
  <j-select-biz-component :width="1000" v-bind="configs" v-on="$listeners"/>
</template>

<script>
  import JSelectBizComponent from './JSelectBizComponent'

  export default {
    name: 'JSelectPosition',
    components: { JSelectBizComponent },
    props: ['value'],
    data() {
      return {
        settings: {
          name: 'position',
          displayKey: 'name',
          returnKeys: ['id', 'code'],
          listUrl: '/sys/position/list',
          queryParamCode: 'name',
          queryParamText: 'name',
          columns: [
            { title: 'name', dataIndex: 'name', align: 'center', width: '30%', widthRight: '70%' },
            { title: 'code', dataIndex: 'code', align: 'center', width: '35%' },
            { title: 'level', dataIndex: 'rank_dictText', align: 'center', width: '25%' }
          ]
        }
      }
    },
    computed: {
      configs() {
        return Object.assign({ value: this.value }, this.settings, this.$attrs)
      }
    }
  }
</script>

<style lang="less" scoped></style>
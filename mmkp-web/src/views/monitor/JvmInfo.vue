<template>
  <a-skeleton active :loading="loading" :paragraph="{rows: 17}">
    <a-card :bordered="false">

      <a-alert type="info" :showIcon="true">
        <div slot="message">
          Last updated：{{ this.time }}
          <a-divider type="vertical"/>
          <a @click="handleClickUpdate">Update now</a>
        </div>
      </a-alert>

      <a-table
        rowKey="id"
        size="middle"
        :columns="columns"
        :dataSource="dataSource"
        :pagination="false"
        :loading="tableLoading"
        style="margin-top: 20px;">

        <template slot="param" slot-scope="text, record">
          <a-tag :color="textInfo[record.param].color">{{ text }}</a-tag>
        </template>

        <template slot="text" slot-scope="text, record">
          {{ textInfo[record.param].text }}
        </template>

        <template slot="value" slot-scope="text, record">
          {{ text }} {{ textInfo[record.param].unit }}
        </template>

      </a-table>
    </a-card>
  </a-skeleton>
</template>
<script>
import moment from 'moment'
import { getAction } from '@api/manage'

moment.locale('zh-cn')

export default {
  data() {
    return {
      time: '',
      loading: true,
      tableLoading: true,
      columns: [{
        title: 'param',
        width: '30%',
        dataIndex: 'param',
        scopedSlots: { customRender: 'param' }
      }, {
        title: 'Description',
        width: '40%',
        dataIndex: 'text',
        scopedSlots: { customRender: 'text' }
      }, {
        title: 'now value',
        width: '30%',
        dataIndex: 'value',
        scopedSlots: { customRender: 'value' }
      }],
      dataSource: [],
      textInfo: {
        'jvm.memory.max': { color: 'purple', text: 'jvm.memory.max', unit: 'MB' },
        'jvm.memory.committed': { color: 'purple', text: 'jvm.memory.committed', unit: 'MB' },
        'jvm.memory.used': { color: 'purple', text: 'jvm.memory.used', unit: 'MB' },
        'jvm.buffer.memory.used': { color: 'cyan', text: 'jvm.buffer.memory.used', unit: 'MB' },
        'jvm.buffer.count': { color: 'cyan', text: 'jvm.buffer.count', unit: '个' },
        'jvm.threads.daemon': { color: 'green', text: 'jvm.threads.daemon', unit: '个' },
        'jvm.threads.live': { color: 'green', text: 'jvm.threads.live', unit: '个' },
        'jvm.threads.peak': { color: 'green', text: 'jvm.threads.peak', unit: '个' },
        'jvm.classes.loaded': { color: 'orange', text: 'jvm.classes.loaded', unit: '个' },
        'jvm.classes.unloaded': { color: 'orange', text: 'jvm.classes.unloaded', unit: '个' },
        'jvm.gc.memory.allocated': { color: 'pink', text: 'jvm.gc.memory.allocated', unit: 'MB' },
        'jvm.gc.memory.promoted': { color: 'pink', text: 'jvm.gc.memory.promoted', unit: 'MB' },
        'jvm.gc.max.data.size': { color: 'pink', text: 'jvm.gc.max.data.size', unit: 'MB' },
        'jvm.gc.live.data.size': { color: 'pink', text: 'jvm.gc.live.data.size', unit: 'MB' },
        'jvm.gc.pause.count': { color: 'blue', text: 'jvm.gc.pause.count', unit: 'times' },
        'jvm.gc.pause.totalTime': { color: 'blue', text: 'jvm.gc.pause.totalTime', unit: 'seconds' }
      },
      moreInfo: {
        'jvm.gc.pause': ['.count', '.totalTime']
      }
    }
  },
  mounted() {
    this.loadTomcatInfo()
  },
  methods: {

    handleClickUpdate() {
      this.loadTomcatInfo()
    },

    loadTomcatInfo() {
      this.tableLoading = true
      this.time = moment().format('YYYY-MM-DD HH:mm:ss')
      Promise.all([
        getAction('actuator/metrics/jvm.memory.max'),
        getAction('actuator/metrics/jvm.memory.committed'),
        getAction('actuator/metrics/jvm.memory.used'),
        getAction('actuator/metrics/jvm.buffer.memory.used'),
        getAction('actuator/metrics/jvm.buffer.count'),
        getAction('actuator/metrics/jvm.threads.daemon'),
        getAction('actuator/metrics/jvm.threads.live'),
        getAction('actuator/metrics/jvm.threads.peak'),
        getAction('actuator/metrics/jvm.classes.loaded'),
        getAction('actuator/metrics/jvm.classes.unloaded'),
        getAction('actuator/metrics/jvm.gc.memory.allocated'),
        getAction('actuator/metrics/jvm.gc.memory.promoted'),
        getAction('actuator/metrics/jvm.gc.max.data.size'),
        getAction('actuator/metrics/jvm.gc.live.data.size'),
        getAction('actuator/metrics/jvm.gc.pause')
      ]).then((res) => {

        let info = []
        res.forEach((value, id) => {
          let more = this.moreInfo[value.name]
          if (!(more instanceof Array)) {
            more = ['']
          }
          more.forEach((item, idx) => {
            let param = value.name + item
            let val = value.measurements[idx].value

            if (param === 'jvm.memory.max'
              || param === 'jvm.memory.committed'
              || param === 'jvm.memory.used'
              || param === 'jvm.buffer.memory.used'
              || param === 'jvm.gc.memory.allocated'
              || param === 'jvm.gc.memory.promoted'
              || param === 'jvm.gc.max.data.size'
              || param === 'jvm.gc.live.data.size'
            ) {
              val = this.convert(val, Number)
            }
            info.push({ id: param + id, param, text: 'false value', value: val })
          })
        })
        this.dataSource = info

      }).catch((e) => {
        console.error(e)
        this.$message.error('failure')
      }).finally(() => {
        this.loading = false
        this.tableLoading = false
      })
    },

    convert(value, type) {
      if (type === Number) {
        return Number(value / 1048576).toFixed(3)
      } else if (type === Date) {
        return moment(value * 1000).format('YYYY-MM-DD HH:mm:ss')
      }
      return value
    }
  }
}
</script>
<style></style>

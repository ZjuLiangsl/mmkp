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
        'tomcat.sessions.created': { color: 'green', text: 'tomcat.sessions.created', unit: 'unit' },
        'tomcat.sessions.expired': { color: 'green', text: 'tomcat.sessions.expired', unit: 'unit' },
        'tomcat.sessions.active.current': { color: 'green', text: 'tomcat.sessions.active.current', unit: 'unit' },
        'tomcat.sessions.active.max': { color: 'green', text: 'tomcat.sessions.active.max', unit: 'unit' },
        'tomcat.sessions.rejected': { color: 'green', text: 'tomcat.sessions.rejected', unit: 'unit' },

        'tomcat.global.sent': { color: 'purple', text: 'tomcat.global.sent', unit: 'bytes' },
        'tomcat.global.request.max': { color: 'purple', text: 'tomcat.global.request.max', unit: 'seconds' },
        'tomcat.global.request.count': { color: 'purple', text: 'tomcat.global.request.count', unit: 'time' },
        'tomcat.global.request.totalTime': { color: 'purple', text: 'tomcat.global.request.totalTime', unit: 'seconds' },

        'tomcat.servlet.request.max': { color: 'cyan', text: 'tomcat.servlet.request.max', unit: 'seconds' },
        'tomcat.servlet.request.count': { color: 'cyan', text: 'tomcat.servlet.request.count', unit: 'time' },
        'tomcat.servlet.request.totalTime': { color: 'cyan', text: 'tomcat.servlet.request.totalTime', unit: 'seconds' },

        'tomcat.threads.current': { color: 'pink', text: 'tomcat.threads.current', unit: 'unit:' },
        'tomcat.threads.config.max': { color: 'pink', text: 'tomcat.threads.config.max', unit: 'unit:' }
      },
      moreInfo: {
        'tomcat.global.request': ['.count', '.totalTime'],
        'tomcat.servlet.request': ['.count', '.totalTime']
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
        getAction('actuator/metrics/tomcat.sessions.created'),
        getAction('actuator/metrics/tomcat.sessions.expired'),
        getAction('actuator/metrics/tomcat.sessions.active.current'),
        getAction('actuator/metrics/tomcat.sessions.active.max'),
        getAction('actuator/metrics/tomcat.sessions.rejected')
        // getAction('actuator/metrics/tomcat.global.sent'),
        // getAction('actuator/metrics/tomcat.global.request.max'),
        // getAction('actuator/metrics/tomcat.global.request'),
        // getAction('actuator/metrics/tomcat.threads.current'),
        // getAction('actuator/metrics/tomcat.threads.config.max')
        //getAction('actuator/metrics/tomcat.servlet.request'),
        // getAction('actuator/metrics/tomcat.servlet.request.max'),
      ]).then((res) => {
        let tomcatInfo = []
        res.forEach((value, id) => {
          let more = this.moreInfo[value.name]
          if (!(more instanceof Array)) {
            more = ['']
          }
          more.forEach((item, idx) => {
            let param = value.name + item
            tomcatInfo.push({
              id: param + id, param,
              text: 'false value',
              value: value.measurements[idx].value
            })
          })
        })
        this.dataSource = tomcatInfo
      }).catch((e) => {
        console.error(e)
        this.$message.error('failure')
      }).finally(() => {
        this.loading = false
        this.tableLoading = false
      })
    }
  }
}
</script>
<style></style>

<template>
  <div class="page-header-index-wide">
    <a-row :gutter="24">
      <a-col :sm="24" :md="12" :xl="6" :style="{ marginBottom: '18px' }">
        <chart-card :loading="loading" title="Data Source" :total="totalData.dsTotal">
          <!--          <a-tooltip title="指标说明" slot="action">-->
          <!--            <a-icon type="info-circle-o" />-->
          <!--          </a-tooltip>-->
          <div>
            <mini-area :data-source="totalData.dsTotalData" y="count"/>
          </div>
          <!--          <template slot="footer">历史最多<span>{{ totalData.dsTotalMax }}</span></template>-->
        </chart-card>
      </a-col>
      <a-col :sm="24" :md="12" :xl="6" :style="{ marginBottom: '24px' }">
        <chart-card :loading="loading" title="Total Table" :total="totalData.tableTotal">
          <!--          <a-tooltip title="指标说明" slot="action">-->
          <!--            <a-icon type="info-circle-o" />-->
          <!--          </a-tooltip>-->
          <div>
            <mini-area :data-source="totalData.tableTotalData" y="count"/>
          </div>
          <!--          <template slot="footer">历史最多<span>{{totalData.tableTotalMax}}</span></template>-->
        </chart-card>
      </a-col>
      <a-col :sm="24" :md="12" :xl="6" :style="{ marginBottom: '24px' }">
        <chart-card :loading="loading" title="Temporary Table" :total="totalData.stageTableTotal">
          <!--          <a-tooltip title="指标说明" slot="action">-->
          <!--            <a-icon type="info-circle-o" />-->
          <!--          </a-tooltip>-->
          <div>
            <mini-progress color="rgb(19, 194, 194)" :target="totalData.stageTableTotal1"
                           :percentage="totalData.stageTableTotal1" :height="8"/>
          </div>
          <!--          <template slot="footer">转化率 <span>{{totalData.stageTableTotal1}}%</span></template>-->
        </chart-card>
      </a-col>
      <a-col :sm="24" :md="12" :xl="6" :style="{ marginBottom: '24px' }">
        <chart-card :loading="loading" title="Temporary Data" :total="totalData.stageDataTotal">
          <!--          <a-tooltip title="指标说明" slot="action">-->
          <!--            <a-icon type="info-circle-o" />-->
          <!--          </a-tooltip>-->
          <div>
            <mini-progress color="rgb(19, 194, 194)" :target="totalData.stageDataTotal1"
                           :percentage="totalData.stageDataTotal1" :height="8"/>
          </div>
          <!--          <template slot="footer">转化率 <span>{{totalData.stageDataTotal1}}%</span></template>-->
        </chart-card>
      </a-col>
    </a-row>

    <a-row>
      <a-col :span="24">
        <a-card :loading="loading" :bordered="false" title="Table Synchronization Count" :style="{ marginTop: '24px' }">
          <a-row>
            <pie title="pie" :height="360" :data-source="pieData"/>
          </a-row>
        </a-card>
      </a-col>
    </a-row>

    <!--<a-row>
      <a-col :span="24">
        <a-card :loading="loading" :bordered="false" title="最近一周访问量统计" :style="{ marginTop: '24px' }">
          <a-row>
            <a-col :span="6">
              <head-info title="今日IP" :content="loginfo.todayIp"></head-info>
            </a-col>
            <a-col :span="2">
              <a-spin class='circle-cust'>
                <a-icon slot="indicator" type="environment" style="font-size: 24px"  />
              </a-spin>
            </a-col>
            <a-col :span="6">
              <head-info title="今日访问" :content="loginfo.todayVisitCount"></head-info>
            </a-col>
            <a-col :span="2">
              <a-spin class='circle-cust'>
                <a-icon slot="indicator" type="team" style="font-size: 24px"  />
              </a-spin>
            </a-col>
            <a-col :span="6">
              <head-info title="总访问量" :content="loginfo.totalVisitCount"></head-info>
            </a-col>
            <a-col :span="2">
              <a-spin class='circle-cust'>
                <a-icon slot="indicator" type="rise" style="font-size: 24px"  />
              </a-spin>
            </a-col>
          </a-row>
          <line-chart-multid :fields="visitFields" :dataSource="visitInfo"></line-chart-multid>
        </a-card>
      </a-col>
    </a-row>-->
  </div>
</template>

<script>
import ChartCard from '@/components/ChartCard'
import ACol from 'ant-design-vue/es/grid/Col'
import ATooltip from 'ant-design-vue/es/tooltip/Tooltip'
import MiniArea from '@/components/chart/MiniArea'
import MiniBar from '@/components/chart/MiniBar'
import MiniProgress from '@/components/chart/MiniProgress'
import RankList from '@/components/chart/RankList'
import Bar from '@/components/chart/Bar'
import LineChartMultid from '@/components/chart/LineChartMultid'
import HeadInfo from '@/components/tools/HeadInfo.vue'

import Trend from '@/components/Trend'
import { getLoginfo, getVisitInfo } from '@/api/api'
import { postAction } from '@api/manage'
import moment from 'dayjs'

const beginDay = new Date().getTime()
import Pie from '@/components/chart/Pie'

const rankList = []
for (let i = 0; i < 7; i++) {
  rankList.push({
    name: '白鹭岛 ' + (i + 1) + ' 号店',
    total: 1234.56 - i * 100
  })
}
const barData = []
for (let i = 0; i < 12; i += 1) {
  barData.push({
    x: `${i + 1}月`,
    y: Math.floor(Math.random() * 1000) + 200
  })
}
export default {
  name: 'IndexChart',
  components: {
    ATooltip,
    ACol,
    ChartCard,
    MiniArea,
    MiniBar,
    MiniProgress,
    RankList,
    Bar,
    Trend,
    LineChartMultid,
    HeadInfo,
    Pie
  },
  data() {
    return {
      loading: true,
      center: null,
      rankList,
      barData,
      totalData: {},
      pieData: [],
      loginfo: {},
      visitFields: ['ip', 'visit'],
      visitInfo: [],
      indicator: <a-icon type="loading" style="font-size: 24px" spin />
    }
  },
  created() {
    setTimeout(() => {
      this.loading = !this.loading
    }, 1000)
    this.initLogInfo()
    this.getTotal()
  },
  methods: {
    initLogInfo() {
      getLoginfo(null).then((res) => {
        if (res.success) {
          Object.keys(res.result).forEach(key => {
            res.result[key] = res.result[key] + ''
          })
          this.loginfo = res.result
        }
      })
      getVisitInfo().then(res => {
        if (res.success) {
          this.visitInfo = res.result
        }
      })
    },

    getTotal() {
      postAction('/idx/total', {}).then(res => {
        if (res.success == true) {
          this.totalData.dsTotal = res.result.dsTotal + ''
          this.totalData.dsTotalMax = 0
          this.totalData.dsTotalData = []
          for (let i = 1; i < 7; i++) {
            let yv = Math.round(Math.random() * (parseInt(this.totalData.dsTotal) + 3))
            if (yv > this.totalData.dsTotalMax) {
              this.totalData.dsTotalMax = yv
            }
            this.totalData.dsTotalData.push({
              x: moment(new Date(beginDay - 1000 * 60 * 60 * 24 * i)).format('YYYY-MM-DD'),
              y: yv
            })
          }

          this.totalData.tableTotal = res.result.tableTotal + ''
          this.totalData.tableTotalMax = 0
          this.totalData.tableTotalData = []
          for (let i = 1; i < 7; i++) {
            let yv = Math.round(Math.random() * (parseInt(this.totalData.tableTotal) + 3))
            if (yv > this.totalData.tableTotalMax) {
              this.totalData.tableTotalMax = yv
            }
            this.totalData.tableTotalData.push({
              x: moment(new Date(beginDay - 1000 * 60 * 60 * 24 * i)).format('YYYY-MM-DD'),
              y: yv
            })
          }

          this.totalData.stageTableTotal = res.result.stageTableTotal + ''
          this.totalData.stageTableTotal1 = parseFloat((Math.random() * 100).toFixed(2))
          this.totalData.stageDataTotal = res.result.stageDataTotal + ''
          this.totalData.stageDataTotal1 = parseFloat((Math.random() * 100).toFixed(2))

          this.pieData.push({ item: 'Synchronized Table', count: res.result.tableTotal - res.result.stageTableTotal })
          this.pieData.push({ item: 'Temporary Table', count: res.result.stageTableTotal })
        } else {
          this.$message.error(res.message)
          console.error(res)
        }
      })
    }
  }
}
</script>

<style lang="less" scoped>
.circle-cust {
  position: relative;
  top: 28px;
  left: -100%;
}

.extra-wrapper {
  line-height: 55px;
  padding-right: 24px;

  .extra-item {
    display: inline-block;
    margin-right: 24px;

    a {
      margin-left: 24px;
    }
  }
}

/* 首页访问量统计 */
.head-info {
  position: relative;
  text-align: left;
  padding: 0 32px 0 0;
  min-width: 125px;

  &.center {
    text-align: center;
    padding: 0 32px;
  }

  span {
    color: rgba(0, 0, 0, .45);
    display: inline-block;
    font-size: .95rem;
    line-height: 42px;
    margin-bottom: 4px;
  }

  p {
    line-height: 42px;
    margin: 0;

    a {
      font-weight: 600;
      font-size: 1rem;
    }
  }
}
</style>
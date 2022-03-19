<template>
  <j-modal
    centered
    :title="name + 'select'"
    :width="width"
    :visible="visible"
    switchFullscreen
    @ok="handleOk"
    @cancel="close"
    cancelText="Close">

    <a-row :gutter="18">
      <a-col :span="16">
        <a-form layout="inline" class="j-inline-form">
          <a-form-item :label="(queryParamText||name)">
            <a-input v-model="queryParam[queryParamCode||valueKey]" :placeholder="'input' + (queryParamText||name)" @pressEnter="searchQuery"/>
          </a-form-item>
          <j-select-biz-query-item v-if="queryConfig.length>0" v-show="showMoreQueryItems" :queryParam="queryParam" :queryConfig="queryConfig" @pressEnter="searchQuery"/>
          <a-button type="primary" @click="searchQuery" icon="search">Query</a-button>
          <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">Reset</a-button>
          <a v-if="queryConfig.length>0" @click="showMoreQueryItems=!showMoreQueryItems" style="margin-left: 8px">
            {{ showMoreQueryItems ? 'Close' : 'Open' }}
            <a-icon :type="showMoreQueryItems ? 'up' : 'down'"/>
          </a>
        </a-form>

        <a-table
          size="middle"
          bordered
          :rowKey="rowKey"
          :columns="innerColumns"
          :dataSource="dataSource"
          :pagination="ipagination"
          :loading="loading"
          :scroll="{ y: 240 }"
          :rowSelection="{selectedRowKeys, onChange: onSelectChange, type: multiple ? 'checkbox':'radio'}"
          :customRow="customRowFn"
          @change="handleTableChange">
        </a-table>

      </a-col>
      <a-col :span="8">
        <a-card :title="'selected' + name" :bordered="false" :head-style="{padding:0}" :body-style="{padding:0}">

          <a-table size="middle" :rowKey="rowKey" bordered v-bind="selectedTable">
              <span slot="action" slot-scope="text, record, index">
                <a @click="handleDeleteSelected(record, index)">Delete</a>
              </span>
          </a-table>

        </a-card>
      </a-col>
    </a-row>
  </j-modal>
</template>

<script>
  import { getAction } from '@/api/manage'
  import Ellipsis from '@/components/Ellipsis'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import { cloneObject, pushIfNotExist } from '@/utils/util'
  import JSelectBizQueryItem from './JSelectBizQueryItem'

  export default {
    name: 'JSelectBizComponentModal',
    mixins: [JeecgListMixin],
    components: {Ellipsis, JSelectBizQueryItem},
    props: {
      value: {
        type: Array,
        default: () => []
      },
      visible: {
        type: Boolean,
        default: false
      },
      valueKey: {
        type: String,
        required: true
      },
      multiple: {
        type: Boolean,
        default: true
      },
      width: {
        type: Number,
        default: 900
      },

      name: {
        type: String,
        default: ''
      },
      listUrl: {
        type: String,
        required: true,
        default: ''
      },
      valueUrl: {
        type: String,
        default: ''
      },
      displayKey: {
        type: String,
        default: null
      },
      columns: {
        type: Array,
        required: true,
        default: () => []
      },
      queryParamCode: {
        type: String,
        default: null
      },
      queryParamText: {
        type: String,
        default: null
      },
      queryConfig: {
        type: Array,
        default: () => []
      },
      rowKey: {
        type: String,
        default: 'id'
      },
      ellipsisLength: {
        type: Number,
        default: 12
      },
    },
    data() {
      return {
        innerValue: [],
        selectedTable: {
          pagination: false,
          scroll: { y: 240 },
          columns: [
            {
              ...this.columns[0],
              width: this.columns[0].widthRight || this.columns[0].width,
            },
            { title: 'Operation', dataIndex: 'action', align: 'center', width: 60, scopedSlots: { customRender: 'action' }, }
          ],
          dataSource: [],
        },
        renderEllipsis: (value) => (<ellipsis length={this.ellipsisLength}>{value}</ellipsis>),
        url: { list: this.listUrl },
        ipagination: {
          current: 1,
          pageSize: 5,
          pageSizeOptions: ['5', '10', '20', '30'],
          showTotal: (total, range) => {
            return range[0] + '-' + range[1] + ' Total' + total
          },
          showQuickJumper: true,
          showSizeChanger: true,
          total: 0
        },
        options: [],
        dataSourceMap: {},
        showMoreQueryItems: false,
      }
    },
    computed: {
      innerColumns() {
        let columns = cloneObject(this.columns)
        columns.forEach(column => {
          if (this.ellipsisLength !== -1) {
            column.customRender = (text) => this.renderEllipsis(text)
          }
        })
        return columns
      },
    },
    watch: {
      value: {
        deep: true,
        immediate: true,
        handler(val) {
          this.innerValue = cloneObject(val)
          this.selectedRowKeys = []
          this.valueWatchHandler(val)
          this.queryOptionsByValue(val)
        }
      },
      dataSource: {
        deep: true,
        handler(val) {
          this.emitOptions(val)
          this.valueWatchHandler(this.innerValue)
        }
      },
      selectedRowKeys: {
        immediate: true,
        deep: true,
        handler(val) {
          if(this.innerValue){
            this.innerValue.length=0;
          }
          this.selectedTable.dataSource = val.map(key => {
            for (let data of this.dataSource) {
              if (data[this.rowKey] === key) {
                pushIfNotExist(this.innerValue, data[this.valueKey])
                return data
              }
            }
            for (let data of this.selectedTable.dataSource) {
              if (data[this.rowKey] === key) {
                pushIfNotExist(this.innerValue, data[this.valueKey])
                return data
              }
            }
            console.warn('The selected row information was not found，key：' + key)
            return {}
          })
        },
      }
    },

    methods: {

      close() {
        this.$emit('update:visible', false)
      },

      valueWatchHandler(val) {
        val.forEach(item => {
          this.dataSource.concat(this.selectedTable.dataSource).forEach(data => {
            if (data[this.valueKey] === item) {
              pushIfNotExist(this.selectedRowKeys, data[this.rowKey])
            }
          })
        })
      },

      queryOptionsByValue(value) {
        if (!value || value.length === 0) {
          return
        }
        let notExist = false
        for (let val of value) {
          let find = false
          for (let option of this.options) {
            if (val === option.value) {
              find = true
              break
            }
          }
          if (!find) {
            notExist = true
            break
          }
        }
        if (!notExist) return
        getAction(this.valueUrl || this.listUrl, {
          [this.valueKey]: value.join(',') + ',',
          pageNo: 1,
          pageSize: value.length
        }).then((res) => {
          if (res.success) {
            let dataSource = res.result
            if (!(dataSource instanceof Array)) {
              dataSource = res.result.records
            }
            this.emitOptions(dataSource, (data) => {
              pushIfNotExist(this.innerValue, data[this.valueKey])
              pushIfNotExist(this.selectedRowKeys, data[this.rowKey])
              pushIfNotExist(this.selectedTable.dataSource, data, this.rowKey)
            })
          }
        })
      },

      emitOptions(dataSource, callback) {
        dataSource.forEach(data => {
          let key = data[this.valueKey]
          this.dataSourceMap[key] = data
          pushIfNotExist(this.options, { label: data[this.displayKey || this.valueKey], value: key }, 'value')
          typeof callback === 'function' ? callback(data) : ''
        })
        this.$emit('options', this.options, this.dataSourceMap)
      },

      handleOk() {
        let value = this.selectedTable.dataSource.map(data => data[this.valueKey])
        this.$emit('input', value)
        this.close()
      },
      handleDeleteSelected(record, index) {
        this.selectedRowKeys.splice(this.selectedRowKeys.indexOf(record[this.rowKey]), 1)
        this.selectedTable.dataSource.splice(this.selectedTable.dataSource.indexOf(record), 1)
        this.innerValue.splice(this.innerValue.indexOf(record[this.valueKey]), 1)
        console.log("this.selectedRowKeys:",this.selectedRowKeys)
        console.log("this.selectedTable.dataSource:",this.selectedTable.dataSource)
      },

      customRowFn(record) {
        return {
          on: {
            click: () => {
              let key = record[this.rowKey]
              if (!this.multiple) {
                this.selectedRowKeys = [key]
                this.selectedTable.dataSource = [record]
              } else {
                let index = this.selectedRowKeys.indexOf(key)
                if (index === -1) {
                  this.selectedRowKeys.push(key)
                  this.selectedTable.dataSource.push(record)
                } else {
                  this.handleDeleteSelected(record, index)
                }
              }
            }
          }
        }
      },

    }
  }
</script>
<style lang="less" scoped>
  .full-form-item {
    display: flex;
    margin-right: 0;

    /deep/ .ant-form-item-control-wrapper {
      flex: 1 1;
      display: inline-block;
    }
  }

  .j-inline-form {
    /deep/ .ant-form-item {
      margin-bottom: 12px;
    }

    /deep/ .ant-form-item-label {
      line-height: 32px;
      width: auto;
    }

    /deep/ .ant-form-item-control {
      height: 32px;
      line-height: 32px;
    }
  }
</style>
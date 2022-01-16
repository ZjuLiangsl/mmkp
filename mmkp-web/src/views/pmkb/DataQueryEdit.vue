<template>
  <div>
    <a-card :bordered="false">
      <!-- 查询区域 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline" @keyup.enter.native="searchQuery">

          <a-row :gutter="24">
            <a-col :md="6" :sm="8" v-for="(item, key) in searchFields" :key="key">
              <a-form-item :label="item.title">
                <j-dict-select-tag v-if="item.type=='j-dict-select-tag'" v-model="queryParam[item.field]"
                                   :dictCode="item.props['dict-code']" style="width: 95%"/>
                <a-input-number v-else-if="item.type=='a-input-number'" v-model="queryParam[item.field]"
                                :max="item.props['max']" :step="item.props['step']" style="width: 95%"/>
                <a-date-picker v-else-if="item.type=='a-date-picker' " v-model="queryParam[item.field]"
                               :format="item.props['format']"
                               @change="(value,valueStr) => onDateChange(valueStr, item.field)" style="width: 95%"/>
                <a-date-picker v-else-if="item.type=='a-date-picker-time' " v-model="queryParam[item.field]"
                               :showTime="true" :format="item.props['format']"
                               @change="(value,valueStr) => onDateChange(valueStr, item.field)" style="width: 95%"/>
                <a-textarea v-else-if="item.type=='a-textarea'" v-model="queryParam[item.field]" style="width: 95%"/>
                <a-input v-else v-model="queryParam[item.field]" style="width: 95%"/>
              </a-form-item>
            </a-col>

            <a-col :md="6" :sm="8">
              <span v-if="searchFields.length>0" style="float: left;overflow: hidden;"
                    class="table-page-search-submitButtons">
                <a-button type="primary" @click="searchQuery" icon="search">Query</a-button>
                <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">Reset</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <!-- 操作按钮区域 -->
      <div class="table-operator">
        <a-button @click="handleAdd" type="primary" icon="plus">New</a-button>
      </div>

      <!-- table区域-begin -->
      <div>
        <a-table
          v-if="isPagination"
          ref="table"
          bordered
          size="middle"
          :scroll="{ x: scrollX }"
          :rowKey="rowKeyStr"
          :columns="columns"
          :dataSource="dataSource"
          :pagination="ipagination"
          :loading="loading"
          :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
          @change="handleTableChange">
          <span slot="action" slot-scope="text, record">
            <a @click="handleEdit(record)">Edit</a>
            <a-divider type="vertical"/>
            <a @click="deleteRow(record)">Delete</a>
          </span>
        </a-table>
        <a-table
          v-else
          ref="table"
          bordered
          size="middle"
          :scroll="{ x: scrollX }"
          :rowKey="rowKeyStr"
          :columns="columns"
          :dataSource="dataSource"
          :pagination="false"
          :loading="loading"
          :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
          @change="handleTableChange">
          <span slot="action" slot-scope="text, record">
            <a @click="handleEdit(record)">Edit</a>
            <a-divider type="vertical"/>
            <a @click="deleteRow(record)">Delete</a>
          </span>
        </a-table>
      </div>
      <!-- table区域-end -->

      <data-query-edit-modal ref="modalForm" :formFields="formFields" :pkFields="pkFields" :formWidth="table.formWidth"
                             :table="table" @ok="modalFormOk"/>

    </a-card>
  </div>
</template>

<script>
import { deleteAction, getAction, postAction } from '@api/manage'
import { ajaxGetDictItems, getDictItemsFromCache } from '@api/api'
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import DataQueryEditModal from '@views/pmkb/modules/DataQueryEditModal'

import md5 from 'md5'

export default {
  name: 'DataQueryEdit',
  mixins: [JeecgListMixin],
  components: {
    DataQueryEditModal
  },
  data() {
    return {
      isPagination: false,
      tableId: this.$route.params.id,
      table: {},
      pkFields: [],
      searchFields: [],
      formFields: [],
      columns: [],
      tableData: [],
      rowData: {},
      url: {
        list: ''
      },
      scrollX: 0,
      searchFormData: {},
      disableMixinCreated: true,

      checkRuleData: [],
      checkRuleDataMap: {}
    }
  },
  created() {
  },
  watch: {
    '$route': function(newRoute) {
      this.loadMeta()
    }
  },
  beforeMount() {
    this.loadRule()
  },
  mounted() {
    this.loadMeta()
  },
  methods: {
    loadRule() {
      // 加载Constraint Strategy列表
      getAction('/sys/checkRule/listAll', {}).then(res => {
        if (res.success == true) {
          this.checkRuleData = res.result
          this.checkRuleData.forEach(item => {
            this.checkRuleDataMap[item.ruleCode] = item.ruleJson
          })
        } else {
          this.$message.error('Constraint Strategy For failure!')
          console.error(res)
        }
      })
    },
    loadMeta() {
      if (this.$route.params.id) {
        this.table = {}
        this.pkFields = []
        this.searchFields = []
        this.formFields = []
        this.columns = []
        getAction('/md/tables/table-fields', { id: this.$route.params.id }).then(res => {
          if (res.success == true) {
            let tableFields = res.result
            this.table = tableFields.table
            tableFields.fields.forEach((item, index) => {
              let name = item.fieldName
              let compType = item.fieldCompType
              let title = item.fieldRemark ? item.fieldRemark : (item.fieldComment ? item.fieldComment : name)
              if (item.isPk == '1') {
                this.pkFields.push(name)
              }
              if (item.isSearchField == '1') {
                let fieldObj = { 'type': compType, 'title': title, 'field': name, 'value': null }
                fieldObj['style'] = { 'width': '95%' }
                if (compType == 'input') {
                  fieldObj['props'] = { 'type': 'text' }
                } else if (compType == 'select') {
                  fieldObj.type = 'j-dict-select-tag'
                  fieldObj['props'] = { 'dict-code': item.compDictCode }
                } else if (compType == 'a-input-number') {
                  let maxStr = ''
                  let maxStr2 = ''
                  let stepStr = ''
                  for (let x = 0; x < item.fieldLength - item.fieldDigit; x++) {
                    maxStr = maxStr + '9'
                  }
                  if (item.fieldDigit == 0) {
                    stepStr = '1'
                    maxStr2 = ''
                  } else {
                    if (item.fieldDigit == 1) {
                      stepStr = '0.1'
                      maxStr2 = '.9'
                    } else {
                      for (let x = 0; x < item.fieldDigit - 1; x++) {
                        stepStr = '0' + stepStr
                        maxStr2 = '9' + maxStr2
                      }
                      stepStr = '0.' + stepStr + '1'
                      maxStr2 = '.' + maxStr2 + '9'
                    }
                  }
                  maxStr = maxStr + maxStr2
                  if (item.fieldType.toLowerCase() == 'tinyint') {
                    maxStr = '127'
                  } else if (item.fieldType.toLowerCase() == 'smallint') {
                    maxStr = '32767'
                  } else if (item.fieldType.toLowerCase() == 'mediumint') {
                    maxStr = '8388607'
                  } else if (item.fieldType.toLowerCase() == 'int') {
                    maxStr = '2147483647'
                  } else if (item.fieldType.toLowerCase() == 'bigint') {
                    maxStr = '9223372036854775807'
                  }
                  fieldObj['props'] = {
                    'max': Number(maxStr),
                    'step': Number(stepStr)
                  }
                } else if (compType == 'a-date-picker') {
                  fieldObj.type = 'a-date-picker'
                  if (item.extField2) {
                    fieldObj['props'] = { 'format': item.extField2 }
                  } else {
                    fieldObj['props'] = { 'format': 'YYYY-MM-DD' }
                  }
                } else if (compType == 'a-time-picker') {
                  fieldObj.type = 'a-date-picker-time'
                  if (item.extField2) {
                    fieldObj['props'] = { 'format': item.extField2 }
                  } else {
                    fieldObj['props'] = { 'format': 'YYYY-MM-DD HH:mm:ss' }
                  }
                }
                this.searchFields.push(fieldObj)
              }
              if (item.isFormDisplay == '1') {
                let fieldObj = {
                  'type': compType,
                  'title': title,
                  'field': name,
                  'value': item.extField1 ? item.extField1 : null
                }
                fieldObj['style'] = { 'width': '95%' }

                if (compType == 'select') {
                  fieldObj.type = 'j-dict-select-tag'
                  fieldObj['props'] = { 'dict-code': item.compDictCode }
                } else if (compType == 'a-input-number') {
                  let maxStr = ''
                  let maxStr2 = ''
                  let stepStr = ''
                  for (let x = 0; x < item.fieldLength - item.fieldDigit; x++) {
                    maxStr = maxStr + '9'
                  }
                  if (item.fieldDigit == 0) {
                    stepStr = '1'
                    maxStr2 = ''
                  } else {
                    if (item.fieldDigit == 1) {
                      stepStr = '0.1'
                      maxStr2 = '.9'
                    } else {
                      for (let x = 0; x < item.fieldDigit - 1; x++) {
                        stepStr = '0' + stepStr
                        maxStr2 = '9' + maxStr2
                      }
                      stepStr = '0.' + stepStr + '1'
                      maxStr2 = '.' + maxStr2 + '9'
                    }
                  }
                  maxStr = maxStr + maxStr2
                  if (item.fieldType.toLowerCase() == 'tinyint') {
                    maxStr = '127'
                  } else if (item.fieldType.toLowerCase() == 'smallint') {
                    maxStr = '32767'
                  } else if (item.fieldType.toLowerCase() == 'mediumint') {
                    maxStr = '8388607'
                  } else if (item.fieldType.toLowerCase() == 'int') {
                    maxStr = '2147483647'
                  } else if (item.fieldType.toLowerCase() == 'bigint') {
                    maxStr = '9223372036854775807'
                  }
                  fieldObj['props'] = {
                    'max': Number(maxStr),
                    'step': Number(stepStr)
                  }
                } else if (compType == 'a-date-picker') {
                  fieldObj.type = 'a-date-picker'
                  if (item.extField2) {
                    fieldObj['props'] = { 'format': item.extField2 }
                  } else {
                    fieldObj['props'] = { 'format': 'YYYY-MM-DD' }
                  }
                } else if (compType == 'a-time-picker') {
                  fieldObj.type = 'a-date-picker'
                  if (item.extField2) {
                    fieldObj['props'] = { 'show-time': true, 'format': item.extField2 }
                  } else {
                    fieldObj['props'] = { 'show-time': true, 'format': 'YYYY-MM-DD HH:mm:ss' }
                  }
                } else if (compType == 'a-input-search') {
                  fieldObj.inject = true
                  fieldObj['emit'] = [
                    {
                      name: 'change',
                      inject: {
                        'dsCode': item.dsCode,
                        'fkDbName': item.fkDbName,
                        'fkTableName': item.fkTableName,
                        'fkFieldName': item.fkFieldName
                      }
                    },
                    {
                      name: 'search',
                      inject: {
                        'dsCode': item.dsCode,
                        'fkDbName': item.fkDbName,
                        'fkTableName': item.fkTableName,
                        'fkFieldName': item.fkFieldName
                      }
                    }
                  ]
                  fieldObj['emitPrefix'] = 'fk'
                  fieldObj['props'] = { 'enter-button': true }

                }

                if (fieldObj['props']) {
                  fieldObj['props'].disabled = false
                } else {
                  fieldObj['props'] = { disabled: false }
                }
                fieldObj['validate'] = []
                if (item.isNullable == '0' || item.extField3 == '0') {
                  fieldObj['validate'].push({ required: true, message: title + '不能为空!' })
                }
                if (item.compRuleCodes) {
                  let ruleCodes = item.compRuleCodes.split(',')
                  ruleCodes.forEach(c => {
                    fieldObj['validate'].push.apply(fieldObj['validate'], JSON.parse(this.checkRuleDataMap[c]))
                  })
                }
                this.formFields.push(fieldObj)
              } else {
                this.formFields.push({
                  type: 'hidden',
                  field: name,
                  value: item.extField1 ? item.extField1 : null
                })
              }
              if (item.isTableDisplay == '1') {
                let columnObj = {
                  title: title,
                  align: 'center',
                  dataIndex: name,
                  width: 120,
                  ellipsis: true
                }
                if (compType == 'select') {
                  columnObj['customRender'] = function(t, r, index) {
                    let dictOptions = getDictItemsFromCache(item.compDictCode)
                    if (dictOptions && dictOptions.length > 0) {
                      let dictItem = dictOptions.filter(item => (t + '') === item.value)[0]
                      if (dictItem) {
                        return dictItem.text || dictItem.label
                      } else {
                        return t
                      }
                    } else {
                      return t
                    }
                  }
                }
                this.columns.push(columnObj)
              }

            })
            this.columns.push({
              title: 'Operation',
              dataIndex: 'action',
              scopedSlots: { customRender: 'action' },
              align: 'center',
              width: 120
            })
            this.scrollX = this.columns.length * 110
            if (this.table['isPaging'] == '1') {
              this.isPagination = true
              this.url.list = 'dqe/page?dsCode=' + this.table.dsCode + '&dbName=' + this.table.dbName + '&tableName=' + this.table.tableName
            } else {
              this.isPagination = false
              this.url.list = 'dqe/list?dsCode=' + this.table.dsCode + '&dbName=' + this.table.dbName + '&tableName=' + this.table.tableName
            }
            this.loadData()

          } else {
            this.$message.error('页面初始化错误！')
            console.error(res)
          }
        })
      }
    },
    rowKeyStr(record) {
      let key = md5(JSON.stringify(record))
      return key
    },
    deleteRow(record) {
      this.loading = true
      postAction('/dqe/delete', { pkFields: this.pkFields, record: record, table: this.table }).then((res) => {
        if (res.success) {
          this.reCalculatePage(this.selectedRowKeys.length)
          this.$message.success(res.message)
          this.loadData()
          this.onClearSelected()
        } else {
          this.$message.warning(res.message)
        }
      }).finally(() => {
        this.loading = false
      })
    },
    onDateChange(valueStr, field) {
      this.queryParam[field] = valueStr
    }
  }
}
</script>

<style scoped>
</style>
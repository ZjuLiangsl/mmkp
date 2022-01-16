<template>
  <a-modal
    :title="title"
    :width="1200"
    :visible="visible"
    :dialog-style="{ top: '20px' }"
    :confirmLoading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
    cancelText="Close">

    <a-spin :spinning="confirmLoading">
      <a-collapse v-model="activeKey">
        <a-collapse-panel key="1" header="Table Information">
          <a-form :form="form" labelAlign="right">
            <a-row :gutter="24">
              <a-col :span="12">
                <a-form-item
                  :labelCol="labelCol"
                  :wrapperCol="wrapperCol"
                  label="Source Name">
                  <a-input :disabled="true" placeholder="Please Input Table Name" v-decorator="['dsCode', validatorRules.dsCode]"/>
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item
                  :labelCol="labelCol"
                  :wrapperCol="wrapperCol"
                  label="Database Name">
                  <a-input :disabled="true" placeholder="Please Input Table Name" v-decorator="['dbName', validatorRules.dbName]"/>
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="24">
              <a-col :span="12">
                <a-form-item
                  :labelCol="labelCol"
                  :wrapperCol="wrapperCol"
                  label="Table Name">
                  <a-input :disabled="true" placeholder="Please Input Table Name" v-decorator="['tableName', validatorRules.tableName]"/>
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item
                  :labelCol="labelCol"
                  :wrapperCol="wrapperCol"
                  label="Table Annotation">
                  <a-input :disabled="true" placeholder="Please Input Table Annotation"
                           v-decorator="['tableComment', validatorRules.tableComment]"/>
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="24">
              <a-col :span="12">
                <a-form-item
                  :labelCol="labelCol"
                  :wrapperCol="wrapperCol"
                  label="Width">
                  <a-input-number style="width: 100%" placeholder="Please Input Width"
                                  v-decorator="['formWidth', validatorRules.formWidth]"/>
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item
                  :labelCol="labelCol"
                  :wrapperCol="wrapperCol"
                  label="View List/ View Table">
                  <a-radio-group :options="[{label:'List',value:'0'},{label:'Table',value:'1'}]"
                                 v-decorator="['isPaging', validatorRules.isPaging]"/>
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="24">
              <a-col :span="24">
                <a-form-item
                  :labelCol="{span: 4}"
                  :wrapperCol="{span: 20}"
                  label="Table Annotation">
                  <a-input placeholder="Please Select Table Annotation" v-decorator="['tableRemark', validatorRules.tableRemark]"/>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-collapse-panel>
        <a-collapse-panel key="2" header="Field Information">
          <a-table :columns="columns" :data-source="data" bordered :pagination="false" :scroll="{ x: 1200, y: 600 }"
                   size="small" rowKey="id">
            <template
              v-for="col in rowEditColumns"
              :slot="col"
              slot-scope="text, record, index"
            >
              <div :key="col">
                <template v-if="record.editable">
                  <a-select allowClear v-if="col == 'fieldCompType' && text!='a-input-search'"
                            style="width:100%;margin: -5px 0;" :default-value="text"
                            @change="value => handleChangeRow(value, record.id, col,record)">
                    <a-select-option v-for="(item, key) in compTypes" :key="key" :value="item.value">
                      <span :title=" item.text || item.label ">
                        {{ item.text || item.label }}
                      </span>
                    </a-select-option>
                  </a-select>
                  <span v-else-if="col == 'fieldCompType' && text=='a-input-search' ">
                    Foreign key Checkbox
                  </span>
                  <a-select allowClear v-else-if="col == 'compDictCode' && record['fieldCompType']!='a-input-search'"
                            :disabled="record['fieldCompType']!='select'"
                            style="width:100%;margin: -5px 0;" :default-value="text"
                            @change="value => handleChangeRow(value, record.id, col,record)">
                    <a-select-option v-for="(item, key) in dictData" :key="key" :value="item.dictCode">
                      <span :title=" item.dictName || item.description ">
                        {{ item.dictName }}
                      </span>
                    </a-select-option>
                  </a-select>
                  <span v-else-if="col == 'compDictCode' && record['fieldCompType']=='a-input-search' ">
                  </span>
                  <a-select mode="multiple" allowClear
                            v-else-if="col == 'compRuleCodes' && record['fieldCompType']!='a-input-search'"
                            :default-value="getRuleCode(text)" style="width:100%;margin: -5px 0;"
                            :maxTagCount="1" :maxTagTextLength="2"
                            @change="value => handleChangeRow(value, record.id, col,record)">
                    <a-select-option v-for="(item, key) in checkRuleData" :key="key" :value="item.ruleCode">
                      <span :title=" item.ruleName || item.ruleDescription ">
                        {{ item.ruleName }}
                      </span>
                    </a-select-option>
                  </a-select>
                  <span v-else-if="col == 'compRuleCodes' && record['fieldCompType']=='a-input-search' ">
                  </span>
                  <a-checkbox v-else-if="col == 'isSearchField' || col == 'isTableDisplay' || col == 'isFormDisplay'"
                              :checked="text=='1'"
                              @change="e => handleChangeRow(e.target.checked, record.id, col,record)"/>
                  <a-checkbox v-else-if="col == 'extField3' && record.isNullable=='1'" :checked="text=='1'"
                              @change="e => handleChangeRow(e.target.checked, record.id, col,record)"/>
                  <span v-else-if="col == 'extField3' && record.isNullable=='0'">
                    <a-tag v-if="text=='0' || record.isNullable=='0'" color="red">N</a-tag>
                    <a-tag v-else>Y</a-tag>
                  </span>
                  <span v-else-if="col == 'extField4' || col == 'extField5'">
                    <a-date-picker
                      v-if="record['fieldType'].indexOf('time')>-1 && record['fieldCompType']!='a-input-search'"
                      :default-value="text" valueFormat="YYYY-MM-DD HH:mm:ss" format="YYYY-MM-DD HH:mm:ss"
                      :show-time="{ defaultValue: moment('00:00:00', 'HH:mm:ss') }"
                      @change="value => handleChangeRow(value, record.id, col,record)"/>
                    <a-date-picker
                      v-else-if="record['fieldType'].indexOf('date')>-1 && record['fieldCompType']!='a-input-search'"
                      :default-value="text" valueFormat="YYYY-MM-DD" format="YYYY-MM-DD"
                      @change="value => handleChangeRow(value, record.id, col,record)"/>
                    <a-input-number
                      v-else-if="record['fieldType'].indexOf('int')>-1 && record['fieldCompType']!='a-input-search'"
                      :default-value="text" @change="value => handleChangeRow(value, record.id, col,record)"/>
                    <a-input-number
                      v-else-if="record['fieldType'].indexOf('decimal')>-1 && record['fieldCompType']!='a-input-search'"
                      :default-value="text" step="0.01"
                      @change="value => handleChangeRow(value, record.id, col,record)"/>
                    <span v-else></span>
                  </span>
                  <a-input allowClear v-else style="margin: -5px 0" :value="text"
                           @change="e => handleChangeRow(e.target.value, record.id, col,record)"/>
                </template>
                <template v-else>
                  <span v-if="col == 'isSearchField' || col == 'isTableDisplay' || col == 'isFormDisplay'">
                    <a-tag v-if="text=='0'">N</a-tag>
                    <a-tag v-else color="blue">Y</a-tag>
                  </span>
                  <span v-else-if="col == 'extField3' ">
                    <a-tag v-if="text=='0' || record.isNullable=='0'" color="red">N</a-tag>
                    <a-tag v-else>Y</a-tag>
                  </span>
                  <span v-else-if="col == 'fieldCompType' && text!='a-input-search' ">
                    {{ compTypeMap[text] }}
                  </span>
                  <span v-else-if="col == 'fieldCompType' && text=='a-input-search' ">
                    Foreign key Checkbox
                  </span>
                  <span v-else-if="col == 'compDictCode' ">
                    {{ dictDataMap[text] }}
                  </span>
                  <span v-else-if="col == 'compRuleCodes' ">
                    {{ showRuleText(text) }}
                  </span>
                  <span v-else :title="text">
                    {{ text }}
                  </span>
                </template>
              </div>
            </template>
            <template slot="operation" slot-scope="text, record, index">
              <div class="editable-row-operations">
                <span v-if="record.editable">
                  <a @click="() => saveRow(record.id)">Save</a>
                  <a @click="() => cancelRow(record.id)" style="margin-left: 10px;">Cancel</a>
                </span>
                <span v-else>
                  <a :disabled="editingKey !== ''" @click="() => editRow(record.id)">Edit</a>
                </span>
              </div>
            </template>
          </a-table>
        </a-collapse-panel>
      </a-collapse>

    </a-spin>
  </a-modal>
</template>

<script>
import pick from 'lodash.pick'
import { getAction, httpAction, postAction } from '@/api/manage'
import { validateDuplicateValue } from '@/utils/util'
import { ajaxGetDictItems, getDictItemsFromCache } from '@/api/api'
import moment from 'moment'
import axios from 'axios'

export default {
  name: 'DataQueryConfigModal',
  components: {},
  data() {
    return {
      moment,
      activeKey: ['1', '2'],
      title: 'Operation',
      visible: false,
      model: {},
      labelCol: {
        span: 6
      },
      wrapperCol: {
        span: 18
      },
      confirmLoading: false,
      form: this.$form.createForm(this),
      validatorRules: {
        dsCode: {
          validateFirst: true,
          rules: [{ required: true, message: 'Please Select Data Source!' }]
        },
        dbName: {
          validateFirst: true,
          rules: [{ required: true, message: 'Please Select Database!' }]
        },
        tableName: {
          validateFirst: true,
          rules: [
            { required: true, message: 'Please Input Table Name!' },
            {
              validator: (rule, value, callback) => {
                let pattern = /^[a-z|A-Z][a-z|A-Z\d_]{0,}$/
                if (!pattern.test(value)) {
                  callback('The table name must start with a letter and can contain digits or underscores')
                } else {
                  validateDuplicateValue('pmkb_metadata_tables', 'table_name', value, this.model.id, callback)
                }
              }
            }
          ]
        },
        tableComment: { rules: [{ required: false, message: 'Please Input Table Annotation!' }] },
        tableRemark: { rules: [{ required: false, message: 'Please Select Table Annotation!' }] }
      },
      url: {
        add: '/sys/dataSource/add',
        edit: '/sys/dataSource/edit'
      },

      data: [],
      cacheData: [],
      fieldData: [],
      rowEditColumns: ['fieldRemark', 'isSearchField', 'isTableDisplay', 'isFormDisplay', 'fieldCompType', 'compDictCode', 'compRuleCodes', 'extField3', 'extField4', 'extField5'],
      columns: [
        {
          title: 'Field Name',
          dataIndex: 'fieldName',
          ellipsis: true,
          width: 100,
          fixed: 'left'
        },
        {
          title: 'Field Type',
          dataIndex: 'fieldTypeFull',
          ellipsis: true,
          width: 100,
          fixed: 'left'
        },
        {
          title: 'Field Annotation',
          dataIndex: 'fieldComment',
          ellipsis: true,
          width: 100,
          fixed: 'left'
        },

        {
          title: 'Remark',
          dataIndex: 'fieldRemark',
          scopedSlots: { customRender: 'fieldRemark' },
          ellipsis: true
        },
        {
          title: 'Query Columns',
          dataIndex: 'isSearchField',
          align: 'center',
          width: 150,
          scopedSlots: { customRender: 'isSearchField' }
        },
        {
          title: 'Table Show',
          dataIndex: 'isTableDisplay',
          align: 'center',
          width: 100,
          scopedSlots: { customRender: 'isTableDisplay' }
        },
        {
          title: 'Form Show',
          dataIndex: 'isFormDisplay',
          align: 'center',
          width: 100,
          scopedSlots: { customRender: 'isFormDisplay' }
        },
        {
          title: 'Nullable',
          dataIndex: 'extField3',
          align: 'center',
          width: 100,
          scopedSlots: { customRender: 'extField3' }
        },
        {
          title: 'Type',
          dataIndex: 'fieldCompType',
          width: 100,
          ellipsis: true,
          scopedSlots: { customRender: 'fieldCompType' }
        },
        {
          title: 'Min',
          dataIndex: 'extField4',
          width: 150,
          ellipsis: true,
          scopedSlots: { customRender: 'extField4' }
        },
        {
          title: 'Max',
          dataIndex: 'extField5',
          width: 150,
          ellipsis: true,
          scopedSlots: { customRender: 'extField5' }
        },
        {
          title: 'Dictionary',
          dataIndex: 'compDictCode',
          width: 120,
          ellipsis: true,
          scopedSlots: { customRender: 'compDictCode' }
        },
        {
          title: 'Constraint Strategy',
          dataIndex: 'compRuleCodes',
          width: 200,
          ellipsis: true,
          scopedSlots: { customRender: 'compRuleCodes' }
        },
        {
          title: 'Operation',
          dataIndex: 'operation',
          scopedSlots: { customRender: 'operation' },
          width: 100,
          fixed: 'right'
        }
      ],
      editingKey: '',
      compTypes: [],
      compTypeMap: {},
      dsCode: '',
      dbName: '',
      dictData: [],
      dictDataMap: {},
      checkRuleData: [],
      checkRuleDataMap: {}
    }
  },
  created() {
    this.loadDict()
  },
  methods: {
    loadFieldsData(tableId) {
      getAction('/md/tables/table-fields', { id: tableId }).then(res => {
        this.data = res.result.fields
        if (res.success == true) {
          this.data = res.result.fields
          this.cacheData = this.data.map(item => ({ ...item }))
          this.fieldData = this.data.map(item => ({ ...item }))
        } else {
          this.$message.error('Field  For failure!')
          console.error(res)
        }
      })

    },
    loadDict() {
      // 加载组件类型
      if (getDictItemsFromCache('form_comp_type')) {
        //优先从缓存中读取字典配置
        this.compTypes = getDictItemsFromCache('form_comp_type')
        this.compTypes.forEach(item => {
          this.compTypeMap[item.value] = item.text || item.label
        })
      } else {
        //根据字典Code, 初始化字典数组
        ajaxGetDictItems('form_comp_type', null).then((res) => {
          if (res.success) {
            this.compTypes = res.result
            this.compTypes.forEach(item => {
              this.compTypeMap[item.value] = item.text || item.label
            })
          }
        })
      }

      // 加载Dictionary
      getAction('/sys/dict/listAll', {}).then(res => {
        if (res.success == true) {
          this.dictData = res.result
          this.dictData.forEach(item => {
            this.dictDataMap[item.dictCode] = item.dictName
          })
        } else {
          this.$message.error('Dictionary For failure!')
          console.error(res)
        }
      })

      // 加载Constraint Strategy列表
      getAction('/sys/checkRule/listAll', {}).then(res => {
        if (res.success == true) {
          this.checkRuleData = res.result
          this.checkRuleData.forEach(item => {
            this.checkRuleDataMap[item.ruleCode] = item.ruleName
          })
        } else {
          this.$message.error('Constraint Strategy For failure!')
          console.error(res)
        }
      })
    },
    showRuleText(text) {
      let result = ''
      if (text && text.length > 0) {
        let codes = text.split(',')
        codes.forEach((item, index) => {
          if (index == codes.length - 1) {
            result = result + (this.checkRuleDataMap[item] ? this.checkRuleDataMap[item] : item)
          } else {
            result = result + (this.checkRuleDataMap[item] ? this.checkRuleDataMap[item] : item) + ','
          }
        })
      }
      return result
    },
    getRuleCode(text) {
      let result = []
      if (text && text.length > 0) {
        let codes = text.split(',')
        codes.forEach((item, index) => {
          result.push(item)
        })
      }
      return result
    },
    add() {
      this.edit({})
    },
    edit(record) {
      if (record.id) {
        this.loadFieldsData(record.id)
      }
      this.form.resetFields()
      this.model = Object.assign({}, record)
      this.visible = true
      this.$nextTick(() => {
        this.form.setFieldsValue(pick(this.model, 'dsCode', 'dbName', 'tableName', 'tableComment', 'tableRemark', 'formWidth', 'isPaging'))
      })
    },
    close() {
      this.$emit('close')
      this.visible = false
    },
    handleOk() {
      this.form.validateFields((err, values) => {
        if (!err) {
          console.log(values)
          console.log(this.data)
          postAction('/md/tables/saveExt', {
            table: values,
            fields: this.data
          }).then(res => {
            if (res.success == true) {
              this.$message.success('Save Success')
              this.$emit('ok')
              this.close()
            } else {
              this.$message.error('Save Failed！')
              console.error(res)
            }
          })
        }
      })
    },
    handleCancel() {
      this.close()
    },

    handleRowAdd() {
      if (this.editingKey) {
        this.$message.warning('Please save the current modified line first！')
      } else {
        this.fieldAddNo = this.fieldAddNo + 1
        this.data.unshift({ id: 'field-add-' + this.fieldAddNo })
        this.cacheData.unshift({ id: 'field-add-' + this.fieldAddNo })
        this.editRow('field-add-' + this.fieldAddNo)
      }
    },
    handleChangeRow(value, id, column, row) {
      const newData = [...this.data]
      const target = newData.filter(item => id === item.id)[0]
      if (target) {
        if (column == 'isSearchField' || column == 'isTableDisplay' || column == 'isFormDisplay' || column == 'extField3') {
          target[column] = value ? '1' : '0'
        } else if (column == 'compRuleCodes') {
          target[column] = value.join(',')
        } else {
          target[column] = value
        }
        this.data = newData
      }
    },
    editRow(id) {
      const newData = [...this.data]
      const target = newData.filter(item => id === item.id)[0]
      this.editingKey = id
      if (target) {
        target.editable = true
        this.data = newData
      }
    },
    saveRow(id) {
      const newData = [...this.data]
      const newCacheData = [...this.cacheData]
      const target = newData.filter(item => id === item.id)[0]
      const targetCache = newCacheData.filter(item => id === item.id)[0]
      if (target && targetCache) {
        delete target.editable
        this.data = newData
        Object.assign(targetCache, target)
        this.cacheData = newCacheData
      }
      this.editingKey = ''
    },
    cancelRow(id) {
      const newData = [...this.data]
      const target = newData.filter(item => id === item.id)[0]
      this.editingKey = ''
      if (target) {
        let currentAddId = 'field-add-' + this.fieldAddNo
        if (target.id == currentAddId) {
          delete target.editable
          this.data = newData
          this.data.forEach((item, index) => {
            if (currentAddId === item.id) {
              this.data.splice(index, 1)
              this.cacheData.splice(index, 1)
            }
          })

        } else {
          Object.assign(target, this.cacheData.filter(item => id === item.id)[0])
          delete target.editable
          this.data = newData
        }
      }
    }
  }
}
</script>

<style lang="less" scoped></style>
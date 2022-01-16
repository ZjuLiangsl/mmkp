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
                  <a-select
                    allowClear
                    placeholder="Please Select Data Source"
                    @change="handleDsChange"
                    :disabled="!!model.id"
                    v-decorator="['dsCode', validatorRules.dsCode]"
                  >
                    <a-select-option v-for="(item, key) in dsDictItems" :key="key" :value="item.value">
                      {{ item.text }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item
                  :labelCol="labelCol"
                  :wrapperCol="wrapperCol"
                  label="Database Name">
                  <a-select
                    allowClear
                    placeholder="Please Select Database"
                    @change="handleDbChange"
                    :disabled="!!model.id"
                    v-decorator="['dbName', validatorRules.dbName]"
                  >
                    <a-select-option v-for="item in dbList" :key="item" :value="item">
                      {{ item }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="24">
              <a-col :span="12">
                <a-form-item
                  :labelCol="labelCol"
                  :wrapperCol="wrapperCol"
                  label="Table Name">
                  <a-input :disabled="!!model.id" placeholder="Please Input Table Name"
                           v-decorator="['tableName', validatorRules.tableName]"/>
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item
                  :labelCol="labelCol"
                  :wrapperCol="wrapperCol"
                  label="Table Annotation">
                  <a-input placeholder="Please Input Table Annotation" v-decorator="['tableComment', validatorRules.tableComment]"/>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-collapse-panel>
        <a-collapse-panel key="2" header="Field Information">
          <a-button class="editable-add-btn" @click="handleRowAdd" type="primary" style="margin:5px;">
            New Field
          </a-button>
          <a-table :columns="columns" :data-source="data" bordered :pagination="false" size="small" rowKey="id">
            <template
              v-for="col in rowEditColumns"
              :slot="col"
              slot-scope="text, record, index"
            >
              <div :key="col">
                <template v-if="record.editable">
                  <a-select allowClear v-if="col == 'fieldType'" style="width:100%;margin: -5px 0;" :value="text"
                            @change="value => handleChangeRow(value, record.id, col,record)">
                    <a-select-option v-for="(item, key) in dataTypes" :key="key" :value="item.value"
                                     :disabled="(record.fieldType=='varchar' && item.value!='varchar' && (record.id.indexOf('field-add-')<0 || record.dataState!='1'))
                                     || (record.fieldType=='int' && item.value!='int'&& item.value!='varchar' && item.value!='decimal' && (record.id.indexOf('field-add-')<0 || record.dataState!='1'))
                                     || (record.fieldType=='decimal' && item.value!='decimal' && item.value!='varchar' && item.value!='int' && (record.id.indexOf('field-add-')<0 || record.dataState!='1'))
                                     || (record.fieldType=='date' && item.value!='date' && item.value!='varchar' && item.value!='datetime' && (record.id.indexOf('field-add-')<0 || record.dataState!='1'))
                                     || (record.fieldType=='datetime' && item.value!='datetime' && item.value!='varchar' && item.value!='date' && (record.id.indexOf('field-add-')<0 || record.dataState!='1'))
                                     || (record.fieldType=='reference' && (record.id.indexOf('field-add-')<0 || record.dataState!='1'))">
                      <span :title=" item.text || item.label ">
                        {{ item.text || item.label }}
                      </span>
                    </a-select-option>
                  </a-select>
                  <!--<a-select allowClear v-else-if="col == 'fkTableName' && record.id.indexOf('field-add-')>-1" style="width:100%;margin: -5px 0;" :default-value="text" @change="value => handleChangeRow(value, record.id, col,record)"
                            show-search :filter-option="filterOption">
                    <a-select-option v-for="item in fkTableNames" :key="item.key" :value="item.tableName">
                      <span :title=" item.title ">
                        {{item.title}}
                      </span>
                    </a-select-option>
                  </a-select>
                  <span v-else-if="col == 'fkTableName' && record.id.indexOf('field-add-')<0">{{ text }}</span>
                  <a-select allowClear v-else-if="col == 'fkFieldName' && record.id.indexOf('field-add-')>-1" style="width:100%;margin: -5px 0;" :default-value="text" @change="(value,option) => handleChangeRow(value, record.id, col,record,option)">
                    <a-select-option v-for="item in fkFieldNames" :key="item.key" :value="item.fieldName">
                      <span :title="item.title">
                        {{item.title}}
                      </span>
                    </a-select-option>
                  </a-select>
                  <span v-else-if="col == 'fkFieldName' && record.id.indexOf('field-add-')<0">{{ text }}</span>-->
                  <a-checkbox v-else-if="col == 'isPk'" :checked="text=='1'"
                              @change="e => handleChangeRow(e.target.checked, record.id, col,record)"/>
                  <a-input-number allowClear v-else-if="col == 'fieldLength' " style="width:100%;margin: -5px 0;"
                                  :min="record.fieldLength?record.fieldLength:0"
                                  :value="text" @change="value => handleChangeRow(value, record.id, col,record)"/>
                  <a-input allowClear v-else style="margin: -5px 0" :value="text"
                           @change="e => handleChangeRow(e.target.value, record.id, col,record)"/>
                </template>
                <template v-else>
                  <span v-if="col == 'isPk'">
                    <a-tag v-if="text=='1'" color="blue">Y</a-tag>
                    <a-tag v-else>N</a-tag>
                  </span>
                  <span v-else-if="col == 'fieldName' && record.dataState=='1'" style="color: blue">
                    {{ text }}
                  </span>
                  <span v-else-if="col == 'fieldName' && record.dataState=='2'" style="color: sandybrown">
                    {{ text }}
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
                  <a :disabled="editingKey !== ''" @click="() => delRow(record.id)" style="margin-left: 10px;">Delete</a>
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
import axios from 'axios'

export default {
  name: 'MetadataTableModal',
  components: {},
  data() {
    return {
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
        tableComment: { rules: [{ required: true, message: 'Please Input Table Annotation!' }] }
      },
      url: {
        add: '/sys/dataSource/add',
        edit: '/sys/dataSource/edit'
      },
      dsDictItems: [],
      dbList: [],

      data: [],
      cacheData: [],
      fieldData: [],
      rowEditColumns: ['fieldName', 'fieldType', 'fieldLength', 'isPk', 'fieldComment'],
      columns: [
        {
          title: 'Field Name',
          dataIndex: 'fieldName',
          width: 200,
          scopedSlots: { customRender: 'fieldName' }
        },
        {
          title: 'Field Type',
          dataIndex: 'fieldType',
          width: 100,
          scopedSlots: { customRender: 'fieldType' }
        },
        {
          title: 'Field Length',
          dataIndex: 'fieldLength',
          width: 80,
          scopedSlots: { customRender: 'fieldLength' }
        },
        {
          title: 'Field Annotation',
          dataIndex: 'fieldComment',
          scopedSlots: { customRender: 'fieldComment' },
          width: 200,
          ellipsis: true
        },
        {
          title: 'isPk',
          dataIndex: 'isPk',
          width: 80,
          scopedSlots: { customRender: 'isPk' }
        },
        /*{
          title: '外键关联表',
          dataIndex: 'fkTableName',
          scopedSlots: { customRender: 'fkTableName' },
          width: 240,
          ellipsis: true
        },
        {
          title: '外键关联字段',
          dataIndex: 'fkFieldName',
          scopedSlots: { customRender: 'fkFieldName' },
          width: 180,
          ellipsis: true
        },*/
        {
          title: 'Operation',
          dataIndex: 'operation',
          scopedSlots: { customRender: 'operation' },
          width: 100
          // ,fixed: 'right'
        }
      ],
      editingKey: '',
      dataTypes: [],
      dsCode: '',
      dbName: '',
      fkTableNames: [],
      fkFieldNames: [],
      fkFieldMaps: {},
      fieldAddNo: 0
    }
  },
  created() {
    this.loadDataType()
  },
  methods: {
    loadDsDictItems() {
      getAction('/sys/dataSource/getDsDict', {}).then(res => {
        if (res.success == true) {
          this.dsDictItems = res.result
        } else {
          this.$message.error('Data Source For failure!')
          console.error(res)
        }
      })
    },
    handleDsChange(selectedItem) {
      this.dsCode = selectedItem
      getAction('/md/show-databases', { dsCode: selectedItem }).then(res => {
        if (res.success == true) {
          this.dbList = res.result
        } else {
          this.$message.error('Database For failure!')
          console.error(res)
        }
      })
    },
    handleDbChange(selectedItem) {
      this.dbName = selectedItem
      this.loadFkTableNames(this.dsCode, this.dbName, '')
    },
    loadFieldsData(dsCode, dbName, tableName) {
      getAction('/md/fields/list', { dsCode: dsCode, dbName: dbName, tableName: tableName }).then(res => {
        if (res.success == true) {
          this.data = res.result
          this.cacheData = this.data.map(item => ({ ...item }))
          this.fieldData = this.data.map(item => ({ ...item }))
        } else {
          this.$message.error('Field  For failure!')
          console.error(res)
        }
      })
    },
    loadDataType() {
      //优先从缓存中读取字典配置
      if (getDictItemsFromCache('field_data_type')) {
        this.dataTypes = getDictItemsFromCache('field_data_type')
        return
      }
      //根据字典Code, 初始化字典数组
      ajaxGetDictItems('field_data_type', null).then((res) => {
        if (res.success) {
          this.dataTypes = res.result
        }
      })
    },
    loadFkTableNames(dsCode, dbName, tableName) {
      postAction('/editor/load-tables', {
        dsCode: dsCode,
        dbCode: dbName,
        searchText: ''
      }).then(res => {
        if (res.success == true) {
          this.fkTableNames = res.result.filter(item => tableName != item.tableName)
        } else {
          this.$message.error('Table For failure!')
          console.error(res)
        }
      })
    },
    add() {
      this.data = []
      this.cacheData = []
      this.fieldData = []
      this.dsCode = ''
      this.dbName = ''
      this.dbList = []
      this.fkTableNames = []
      this.fkFieldNames = []
      this.fieldAddNo = 0
      this.editingKey = ''
      this.edit({})
    },
    edit(record) {
      this.loadDsDictItems()
      if (record.tableName) {
        this.dsCode = record.dsCode
        this.dbName = record.dbName
        this.loadFieldsData(record.dsCode, record.dbName, record.tableName)
        // this.loadFkTableNames(record.dsCode, record.dbName, record.tableName);
      }
      this.form.resetFields()
      this.model = Object.assign({}, record)
      this.visible = true
      this.editingKey = ''
      this.$nextTick(() => {
        this.form.setFieldsValue(pick(this.model, 'dsCode', 'dbName', 'tableName', 'tableComment', 'tableRemark'))
      })
    },
    close() {
      this.$emit('close')
      this.visible = false
    },
    handleOk() {
      this.form.validateFields((err, values) => {
        if (!err) {
          if (this.data.length == 0) {
            this.$message.warning('Field Cannot Be Empty！')
            return
          }
          let formData = this.form.getFieldsValue()
          postAction('/md/tables/save-remote', {
            table: formData,
            fields: this.data,
            isAdd: !this.model.id,
            hasExt: false
          }).then(res => {
            if (res.success == true) {
              this.$message.success('Operation is successful！')
              this.$emit('ok')
              this.close()
            } else {
              this.$message.error('Category Design Save Failure!！')
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
    filterOption(input, option) {
      return (
        option.componentOptions.children[0].children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
      )
    },
    handleChangeRow(value, id, column, row, option) {
      if (!value) {
        if (column === 'fieldName') {
          this.$message.warning('Field names cannot be empty！')
          return
        }
        if (column === 'fieldType') {
          this.$message.warning('The field type cannot be empty！')
          return
        }
        // if(column==='fieldComment'){
        //   this.$message.warning('字段注释不能为空！');
        //   return ;
        // }
      }
      if (value <= 0 && column === 'fieldLength' && row.fieldType === 'varchar') {
        this.$message.warning('The value is a string that cannot contain 0 characters！')
        return
      }

      if (column == 'isPk') {
        if (value) {
          let p = 0
          this.data.forEach(row => {
            if (row.isPk == '1') {
              p++
            }
          })
          if (p > 0) {
            this.$message.warning('The table already has primary keys. You are not advised to set the joint primary key')
            return
          }
          this.$message.info('Note that when setting a primary key, ensure that there are no null values in this column and that the column values are not duplicated！')
        }
      }
      const newData = [...this.data]
      const target = newData.filter(item => id === item.id)[0]
      if (target) {
        if (column == 'isNullable' || column == 'isPk') {
          target[column] = value ? '1' : '0'
        } else {
          target[column] = value
        }
        if (target.id.indexOf('field-add-') > -1) {
          target['dataState'] = '1'
        }
        if (target['dataState'] == '0') {
          target['dataState'] = '2'
        }
        if (column == 'fkTableName') {
          postAction('/editor/load-fields', {
            dsCode: this.dsCode,
            dbCode: this.dbName,
            tableName: value
          }).then(res => {
            if (res.success == true) {
              this.fkFieldNames = res.result
              this.fkFieldNames.forEach((item, index) => {
                this.fkFieldMaps[item.key] = item
              })
            } else {
              this.$message.error('Table For failure!')
              console.error(res)
            }
          })
        } else if (column == 'fkFieldName') {
          let fkField = this.fkFieldMaps[option.key]
          row.fieldType = fkField.fieldType
          row.fieldLength = fkField.fieldLength
          row.fieldDigit = fkField.fieldDigit
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
    delRow(id) {
      this.data.forEach((item, index) => {
        if (id === item.id) {
          this.data.splice(index, 1)
          this.cacheData.splice(index, 1)
        }
      })
    },
    saveRow(id) {
      const newData = [...this.data]
      const newCacheData = [...this.cacheData]
      const target = newData.filter(item => id === item.id)[0]
      if (target) {
        if (!target['fieldName']) {
          this.$message.warning('Field names cannot be empty！')
          return
        }
        if (!target['fieldType']) {
          this.$message.warning('The field type cannot be empty！')
          return
        }
        // if(!target['fieldComment']){
        //   this.$message.warning('字段注释不能为空！');
        //   return ;
        // }

        let pattern = /^[a-z|A-Z][a-z|A-Z\d_]{0,}$/
        if (!pattern.test(target['fieldName'])) {
          this.$message.warning('The field name must start with a letter and can contain digits or underscores！')
          return
        }

        if ((!target['fieldLength'] || target['fieldLength'] <= 0) && target['fieldType'] === 'varchar') {
          this.$message.warning('The value is a string that cannot contain 0 characters！')
          return
        }
      }
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
<template>
  <a-modal
    :title="title"
    :width="1000"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
    cancelText="Close">

    <a-spin :spinning="confirmLoading">
      <a-form-model ref="form" :model="model" :rules="validatorRules">

        <a-form-model-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="Strategy Name" prop="ruleName">
          <a-input placeholder="Plases Input Strategy Name" v-model="model.ruleName"/>
        </a-form-model-item>
        <a-form-model-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="Strategy Code" prop="ruleCode">
          <a-input placeholder="Plases Input Strategy Code" v-model="model.ruleCode"/>
        </a-form-model-item>
        <a-form-model-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="Strategy Description" prop="ruleDescription">
          <a-textarea placeholder="Plases Input Strategy Description" v-model="model.ruleDescription"/>
        </a-form-model-item>

      </a-form-model>
      <!-- 规则设计 -->
      <!--<a-tabs v-model="tabs.activeKey">
        <a-tab-pane tab="局部规则" :key="tabs.design.key" forceRender>
          <a-alert type="info" showIcon message="局部规则按照你输入的位数有序的校验。"/>
          <j-editable-table ref="designTable" dragSort rowNumber  :maxHeight="240" :columns="tabs.design.columns" :dataSource="tabs.design.dataSource" style="margin-top: 8px;">

            <template #action="props">
              <my-action-button :rowEvent="props"/>
            </template>

          </j-editable-table>
        </a-tab-pane>

        <a-tab-pane tab="全局规则" :key="tabs.global.key" forceRender>
          <j-editable-table ref="globalTable" dragSort rowNumber actionButton :maxHeight="240" :columns="tabs.global.columns"  :dataSource="tabs.global.dataSource">

            <template #actionButtonAfter>
              <a-alert type="info" showIcon message="全局规则可校验用户输入的所有字符；全局规则的优先级比局部规则的要高。" style="margin-bottom: 8px;"/>
            </template>

            <template #action="props">
              <my-action-button :rowEvent="props" allowEmpty/>
            </template>

          </j-editable-table>
        </a-tab-pane>
      </a-tabs>-->
      <a-divider orientation="left">
        Strategy of design
      </a-divider>
      <j-editable-table ref="globalTable" dragSort rowNumber :maxHeight="240" :columns="tabs.global.columns"
                        :dataSource="tabs.global.dataSource">

        <template #actionButtonAfter>
          <a-alert type="info" showIcon message="A global rule validates all characters entered by the user!" style="margin-bottom: 8px;"/>
        </template>

        <template #action="props">
          <my-action-button :rowEvent="props" allowEmpty/>
        </template>

      </j-editable-table>
    </a-spin>
  </a-modal>
</template>

<script>
import pick from 'lodash.pick'
import { httpAction } from '@/api/manage'
import { validateDuplicateValue, alwaysResolve, failedSymbol } from '@/utils/util'
import { FormTypes } from '@/utils/JEditableTableUtil'
import JEditableTable from '@comp/jeecg/JEditableTable'

  export default {
    name: 'SysCheckRuleModal',
    components: {
      JEditableTable,
      'my-action-button': {
        props: { rowEvent: Object, allowEmpty: Boolean },
        methods: {
          confirmIsShow() {
            const { index, allValues: { inputValues } } = this.rowEvent
            let value = inputValues[index]
            return value.digits || value.pattern
          },
          handleLineAdd() {
            const { target } = this.rowEvent
            target.add()
          },
          handleLineDelete() {
            const { rowId, target } = this.rowEvent
            target.removeRows(rowId)
          },
          renderDeleteButton() {
            if (this.allowEmpty || this.rowEvent.index > 0) {
              if (this.confirmIsShow()) {
                return (
                  <a-popconfirm title="Sure you want to Delete ?" onConfirm={this.handleLineDelete}>
                    <a-button icon="minus"/>
                  </a-popconfirm>
                )
              } else {
                return (
                  <a-button icon="minus" onClick={this.handleLineDelete}/>
                )
              }
            }
            return ''
          },
        },
        render() {
          return (
            <div>
              <a-button onClick={this.handleLineAdd} icon="plus"/>
              &nbsp;
              {this.renderDeleteButton()}
            </div>
          )
        }
      }
    },
    data() {
      return {
        title: 'Operation',
        visible: false,
        model: {},
        labelCol: {
          xs: { span: 24 },
          sm: { span: 5 },
        },
        wrapperCol: {
          xs: { span: 24 },
          sm: { span: 16 },
        },
        confirmLoading: false,
        validatorRules: {
          ruleName: [{required: true, message: 'Plases Input Strategy Name!'}],
          ruleCode: [
            {required: true, message: 'Plases Input Strategy Code!'},
            {validator: (rule, value, callback) => validateDuplicateValue('sys_check_rule', 'rule_code', value, this.model.id, callback)}
          ],
        },
        tabs: {
          activeKey: 'design',
          global: {
            key: 'global',
            columns: [
              {
                title: 'Priority',
                key: 'priority',
                width: '15%',
                type: FormTypes.select,
                defaultValue: '1',
                options: [
                  { title: 'Priority to run', value: '1' },
                  { title: 'The last run', value: '0' },
                ],
                validateRules: []
              },
              {
                title: 'Strategy',
                key: 'pattern',
                width: '50%',
                type: FormTypes.input,
                validateRules: [
                  { required: true, message: 'Strategy cannot be empty' },
                  { handler: this.validatePatternHandler },
                ]
              },
              {
                title: 'Tooltip text',
                key: 'message',
                width: '20%',
                type: FormTypes.input,
                validateRules: [
                  { required: true, message: '${title} cannot be empty' },
                ]
              },
              {
                title: 'Operation',
                key: 'action',
                width: '15%',
                slotName: 'action',
                type: FormTypes.slot
              }
            ],
            dataSource: [],
          },
          design: {
            key: 'design',
            columns: [
              {
                title: 'design',
                key: 'digits',
                width: '15%',
                type: FormTypes.inputNumber,
                validateRules: [
                  { required: true, message: '${title} cannot be empty' },
                  { pattern: /^[1-9]\d*$/, message: 'Please enter a positive integer above zero' },
                ]
              },
              {
                title: 'Strategy',
                key: 'pattern',
                width: '50%',
                type: FormTypes.input,
                validateRules: [
                  { required: true, message: 'Strategy cannot be empty' },
                  { handler: this.validatePatternHandler }
                ]
              },
              {
                title: 'Tooltip text',
                key: 'message',
                width: '20%',
                type: FormTypes.input,
                validateRules: [
                  { required: true, message: '${title} cannot be empty' },
                ]
              },
              {
                title: 'Operation',
                key: 'action',
                width: '15%',
                slotName: 'action',
                type: FormTypes.slot
              },
            ],
            dataSource: [],
          }
        },
        url: {
          add: '/sys/checkRule/add',
          edit: '/sys/checkRule/edit',
        },
      }
    },
    created() {
    },
    methods: {

    validatePatternHandler(type, value, row, column, callback, target) {
      if (type === 'blur' || type === 'getValues') {
        try {
          new RegExp(value)
          callback(true)
        } catch (e) {
          callback(false, 'Please Enter a correct regular expression')
        }
      } else {
        callback(true) // 不填写或者填写 null 代表不进行任何操作
      }
    },

    add() {
      this.edit({})
    },
    edit(record) {
      this.tabs.activeKey = this.tabs.design.key
      this.tabs.global.dataSource = [{ priority: '', pattern: '', message: '' }]
      this.tabs.design.dataSource = [{ digits: '', pattern: '', message: '' }]
      this.visible = true
      this.$nextTick(() => {
        this.$refs.form.resetFields()
        this.model = Object.assign({}, record)

        // 子表数据
        let ruleJson = this.model.ruleJson
        if (ruleJson) {
          let ruleList = JSON.parse(ruleJson)
          // 筛选出全局规则和局部规则
          let global = [], design = [], priority = '1'
          ruleList.forEach(rule => {
            if (rule.digits === '*') {
              global.push(Object.assign(rule, { priority }))
            } else {
              priority = '0'
              design.push(rule)
            }
          })
          this.tabs.global.dataSource = global
          this.tabs.design.dataSource = design
        }
      })
    },
    close() {
      this.$emit('close')
      this.visible = false
    },
    handleOk() {
      Promise.all([
        // 主表单校验
        alwaysResolve(new Promise((resolve, reject) => {
          this.$refs.form.validate((ok, err) => ok ? resolve(this.model) : reject(err))
        })),
        // 局部规则子表校验
        // alwaysResolve(this.$refs.designTable.getValuesPromise),
        // 全局规则子表校验
        alwaysResolve(this.$refs.globalTable.getValuesPromise)
      ]).then(results => {
        let [mainResult, globalResult] = results

        if (mainResult.type === failedSymbol) {
          return Promise.reject('The main table fails to pass the check')
        }
          // else if (designResult.type === failedSymbol) {
          //   this.tabs.activeKey = this.tabs.design.key
          //   return Promise.reject('局部规则子表校验未通过')
        // }
        else if (globalResult.type === failedSymbol) {
          this.tabs.activeKey = this.tabs.global.key
          return Promise.reject('The global rule subtable fails to be verified')
        } else {
          // 所有校验已通过，这一步是整合数据
          let mainValues = mainResult.data, globalValues = globalResult.data

          // 整合两个子表的数据
          let firstGlobal = [], afterGlobal = []
          globalValues.forEach(v => {
            v.digits = '*'
            if (v.priority === '1') {
              firstGlobal.push(v)
            } else {
              afterGlobal.push(v)
            }
          })
          let concatValues = firstGlobal.concat(afterGlobal)
          let subValues = concatValues.map(i => pick(i, 'digits', 'pattern', 'message'))

          // 生成 formData，用于传入后台
          let ruleJson = JSON.stringify(subValues)
          let formData = Object.assign(this.model, mainValues, { ruleJson })

          // 判断请求方式和请求地址，并发送请求
          let method = 'post', httpUrl = this.url.add
          if (this.model.id) {
            method = 'put'
            httpUrl = this.url.edit
          }
          this.confirmLoading = true
          return httpAction(httpUrl, formData, method)
        }
      }).then((res) => {
        if (res.success) {
          this.$message.success(res.message)
          this.$emit('ok')
          this.close()
        } else {
          this.$message.warning(res.message)
        }
      }).catch(e => {
        console.error(e)
      }).finally(() => {
        this.confirmLoading = false
      })
    },
    handleCancel() {
      this.close()
    }

  }
}
</script>

<style lang="less" scoped></style>
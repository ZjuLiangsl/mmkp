<template>
  <a-modal
    :title="title"
    :width="800"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
    cancelText="Close">

    <a-spin :spinning="confirmLoading">
      <a-form :form="form">

        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="Data Source Code">
          <a-input placeholder="Please Input Data Source Code" :disabled="!!model.id"
                   v-decorator="['code', validatorRules.code]"/>
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="Data Source Name">
          <a-input placeholder="Please Input Data Source Name" v-decorator="['name', validatorRules.name]"/>
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="Database Type">
          <j-dict-select-tag placeholder="Please SelectDatabase Type" dict-code="database_type" triggerChange
                             v-decorator="['dbType', validatorRules.dbType]" @change="handleDbTypeChange"/>
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="Driver">
          <a-input placeholder="Please Input Driver" v-decorator="['dbDriver', validatorRules.dbDriver]"/>
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="Source Address">
          <a-input placeholder="Please Input Source Address" v-decorator="['dbUrl', validatorRules.dbUrl]"/>
        </a-form-item>

        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="User">
          <a-input placeholder="Please Input User Account" v-decorator="['dbUsername', validatorRules.dbUsername]"/>
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="Password">
          <a-row :gutter="8">
            <a-col :span="21">
              <a-input-password placeholder=" Please Input Password"
                                v-decorator="['dbPassword', validatorRules.dbPassword]"/>
            </a-col>
            <a-col :span="3">
              <a-button type="primary" size="small" style="width: 100%" @click="handleTest">Test</a-button>
            </a-col>
          </a-row>
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="Remark">
          <a-textarea placeholder="Please Input Remark" v-decorator="['remark', {}]"/>
        </a-form-item>
      </a-form>
    </a-spin>
  </a-modal>
</template>

<script>
import pick from 'lodash.pick'
import { httpAction, postAction } from '@/api/manage'
import { validateDuplicateValue } from '@/utils/util'

export default {
  name: 'SysDataSourceModal',
  components: {},
  data() {
    return {
      title: 'Operation',
      visible: false,
      model: {},
      labelCol: {
        xs: { span: 24 },
        sm: { span: 5 }
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 16 }
      },

      confirmLoading: false,
      form: this.$form.createForm(this),
      validatorRules: {
        code: {
          validateFirst: true,
          rules: [
            { required: true, message: 'Please Input Data Source Code!' },
            {
              validator: (rule, value, callback) => {
                let pattern = /^[a-z|A-Z][a-z|A-Z\d_-]{0,}$/
                if (!pattern.test(value)) {
                  callback('The code must start with a letter and can contain digits, underscores, and dashes')
                } else {
                  validateDuplicateValue('sys_data_source', 'code', value, this.model.id, callback)
                }
              }
            }
          ]
        },
        name: { rules: [{ required: true, message: 'Please Input Data Source Name!' }] },
        dbType: { rules: [{ required: true, message: 'Please SelectDatabase Type!' }] },
        dbDriver: { rules: [{ required: true, message: 'Please Input Driver!' }] },
        dbUrl: { rules: [{ required: true, message: 'Please Input Source Address!' }] },
        dbName: { rules: [{ required: true, message: 'Please Input dbName!' }] },
        dbUsername: { rules: [{ required: true, message: 'Please Input User Account!' }] },
        dbPassword: { rules: [{ required: false, message: ' Please Input Password!' }] }
      },
      url: {
        add: '/sys/dataSource/add',
        edit: '/sys/dataSource/edit'
      },
      dbDriverMap: {
        'mysql': { dbDriver: 'com.mysql.cj.jdbc.Driver' }
      },
      dbUrlMap: {
        'mysql': { dbUrl: 'jdbc:mysql://127.0.0.1:3306/pmkb-db?characterEncoding=UTF-8&useUnicode=true&useSSL=false&tinyInt1isBit=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai' }
      }
    }
  },
  created() {
  },
  methods: {
    add() {
      this.edit({})
    },
    edit(record) {
      this.form.resetFields()
      this.model = Object.assign({}, record)
      this.visible = true
      this.$nextTick(() => {
        this.form.setFieldsValue(pick(this.model, 'code', 'name', 'remark', 'dbType', 'dbDriver', 'dbUrl', 'dbUsername', 'dbPassword'))
      })
    },
    close() {
      this.$emit('close')
      this.visible = false
    },
    handleOk() {
      this.form.validateFields((err, values) => {
        if (!err) {
          this.confirmLoading = true
          let formData = Object.assign(this.model, values)
          let httpUrl = this.url.add, method = 'post'
          if (this.model.id) {
            httpUrl = this.url.edit
            method = 'put'
            formData['code'] = undefined
          }
          httpAction(httpUrl, formData, method).then((res) => {
            if (res.success) {
              this.$message.success(res.message)
              this.$emit('ok')
              this.close()
            } else {
              this.$message.warning(res.message)
            }
          }).finally(() => {
            this.confirmLoading = false
          })
        }
      })
    },
    handleCancel() {
      this.close()
    },
    handleTest() {
      let keys = ['dbType', 'dbDriver', 'dbUrl', 'dbName', 'dbUsername', 'dbPassword']
      let fieldsValues = this.form.getFieldsValue(keys)
      let setFields = {}
      keys.forEach(key => setFields[key] = { value: fieldsValues[key], errors: null })
      this.form.setFields(setFields)
      this.$nextTick(() => {
        this.form.validateFields(keys, (errors, values) => {
          if (!errors) {
            let loading = this.$message.loading('Connection……', 0)
            postAction('/sys/dataSource/testConnection', fieldsValues).then(res => {
              if (res.success) {
                this.$message.success('Connection Success')
              } else {
                throw new Error(res.message)
              }
            }).catch(error => {
              this.$warning({ title: 'Connection Failure', content: error.message || error })
            }).finally(() => loading())
          }
        })
      })
    },
    handleDbTypeChange(val) {
      let dbDriver = this.dbDriverMap[val]
      let dbUrl = this.dbUrlMap[val]
      if (dbDriver) {
        this.form.setFieldsValue(dbDriver)
      }
      if (dbUrl) {
        this.form.setFieldsValue(dbUrl)
      }
    }
  }
}
</script>

<style lang="less" scoped></style>
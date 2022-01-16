<template>
  <a-modal
    title="重新设定密码"
    :width="800"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleSubmit"
    @cancel="handleCancel"
    cancelText="关闭"
    style="top:20px;"
  >
    <a-spin :spinning="confirmLoading">
      <a-form :form="form">

        <a-form-item label="Account" :labelCol="labelCol" :wrapperCol="wrapperCol">
          <a-input placeholder="Please Input Account" v-decorator="[ 'username', {}]" :readOnly="true"/>
        </a-form-item>

        <a-form-item label="Password " :labelCol="labelCol" :wrapperCol="wrapperCol" hasFeedback>
          <a-input type="password" placeholder="Please Input password" v-decorator="[ 'password', validatorRules.password]"/>
        </a-form-item>

        <a-form-item label="Confirm password" :labelCol="labelCol" :wrapperCol="wrapperCol" hasFeedback>
          <a-input type="password" @blur="handleConfirmBlur" placeholder="Please Confirm password"
                   v-decorator="[ 'confirmpassword', validatorRules.confirmpassword]"/>
        </a-form-item>

      </a-form>
    </a-spin>
  </a-modal>
</template>

<script>
import { changePassword } from '@/api/api'

export default {
  name: 'PasswordModal',
  data() {
    return {
      visible: false,
      confirmLoading: false,
      confirmDirty: false,
      validatorRules: {
        password: {
          rules: [{
            required: true,
            pattern: /^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z].{6,}$/,
            message: 'Password consists of 6 digits, upper - and lower-case letters！'
          }, {
            validator: this.validateToNextPassword
          }]
        },
        confirmpassword: {
          rules: [{
            required: true, message: 'Please Input the password again!'
          }, {
            validator: this.compareToFirstPassword
          }]
        }
      },

      model: {},

      labelCol: {
        xs: { span: 24 },
        sm: { span: 5 }
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 16 }
      },
      form: this.$form.createForm(this)
    }
  },
  created() {
    console.log('created')
  },

  methods: {
    show(username) {
      this.form.resetFields()
      this.visible = true
      this.model.username = username
      this.$nextTick(() => {
        this.form.setFieldsValue({ username: username })
      })
    },
    close() {
      this.$emit('close')
      this.visible = false
      this.disableSubmit = false
      this.selectedRole = []
    },
    handleSubmit() {
      // 触发表单验证
      this.form.validateFields((err, values) => {
        if (!err) {
          this.confirmLoading = true
          let formData = Object.assign(this.model, values)
          changePassword(formData).then((res) => {
            if (res.success) {
              this.$message.success(res.message)
              this.$emit('ok')
            } else {
              this.$message.warning(res.message)
            }
          }).finally(() => {
            this.confirmLoading = false
            this.close()
          })
        }
      })
    },
    handleCancel() {
      this.close()
    },
    validateToNextPassword(rule, value, callback) {
      const form = this.form
      const confirmpassword = form.getFieldValue('confirmpassword')
      console.log('confirmpassword==>', confirmpassword)
      if (value && confirmpassword && value !== confirmpassword) {
        callback('The two passwords are different！')
      }
      if (value && this.confirmDirty) {
        form.validateFields(['confirm'], { force: true })
      }
      callback()
    },
    compareToFirstPassword(rule, value, callback) {
      const form = this.form
      if (value && value !== form.getFieldValue('password')) {
        callback('The two passwords are different！')
      } else {
        callback()
      }
    },
    handleConfirmBlur(e) {
      const value = e.target.value
      this.confirmDirty = this.confirmDirty || !!value
    }
  }
}
</script>
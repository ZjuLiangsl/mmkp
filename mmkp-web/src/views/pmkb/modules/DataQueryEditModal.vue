<template>
  <a-modal
    :title="title"
    :width="formWidth"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
    cancelText="Close">

    <a-spin :spinning="confirmLoading">
      <form-create :rule="formFields" v-model="fApi" :value.sync="model" :option="fOptions" @mounted="fMounted"
                   @fk-search="fkSearch" @fk-change="fkSearch"/>
    </a-spin>

    <fk-select-modal ref="fkModal" @ok="onOk"/>
  </a-modal>
</template>

<script>
import pick from 'lodash.pick'
import { httpAction, postAction } from '@/api/manage'
import FkSelectModal from '@views/pmkb/modules/FkSelectModal'
import moment from 'moment'

export default {
  name: 'DataQueryEditModal',
  props: {
    formFields: {
      type: Array,
      default: []
    },
    pkFields: {
      type: Array,
      default: []
    },
    formWidth: {
      type: Number,
      default: 1000
    },
    table: {
      type: Object,
      default: {}
    }
  },
  components: { FkSelectModal },
  data() {
    return {
      title: 'Operation',
      visible: false,
      model: {},
      confirmLoading: false,
      fApi: null,
      fOptions: {
        submitBtn: false,
        resetBtn: false,
        global: {
          '*': {
            col: {
              span: 12
            },
            wrap: {
              labelCol: { span: 4 },
              wrapperCol: { span: 20 }
            }
          }
        }
      },
      isEdit: true,
      url: {
        add: '/dqe/add',
        edit: '/dqe/edit'
      }
    }
  },
  watch: {
    visible(val, oldVal) {
      if (!val) {
        Object.keys(this.model).forEach((key) => (this.model[key] = null))
      }
    }
  },
  created() {
  },
  methods: {
    add() {
      this.isEdit = false
      this.edit(null)
    },
    edit(record) {
      if (record) {
        this.isEdit = true
      } else {
        this.isEdit = false
        record = {}
      }
      this.formFields.forEach(f => {
        let type = f.type
        let field = f.field
        if (record[field]) {
          this.model[field] = record[field]
        } else {
          this.model[field] = null
        }
      })
      this.visible = true
      if (this.fApi) {
        this.pkFields.forEach(p => {
          const rule = this.fApi.getRule(p)
          rule.props.disabled = this.isEdit
        })
      }
    },
    close() {
      this.$emit('close')
      this.visible = false
    },
    handleOk() {
      this.fApi.validate((valid, fail) => {
        if (valid) {
          this.confirmLoading = true
          this.formFields.forEach(f => {
            let type = f.type
            let field = f.field
            if ('a-date-picker' == type) {
              if (this.model[field]) {
                this.model[field] = moment(this.model[field]).format(f.props['format'])
              }
            }
          })
          let httpUrl = this.url.add, method = 'post'
          if (this.isEdit) {
            httpUrl = this.url.edit
          }
          httpAction(httpUrl, {
            table: this.table,
            record: this.model,
            pkFields: this.pkFields
          }, method).then((res) => {
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
        } else {
          // 表单验证未通过
        }
      })
    },
    handleCancel() {
      this.close()
    },
    fMounted(fApi) {
      this.fApi = fApi
      this.pkFields.forEach(p => {
        const rule = this.fApi.getRule(p)
        rule.props.disabled = this.isEdit
      })
    },
    fkSearch(inject) {
      let title = inject.self.title
      let dsCode = inject.inject.dsCode
      let fkDbName = inject.inject.fkDbName
      let fkTableName = inject.inject.fkTableName
      let fkFieldName = inject.inject.fkFieldName
      let fieldName = inject.self.field
      this.$refs.fkModal.loadMeta(title, dsCode, fkDbName, fkTableName, fkFieldName, fieldName)
    },
    onOk(fieldName, fieldValue) {
      this.fApi.setValue(fieldName, fieldValue)
    }
  }
}
</script>

<style lang="less" scoped></style>
<template>
  <a-drawer
    :title="title"
    :width="drawerWidth"
    @close="handleCancel"
    :visible="visible"
    :confirmLoading="confirmLoading">

    <div :style="{width: '100%',border: '1px solid #e9e9e9',padding: '10px 16px',background: '#fff',}">
      <a-spin :spinning="confirmLoading">
        <a-form-model ref="form" :model="model" :rules="validatorRules">

          <a-form-model-item label="Type" :labelCol="labelCol" :wrapperCol="wrapperCol">
            <a-radio-group @change="onChangeMenuType" v-model="model.menuType">
              <a-radio :value="0">Menu</a-radio>
              <a-radio :value="1">Sub Menu</a-radio>
              <a-radio :value="2">Button/Permission</a-radio>
            </a-radio-group>
          </a-form-model-item>

          <a-form-model-item
            :labelCol="labelCol"
            :wrapperCol="wrapperCol"
            :label="menuLabel"
            prop="name"
            hasFeedback>
            <a-input placeholder="Please Input Menu Name" v-model="model.name" :readOnly="disableSubmit"/>
          </a-form-model-item>


          <a-form-model-item
            v-show="model.menuType!=0"
            label="Previous Menu"
            :labelCol="labelCol"
            :wrapperCol="wrapperCol"
            :validate-status="validateStatus"
            :hasFeedback="true"
            :required="true">
            <span slot="help">{{ validateStatus == 'error' ? 'Please Select Previous Menu' : '&nbsp;&nbsp;' }}</span>
            <a-tree-select
              style="width:100%"
              :dropdownStyle="{ maxHeight: '200px', overflow: 'auto' }"
              :treeData="treeData"
              v-model="model.parentId"
              placeholder="Please Select Previous Menu"
              :disabled="disableSubmit"
              @change="handleParentIdChange">
            </a-tree-select>
          </a-form-model-item>

          <a-form-model-item
            :labelCol="labelCol"
            :wrapperCol="wrapperCol"
            prop="url"
            label="Path ">
            <a-input placeholder="Please Input Path" v-model="model.url" :readOnly="disableSubmit"/>
          </a-form-model-item>

          <a-form-model-item
            v-show="show"
            :labelCol="labelCol"
            :wrapperCol="wrapperCol"
            prop="component"
            label="Unit">
            <a-input placeholder="Please Input Unit" v-model="model.component" :readOnly="disableSubmit"/>
          </a-form-model-item>

          <a-form-model-item
            v-show="model.menuType==0"
            :labelCol="labelCol"
            :wrapperCol="wrapperCol"
            label="Default Url">
            <a-input placeholder="Please Input Default Url" v-model="model.redirect" :readOnly="disableSubmit"/>
          </a-form-model-item>

          <a-form-model-item
            v-show="!show"
            :labelCol="labelCol"
            :wrapperCol="wrapperCol"
            prop="perms"
            label="Authorization Id">
            <a-input placeholder="Please Input Authorization Id, Eg: user:list" v-model="model.perms" :readOnly="disableSubmit"/>
          </a-form-model-item>

          <a-form-model-item
            v-show="!show"
            :labelCol="labelCol"
            :wrapperCol="wrapperCol"
            label="Authorization Plicy">
            <j-dict-select-tag v-model="model.permsType" placeholder="Please Select Authorization Plicy" :type="'radio'"
                               dictCode="global_perms_type"/>


          </a-form-model-item>
          <a-form-model-item
            v-show="!show"
            :labelCol="labelCol"
            :wrapperCol="wrapperCol"
            label="Status">
            <j-dict-select-tag v-model="model.status" placeholder="Please Select Status" :type="'radio'" dictCode="valid_status"/>

          </a-form-model-item>

          <a-form-model-item
            v-show="show"
            :labelCol="labelCol"
            :wrapperCol="wrapperCol"
            label="Icon">
            <a-input placeholder="Please Select Icon" v-model="model.icon" :readOnly="disableSubmit">
              <a-icon slot="addonAfter" type="setting" @click="selectIcons"/>
            </a-input>
          </a-form-model-item>

          <a-form-model-item
            v-show="show"
            :labelCol="labelCol"
            :wrapperCol="wrapperCol"
            prop="sortNo"
            label="Order">
            <a-input-number placeholder="Please Input Order" v-model="model.sortNo" style="width: 200px"
                            :readOnly="disableSubmit"/>
          </a-form-model-item>

          <a-form-model-item
            v-show="show"
            :labelCol="labelCol"
            :wrapperCol="wrapperCol"
            label="Route Menu">
            <a-switch checkedChildren="Y" unCheckedChildren="N" v-model="model.route"/>
          </a-form-model-item>

          <a-form-model-item
            v-show="show"
            :labelCol="labelCol"
            :wrapperCol="wrapperCol"
            label="Hide Route">
            <a-switch checkedChildren="Y" unCheckedChildren="N" v-model="model.hidden"/>
          </a-form-model-item>

          <a-form-model-item
            v-show="show"
            :labelCol="labelCol"
            :wrapperCol="wrapperCol"
            label="Cache Route">
            <a-switch checkedChildren="Y" unCheckedChildren="N" v-model="model.keepAlive"/>
          </a-form-model-item>


          <a-form-model-item
            v-show="show"
            :labelCol="labelCol"
            :wrapperCol="wrapperCol"
            label="Merge Route">
            <a-switch checkedChildren="Y" unCheckedChildren="N" v-model="model.alwaysShow"/>
          </a-form-model-item>

          <a-form-model-item
            v-show="show"
            :labelCol="labelCol"
            :wrapperCol="wrapperCol"
            label="Open Type">
            <a-switch checkedChildren="external" unCheckedChildren="internal" v-model="model.internalOrExternal"/>
          </a-form-model-item>

        </a-form-model>

        <!-- 选择图标 -->
        <icons @choose="handleIconChoose" @close="handleIconCancel" :iconChooseVisible="iconChooseVisible"></icons>
      </a-spin>
      <a-row :style="{textAlign:'right'}">
        <a-button :style="{marginRight: '8px'}" @click="handleCancel">
          Close
        </a-button>
        <a-button :disabled="disableSubmit" @click="handleOk" type="primary">Save</a-button>
      </a-row>
    </div>
  </a-drawer>
</template>

<script>
import { addPermission, editPermission, queryTreeList, duplicateCheck } from '@/api/api'
import Icons from './icon/Icons'

export default {
  name: 'PermissionModal',
  components: { Icons },
  data() {
    return {
      drawerWidth: 700,
      treeData: [],
      title: 'Operation',
      visible: false,
      disableSubmit: false,
      model: {},
      show: true,//根据菜单类型，动态显示隐藏表单元素
      menuLabel: 'Menu Name',
      labelCol: {
        xs: { span: 24 },
        sm: { span: 5 }
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 16 }
      },
      confirmLoading: false,
      iconChooseVisible: false,
      validateStatus: ''
    }
  },
  computed: {
    validatorRules: function() {
      return {
        name: [{ required: true, message: '请输入菜单标题!' }],
        component: [{ required: this.show, message: '请输入前端组件!' }],
        url: [{ required: this.show, message: '请输入菜单路径!' }],
        permsType: [{ required: true, message: '请输入授权策略!' }],
        perms: [{ required: false, message: '请输入授权标识!' }, { validator: this.validatePerms }]
      }
    }
  },
  created() {
  },
  methods: {
    loadTree() {
      var that = this
      queryTreeList().then((res) => {
        if (res.success) {
          console.log(res)
          that.treeData = []
          let treeList = res.result.treeList
          for (let a = 0; a < treeList.length; a++) {
            let temp = treeList[a]
            temp.isLeaf = temp.leaf
            that.treeData.push(temp)
          }
        }
      })
    },
    add() {
      //初始化默认值
      this.edit({ status: '1', permsType: '1', sortNo: 1.0, route: true, menuType: 0 })
    },
    edit(record) {
      this.resetScreenSize() // 调用此方法,根据屏幕宽度自适应调整抽屉的宽度
      this.model = Object.assign({}, record)

      //根据菜单类型，动态展示页面字段
      console.log('record: ', record)
      this.show = record.menuType == 2 ? false : true
      this.menuLabel = record.menuType == 2 ? '按钮/权限' : '菜单名称'

      this.visible = true
      this.loadTree()
    },
    close() {
      this.$emit('close')
      this.disableSubmit = false
      this.visible = false
      this.$refs.form.resetFields()
    },
    handleOk() {
      const that = this
      // 触发表单验证
      this.$refs.form.validate(valid => {
        if (valid) {
          if ((this.model.menuType == 1 || this.model.menuType == 2) && !this.model.parentId) {
            that.validateStatus = 'error'
            that.$message.error('请检查你填的类型以及信息是否正确！')
            return
          } else {
            that.validateStatus = 'success'
          }
          that.confirmLoading = true
          let obj
          if (!this.model.id) {
            obj = addPermission(this.model)
          } else {
            obj = editPermission(this.model)
          }
          obj.then((res) => {
            if (res.success) {
              that.$message.success(res.message)
              that.$emit('ok')
            } else {
              that.$message.warning(res.message)
            }
          }).finally(() => {
            that.confirmLoading = false
            that.close()
          })
        } else {
          return false
        }
      })
    },
    handleCancel() {
      this.close()
    },
    validateNumber(rule, value, callback) {
      if (!value || new RegExp(/^[0-9]*[1-9][0-9]*$/).test(value)) {
        callback()
      } else {
        callback('请输入正整数!')
      }
    },
    validatePerms(rule, value, callback) {
      if (value && value.length > 0) {
        //校验授权标识是否存在
        var params = {
          tableName: 'sys_permission',
          fieldName: 'perms',
          fieldVal: value,
          dataId: this.model.id
        }
        duplicateCheck(params).then((res) => {
          if (res.success) {
            callback()
          } else {
            callback('授权标识已存在!')
          }
        })
      } else {
        callback()
      }
    },
    onChangeMenuType(e) {
      if (this.model.menuType == 2) {
        this.show = false
        this.menuLabel = 'Button/Permission'
      } else {
        this.show = true
        this.menuLabel = 'Menu Name'
      }
      this.$nextTick(() => {
        this.$refs.form.validateField(['url', 'component'])
      })
    },
    selectIcons() {
      this.iconChooseVisible = true
    },
    handleIconCancel() {
      this.iconChooseVisible = false
    },
    handleIconChoose(value) {
      console.log(value)
      this.model.icon = value
      this.iconChooseVisible = false
    },
    // 根据屏幕变化,设置抽屉尺寸
    resetScreenSize() {
      let screenWidth = document.body.clientWidth
      if (screenWidth < 500) {
        this.drawerWidth = screenWidth
      } else {
        this.drawerWidth = 700
      }
    },
    handleParentIdChange(value) {
      if (!value) {
        this.validateStatus = 'error'
      } else {
        this.validateStatus = 'success'
      }
    }
  }
}
</script>

<style scoped>

</style>
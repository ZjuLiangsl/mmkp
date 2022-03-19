<template>
<div class="j-super-query-box">

  <slot name="button" :isActive="superQueryFlag" :isMobile="izMobile" :open="handleOpen" :reset="handleReset">
    <a-tooltip v-if="superQueryFlag" v-bind="tooltipProps" :mouseLeaveDelay="0.2">
      <span v-show="false">{{tooltipProps}}</span>
      <template slot="title">
        <span>Advanced query conditions have taken effect</span>
        <a-divider type="vertical"/>
        <a @click="handleReset">Empty</a>
      </template>
      <a-button-group>
        <a-button type="primary" @click="handleOpen">
          <a-icon type="appstore" theme="twoTone" spin/>
          <span>advanced query</span>
        </a-button>
        <a-button v-if="izMobile" type="primary" icon="delete" @click="handleReset"/>
      </a-button-group>
    </a-tooltip>
    <a-button v-else type="primary" icon="filter" @click="handleOpen">advanced query</a-button>
  </slot>

  <j-modal
    title="Advanced query constructor"
    :width="1000"
    :visible="visible"
    @cancel="handleCancel"
    :mask="false"
    :fullscreen="izMobile"
    class="j-super-query-modal"
    style="top:5%;max-height: 95%;"
  >

    <template slot="footer">
      <div style="float: left">
        <a-button :loading="loading" @click="handleReset">Reset</a-button>
        <a-button :loading="loading" @click="handleSave">save</a-button>
      </div>
      <a-button :loading="loading" @click="handleCancel">close</a-button>
      <a-button :loading="loading" type="primary" @click="handleOk">Query</a-button>
    </template>

    <a-spin :spinning="loading">
      <a-row>
        <a-col :sm="24" :md="24-5">

          <a-empty v-if="queryParamsModel.length === 0" style="margin-bottom: 12px;">
            <div slot="description">
              <span>There are no query criteria</span>
              <a-divider type="vertical"/>
              <a @click="handleAdd">click to add</a>
            </div>
          </a-empty>

          <a-form v-else layout="inline">

            <a-row style="margin-bottom: 12px;">
              <a-col :md="12" :xs="24">
                <a-form-item label="Filtering condition matching" :labelCol="{md: 6,xs:24}" :wrapperCol="{md: 18,xs:24}" style="width: 100%;">
                  <a-select v-model="matchType" :getPopupContainer="node=>node.parentNode" style="width: 100%;">
                    <a-select-option value="and">AND（All conditions require a match）</a-select-option>
                    <a-select-option value="or">OR（Any one of the conditions matches）</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>

            <a-row type="flex" style="margin-bottom:10px" :gutter="16" v-for="(item, index) in queryParamsModel" :key="index">

              <a-col :md="8" :xs="24" style="margin-bottom: 12px;">
                <a-tree-select
                  showSearch
                  v-model="item.field"
                  :treeData="fieldTreeData"
                  :dropdownStyle="{ maxHeight: '400px', overflow: 'auto' }"
                  placeholder="Select query field"
                  allowClear
                  treeDefaultExpandAll
                  :getPopupContainer="node=>node.parentNode"
                  style="width: 100%"
                  @select="(val,option)=>handleSelected(option,item)"
                >
                </a-tree-select>
              </a-col>

              <a-col :md="4" :xs="24" style="margin-bottom: 12px;">
                <a-select placeholder="Match rule" :value="item.rule" :getPopupContainer="node=>node.parentNode" @change="handleRuleChange(item,$event)">
                  <a-select-option value="eq">eq</a-select-option>
                  <a-select-option value="like">like</a-select-option>
                  <a-select-option value="right_like">right_like</a-select-option>
                  <a-select-option value="left_like">left_like</a-select-option>
                  <a-select-option value="in">in</a-select-option>
                  <a-select-option value="ne">ne</a-select-option>
                  <a-select-option value="gt">gt</a-select-option>
                  <a-select-option value="ge">ge</a-select-option>
                  <a-select-option value="lt">lt</a-select-option>
                  <a-select-option value="le">le</a-select-option>
                </a-select>
              </a-col>

              <a-col :md="8" :xs="24" style="margin-bottom: 12px;">
                <j-search-select-tag v-if="item.type==='sel_search'" v-model="item.val" :dict="getDictInfo(item)" placeholder="Please Select"/>
                <template v-else-if="item.type==='list_multi'">
                  <j-multi-select-tag v-if="item.options" v-model="item.val" :options="item.options" placeholder="Please Select"/>
                  <j-multi-select-tag v-else v-model="item.val" :dictCode="getDictInfo(item)" placeholder="Please Select"/>
                </template>

                <template v-else-if="item.dictCode">
                  <template v-if="item.type === 'table-dict'">
                    <j-popup
                      v-model="item.val"
                      :code="item.dictTable"
                      :field="item.dictCode"
                      :orgFields="item.dictCode"
                      :destFields="item.dictCode"
                      :multi="true"
                    ></j-popup>
                  </template>
                  <template v-else>
                    <j-multi-select-tag v-show="allowMultiple(item)" v-model="item.val" :dictCode="item.dictCode" placeholder="Please Select"/>
                    <j-dict-select-tag v-show="!allowMultiple(item)" v-model="item.val" :dictCode="item.dictCode" placeholder="Please Select"/>
                  </template>
                </template>
                <j-popup
                  v-else-if="item.type === 'popup'"
                  :value="item.val"
                  v-bind="item.popup"
                  group-id="superQuery"
                  @input="(e,v)=>handleChangeJPopup(item,e,v)"
                  :multi="true"/>
                <j-select-multi-user
                  v-else-if="item.type === 'select-user' || item.type === 'sel_user'"
                  v-model="item.val"
                  :buttons="false"
                  :multiple="false"
                  placeholder="select user"
                  :returnKeys="['id', item.customReturnField || 'username']"
                />
                <j-select-depart
                  v-else-if="item.type === 'select-depart' || item.type === 'sel_depart'"
                  v-model="item.val"
                  :multi="false"
                  placeholder="select dept"
                  :customReturnField="item.customReturnField || 'id'"
                />
                <a-select
                  v-else-if="item.options instanceof Array"
                  v-model="item.val"
                  :options="item.options"
                  allowClear
                  placeholder="Please Select"
                  :mode="allowMultiple(item)?'multiple':''"
                />
                <j-area-linkage v-model="item.val" v-else-if="item.type==='area-linkage' || item.type==='pca'" style="width: 100%"/>
                <j-date v-else-if=" item.type=='date' " v-model="item.val" placeholder="select date" style="width: 100%"></j-date>
                <j-date v-else-if=" item.type=='datetime' " v-model="item.val" placeholder="select time" :show-time="true" date-format="YYYY-MM-DD HH:mm:ss" style="width: 100%"></j-date>
                <a-time-picker v-else-if="item.type==='time'" :value="item.val ? moment(item.val,'HH:mm:ss') : null" format="HH:mm:ss" style="width: 100%" @change="(time,value)=>item.val=value"/>
                <a-input-number v-else-if=" item.type=='int'||item.type=='number' " style="width: 100%" placeholder="请输入数值" v-model="item.val"/>
                <a-select v-else-if="item.type=='switch'" placeholder="Please Select" v-model="item.val">
                  <a-select-option value="Y">yes</a-select-option>
                  <a-select-option value="N">no</a-select-option>
                </a-select>
                <a-input v-else v-model="item.val" placeholder="input"/>
              </a-col>

              <a-col :md="4" :xs="0" style="margin-bottom: 12px;">
                <a-button @click="handleAdd" icon="plus"></a-button>&nbsp;
                <a-button @click="handleDel( index )" icon="minus"></a-button>
              </a-col>

              <a-col :md="0" :xs="24" style="margin-bottom: 12px;text-align: right;">
                <a-button @click="handleAdd" icon="plus"></a-button>&nbsp;
                <a-button @click="handleDel( index )" icon="minus"></a-button>
              </a-col>

            </a-row>

          </a-form>
        </a-col>
        <a-col :sm="24" :md="5">

          <a-card class="j-super-query-history-card" :bordered="true">
            <div slot="title">
              Saved query
            </div>

            <a-empty v-if="saveTreeData.length === 0" class="j-super-query-history-empty" description="No queries are saved"/>
            <a-tree
              v-else
              class="j-super-query-history-tree"
              showIcon
              :treeData="saveTreeData"
              :selectedKeys="[]"
              @select="handleTreeSelect"
            >
            </a-tree>
          </a-card>


        </a-col>
      </a-row>


    </a-spin>

    <a-modal title="Please enter a name to save" :visible="prompt.visible" @cancel="prompt.visible=false" @ok="handlePromptOk">
      <a-input v-model="prompt.value"></a-input>
    </a-modal>

  </j-modal>
</div>
</template>

<script>
  import moment from 'moment'
  import * as utils from '@/utils/util'
  import { mixinDevice } from '@/utils/mixin'
  import JDate from '@/components/jeecg/JDate.vue'
  import JSelectDepart from '@/components/jeecgbiz/JSelectDepart'
  import JSelectMultiUser from '@/components/jeecgbiz/JSelectMultiUser'
  import JMultiSelectTag from '@/components/dict/JMultiSelectTag'
  import JAreaLinkage from '@comp/jeecg/JAreaLinkage'

  export default {
    name: 'JSuperQuery',
    mixins: [mixinDevice],
    components: { JAreaLinkage, JMultiSelectTag, JDate, JSelectDepart, JSelectMultiUser },
    props: {

      fieldList: {
        type: Array,
        required: true
      },

      callback: {
        type: String,
        required: false,
        default: 'handleSuperQuery'
      },

      loading: {
        type: Boolean,
        default: false
      },


      saveCode: {
        type: String,
        default: null
      }

    },
    data() {
      return {
        moment,
        fieldTreeData: [],

        prompt: {
          visible: false,
          value: ''
        },

        visible: false,
        queryParamsModel: [],
        treeIcon: <a-icon type="file-text"/>,
        saveTreeData: [],
        saveCodeBefore: 'JSuperQuerySaved_',
        matchType: 'and',
        superQueryFlag: false,
      }
    },
    computed: {
      izMobile() {
        return this.device === 'mobile'
      },
      tooltipProps() {
        return this.izMobile ? { visible: false } : {}
      },
      fullSaveCode() {
        let saveCode = this.saveCode
        if (saveCode == null || saveCode === '') {
          saveCode = this.$route.fullPath
        }
        return this.saveCodeBefore + saveCode
      },
    },
    watch: {
      fullSaveCode: {
        immediate: true,
        handler() {
          let list = this.$ls.get(this.fullSaveCode)
          if (list instanceof Array) {
            this.saveTreeData = list.map(i => this.renderSaveTreeData(i))
          }
        }
      },
      fieldList: {
        deep: true,
        immediate: true,
        handler(val) {
          let mainData = [], subData = []
          val.forEach(item => {
            let data = { ...item }
            data.label = data.label || data.text
            let hasChildren = (data.children instanceof Array)
            data.disabled = hasChildren
            data.selectable = !hasChildren
            if (hasChildren) {
              data.children = data.children.map(item2 => {
                let child = { ...item2 }
                child.label = child.label || child.text
                child.label = data.label + '-' + child.label
                child.value = data.value + ',' + child.value
                child.val = ''
                return child
              })
              data.val = ''
              subData.push(data)
            } else {
              mainData.push(data)
            }
          })
          this.fieldTreeData = mainData.concat(subData)
        }
      }
    },

    methods: {
      show() {
        if (!this.queryParamsModel || this.queryParamsModel.length === 0) {
          this.resetLine()
        }
        this.visible = true
      },

      getDictInfo(item) {
        let str = ''
        if(!item.dictTable){
          str = item.dictCode
        }else{
          str = item.dictTable+','+item.dictText+','+item.dictCode
        }
        return str
      },
      handleOk() {
        if (!this.isNullArray(this.queryParamsModel)) {
          let event = {
            matchType: this.matchType,
            params: this.removeEmptyObject(this.queryParamsModel)
          }
          if (this.izMobile) {
            this.visible = false
          }
          this.emitCallback(event)
        } else {
          this.$message.warn("Null conditions cannot be queried")
        }
      },
      emitCallback(event = {}) {
        let { params = [], matchType = this.matchType } = event
        this.superQueryFlag = (params && params.length > 0)
        for (let param of params) {
          if (Array.isArray(param.val)) {
            param.val = param.val.join(',')
          }
        }
        this.$emit(this.callback, params, matchType)
      },
      handleCancel() {
        this.close()
      },
      close() {
        this.$emit('close')
        this.visible = false
      },
      handleAdd() {
        this.addNewLine()
      },
      addNewLine() {
        this.queryParamsModel.push({ rule: 'eq' })
      },
      resetLine() {
        this.superQueryFlag = false
        this.queryParamsModel = []
        this.addNewLine()
      },
      handleDel(index) {
        this.queryParamsModel.splice(index, 1)
      },
      handleSelected(node, item) {
        let { type, dbType, options, dictCode, dictTable, dictText, customReturnField, popup } = node.dataRef
        item['type'] = type
        item['dbType'] = dbType
        item['options'] = options
        item['dictCode'] = dictCode
        item['dictTable'] = dictTable
        item['dictText'] = dictText
        item['customReturnField'] = customReturnField
        if (popup) {
          item['popup'] = popup
        }
        this.$set(item, 'val', undefined)
      },
      handleOpen() {
        this.show()
      },
      handleReset() {
        this.resetLine()
        this.emitCallback()
      },
      handleSave() {
        let queryParams = this.removeEmptyObject(this.queryParamsModel)
        if (this.isNullArray(queryParams)) {
          this.$message.warning('An empty condition cannot be saved')
        } else {
          this.prompt.value = ''
          this.prompt.visible = true
        }
      },
      handlePromptOk() {
        let { value } = this.prompt
        if(!value){
          this.$message.warning('The save name cannot be empty')
          return
        }
        let records = this.removeEmptyObject(this.queryParamsModel)
        let filterList = this.saveTreeData.filter(i => i.originTitle === value)
        if (filterList.length > 0) {
          this.$confirm({
            content: `${value} Exists, whether to overwrite？`,
            onOk: () => {
              this.prompt.visible = false
              filterList[0].records = records
              this.saveToLocalStore()
              this.$message.success('Save Success')
            }
          })
        } else {
          this.prompt.visible = false
          this.saveTreeData.push(this.renderSaveTreeData({
            title: value,
            matchType: this.matchType,
            records: records
          }))
          this.saveToLocalStore()
          this.$message.success('Save Success')
        }
      },
      handleTreeSelect(idx, event) {
        if (event.selectedNodes[0]) {
          let { matchType, records } = event.selectedNodes[0].data.props
          this.matchType = matchType || this.matchType
          this.queryParamsModel = utils.cloneObject(records)
        }
      },
      handleRemoveSaveTreeItem(event, vNode) {
        event.stopPropagation()

        this.$confirm({
          content: ' Delete current query？',
          onOk: () => {
            let { eventKey } = vNode
            this.saveTreeData.splice(Number.parseInt(eventKey.substring(2)), 1)
            this.saveToLocalStore()
          },
        })
      },

      saveToLocalStore() {
        let saveValue = this.saveTreeData.map(({ originTitle, matchType, records }) => ({ title: originTitle, matchType, records }))
        this.$ls.set(this.fullSaveCode, saveValue)
      },

      isNullArray(array) {
        if (!array || array.length === 0) {
          return true
        }
        if (array.length === 1) {
          let obj = array[0]
          if (!obj.field || (obj.val == null || obj.val === '') || !obj.rule) {
            return true
          }
        }
        return false
      },
      removeEmptyObject(arr) {
        let array = utils.cloneObject(arr)
        for (let i = 0; i < array.length; i++) {
          let item = array[i]
          if (item == null || Object.keys(item).length <= 0) {
            array.splice(i--, 1)
          } else {
            if (Array.isArray(item.options)) {
              delete item.options
            }
          }
        }
        return array
      },

      renderSaveTreeData(item) {
        item.icon = this.treeIcon
        item.originTitle = item['title']
        item.title = (arg1, arg2) => {
          let vNode
          if (arg1.dataRef) {
            vNode = arg1
          } else if (arg2.dataRef) {
            vNode = arg2
          } else {
            return <span style="color:red;">Antdv version is not supported</span>
          }
          let {originTitle} = vNode.dataRef
          return (
            <div class="j-history-tree-title">
              <span>{originTitle}</span>

              <div class="j-history-tree-title-closer" onClick={e => this.handleRemoveSaveTreeItem(e, vNode)}>
                <a-icon type="close-circle"/>
              </div>
            </div>
          )
        }
        return item
      },

      allowMultiple(item) {
        return item.rule === 'in'
      },

      handleRuleChange(item, newValue) {
        let oldValue = item.rule
        this.$set(item, 'rule', newValue)
        if (oldValue === 'in') {
          if (item.dictCode || item.options instanceof Array) {
            let value = item.val
            if (typeof item.val === 'string') {
              value = item.val.split(',')[0]
            } else if (Array.isArray(item.val)) {
              value = item.val[0]
            }
            this.$set(item, 'val', value)
          }
        }
      },

      handleChangeJPopup(item, e, values) {
        item.val = values[item.popup['destFields']]
      },

    }
  }
</script>

<style lang="less" scoped>

  .j-super-query-box {
    display: inline-block;
  }

  .j-super-query-modal {

    .j-super-query-history-card {
      /deep/ .ant-card-body,
      /deep/ .ant-card-head-title {
        padding: 0;
      }

      /deep/ .ant-card-head {
        padding: 4px 8px;
        min-height: initial;
      }
    }

    .j-super-query-history-empty {
      /deep/ .ant-empty-image {
        height: 80px;
        line-height: 80px;
        margin-bottom: 0;
      }

      /deep/ img {
        width: 80px;
        height: 65px;
      }

      /deep/ .ant-empty-description {
        color: #afafaf;
        margin: 8px 0;
      }
    }

    .j-super-query-history-tree {

      .j-history-tree-title {
        width: calc(100% - 24px);
        position: relative;
        display: inline-block;

        &-closer {
          color: #999999;
          position: absolute;
          top: 0;
          right: 0;
          width: 24px;
          height: 24px;
          text-align: center;
          opacity: 0;
          transition: opacity 0.3s, color 0.3s;

          &:hover {
            color: #666666;
          }

          &:active {
            color: #333333;
          }
        }

        &:hover {
          .j-history-tree-title-closer {
            opacity: 1;
          }
        }

      }

      /deep/ .ant-tree-switcher {
        display: none;
      }

      /deep/ .ant-tree-node-content-wrapper {
        width: 100%;
      }
    }

  }

</style>
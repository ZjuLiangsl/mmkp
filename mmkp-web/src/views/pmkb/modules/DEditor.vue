<template>
  <div>
    <div class="left_div">
      <div id="deditor_model_div_id" class="data_model_div">
        <VisualModeling
          ref="visualModel"
          :data="modelData"
          :columns="modelColumns"
          :config="config"
          :edgeMenu="edgeMenu"
          :onFocusNode="focusNode"
          :onFocusEdge="focusEdge"
          :onFocusCanvas="focusCanvas"
          :onDblClickNode="dbClikNode"
          :onChange="change"
          height="700"
        />
        <div style="margin-top: 8px">
          <a-button icon="sync" type="primary" style="float: right;" title="Sync: Synchronize structure, content data to knowledge base." @click="sync">
            Save
          </a-button>
          <a-button icon="redo" type="primary" style="float:right;margin-right: 20px;" title="Rest: Just empty the canvas without doing anything to the data."
                    @click="redo">
            Reset
          </a-button>
        </div>
      </div>
    </div>
    <div class="right_div">
      <a-form :form="form" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <!--<a-form-item label="Source Name">
          <a-select
            v-decorator="[
              'dsCode',
              { rules: [{ required: true, message: 'Please Select Data Source!' }] },
            ]"
            placeholder="Please Select Data Source"
            @change="handleDsChange"
          >
            <a-select-option v-for="(item, key) in dsList" :key="key" :value="item.value">
              {{ item.text }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Database Name">
          <a-select
            v-decorator="[
              'dbCode',
              { rules: [{ required: true, message: 'Please Select Database!' }] },
            ]"
            placeholder="Please Select Database"
            @change="handleDbChange"
          >
            <a-select-option v-for="item in dbList" :key="item" :value="item">
              {{ item }}
            </a-select-option>
          </a-select>
        </a-form-item>-->
        <a-form-item label="Table Name">
          <a-select show-search option-filter-prop="children" :filter-option="filterTableOption" size="default"
                    placeholder="Please Input Table Name"
                    v-decorator="[
                      'tableName',
                      { rules: [{ required: true, message: 'Please Input Table Name!' }] },
                    ]"
                    @change="handleTableChange">
            <a-select-option v-for="item in tableList" :key="item.key" :value="item.tableName">
              {{ item.title }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
      <a-button icon="diff" type="primary" @click="handleAdd">
        New
      </a-button>
      <a-button style="margin-left: 20px" icon="delete" type="primary" @click="handleDel">
         Delete
      </a-button>
      <div class="tree_div">
        <a-table :columns="columnData" :data-source="tableData"
                 size="middle" :pagination="{position:'bottom'}"
                 bordered
                 :rowKey="tableRowKey"
                 :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange1 }"
                 :customRow="customRow">
          <div
            slot="filterDropdown"
            slot-scope="{ setSelectedKeys, selectedKeys, confirm, clearFilters, column }"
            style="padding: 8px"
          >
            <a-input-search
              enter-button
              v-ant-ref="c => (searchInput = c)"
              :placeholder="`搜索 ${column.title}`"
              :value="selectedKeys[0]"
              style="width: 188px; margin-bottom: 8px; "
              @change="e => setSelectedKeys(e.target.value ? [e.target.value] : [])"
              @search="() => handleSearch(selectedKeys, confirm, column.dataIndex)"
            />
          </div>
          <a-icon
            slot="filterIcon"
            slot-scope="filtered"
            type="search"
            :style="{ color: filtered ? '#108ee9' : undefined }"
          />
          <template slot="customRender" slot-scope="text, record, index, column">
            <span v-if="searchText && searchedColumn === column.dataIndex">
              <template
                v-for="(fragment, i) in text
                  .toString()
                  .split(new RegExp(`(?<=${searchText})|(?=${searchText})`, 'i'))"
              >
                <mark
                  v-if="fragment.toLowerCase() === searchText.toLowerCase()"
                  :key="i"
                  class="highlight"
                >{{ fragment }}</mark>
                <template v-else>{{ fragment }}</template>
              </template>
            </span>
            <template v-else>
              {{ text }}
            </template>
          </template>
        </a-table>
      </div>
    </div>

    <a-modal
      :title="modelTitle"
      :visible="modelVisible"
      :confirm-loading="modelLoading"
      @ok="handleOk"
      @cancel="handleCancel"
    >
      <form-create :rule="fFields" v-model="fApi" :value.sync="model" :option="fOptions" @mounted="fMounted"/>
    </a-modal>

    <a-modal
      :title="refModelTitle"
      :visible="refModelVisible"
      :confirm-loading="refModelLoading"
      @cancel="refModelCancel"
    >
      <template slot="footer">
        <a-button @click="refModelCancel">
          Close
        </a-button>
        <a-button type="primary" @click="refModelOk">
          OK
        </a-button>
        <a-button type="primary" @click="refModelAdd">
          New
        </a-button>
      </template>
      <a-tabs :active-key="activeKey" @change="tabsChange">
        <a-tab-pane v-for="(item,i) in tabsName" :key="tabsKey[i]" :tab="item">
          <a-table :columns="refModelColumnsData"
                   :data-source="refModelTableData"
                   :rowKey="refModelTableRowKey"
                   :row-selection="{ selectedRowKeys: refModelSelectKeys, onChange: onSelectChange,type:'radio' }"
                   size="middle" :pagination="{position:'bottom'}">
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-modal>
  </div>
</template>

<script>
import VisualModeling from 'react-visual-modeling'
import 'react-visual-modeling/dist/index.css'
import { getAction, httpAction, postAction } from '@api/manage'
import { ajaxGetDictItems, getDictItemsFromCache } from '@api/api'
import { alwaysResolve, validateDuplicateValue } from '@/utils/util'

import moment from 'moment'
import * as api from '@api/api'

import Sortable from 'sortablejs'

export default {
  name: 'DEditer',

  components: {
    VisualModeling
  },
  data() {
    return {
      moment,
      form: this.$form.createForm(this, { name: 'search' }),
      dsList: [],
      dsCode: '',
      dbList: [],
      dbCode: '',
      tableList: [],
      tableName: undefined,
      tableData: [],
      columnData: [],
      tableRowKey: '',
      selectedRowKeys: [],
      searchText: '',
      searchInput: null,
      searchedColumn: '',

      modelColumns: [
        {
          key: 'fieldComment',
          primaryKey: true,
          width: 100,
          title: 'Field Name'
        },
        {
          key: 'fieldValue',
          width: 110,
          title: '字段值',
          render: (val, row) => {
            if (val) {
              return val.toString()
            } else {
              return ''
            }
          }
        },
        {
          key: 'option',
          width: 60,
          title: 'Operation',
          render: (val, row) => {
            return ''
          }
        }
      ],
      modelData: {
        'nodes': [],
        'edges': []
      },
      config: {
        allowKeyboard: true,
        titleRender: (title, node) => {
          return title
        }
      },
      edgeMenu: [
        {
          key: 'delete',
          title: ' Delete',
          onClick: (key, data) => {
            let dataid = data.sourceNode + '.' + data.source + '_to_' + data.targetNode + '.' + data.target
            this.delEdge(data.id, dataid)

            let tmpName = data.sourceNode.split(':')[0].split('.')[2]
            let field = this.nodeColumnDataMap[tmpName].filter(c => c.title == data.source)[0].dataIndex

            postAction('/stage-data/delReferenceField', {
              field: field,
              sourceNode: data.sourceNode,
              targetNode: data.targetNode
            }).then(res => {
              if (res.success == true) {
                let sourceNode = this.modelData.nodes.filter(n => n.id == data.sourceNode)[0]
                sourceNode.fields.forEach(f => {
                  if (f.fieldComment == data.source) {
                    // let tmp = data.targetNode.split(":")
                    // f.fieldValue = tmp[0].split(".")[2] +":"+ tmp[1].split(".")[1]

                    let doc = document.getElementsByClassName('data_model_div')[0]
                    let nodes = doc.getElementsByClassName('node table-node')
                    for (let i = 0; i < nodes.length; i++) {
                      let node = nodes[i]
                      let titleDom = node.getElementsByClassName('title-text')[0]
                      if (titleDom.innerHTML == this.nodeTitleIds[data.sourceNode]) {
                        let items = node.getElementsByClassName('field-item')
                        for (let i = 0; i < items.length; i = i + 3) {
                          let ele = items[i]
                          let html = ele.innerText
                          if (html == data.source) {
                            items[i + 1].innerHTML = ''
                            let an = document.createElement('span')
                            items[i + 1].style.color = '#198aee'
                            an.style.cursor = 'pointer'
                            an.innerHTML = '(空)'
                            an.style.fontSize = '14px'
                            an.onmouseover = () => {
                              an.style.fontWeight = 'bold'
                              an.style.fontSize = '18px'
                            }
                            an.onmouseout = () => {
                              an.style.fontWeight = 'normal'
                              an.style.fontSize = '14px'
                            }
                            an.style.marginLeft = '5px'
                            an.title = value
                            an.onclick = () => {
                              window.referenceFieldSelect(titleDom.innerHTML, html, '(空)')
                            }
                            items[i + 1].title = '(空)'
                            items[i + 1].appendChild(an)
                          }
                        }
                      }
                    }

                    this.nodeIdDataMap[data.sourceNode][res.result.field] = undefined

                    this.referenceFieldOpenState[data.sourceNode + '.' + data.source] = false
                    this.setNodeStyle()
                  }
                })
              } else {
                this.$message.error('Save Failed！')
                console.error(res)
              }
            })

            this.referenceFieldOpenState[data.sourceNode + '.' + data.source] = false
            this.setNodeStyle()
          }
        }
      ],
      currentNode: {},
      currentEdge: {},

      nodeTitleIds: {},
      nodeIdDataMap: {},
      formField: {},
      nodePkFiled: {},
      pkField: {},
      nodeReferenceField: {},
      referenceField: {},
      referenceFieldMap: {},
      nodeDataState: {},
      nodeColumnDataMap: {},
      nodeReferenceFieldOpenState: {},

      tableOption: {},
      checkRuleData: [],
      checkRuleDataMap: {},

      modelVisible: false,
      modelLoading: false,
      modelTitle: 'New',
      modelIsAdd: true,
      fFields: [],
      model: {},
      fApi: null,
      fOptions: {
        submitBtn: false,
        resetBtn: false,
        global: {
          '*': {
            col: {
              span: 24
            },
            wrap: {
              labelCol: { span: 6 },
              wrapperCol: { span: 18 }
            }
          }
        }
      },

      currentNodeId: '',
      currentField: '',
      currentFieldValue: '',
      refModelTitle: '',
      refModelVisible: false,
      refModelLoading: false,
      tabsName: [],
      tabsKey: [],
      activeKey: '',
      tabsData: {},
      refModelColumnsData: [],
      refModelTableData: [],
      refModelTableRowKey: '',
      refModelSelectKeys: [],
      referenceFieldOpenState: {}
    }
  },
  created() {
    window.referenceFieldSwitch = this.referenceFieldSwitch
    window.referenceFieldSelect = this.referenceFieldSelect
  },
  mounted() {
    // this.loadItems()
  },
  methods: {
    customRow(record, index) {
      return {
        style: {
          cursor: 'pointer'
        },
        on: {
          mouseenter: (event) => {
            var ev = event || window.event
            ev.target.draggable = true
          },
          dragstart: (event) => {
            var ev = event || window.event
            ev.stopPropagation()

            let tableName = this.form.getFieldValue('tableName')
            let pkField = this.pkField[tableName]
            let pkFieldStr = pkField.join('||')
            let pkFieldValue = []
            let fields = []
            for (let i = 0; i < this.columnData.length; i++) {
              let c = this.columnData[i]
              fields.push({ 'fieldComment': c.title, 'fieldValue': record[c.dataIndex] })

              if (pkField.indexOf(c.dataIndex) > -1) {
                pkFieldValue.push(record[c.dataIndex])
              }
            }
            let pkFieldValueStr = pkFieldValue.join('||')
            let key = this.dsCode + '.' + this.dbCode + '.' + tableName
            let id = key + ':' + pkFieldStr + '.' + pkFieldValueStr
            let tmp = this.tableList.filter(t => t.tableName == tableName)[0].title.split(':')
            let title = (tmp[1] ? tmp[1] : tableName) + ':' + pkFieldValueStr
            this.nodePkFiled[title] = this.nodePkFiled[tableName]
            this.pkField[title] = this.pkField[tableName]
            this.nodeReferenceField[title] = this.nodeReferenceField[tableName]
            this.referenceField[title] = this.referenceField[tableName]
            this.nodeDataState[title] = record.dataState
            this.nodeIdDataMap[id] = record
            this.nodeTitleIds[id] = title
            this.nodeTitleIds[title] = id
            let maxTop = 0
            let maxLeft = 0
            let doc = document.getElementsByClassName('data_model_div')[0]
            let nodes = doc.getElementsByClassName('node table-node')
            if (nodes.length > 0) {
              for (let i = 0; i < nodes.length; i++) {
                let fields = nodes[i].getElementsByClassName('field')
                let attrs = nodes[i].getAttribute('style').split(';')
                let top = attrs[0].split(':')[1].replace('px', '')
                let left = attrs[1].split(':')[1].replace('px', '')

                if (left >= parseInt(maxLeft)) {
                  maxLeft = parseInt(left)
                  maxTop = parseInt(top) + (fields.length + 1) * 30
                }
              }
            }
            let nodeData = {
              'top': maxTop + 25,
              'left': maxLeft > 0 ? maxLeft : 50,
              'id': id,
              'title': title,
              'fields': fields
            }
            const target = this.modelData.nodes.filter(item => item.id == nodeData.id)
            if (target.length == 0) {
              this.modelData.nodes.push(nodeData)
            }

            let pk = tableName + ':' + pkFieldValueStr
            postAction('/stage-data/reference-relation-by-pk', {
              dsCode: this.dsCode,
              dbCode: this.dbCode,
              pk: pk
            }).then(res => {
              if (res.success == true) {
                let nodeIds = res.result.nodeIds
                let rightNodeIds = res.result.rightNodeIds
                let doc = document.getElementsByClassName('data_model_div')[0]
                let nodes = doc.getElementsByClassName('node table-node')

                this.modelData.nodes.forEach(item => {
                  let mid = item.id
                  if (nodeIds.indexOf(mid) > -1) {
                    for (let i = 0; i < nodes.length; i++) {
                      let node = nodes[i]
                      let titleDom = node.getElementsByClassName('title-text')[0]

                      if (titleDom.innerHTML == this.nodeTitleIds[mid]) {
                        let items = node.getElementsByClassName('field-item')
                        for (let i = 0; i < items.length; i = i + 3) {
                          let ele = items[i]
                          let html = ele.innerText
                          if (items[i + 1].innerText == pk && mid != id) {
                            let edge = {
                              id: mid + '.' + html + '_to_' + id + '.' + this.nodePkFiled[tableName][0],
                              source: html,
                              sourceNode: mid,
                              target: this.nodePkFiled[tableName][0],
                              targetNode: id
                            }
                            this.modelData.edges.push(edge)
                            this.referenceFieldOpenState[mid + '.' + html] = true
                          }
                        }
                      }
                    }
                  }
                  // 待验证
                  // console.log(mid)
                  // console.log(rightNodeIds)
                  // if (rightNodeIds.indexOf(mid)>-1) {
                  //   for (let i = 0; i < nodes.length; i++) {
                  //     let node = nodes[i]
                  //     console.log(node)
                  //     let titleDom = node.getElementsByClassName('title-text')[0]
                  //
                  //     if (titleDom.innerHTML == this.nodeTitleIds[mid]) {
                  //       console.log(titleDom.innerHTML)
                  //       let items = node.getElementsByClassName('field-item')
                  //       for (let i = 0; i < items.length; i = i + 3) {
                  //         let ele = items[i]
                  //         let html = ele.innerText
                  //         console.log(items[i+1].innerText)
                  //         console.log(pk)
                  //         if(items[i+1].innerText == pk ){
                  //           console.log(pk)
                  //           let edge = {
                  //             id: mid+"."+html+"_to_"+id+"."+this.nodePkFiled[tableName][0],
                  //             source: html,
                  //             sourceNode : mid,
                  //             target:this.nodePkFiled[tableName][0],
                  //             targetNode:id
                  //           }
                  //           this.modelData.edges.push(edge)
                  //           this.referenceFieldOpenState[mid+"."+html] = true
                  //         }
                  //       }
                  //     }
                  //   }
                  // }
                })

                this.$nextTick(() => {
                  this.setNodeStyle()
                })
              } else {
                this.$message.error('Table For failure!')
                console.error(res)
              }
            })
          }
        }
      }
    },
    loadItems() {
      getAction('/sys/dataSource/getDsDict', {}).then(res => {
        if (res.success == true) {
          this.dsList = res.result
          if (this.dsList && this.dsList.length > 0) {
            this.dsCode = this.dsList[0].value
            this.handleDsChange(this.dsCode)
          }
        } else {
          this.$message.error('Data Source For failure!')
          console.error(res)
        }
      })

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
    handleDsChange(selectedItem) {
      if (selectedItem) {
        this.dsCode = selectedItem
        getAction('/md/show-databases', { dsCode: selectedItem }).then(res => {
          if (res.success == true) {
            this.dbList = res.result
            if (this.dbList && this.dbList.length > 0) {
              this.dbCode = this.dbList[0]
            }
          } else {
            this.$message.error('Database For failure!')
            console.error(res)
          }
        })
      } else {
        this.dsCode = undefined
        this.dbList = []
      }
    },
    handleDbChange(selectedItem) {
      if (selectedItem) {
        this.dbCode = selectedItem
        this.loadTableList()
      } else {
        this.dbCode = undefined
      }
    },
    updateCode(dsCode, dbCode) {
      if (this.dsCode != dsCode || this.dbCode != dbCode) {
        this.dsCode = dsCode
        this.dbCode = dbCode
        this.loadTableList()
      }
    },
    loadTableList() {
      postAction('/editor/load-tables', {
        dsCode: this.dsCode,
        dbCode: this.dbCode,
        searchText: ''
      }).then(res => {
        if (res.success == true) {
          this.tableList = res.result
        } else {
          this.$message.error('Table For failure!')
          console.error(res)
        }
      })
    },
    filterTableOption(input, option) {
      return (
        option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
      )
    },
    handleTableChange(selectedItem) {
      if (selectedItem) {
        this.tableName = selectedItem
        this.loadTableColumnData(selectedItem)
      } else {
        this.tableName = undefined
      }
    },
    loadTableColumnData(tableName) {
      postAction('/editor/load-fields', {
        dsCode: this.dsCode,
        dbCode: this.dbCode,
        tableName: tableName
      }).then(res => {
        if (res.success == true) {
          let result = res.result
          let colData = []
          let nodePkField = [], pkField = []
          let nodeReferenceField = [], referenceField = []
          result.forEach(item => {
            let title = item.fieldComment ? item.fieldComment : item.fieldName
            if (item.isPk == '1') {
              nodePkField.push(title)
              pkField.push(item.fieldName)
            }
            if ('reference' == item.fieldType) {
              nodeReferenceField.push(title)
              referenceField.push(item.fieldName)
            }
            colData.push({
              title: title,
              dataIndex: item.fieldName,
              ellipsis: true,
              width: 100,
              scopedSlots: {
                filterDropdown: 'filterDropdown',
                filterIcon: 'filterIcon',
                customRender: 'customRender'
              },
              onFilter: (value, record) => {
                if (record[item.fieldName]) {
                  return record[item.fieldName]
                    .toString()
                    .toLowerCase()
                    .includes(value.toLowerCase())
                } else {
                  return false
                }
              },
              onFilterDropdownVisibleChange: visible => {
                if (visible) {
                  setTimeout(() => {
                    this.searchInput.focus()
                  })
                }
              },
              customHeaderCell: (column) => {
                return column
              }
            })
          })
          this.nodePkFiled[tableName] = nodePkField
          this.pkField[tableName] = pkField
          this.tableRowKey = pkField[0]
          this.selectedRowKeys = []
          this.nodeReferenceField[tableName] = nodeReferenceField
          this.referenceField[tableName] = referenceField
          this.columnData = colData

          this.nodeColumnDataMap[tableName] = colData
          this.loadTableData(tableName)
          this.loadTableField(tableName)
          this.loadTableReferenceFieldMap(tableName)
        } else {
          this.$message.error('Table For failure!')
          console.error(res)
        }
      })
    },
    loadTableData(tableName) {
      postAction('/stage-data/list', {
        dsCode: this.dsCode,
        dbCode: this.dbCode,
        tableName: tableName
      }).then(res => {
        if (res.success == true) {
          this.tableData = res.result.dataList
          this.tableOption[tableName] = {
            'add': res.result.add,
            'del': res.result.del,
            'edit': res.result.edit,
            'msg': res.result.msg,
            'maxId': res.result.maxId
          }
        } else {
          this.$message.error('数据 For failure!')
          console.error(res)
        }
      })
    },
    loadTableOption(tableName) {
      postAction('/stage-data/load-option', {
        dsCode: this.dsCode,
        dbCode: this.dbCode,
        tableName: tableName
      }).then(res => {
        if (res.success == true) {
          this.tableOption[tableName] = res.result
        } else {
          this.$message.error('表属性 For failure!')
          console.error(res)
        }
      })
    },
    loadTableField(tableName) {
      getAction('/md/tables/table-fields-by-params', {
        dsCode: this.dsCode,
        dbName: this.dbCode,
        tableName: tableName
      }).then(res => {
        if (res.success == true) {
          let tableFields = res.result
          this.table = tableFields.table
          let formFields = []
          tableFields.fields.forEach((item, index) => {
            let name = item.fieldName
            let compType = item.fieldCompType
            let title = item.fieldRemark ? item.fieldRemark : (item.fieldComment ? item.fieldComment : name)
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
                /*let maxStr = ''
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
                }*/
                let step = 1
                if (item.fieldType == 'decimal') {
                  step = 0.01
                }
                fieldObj['props'] = {
                  'step': Number(step)
                }
                if (item.extField4) {
                  fieldObj['props']['min'] = Number(item.extField4)
                }
                if (item.extField5) {
                  fieldObj['props']['max'] = Number(item.extField5)
                }
              } else if (compType == 'a-date-picker') {
                fieldObj.type = 'a-date-picker'
                if (item.extField2) {
                  fieldObj['props'] = { 'format': item.extField2 }
                } else {
                  fieldObj['props'] = { 'format': 'YYYY-MM-DD' }
                }
                if (item.extField4) {
                  fieldObj['props']['min'] = Number(item.extField4)
                }
                if (item.extField5) {
                  fieldObj['props']['max'] = Number(item.extField5)
                }
              } else if (compType == 'a-time-picker') {
                fieldObj.type = 'a-date-picker'
                if (item.extField2) {
                  fieldObj['props'] = { 'show-time': true, 'format': item.extField2 }
                } else {
                  fieldObj['props'] = { 'show-time': true, 'format': 'YYYY-MM-DD HH:mm:ss' }
                }
              }

              if (item.fieldType == 'reference') {
                fieldObj.type = 'hidden'
                // fieldObj.inject = true
                // fieldObj['emit'] = [
                //   {
                //     name: 'change',
                //     inject: {}
                //   },
                //   {
                //     name: 'search',
                //     inject: {}
                //   }
                // ]
                // fieldObj['emitPrefix'] = 'f'
                // fieldObj['props'] = { 'enter-button': true }
              }

              if (item.isPk == '1') {
                fieldObj.type = 'input'
                fieldObj['props'] = { disabled: true }
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
              formFields.push(fieldObj)
            } else {
              formFields.push({
                type: 'hidden',
                field: name,
                value: item.extField1 ? item.extField1 : null
              })
            }
          })
          formFields.push({
            type: 'hidden',
            field: 'dataState',
            value: '1'
          })
          let key = this.dsCode + '.' + this.dbCode + '.' + tableName
          this.formField[key] = formFields
        } else {
          this.$message.error('页面初始化错误！')
          console.error(res)
        }
      })
    },
    loadTableReferenceFieldMap(tableName) {
      postAction('/stage-data/list-reference-relation', {
        dsCode: this.dsCode,
        dbCode: this.dbCode,
        tableName: tableName
      }).then(res => {
        if (res.success == true) {
          let key = this.dsCode + '.' + this.dbCode + '.' + tableName
          this.referenceFieldMap[key] = res.result
        } else {
          this.$message.error('数据 For failure!')
          console.error(res)
        }
      })
    },
    handleSearch(selectedKeys, confirm, dataIndex) {
      confirm()
      this.searchText = selectedKeys[0]
      this.searchedColumn = dataIndex
      console.log(selectedKeys)
      console.log(confirm)
      console.log(dataIndex)
    },

    delEdge(id, dataid) {
      this.modelData.edges.forEach((item, index) => {
        let mid = item.sourceNode + '-' + item.source + '-' + item.targetNode + '-' + item.target
        if (id == mid) {
          this.modelData.edges.splice(index, 1)
        }
      })
      /*postAction('/md/tables/delEdge', { id:dataid}).then(res => {
        if (res.success == true) {
          let result = res.result
          if(result.refNode){
            this.updateTable(result.refNode)
          }
          this.$nextTick(() => {
            this.$nextTick(() => {
              this.$message.success('关联关系 Delete the success！')
            })
          })
        }
        else {
          this.$message.error('关联关系 Delete 失败！')
          console.error(res)
        }
      })*/
    },
    delEdgeById(id) {
      for (let i = 0; i < this.modelData.edges.length;) {
        let item = this.modelData.edges[i]
        let mid = item.sourceNode + '-' + item.source + '-' + item.targetNode + '-' + item.target
        if (id == mid) {
          this.modelData.edges.splice(i, 1)
        } else {
          i++
        }
      }
    },
    delNodeById(id) {
      for (let i = 0; i < this.modelData.nodes.length;) {
        let item = this.modelData.nodes[i]
        let mid = item.id
        if (mid == id) {
          this.modelData.nodes.splice(i, 1)
        } else {
          i++
        }
      }
    },
    focusNode(node) {
      this.currentNode = node
    },
    focusEdge(edge) {
      this.currentEdge = edge
    },
    focusCanvas() {
      this.currentEdge = {}
      this.currentNode = {}
    },
    dbClikNode(node) {
      let tableName = node.id.split(':')[0].split('.')[2]
      this.tableName = tableName
      let data = this.nodeIdDataMap[node.id]
      let key = this.dsCode + '.' + this.dbCode + '.' + tableName
      this.fFields = this.formField[key]
      this.modeltitle = 'Edit'
      this.modelIsAdd = false
      this.loadTableOption(tableName)
      for (let field in data) {
        for (let i = 0; i < this.fFields.length; i++) {
          let f = this.fFields[i]
          if (f.field == field) {
            this.fFields[i].value = data[field]
          }
        }
      }

      /*for (let i = 0; i < this.fFields.length; i++) {
        let f = this.fFields[i]
        if(this.referenceField[tableName].indexOf(f.field)>-1){
          this.fFields[i].type = 'hidden'
        }
      }*/
      this.modelVisible = true
    },
    change(data) {
      if (data.type == 'system.node.delete') {
        let nodes = data.nodes
        let links = data.neighborLinks
        links.forEach(item => {
          this.delEdgeById(item.id)
        })
        nodes.forEach(item => {
          this.delNodeById(item.id)
        })
        links.forEach(item => {
          let target = this.modelData.edges.filter(e => e.sourceNode == item.sourceNode && e.source == item.source)
          if (!target || target.length == 0) {
            this.referenceFieldOpenState[item.sourceNode + '.' + item.source] = false
          }
        })
        this.setNodeStyle()
      }
      if (data.type == 'system.link.connect') {
        let links = data.links
        let ids = []
        this.modelData.edges.forEach((item, index) => {
          let mid = item.sourceNode + '-' + item.source + '-' + item.targetNode + '-' + item.target
          ids.push(mid)
        })
        let id = ''
        let lk = {}
        links.forEach(link => {
          if (ids.indexOf(link.id) < 0) {
            lk = link
            id = link.id
            this.modelData.edges.push({
              id: id,
              source: link.source,
              sourceNode: link.sourceNode,
              target: link.target,
              targetNode: link.targetNode
            })
          }
        })
        this.$nextTick(() => {
          if (id != '') {
            let key = lk.sourceNode.split(':')[0]
            let refKey = lk.targetNode.split(':')[0]
            let referenceFieldRelation = this.referenceFieldMap[key]

            if (this.nodeReferenceField[this.nodeTitleIds[lk.sourceNode]].indexOf(lk.source) < 0) {
              this.delEdgeById(id)
              this.$message.warning('The source field is not a reference field and cannot be wired！')
              return
            }
            if (this.pkField[this.nodeTitleIds[lk.targetNode]].length != 1) {
              this.delEdgeById(id)
              this.$message.warning('Target table multi-field primary key, unwired!')
              return
            }
            if (!referenceFieldRelation[lk.source] || referenceFieldRelation[lk.source].indexOf(refKey) < 0) {
              this.delEdgeById(id)
              this.$message.warning('源字段与目标表没有关联关系,不可连线！')
              return
            }
            this.delEdgeById(id)

            for (let i = 0; i < this.modelData.edges.length;) {
              let e = this.modelData.edges[i]
              if (e.sourceNode == lk.sourceNode && e.source == lk.source) {
                this.modelData.edges.splice(i, 1)
              } else {
                i++
              }
            }

            this.$nextTick(() => {
              let edge = {
                id: lk.sourceNode + '.' + lk.source + '_to_' + lk.targetNode + '.' + this.nodePkFiled[this.nodeTitleIds[lk.targetNode]][0],
                source: lk.source,
                sourceNode: lk.sourceNode,
                target: this.nodePkFiled[this.nodeTitleIds[lk.targetNode]][0],
                targetNode: lk.targetNode
              }
              this.modelData.edges.push(edge)

              this.$nextTick(() => {
                let tmpName = lk.sourceNode.split(':')[0].split('.')[2]
                let field = this.nodeColumnDataMap[tmpName].filter(c => c.title == lk.source)[0].dataIndex

                postAction('/stage-data/updateReferenceField', {
                  field: field,
                  sourceNode: lk.sourceNode,
                  targetNode: lk.targetNode
                }).then(res => {
                  if (res.success == true) {
                    let sourceNode = this.modelData.nodes.filter(n => n.id == lk.sourceNode)[0]
                    sourceNode.fields.forEach(f => {
                      if (f.fieldComment == lk.source) {
                        let tmp = lk.targetNode.split(':')
                        // f.fieldValue = tmp[0].split(".")[2] +":"+ tmp[1].split(".")[1]

                        let doc = document.getElementsByClassName('data_model_div')[0]
                        let nodes = doc.getElementsByClassName('node table-node')
                        for (let i = 0; i < nodes.length; i++) {
                          let node = nodes[i]
                          let titleDom = node.getElementsByClassName('title-text')[0]
                          if (titleDom.innerHTML == this.nodeTitleIds[lk.sourceNode]) {
                            let items = node.getElementsByClassName('field-item')
                            for (let i = 0; i < items.length; i = i + 3) {
                              let ele = items[i]
                              let html = ele.innerText
                              if (html == lk.source) {
                                items[i + 1].innerHTML = tmp[0].split('.')[2] + ':' + tmp[1].split('.')[1]
                              }
                            }
                          }
                        }

                        this.nodeIdDataMap[lk.sourceNode][res.result.field] = tmp[0].split('.')[2] + ':' + tmp[1].split('.')[1]

                        this.referenceFieldOpenState[lk.sourceNode + '.' + lk.source] = true
                        this.setNodeStyle()
                      }
                    })
                  } else {
                    this.$message.error('Save Failed！')
                    console.error(res)
                  }
                })
              })
            })
          }
        })
      }
    },
    setNodeStyle() {
      let doc = document.getElementsByClassName('data_model_div')[0]
      let items = doc.getElementsByClassName('field-item')
      for (let i = 0; i < items.length; i++) {
        let ele = items[i]
        let attr = ele.getAttribute('dataType')
        if (attr == 'fieldComment') {
          ele.setAttribute('title', ele.innerHTML)
          ele.style.paddingLeft = '5px'
          ele.style.paddingRight = '5px'
          ele.style.width = '80px'
          ele.style.overflow = 'hidden'
          ele.style.textOverflow = 'ellipsis'
          ele.style.whiteSpace = 'nowrap'
        } else if (attr == 'fieldValue') {
          ele.setAttribute('title', ele.innerHTML)
          ele.style.paddingLeft = '5px'
          ele.style.paddingRight = '5px'
          ele.style.width = '120px'
          ele.style.overflow = 'hidden'
          ele.style.textOverflow = 'ellipsis'
          ele.style.whiteSpace = 'nowrap'
        } else {
          ele.setAttribute('title', ele.innerHTML)
          ele.style.paddingLeft = '5px'
          ele.style.paddingRight = '5px'
          ele.style.width = '70px'
          ele.style.overflow = 'hidden'
          ele.style.textOverflow = 'ellipsis'
          ele.style.whiteSpace = 'nowrap'
        }
      }

      let nodes = doc.getElementsByClassName('node table-node')
      for (let i = 0; i < nodes.length; i++) {
        let node = nodes[i]

        let titleDom = node.getElementsByClassName('title-text')[0]
        titleDom.style.color = '#676a6c'
        titleDom.style.textAlign = 'center'

        if (this.nodeDataState[titleDom.innerHTML] == '0') {
          node.style.backgroundColor = '#F0F8FF'
        } else {
          node.style.backgroundColor = '#FFFFCC'
        }
        let items = node.getElementsByClassName('field-item')
        for (let i = 0; i < items.length; i = i + 3) {
          let ele = items[i]
          let html = ele.innerText

          if (this.nodePkFiled[titleDom.innerHTML].indexOf(html) > -1) {
            ele.style.color = '#14d007'
            if (items[i + 1]) {
              items[i + 1].style.color = '#14d007'
            }
          }

          if (this.nodeReferenceField[titleDom.innerHTML].indexOf(html) > -1) {
            ele.style.color = '#198aee'
            if (items[i + 1]) {
              items[i + 1].style.color = '#198aee'
              let value = items[i + 1].innerText
              items[i + 1].innerHTML = ''
              let an = document.createElement('span')
              an.style.cursor = 'pointer'
              an.innerHTML = value ? value : '(空)'
              an.style.fontSize = '14px'
              an.onmouseover = () => {
                an.style.fontWeight = 'bold'
                an.style.fontSize = '18px'
              }
              an.onmouseout = () => {
                an.style.fontWeight = 'normal'
                an.style.fontSize = '14px'
              }
              an.style.marginLeft = '5px'
              an.title = value
              an.onclick = () => {
                window.referenceFieldSelect(titleDom.innerHTML, html, value)
              }
              items[i + 1].title = value
              items[i + 1].appendChild(an)
            }
            if (items[i + 2]) {
              items[i + 2].style.color = '#676a6c'
              items[i + 2].innerHTML = ''

              let img = document.createElement('img')
              img.style.width = '18px'
              img.style.height = '18px'
              img.style.cursor = 'pointer'
              img.style.marginLeft = '5px'
              img.style.marginBottom = '3px'
              img.onmouseover = () => {
                img.style.width = '21px'
                img.style.height = '21px'
              }
              img.onmouseout = () => {
                img.style.width = '18px'
                img.style.height = '18px'
              }
              img.onclick = () => {
                window.referenceFieldSwitch(titleDom.innerHTML, html, items[i + 1].innerText)
              }
              if (this.referenceFieldOpenState[this.nodeTitleIds[titleDom.innerHTML] + '.' + html]) {
                img.title = 'Close'
                img.src = '../icon/open_blue.png'
              } else {
                img.title = 'Open'
                img.src = '../icon/close.png'
              }
              items[i + 2].appendChild(img)
            }
          }
        }
      }
    },
    updateNodeByRecourd(record, tableName) {
      let pkField = this.pkField[tableName]
      let pkFieldStr = pkField.join('||')
      let pkFieldValue = []
      let fields = []
      let colData = this.nodeColumnDataMap[tableName]
      for (let i = 0; i < colData.length; i++) {
        let c = colData[i]
        fields.push({ 'fieldComment': c.title, 'fieldValue': record[c.dataIndex] })

        if (pkField.indexOf(c.dataIndex) > -1) {
          pkFieldValue.push(record[c.dataIndex])
        }
      }
      let pkFieldValueStr = pkFieldValue.join('||')
      let key = this.dsCode + '.' + this.dbCode + '.' + tableName
      let id = key + ':' + pkFieldStr + '.' + pkFieldValueStr
      let tmp = this.tableList.filter(t => t.tableName == tableName)[0].title.split(':')
      let title = (tmp[1] ? tmp[1] : tableName) + ':' + pkFieldValueStr
      this.nodePkFiled[title] = this.nodePkFiled[tableName]
      this.pkField[title] = this.pkField[tableName]
      this.nodeReferenceField[title] = this.nodeReferenceField[tableName]
      this.referenceField[title] = this.referenceField[tableName]
      this.nodeDataState[title] = record.dataState
      this.nodeIdDataMap[id] = record
      this.nodeTitleIds[id] = title
      this.nodeTitleIds[title] = id
      let nodeData = {
        'top': 50,
        'left': 50,
        'id': id,
        'title': title,
        'fields': fields
      }
      /////////////////////////////////////////
      let target = this.modelData.nodes.filter(n => n.id == nodeData.id)
      let targetEdges = []
      if (target.length == 0) {
        let doc = document.getElementsByClassName('data_model_div')[0]
        let nodes = doc.getElementsByClassName('node table-node')
        if (nodes.length > 0) {
          let maxTop = 0
          let maxLeft = 0
          for (let i = 0; i < nodes.length; i++) {
            let fields = nodes[i].getElementsByClassName('field')
            let attrs = nodes[i].getAttribute('style').split(';')
            let top = attrs[0].split(':')[1].replace('px', '')
            let left = attrs[1].split(':')[1].replace('px', '')

            if (left >= parseInt(maxLeft)) {
              maxLeft = parseInt(left)
              maxTop = parseInt(top) + 50 + (fields.length + 1) * 30
            }
            if (maxLeft < 200) {
              maxLeft = maxLeft + 300
            }
          }
          nodeData.top = parseInt(maxTop)
          nodeData.left = parseInt(maxLeft)
        }
        this.modelData.nodes.push(nodeData)
      } else {
        let doc = document.getElementsByClassName('data_model_div')[0]
        let nodes = doc.getElementsByClassName('node table-node')
        for (let i = 0; i < nodes.length; i++) {
          let node = nodes[i]
          let titleDom = node.getElementsByClassName('title-text')[0]
          if (target.title == titleDom.innerHTML) {
            let attrs = node.getAttribute('style').split(';')
            let top = attrs[0].split(':')[1].replace('px', '')
            let left = attrs[1].split(':')[1].replace('px', '')
            nodeData.top = parseInt(top.trim())
            nodeData.left = parseInt(left.trim())
          }
        }
        this.modelData.edges.forEach(e => {
          if (e.sourceNode == nodeData.id || e.targetNode == nodeData.id) {
            targetEdges.push(e)
          }
        })
        for (let i = 0; i < this.modelData.edges.length;) {
          let item = this.modelData.edges[i]
          if (item.sourceNode == nodeData.id || item.targetNode == nodeData.id) {
            this.modelData.edges.splice(i, 1)
          } else {
            i++
          }
        }
        this.delNodeById(nodeData.id)
        this.$nextTick(() => {
          this.modelData.nodes.push(nodeData)
        })
      }

      this.$nextTick(() => {
        targetEdges.forEach(te => {
          this.modelData.edges.push(te)
        })
        setTimeout(() => {
          this.setNodeStyle()
        }, 100)
      })
    },
    closeChildenNode(id) {
      let target = this.modelData.edges.filter(e => e.targetNode == id)
      if (target && target.length > 0) {
        return
      }
      for (let i = 0; i < this.modelData.edges.length;) {
        let e = this.modelData.edges[i]
        if (e.sourceNode == id) {
          this.modelData.edges.splice(i, 1)
          this.referenceFieldOpenState[e.sourceNode + '.' + e.source] = false
          this.closeChildenNode(e.targetNode)
        } else {
          i++
        }
      }
      this.delNodeById(id)
    },
    referenceFieldSwitch(title, field, value) {
      if (!value || '(空)' == value) {
        this.$message.warning('This field is empty and cannot be expanded!')
        return
      }
      let isOpen = false
      let nodeId = this.nodeTitleIds[title]
      for (let i = 0; i < this.modelData.edges.length;) {
        let e = this.modelData.edges[i]
        if (e.id.indexOf(nodeId + '.' + field + '_to_') > -1) {
          isOpen = true
          this.modelData.edges.splice(i, 1)
          this.referenceFieldOpenState[e.sourceNode + '.' + e.source] = false
          this.closeChildenNode(e.targetNode)
        } else {
          i++
        }
      }
      if (!isOpen) {
        let tableName = value.split(':')[0]
        let key = this.nodeTitleIds[title].split(':')[0]
        let v = this.nodeTitleIds[title].split(':')[1]
        let tmp = key.split('.')
        if (tableName == tmp[2] && value.split(':')[1] == v.split('.')[1]) {
          this.$message.warning('不支持展开自身！')
          return
        }
        this.referenceFieldOpenState[this.nodeTitleIds[title] + '.' + field] = true
        postAction('/stage-data/openReferenceField', {
          id: nodeId,
          field: field,
          value: value
        }).then(res => {
          if (res.success == true) {
            let record = res.result.record
            postAction('/editor/load-fields', {
              dsCode: this.dsCode,
              dbCode: this.dbCode,
              tableName: tableName
            }).then(res => {
              if (res.success == true) {
                let result = res.result
                let colData = []
                let nodePkField = [], pkField = []
                let nodeReferenceField = [], referenceField = []
                result.forEach(item => {
                  let title = item.fieldComment ? item.fieldComment : item.fieldName
                  if (item.isPk == '1') {
                    nodePkField.push(title)
                    pkField.push(item.fieldName)
                  }
                  if ('reference' == item.fieldType) {
                    nodeReferenceField.push(title)
                    referenceField.push(item.fieldName)
                  }
                  colData.push({
                    title: title,
                    dataIndex: item.fieldName,
                    ellipsis: true,
                    width: 100,
                    scopedSlots: {
                      filterDropdown: 'filterDropdown',
                      filterIcon: 'filterIcon',
                      customRender: 'customRender'
                    },
                    onFilter: (value, record) =>
                      record[item.fieldName]
                        .toString()
                        .toLowerCase()
                        .includes(value.toLowerCase()),
                    onFilterDropdownVisibleChange: visible => {
                      if (visible) {
                        setTimeout(() => {
                          this.searchInput.focus()
                        })
                      }
                    }
                  })
                })
                this.nodePkFiled[tableName] = nodePkField
                this.pkField[tableName] = pkField
                this.nodeReferenceField[tableName] = nodeReferenceField
                this.referenceField[tableName] = referenceField
                this.nodeColumnDataMap[tableName] = colData
                this.loadTableField(tableName)
                this.loadTableReferenceFieldMap(tableName)

                let pkFieldStr = pkField.join('||')
                let pkFieldValue = []
                for (let i = 0; i < colData.length; i++) {
                  let c = colData[i]
                  if (pkField.indexOf(c.dataIndex) > -1) {
                    pkFieldValue.push(record[c.dataIndex])
                  }
                }
                let pkFieldValueStr = pkFieldValue.join('||')
                let key = this.dsCode + '.' + this.dbCode + '.' + tableName
                let id = key + ':' + pkFieldStr + '.' + pkFieldValueStr

                let target = this.modelData.nodes.filter(n => n.id == id)
                if (target && target.length > 0) {

                } else {
                  this.updateNodeByRecourd(record, tableName)
                }

                let edge = {
                  id: nodeId + '.' + field + '_to_' + id + '.' + nodePkField[0],
                  source: field,
                  sourceNode: nodeId,
                  target: nodePkField[0],
                  targetNode: id
                }
                this.modelData.edges.push(edge)

                this.$nextTick(() => {
                  this.setNodeStyle()
                })
              } else {
                this.$message.error('Table For failure!')
                console.error(res)
              }
            })
          } else {
            this.$message.error('关联数据不存在,不能展开！')
          }
        })
      } else {
        this.referenceFieldOpenState[this.nodeTitleIds[title] + '.' + field] = false
        this.setNodeStyle()
      }
    },
    referenceFieldSelect(title, field, value) {
      // console.log(title)
      // console.log(field)
      // console.log(value)
      if ('(空)' == value) {
        value = undefined
      }
      this.currentNodeId = this.nodeTitleIds[title]
      this.currentField = field
      this.currentFieldValue = value
      let tableName = this.nodeTitleIds[title].split(':')[0].split('.')[2]
      // console.log(tableName)
      // console.log(this.nodeReferenceField[tableName])
      // console.log(this.referenceField[tableName])
      let referenceFields = this.referenceFieldMap[this.nodeTitleIds[title].split(':')[0]][field]
      if (!referenceFields) {
        this.$message.warning('该字段还未设置关联关系!')
        return
      }
      // console.log(referenceFields)
      let tabsName = [], tabsKey = []
      referenceFields.forEach(t => {
        let tn = t.split('.')[2]
        let tmp = this.tableList.filter(tl => tl.tableName == tn)[0].title.split(':')
        tabsName.push(tmp[1] ? tmp[1] : tn)
        tabsKey.push(tn)
      })
      // console.log(tabsName)

      this.refModelTitle = '选择[' + field + ']'
      this.tabsName = tabsName
      this.tabsKey = tabsKey
      if (value) {
        this.activeKey = value.split(':')[0]
        let pkValue = parseInt(value.split(':')[1])
        this.refModelSelectKeys = [pkValue]
        let tableName = this.activeKey
        postAction('/editor/load-fields', {
          dsCode: this.dsCode,
          dbCode: this.dbCode,
          tableName: tableName
        }).then(res => {
          if (res.success == true) {
            this.tabsData[tableName] = {}
            let result = res.result
            let colData = []
            let nodePkField = [], pkField = []
            let nodeReferenceField = [], referenceField = []
            result.forEach(item => {
              let title = item.fieldComment ? item.fieldComment : item.fieldName
              if (item.isPk == '1') {
                nodePkField.push(title)
                pkField.push(item.fieldName)
              }
              if ('reference' == item.fieldType) {
                nodeReferenceField.push(title)
                referenceField.push(item.fieldName)
              }
              colData.push({
                title: title,
                dataIndex: item.fieldName,
                ellipsis: true,
                width: 100,
                scopedSlots: {
                  filterDropdown: 'filterDropdown',
                  filterIcon: 'filterIcon',
                  customRender: 'customRender'
                },
                onFilter: (value, record) =>
                  record[item.fieldName]
                    .toString()
                    .toLowerCase()
                    .includes(value.toLowerCase()),
                onFilterDropdownVisibleChange: visible => {
                  if (visible) {
                    setTimeout(() => {
                      this.searchInput.focus()
                    })
                  }
                }
              })
            })
            this.nodePkFiled[tableName] = nodePkField
            this.pkField[tableName] = pkField
            this.refModelTableRowKey = pkField[0]
            this.nodeReferenceField[tableName] = nodeReferenceField
            this.referenceField[tableName] = referenceField
            this.tabsData[tableName]['columns'] = colData
            this.refModelColumnsData = colData
            this.nodeColumnDataMap[tableName] = colData
            postAction('/stage-data/list', {
              dsCode: this.dsCode,
              dbCode: this.dbCode,
              tableName: tableName
            }).then(res => {
              if (res.success == true) {
                let dataList = res.result.dataList
                this.refModelTableData = dataList
                this.tabsData[tableName]['data'] = res.result.dataList
                this.tableOption[tableName] = {
                  'add': res.result.add,
                  'del': res.result.del,
                  'edit': res.result.edit,
                  'msg': res.result.msg,
                  'maxId': res.result.maxId
                }
                this.refModelVisible = true
              } else {
                this.$message.error('数据 For failure!')
                console.error(res)
              }
            })
            this.loadTableField(tableName)
            this.loadTableReferenceFieldMap(tableName)

          } else {
            this.$message.error('Table For failure!')
            console.error(res)
          }
        })

      } else {
        this.activeKey = tabsKey[0]
        let tableName = this.activeKey
        this.refModelSelectKeys = []
        postAction('/editor/load-fields', {
          dsCode: this.dsCode,
          dbCode: this.dbCode,
          tableName: tableName
        }).then(res => {
          if (res.success == true) {
            this.tabsData[tableName] = {}
            let result = res.result
            let colData = []
            let nodePkField = [], pkField = []
            let nodeReferenceField = [], referenceField = []
            result.forEach(item => {
              let title = item.fieldComment ? item.fieldComment : item.fieldName
              if (item.isPk == '1') {
                nodePkField.push(title)
                pkField.push(item.fieldName)
              }
              if ('reference' == item.fieldType) {
                nodeReferenceField.push(title)
                referenceField.push(item.fieldName)
              }
              colData.push({
                title: title,
                dataIndex: item.fieldName,
                ellipsis: true,
                width: 100,
                scopedSlots: {
                  filterDropdown: 'filterDropdown',
                  filterIcon: 'filterIcon',
                  customRender: 'customRender'
                },
                onFilter: (value, record) =>
                  record[item.fieldName]
                    .toString()
                    .toLowerCase()
                    .includes(value.toLowerCase()),
                onFilterDropdownVisibleChange: visible => {
                  if (visible) {
                    setTimeout(() => {
                      this.searchInput.focus()
                    })
                  }
                }
              })
            })
            this.nodePkFiled[tableName] = nodePkField
            this.pkField[tableName] = pkField
            this.refModelTableRowKey = pkField[0]
            this.nodeReferenceField[tableName] = nodeReferenceField
            this.referenceField[tableName] = referenceField
            this.tabsData[tableName]['columns'] = colData
            this.refModelColumnsData = colData
            // console.log(this.refModelColumnsData)
            this.nodeColumnDataMap[tableName] = colData
            postAction('/stage-data/list', {
              dsCode: this.dsCode,
              dbCode: this.dbCode,
              tableName: tableName
            }).then(res => {
              if (res.success == true) {
                this.refModelTableData = res.result.dataList
                // console.log(this.refModelTableData)
                this.tabsData[tableName]['data'] = res.result.dataList
                this.tableOption[tableName] = {
                  'add': res.result.add,
                  'del': res.result.del,
                  'edit': res.result.edit,
                  'msg': res.result.msg,
                  'maxId': res.result.maxId
                }
                this.refModelVisible = true
              } else {
                this.$message.error('数据 For failure!')
                console.error(res)
              }
            })
            this.loadTableField(tableName)
            this.loadTableReferenceFieldMap(tableName)

          } else {
            this.$message.error('Table For failure!')
            console.error(res)
          }
        })
      }
    },
    tabsChange(key) {
      this.activeKey = key
      if (key == this.currentFieldValue.split(':')[0]) {
        let pkValue = parseInt(this.currentFieldValue.split(':')[1])
        this.refModelSelectKeys = [pkValue]
      } else {
        this.refModelSelectKeys = []
      }
      let tableName = this.activeKey
      postAction('/editor/load-fields', {
        dsCode: this.dsCode,
        dbCode: this.dbCode,
        tableName: tableName
      }).then(res => {
        if (res.success == true) {
          this.tabsData[tableName] = {}
          let result = res.result
          let colData = []
          let nodePkField = [], pkField = []
          let nodeReferenceField = [], referenceField = []
          result.forEach(item => {
            let title = item.fieldComment ? item.fieldComment : item.fieldName
            if (item.isPk == '1') {
              nodePkField.push(title)
              pkField.push(item.fieldName)
            }
            if ('reference' == item.fieldType) {
              nodeReferenceField.push(title)
              referenceField.push(item.fieldName)
            }
            colData.push({
              title: title,
              dataIndex: item.fieldName,
              ellipsis: true,
              width: 100,
              scopedSlots: {
                filterDropdown: 'filterDropdown',
                filterIcon: 'filterIcon',
                customRender: 'customRender'
              },
              onFilter: (value, record) =>
                record[item.fieldName]
                  .toString()
                  .toLowerCase()
                  .includes(value.toLowerCase()),
              onFilterDropdownVisibleChange: visible => {
                if (visible) {
                  setTimeout(() => {
                    this.searchInput.focus()
                  })
                }
              }
            })
          })
          this.nodePkFiled[tableName] = nodePkField
          this.pkField[tableName] = pkField
          this.refModelTableRowKey = pkField[0]
          this.nodeReferenceField[tableName] = nodeReferenceField
          this.referenceField[tableName] = referenceField
          this.tabsData[tableName]['columns'] = colData
          this.refModelColumnsData = colData
          this.nodeColumnDataMap[tableName] = colData
          postAction('/stage-data/list', {
            dsCode: this.dsCode,
            dbCode: this.dbCode,
            tableName: tableName
          }).then(res => {
            if (res.success == true) {
              this.refModelTableData = res.result.dataList
              this.tabsData[tableName]['data'] = res.result.dataList
              this.tableOption[tableName] = {
                'add': res.result.add,
                'del': res.result.del,
                'edit': res.result.edit,
                'msg': res.result.msg,
                'maxId': res.result.maxId
              }
            } else {
              this.$message.error('数据 For failure!')
              console.error(res)
            }
          })
          this.loadTableField(tableName)
          this.loadTableReferenceFieldMap(tableName)

        } else {
          this.$message.error('Table For failure!')
          console.error(res)
        }
      })
    },
    onSelectChange(selectedRowKeys) {
      this.refModelSelectKeys = selectedRowKeys
    },
    onSelectChange1(selectedRowKeys) {
      this.selectedRowKeys = selectedRowKeys
    },
    refModelOk() {
      if (this.refModelSelectKeys.length == 0) {
        this.$message.warning('请选择数据!')
        return
      }
      // console.log(this.currentNodeId)
      // console.log(this.currentField)
      // console.log(this.activeKey)
      // console.log(this.pkField[this.activeKey][0])
      // console.log(this.refModelSelectKeys[0])
      // console.log(this.nodeIdDataMap[this.currentNodeId])

      let tmpName = this.currentNodeId.split(':')[0].split('.')[2]
      let field = this.nodeColumnDataMap[tmpName].filter(c => c.title == this.currentField)[0].dataIndex

      postAction('/stage-data/updateReferenceField', {
        field: field,
        sourceNode: this.currentNodeId,
        targetNode: this.dsCode + '.' + this.dbCode + '.' + this.activeKey + ':' + this.pkField[this.activeKey][0] + '.' + this.refModelSelectKeys[0]
      }).then(res => {
        if (res.success == true) {
          let sourceNode = this.modelData.nodes.filter(n => n.id == this.currentNodeId)[0]
          sourceNode.fields.forEach(f => {
            if (f.fieldComment == this.currentField) {
              // f.fieldValue = this.pkField[this.activeKey][0] +":"+ this.refModelSelectKeys[0]

              let doc = document.getElementsByClassName('data_model_div')[0]
              let nodes = doc.getElementsByClassName('node table-node')
              for (let i = 0; i < nodes.length; i++) {
                let node = nodes[i]
                let titleDom = node.getElementsByClassName('title-text')[0]
                if (titleDom.innerHTML == this.nodeTitleIds[this.currentNodeId]) {
                  let items = node.getElementsByClassName('field-item')
                  for (let i = 0; i < items.length; i = i + 3) {
                    let ele = items[i]
                    let html = ele.innerText
                    if (html == this.currentField) {
                      items[i + 1].innerHTML = this.activeKey + ':' + this.refModelSelectKeys[0]
                    }
                  }
                }
              }

              this.nodeIdDataMap[this.currentNodeId][res.result.field] = this.activeKey + ':' + this.refModelSelectKeys[0]
            }
          })
          this.$nextTick(() => {
            setTimeout(() => {
              this.setNodeStyle()
              this.refModelVisible = false

              console.log(this.modelData.edges)
              console.log(this.currentNodeId + '.' + this.currentField + '_to_')
              let target = this.modelData.edges.filter(e => e.id.indexOf(this.currentNodeId + '.' + this.currentField + '_to_') > -1)
              console.log(target)
              if (target.length > 0) {
                let v = this.activeKey + ':' + this.refModelSelectKeys[0]
                this.referenceFieldSwitch(this.nodeTitleIds[this.currentNodeId], this.currentField, v)
                this.$nextTick(() => {
                  this.referenceFieldSwitch(this.nodeTitleIds[this.currentNodeId], this.currentField, v)
                })
              }
            }, 100)
          })
        } else {
          this.$message.error('Save Failed！')
          console.error(res)
        }
      })
    },
    refModelAdd() {
      let tableName = this.activeKey
      postAction('/editor/load-fields', {
        dsCode: this.dsCode,
        dbCode: this.dbCode,
        tableName: tableName
      }).then(res => {
        if (res.success == true) {
          let result = res.result
          let colData = []
          let nodePkField = [], pkField = []
          let nodeReferenceField = [], referenceField = []
          result.forEach(item => {
            let title = item.fieldComment ? item.fieldComment : item.fieldName
            if (item.isPk == '1') {
              nodePkField.push(title)
              pkField.push(item.fieldName)
            }
            if ('reference' == item.fieldType) {
              nodeReferenceField.push(title)
              referenceField.push(item.fieldName)
            }
            colData.push({
              title: title,
              dataIndex: item.fieldName,
              ellipsis: true,
              width: 100,
              scopedSlots: {
                filterDropdown: 'filterDropdown',
                filterIcon: 'filterIcon',
                customRender: 'customRender'
              },
              onFilter: (value, record) =>
                record[item.fieldName]
                  .toString()
                  .toLowerCase()
                  .includes(value.toLowerCase()),
              onFilterDropdownVisibleChange: visible => {
                if (visible) {
                  setTimeout(() => {
                    this.searchInput.focus()
                  })
                }
              }
            })
          })
          this.nodePkFiled[tableName] = nodePkField
          this.pkField[tableName] = pkField
          this.nodeReferenceField[tableName] = nodeReferenceField
          this.referenceField[tableName] = referenceField
          this.nodeColumnDataMap[tableName] = colData
          this.loadTableField(tableName)
          this.loadTableReferenceFieldMap(tableName)

          postAction('/stage-data/load-option', {
            dsCode: this.dsCode,
            dbCode: this.dbCode,
            tableName: tableName
          }).then(res => {
            if (res.success == true) {
              this.tableOption[tableName] = res.result

              let rowData = {}
              let maxId = this.tableOption[tableName].maxId
              rowData[pkField[0]] = maxId
              rowData['dataState'] = '1'
              postAction('/stage-data/save', {
                dsCode: this.dsCode,
                dbCode: this.dbCode,
                tableName: tableName,
                pkField: pkField[0],
                rowData: rowData
              }).then(res => {
                if (res.success == true) {
                  this.loadTableOption(tableName)

                  this.updateNodeByRecourd(rowData, tableName)

                  let targetNodeId = this.dsCode + '.' + this.dbCode + '.' + tableName + ':' + pkField[0] + '.' + maxId
                  for (let i = 0; i < this.modelData.edges.length;) {
                    let e = this.modelData.edges[i]
                    if (e.sourceNode == this.currentNodeId && e.source == this.currentField) {
                      this.modelData.edges.splice(i, 1)
                    } else {
                      i++
                    }
                  }

                  let edge = {
                    id: this.currentNodeId + '.' + this.currentField + '_to_' + targetNodeId + '.' + nodePkField[0],
                    source: this.currentField,
                    sourceNode: this.currentNodeId,
                    target: nodePkField[0],
                    targetNode: targetNodeId
                  }
                  this.modelData.edges.push(edge)

                  this.$nextTick(() => {
                    let tmpName = this.currentNodeId.split(':')[0].split('.')[2]
                    let field = this.nodeColumnDataMap[tmpName].filter(c => c.title == this.currentField)[0].dataIndex

                    postAction('/stage-data/updateReferenceField', {
                      field: field,
                      sourceNode: this.currentNodeId,
                      targetNode: targetNodeId
                    }).then(res => {
                      if (res.success == true) {
                        let sourceNode = this.modelData.nodes.filter(n => n.id == this.currentNodeId)[0]
                        sourceNode.fields.forEach(f => {
                          if (f.fieldComment == this.currentField) {
                            let tmp = targetNodeId.split(':')
                            let v = tmp[0].split('.')[2] + ':' + tmp[1].split('.')[1]

                            let doc = document.getElementsByClassName('data_model_div')[0]
                            let nodes = doc.getElementsByClassName('node table-node')
                            for (let i = 0; i < nodes.length; i++) {
                              let node = nodes[i]
                              let titleDom = node.getElementsByClassName('title-text')[0]
                              if (titleDom.innerHTML == this.nodeTitleIds[this.currentNodeId]) {
                                let items = node.getElementsByClassName('field-item')
                                for (let i = 0; i < items.length; i = i + 3) {
                                  let ele = items[i]
                                  let html = ele.innerText
                                  if (html == this.currentField) {
                                    items[i + 1].innerHTML = v
                                  }
                                }
                              }
                            }

                            this.nodeIdDataMap[this.currentNodeId][res.result.field] = v
                          }
                        })

                        this.$nextTick(() => {
                          setTimeout(() => {
                            this.setNodeStyle()
                          }, 100)
                        })
                      } else {
                        this.$message.error('Save Failed！')
                        console.error(res)
                      }
                    })
                  })
                } else {
                  this.$message.error('Save Failed！')
                  console.error(res)
                }
              }).finally(() => {
                this.refModelVisible = false
              })
            } else {
              this.$message.error('表属性 For failure!')
              console.error(res)
            }
          })

        } else {
          this.$message.error('Table For failure!')
          console.error(res)
        }
      })
    },
    refModelCancel() {
      this.refModelVisible = false
    },

    handleAdd() {
      this.form.validateFields((err, values) => {
        if (!err) {
          let tableName = this.form.getFieldValue('tableName')
          this.tableName = tableName
          if (!this.tableOption[tableName].add) {
            this.$message.warning(this.tableOption[tableName].msg)
            return
          }

          let key = this.dsCode + '.' + this.dbCode + '.' + tableName
          this.fFields = this.formField[key]
          this.title = 'New'
          this.modelIsAdd = true
          this.model = {}
          this.form.reset
          let pkIndex = 0, stateIndex = 0
          for (let i = 0; i < this.fFields.length; i++) {
            let f = this.fFields[i]
            if (f.field == this.pkField[tableName][0]) {
              pkIndex = i
            }
            if (f.field == 'dataState') {
              stateIndex = i
            }
          }
          this.fFields[pkIndex].value = this.tableOption[tableName].maxId
          this.fFields[stateIndex].value = '1'
          this.modelVisible = true
        }
      })
    },
    handleDel() {
      if (this.selectedRowKeys.length == 0) {
        this.$message.warning('请选择要 Delete 的数据！')
        return
      }
      let tableName = this.form.getFieldValue('tableName')
      postAction('/stage-data/del', {
        dsCode: this.dsCode,
        dbCode: this.dbCode,
        tableName: tableName,
        pkField: this.pkField[this.tableName][0],
        ids: this.selectedRowKeys
      }).then(res => {
        if (res.success == true) {
          for (let i = 0; i < this.tableData.length;) {
            let item = this.tableData[i]
            let pkValue = item[this.pkField[tableName][0]]
            if (this.selectedRowKeys.indexOf(pkValue) > -1) {
              this.tableData.splice(i, 1)

              let nodeId = this.dsCode + '.' + this.dbCode + '.' + this.tableName + ':' + this.pkField[tableName][0] + '.' + pkValue
              for (let i = 0; i < this.modelData.edges.length;) {
                let item = this.modelData.edges[i]
                if (item.id.indexOf(nodeId) > -1) {
                  this.modelData.edges.splice(i, 1)
                } else {
                  i++
                }
              }
              this.delNodeById(nodeId)
            } else {
              i++
            }
          }
        } else {
          this.$message.error('数据 Delete 失败！')
          console.error(res)
        }
      })
    },
    handleOk: function() {
      if (!this.tableOption[this.tableName].edit) {
        this.$message.warning(this.tableOption[this.tableName].msg)
        return
      }
      this.fApi.validate((valid, fail) => {
        if (valid) {
          this.confirmLoading = true
          this.fFields.forEach(f => {
            let type = f.type
            let field = f.field
            if ('a-date-picker' == type) {
              if (this.model[field]) {
                this.model[field] = moment(this.model[field]).format(f.props['format'])
              }
            }
          })
          postAction('/stage-data/save', {
            dsCode: this.dsCode,
            dbCode: this.dbCode,
            tableName: this.tableName,
            pkField: this.pkField[this.tableName][0],
            rowData: this.model
          }).then(res => {
            if (res.success == true) {
              this.modelVisible = false
              if (this.modelIsAdd) {
                this.loadTableData(this.tableName)
                this.loadTableOption(this.tableName)
              } else {
                this.loadTableOption(this.tableName)
                if (this.tableName == this.form.getFieldValue('tableName')) {
                  this.loadTableData(this.tableName)
                }
                let key = this.dsCode + '.' + this.dbCode + '.' + this.tableName
                let id = key + ':' + this.pkField[this.tableName][0] + '.' + this.model[this.pkField[this.tableName][0]]
                if (this.model['dataState'] == 0) {
                  this.model['dataState'] = '2'
                }
                this.updateNodeByRecourd(this.model, this.tableName)
              }
            } else {
              this.$message.error('Save Failed！')
              console.error(res)
            }
          }).finally(() => {
            this.modelLoading = false
          })

        } else {
          // 表单验证未通过
        }
      })
    },
    handleCancel() {
      this.modelVisible = false
    },
    fMounted(fApi) {
      this.fApi = fApi
    },

    redo() {
      this.modelData.edges = []
      this.modelData.nodes = []
      this.currentNode = {}
      this.currentEdge = {}
      this.nodeTitleIds = {}
      this.nodeIdDataMap = {}
      this.nodePkFiled = {}
      this.nodeReferenceField = {}
      this.nodeDataState = {}
      this.nodeColumnDataMap = {}
      this.nodeReferenceFieldOpenState = {}

      this.loadTableColumnData(this.form.getFieldValue('tableName'))
      this.$nextTick(() => {
        this.$message.success('The canvas is empty and you can continue！')
      })
    },
    sync() {
      if (this.modelData.nodes.length == 0) {
        this.$message.warning('There is no data in the canvas!')
        return
      }
      if (this.currentNode.options) {
        let ids = [this.currentNode.options.id]
        postAction('/stage-data/sync', {
          ids: ids
        }).then(res => {
          if (res.success == true) {
            this.$message.success('数据同步成功！')
          } else {
            this.$message.error('数据同步发生错误！')
            console.error(res)
          }
        })
      } else {
        const that = this
        this.$confirm({
          title: 'Saving all tables on the mind map?',
          content: 'After a node is selected, only the structures related to the node are synchronized',
          onOk() {
            let ids = []
            that.modelData.nodes.forEach(n => {
              ids.push(n.id)
            })
            postAction('/stage-data/sync', {
              ids: ids
            }).then(res => {
              if (res.success == true) {
                that.$message.success('数据同步成功！')
              } else {
                that.$message.error('数据同步发生错误！')
                console.error(res)
              }
            })
          },
          onCancel() {
            console.log('Cancel')
          },
          class: 'test'
        })
      }
    }
  }
}
</script>

<style scoped>
.left_div {
  width: 69%;
  height: 760px;
  float: left;
  background-color: white;
  padding: 5px;
}

.left_div .data_model_div {
  width: 100%;
  height: 760px;
}

.left_div .data_div {
  width: 100%;
  height: 300px;
}

.right_div {
  width: 30%;
  height: 760px;
  float: right;
  padding: 10px;
  margin-left: 5px;
  background-color: white;
}

.tree_div {
  margin-top: 20px;
  width: 100%;
  height: 620px;
  overflow: auto;
  background-color: white;
  border: grey 1px dashed;
}
</style>

<style>

.butterfly-table-building .table-node {
  border-radius: 20px !important;
}

.butterfly-table-building .table-node .title {
  background-color: #89cff0 !important;
  border-radius: 20px !important;
}

.butterfly-table-building .table-node .field {
  color: #676a6c !important;
  border-radius: 28px !important;
}


.sortable-row-demo .drag-btn {
  cursor: move;
  font-size: 12px;
}

.sortable-row-demo .vxe-body--row.sortable-ghost,
.sortable-row-demo .vxe-body--row.sortable-chosen {
  background-color: #dfecfb;
}
</style>

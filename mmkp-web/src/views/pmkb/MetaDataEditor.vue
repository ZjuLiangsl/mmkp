<template>
  <div>
    <div class="left_div">
      <div id="model_div_id" class="model_div">
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
          <!--<a-button icon="delete" type="primary" style="float:right;margin-right: 20px;" title=" Delete: Delete 本地暂存的结构、关联关系、内容数据!" @click="remove">
             Delete
          </a-button>-->
          <a-button icon="redo" type="primary" style="float:right;margin-right: 20px;" title="Rest: Just empty the canvas without doing anything to the data."
                    @click="redo">
            Reset
          </a-button>
        </div>
      </div>
    </div>
    <div class="right_div">
      <a-form :form="form" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="Source Name">
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
        </a-form-item>
        <a-form-item label="Table Name">
          <a-input-search placeholder="Search Table Name" @change="onChange" @pressEnter="onSearchTable"
                          @search="onSearchTable"/>
        </a-form-item>
      </a-form>
      <a-button icon="diff" type="primary" @click="addTable">
        New Table
      </a-button>
      <div class="tree_div">
        <a-tree :load-data="loadFields" :loadedKeys="loadedKeys" :tree-data="treeData" v-if="treeData.length>0"
                show-icon draggable @dragend="ondragend">
          <template slot="staging" slot-scope="{ key: treeKey, title,'is-leaf':isLeaf }">
            <a-dropdown :trigger="['contextmenu']">
              <span style="color: orange">{{ title }}</span>
              <template #overlay>
                <a-menu v-show="!isLeaf" @click="({ key: menuKey }) => onContextMenuClick(treeKey, menuKey)">
                  <a-menu-item key="1"> Delete</a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </template>
          <template slot="editing" slot-scope="{ key: treeKey, title,'is-leaf':isLeaf }">
            <a-dropdown :trigger="['contextmenu']">
              <span style="color: orange">{{ title }}</span>
              <template #overlay>
                <a-menu v-show="!isLeaf" @click="({ key: menuKey }) => onContextMenuClick(treeKey, menuKey)">
                  <a-menu-item key="1"> Delete</a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </template>
        </a-tree>
        <a-empty v-else/>
      </div>
    </div>

    <a-modal
      title="Category Design"
      :visible="visible"
      :confirm-loading="confirmLoading"
      :width="1200"
      temporary storage
      @ok="saveTable"
      @cancel="handleCancel"
    >
      <a-form-model ref="form" :model="modelMd" :rules="validatorRules">
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-model-item :labelCol="{span: 4}" :wrapperCol="{span: 20}" label="Table Name" prop="tableName">
              <a-input placeholder="Please Input Table Name" v-model="modelMd.tableName" :disabled="!addMd"/>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item :labelCol="{span: 4}" :wrapperCol="{span: 20}" label="Table Annotation" prop="tableComment">
              <a-input placeholder="Please Input Table Annotation" v-model="modelMd.tableComment"/>
            </a-form-model-item>
          </a-col>
        </a-row>
      </a-form-model>
      <a-button class="editable-add-btn" @click="handleRowAdd" type="primary" style="margin:5px;">
        New Field
      </a-button>
      <a-table :columns="columnsMd" :data-source="dataMd" bordered :pagination="false" size="small" rowKey="id">
        <template
          v-for="col in rowEditColumnsMd"
          :slot="col"
          slot-scope="text, record, index"
        >
          <div :key="col" style="text-align: center">
            <template v-if="record.editable">
              <a-select allowClear v-if="col == 'fieldType'" style="width:100%;margin: -5px 0;" :value="text"
                        @change="value => handleChangeRow(value, record.id, col,record)">
                <a-select-option v-for="(item, key) in dataTypes" :key="key" :value="item.value"
                                 :disabled="(record.fieldType=='varchar' && item.value!='varchar' && (record.id.indexOf('field-add-')<0 || record.dataState!='1'))
                                     || (record.fieldType=='int' && item.value!='int'&& item.value!='varchar' && item.value!='decimal' && (record.id.indexOf('field-add-')<0 || record.dataState!='1'))
                                     || (record.fieldType=='decimal' && item.value!='decimal' && item.value!='varchar' && item.value!='int' && (record.id.indexOf('field-add-')<0 || record.dataState!='1'))
                                     || (record.fieldType=='date' && item.value!='date' && item.value!='varchar' && item.value!='datetime' && (record.id.indexOf('field-add-')<0 || record.dataState!='1'))
                                     || (record.fieldType=='datetime' && item.value!='datetime' && item.value!='varchar' && item.value!='date' && (record.id.indexOf('field-add-')<0 || record.dataState!='1'))">
                      <span :title=" item.text || item.label ">
                        {{ item.text || item.label }}
                      </span>
                </a-select-option>
              </a-select>
              <span v-else-if="col == 'isPk'">
                <a-tag v-if="text=='1'" color="blue">Y</a-tag>
                <a-tag v-else>N</a-tag>
              </span>
              <!--<a-checkbox v-else-if="col == 'isPk'" :checked="text=='1'" @change="e => handleChangeRow(e.target.checked, record.id, col,record)"/>-->
              <a-input-number allowClear v-else-if="col == 'fieldLength' && record['fieldType']=='varchar'"
                              style="width:100%;margin: -5px 0;" :min="0"
                              :value="text" @change="value => handleChangeRow(value, record.id, col,record)"/>
              <span v-else-if="col == 'fieldLength' && record['fieldType']!='varchar'">{{ text }}</span>
              <div v-else-if="col == 'extField4' || col == 'extField5'">
                <a-date-picker v-if="record['fieldType']=='datetime'" valueFormat="YYYY-MM-DD HH:mm:ss"
                               format="YYYY-MM-DD HH:mm:ss"
                               :show-time="{ defaultValue: moment('00:00:00', 'HH:mm:ss') }"
                               @change="value => handleChangeRow(value, record.id, col,record)"/>
                <a-date-picker v-else-if="record['fieldType']=='date'" valueFormat="YYYY-MM-DD" format="YYYY-MM-DD"
                               @change="value => handleChangeRow(value, record.id, col,record)"/>
                <a-input-number v-else-if="record['fieldType']=='int'" :default-value="text"
                                @change="value => handleChangeRow(value, record.id, col,record)"/>
                <a-input-number v-else-if="record['fieldType']=='decimal'" :default-value="text" step="0.01"
                                @change="value => handleChangeRow(value, record.id, col,record)"/>
                <span v-else></span>
              </div>
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
                  <a @click="() => saveRowMd(record.id)">Save</a>
                  <a @click="() => cancelRowMd(record.id)" style="margin-left: 10px;">Cancel</a>
                </span>
            <span v-else>
                  <a :disabled="editingKeyMd !== '' || record.isPk=='1'" @click="() => editRowMd(record.id)">Edit</a>
                  <a :disabled="editingKeyMd !== '' || record.isPk=='1'" @click="() => delRowMd(record.id)"
                     style="margin-left: 10px;">Delete</a>
                </span>
          </div>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script>
import VisualModeling from 'react-visual-modeling'
import 'react-visual-modeling/dist/index.css'

import { getAction, postAction } from '@api/manage'
import { ajaxGetDictItems, getDictItemsFromCache } from '@api/api'
import { alwaysResolve, validateDuplicateValue } from '@/utils/util'

import moment from 'moment'
import * as api from '@api/api'

export default {
  name: 'MetaDataEditer',
  components: {
    VisualModeling
  },
  data() {
    return {
      moment,
      form: this.$form.createForm(this, { name: 'search' }),
      modelColumns: [
        {
          key: 'fieldName',
          primaryKey: true,
          width: 100,
          title: 'Field Name'
        },
        {
          key: 'fieldTypeFull',
          width: 110,
          title: 'Field Type',
          render: (val, row) => {
            return val
          }
        },
        {
          key: 'fieldComment',
          width: 100,
          title: 'Field Annotation',
          render: (val, row) => {
            if (val) {
              return val.toString()
            } else {
              return ''
            }
          }
        },
        {
          key: 'refKeys',
          width: 50,
          title: 'FK',
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
          }
        }
      ],
      actionMenu: [],
      currentNode: {},
      currentEdge: {},
      tableState: {},
      stageField: {},
      editField: {},
      referenceField: {},
      referenceFieldOpenState: {},
      pkField: {},
      referenceMap: {},
      canvasNodeKey: {},

      dsList: [],
      dsCode: undefined,
      dbList: [],
      dbCode: undefined,
      searchText: '',

      treeData: [],
      loadedKeys: [],

      checkRuleData: [],
      checkRuleDataMap: {},
      dictsMap: {},

      visible: false,
      confirmLoading: false,
      oldTableName: '',
      modelMd: {},
      validatorRules: {
        tableComment: [{ required: false, message: 'Please Input Table Annotation!' }],
        tableName: [
          { required: true, message: 'Please Input Table Name!' },
          {
            validator: (rule, value, callback) => {
              let pattern = /^[a-z|A-Z][a-z|A-Z\d_]{0,}$/
              if (!pattern.test(value)) {
                callback('The table name must start with a letter and can contain digits or underscores')
              } else {
                return true
              }
            }
          }
        ]
      },
      addMd: true,
      rowKeyMd: '',
      dataMd: [],
      cacheDataMd: [],
      fieldDataMd: [],
      rowEditColumnsMd: ['fieldName', 'fieldType', 'fieldLength', 'isPk', 'fieldComment', 'extField4', 'extField5'],
      columnsMd: [
        {
          title: 'Field Name',
          dataIndex: 'fieldName',
          width: 200,
          scopedSlots: { customRender: 'fieldName' }
        },
        {
          title: 'Type',
          dataIndex: 'fieldType',
          width: 150,
          scopedSlots: { customRender: 'fieldType' }
        },
        {
          title: 'Annotation',
          dataIndex: 'fieldComment',
          scopedSlots: { customRender: 'fieldComment' },
          width: 200,
          ellipsis: true
        },
        {
          title: 'primary key',
          dataIndex: 'isPk',
          width: 100,
          scopedSlots: { customRender: 'isPk' }
        },
        {
          title: 'Length',
          dataIndex: 'fieldLength',
          width: 100,
          scopedSlots: { customRender: 'fieldLength' }
        },

        /*{
          title: '小数点',
          dataIndex: 'fieldDigit',
          width: '8%'
        },
        {
          title: '可为空',
          dataIndex: 'isNullable',
          width: '8%'
        },
        {
          title: '完整类型',
          dataIndex: 'fieldTypeFull',
          width: '8%'
        },
        {
          title: '是否唯一',
          dataIndex: 'isUnique',
          width: '8%'
        },
        {
          title: '外键库',
          dataIndex: 'fkDbName',
          width: '8%'
        },
        {
          title: 'Status',
          dataIndex: 'dataState',
          width: '8%'
        },*/

        {
          title: 'Min',
          dataIndex: 'extField4',
          scopedSlots: { customRender: 'extField4' },
          width: 150,
          ellipsis: true
        },
        {
          title: 'Max',
          dataIndex: 'extField5',
          scopedSlots: { customRender: 'extField5' },
          width: 150,
          ellipsis: true
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
        }
      ],
      editingKeyMd: '',
      dataTypes: [],
      fkTableNames: [],
      fkFieldNames: [],
      fkFieldMaps: {},
      fieldAddNo: 0,

      isReferenceAdd: false,
      primaryNodeId: '',
      primaryField: ''
    }
  },
  created() {
    window.referenceFieldOpen = this.referenceFieldOpen
    window.referenceFieldClose = this.referenceFieldClose
    window.addReferenceTable = this.addReferenceTable
    window.setReferenceTable = this.setReferenceTable
  },
  beforeDestroy() {
  },
  mounted() {
    this.loadItems()
  },
  methods: {
    onContextMenuClick(treeKey, menuKey) {
      let tmp = treeKey.split('.')
      postAction('/md/tables/remove', {
        dsCode: tmp[0], dbName: tmp[1], tableName: tmp[2]
      }).then(res => {
        if (res.success == true) {
          this.delNodeById(treeKey)
          this.loadTreeData()
          let leftNodes = res.result.leftNodes
          if (leftNodes) {
            leftNodes.forEach(ln => {
              let exist = false
              this.modelData.nodes.forEach(n => {
                if (ln.id = n.id) {
                  exist = true
                }
              })
              if (exist) {
                this.updateTable(ln)
              }
            })
          }
          this.tableData = []
          this.tableColumns = []
          this.$message.success('Category  Delete the success！')
        } else {
          this.$message.error(res.message)
          console.error(res)
        }
      })
    },
    delEdge(id, dataid) {
      this.modelData.edges.forEach((item, index) => {
        let mid = item.sourceNode + '-' + item.source + '-' + item.targetNode + '-' + item.target
        if (id == mid) {
          this.modelData.edges.splice(index, 1)
        }
      })
      postAction('/md/tables/delEdge', { id: dataid }).then(res => {
        if (res.success == true) {
          let result = res.result
          if (result.refNode) {
            this.updateTable(result.refNode)
          }
          this.$nextTick(() => {
            this.$nextTick(() => {
              this.$message.success('incidence relation Delete the success！')
            })
          })
        } else {
          this.$message.error('incidence relation Delete failure！')
          console.error(res)
        }
      })
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

      this.isReferenceAdd = false
      this.primaryNodeId = ''
      this.primaryField = ''

      this.dataMd = node.options.fields
      this.cacheDataMd = this.dataMd.map(item => ({ ...item }))
      this.fieldDataMd = this.dataMd.map(item => ({ ...item }))
      this.editingKeyMd = ''
      this.addMd = false
      this.visible = true
      this.$nextTick(() => {
        let tmp = node.options.title.split(':')
        this.oldTableName = tmp[0]
        this.modelMd = {
          tableName: tmp[0],
          tableComment: tmp[1]
        }
        // this.formMd.setFieldsValue({
        //   tableName: tmp[0],
        //   tableComment: tmp[1]
        // })
      })
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
        this.setStyle()
        this.setStageFieldStyle()
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
            if (this.referenceField[lk.sourceNode].indexOf(lk.source) < 0) {
              this.delEdgeById(id)
              this.$message.warning('The source field is not a reference field and cannot be wired！')
              return
            }
            if (this.pkField[lk.targetNode].length != 1) {
              this.delEdgeById(id)
              this.$message.warning('The target table is a multi-field primary key and cannot be wired！')
            } else {
              this.delEdgeById(id)
              this.$nextTick(() => {
                let edge = {
                  id: lk.sourceNode + '.' + lk.source + '_to_' + lk.targetNode + '.' + this.pkField[lk.targetNode][0],
                  source: lk.source,
                  sourceNode: lk.sourceNode,
                  target: this.pkField[lk.targetNode][0],
                  targetNode: lk.targetNode
                }
                this.modelData.edges.push(edge)
                let tmp = lk.sourceNode.split('.')
                let tmp1 = lk.targetNode.split('.')
                if (this.referenceMap[lk.sourceNode]) {
                  if (this.referenceMap[lk.sourceNode][lk.source]) {
                    let have = false
                    this.referenceMap[lk.sourceNode][lk.source].forEach(item => {
                      if (item.tableName == tmp[2] && item.refTableName == tmp1[2] && item.fieldName == lk.source && item.refFieldName == this.pkField[lk.targetNode][0]) {
                        have = true
                      }
                    })
                    if (!have) {
                      this.referenceMap[lk.sourceNode][lk.source].push({
                        dsCode: tmp[0], dbName: tmp[1],
                        tableName: tmp[2], fieldName: lk.source,
                        refTableName: tmp1[2], refFieldName: this.pkField[lk.targetNode][0]
                      })
                    }
                  } else {
                    this.referenceMap[lk.sourceNode][lk.source] = [
                      {
                        dsCode: tmp[0], dbName: tmp[1],
                        tableName: tmp[2], fieldName: lk.source,
                        refTableName: tmp1[2], refFieldName: this.pkField[lk.targetNode][0]
                      }
                    ]
                  }
                } else {
                  this.referenceMap[lk.sourceNode] = {
                    [lk.source]: [
                      {
                        dsCode: tmp[0], dbName: tmp[1],
                        tableName: tmp[2], fieldName: lk.source,
                        refTableName: tmp1[2], refFieldName: this.pkField[lk.targetNode][0]
                      }
                    ]
                  }
                }
                postAction('/md/tables/saveEdge', edge).then(res => {
                  if (res.success == true) {
                    let result = res.result
                    if (result.refNode) {
                      this.updateTable(result.refNode)
                    }
                    this.$nextTick(() => {
                      this.$nextTick(() => {
                        this.$message.success('incidence relation Save success!')
                      })
                    })
                  } else {
                    this.$message.error('incidence relation Save Failure！')
                    console.error(res)
                  }
                })
              })
            }
          }
        })
      }
    },
    setStyle() {
      let items = document.getElementsByClassName('field-item')
      for (let i = 0; i < items.length; i++) {
        let ele = items[i]
        let attr = ele.getAttribute('dataType')
        if (attr == 'fieldTypeFull') {
          ele.setAttribute('title', ele.innerHTML)
          ele.style.paddingLeft = '5px'
          ele.style.paddingRight = '5px'
          ele.style.width = '110px'
          ele.style.overflow = 'hidden'
          ele.style.textOverflow = 'ellipsis'
          ele.style.whiteSpace = 'nowrap'
        } else if (attr == 'refKeys') {
          ele.setAttribute('title', ele.innerHTML)
          // ele.style.paddingLeft = '5px'
          // ele.style.paddingRight = '5px'
          ele.style.width = '80px'
          ele.style.overflow = 'hidden'
          ele.style.textOverflow = 'ellipsis'
          ele.style.whiteSpace = 'nowrap'
          // ele.style.color = 'green';
          // ele.style.textDecoration = 'underline'
        } else {
          ele.setAttribute('title', ele.innerHTML)
          ele.style.paddingLeft = '5px'
          ele.style.paddingRight = '5px'
          ele.style.width = '100px'
          ele.style.overflow = 'hidden'
          ele.style.textOverflow = 'ellipsis'
          ele.style.whiteSpace = 'nowrap'
        }

      }
    },
    setStageFieldStyle() {
      let nodes = document.getElementsByClassName('node table-node')
      for (let i = 0; i < nodes.length; i++) {
        let node = nodes[i]

        let titleDom = node.getElementsByClassName('title-text')[0]
        titleDom.style.color = '#676a6c'
        titleDom.style.textAlign = 'center'

        if (this.tableState[titleDom.innerHTML] == '0') {
          node.style.backgroundColor = '#F0F8FF'
        } else {
          node.style.backgroundColor = '#FFFFCC'
        }
        let items = node.getElementsByClassName('field-item')
        for (let i = 0; i < items.length; i = i + 4) {
          let ele = items[i]
          let html = ele.innerHTML

          if (this.pkField[titleDom.innerHTML].indexOf(html) > -1) {
            ele.style.color = '#14d007'
            items[i + 1].style.color = '#14d007'
            if (items[i + 2]) {
              items[i + 2].style.color = '#14d007'
            }
            if (items[i + 3]) {
              items[i + 3].style.color = '#14d007'
            }
          }

          if (this.referenceField[titleDom.innerHTML].indexOf(html) > -1) {
            items[i + 1].innerHTML = 'reference'
            if (this.referenceMap[titleDom.innerHTML].hasOwnProperty(html)) {
              ele.style.color = '#676a6c'
              items[i + 1].style.color = '#676a6c'
              if (items[i + 2]) {
                items[i + 2].style.color = '#676a6c'
              }
              if (items[i + 3]) {
                items[i + 3].style.color = '#676a6c'
                items[i + 3].innerHTML = ''

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

                let isZs = false
                this.referenceMap[titleDom.innerHTML][html].forEach(item => {
                  if (item.tableName == item.refTableName) {
                    isZs = true
                  }
                })
                if (isZs) {
                  img.title = 'Cancel the associated'
                  img.src = '../icon/self_blue.png'
                } else {
                  img.title = 'Associated with its'
                  img.src = '../icon/self.png'
                }
                img.onclick = () => {
                  window.setReferenceTable(html, this.canvasNodeKey[titleDom.innerHTML])
                }
                items[i + 3].appendChild(img)

                let add = document.createElement('img')
                add.title = 'New'
                add.style.width = '18px'
                add.style.height = '18px'
                add.style.cursor = 'pointer'
                add.style.marginLeft = '5px'
                add.style.marginBottom = '3px'
                add.src = '../icon/add.png'
                add.onmouseover = () => {
                  add.style.width = '21px'
                  add.style.height = '21px'
                }
                add.onmouseout = () => {
                  add.style.width = '18px'
                  add.style.height = '18px'
                }
                add.onclick = () => {
                  window.addReferenceTable(html, this.canvasNodeKey[titleDom.innerHTML])
                }
                items[i + 3].appendChild(add)

                if (isZs && this.referenceMap[titleDom.innerHTML][html].length == 1) {

                } else {
                  // <img src="../img/close.png" style="width: 20px; height: 20px;margin-bottom:8px">
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
                  if (this.referenceFieldOpenState[this.canvasNodeKey[titleDom.innerHTML] + '.' + html]) {
                    img.title = 'Close'
                    img.src = '../icon/open_blue.png'
                    img.onclick = () => {
                      window.referenceFieldClose(html, titleDom.innerHTML)
                    }
                  } else {
                    img.title = 'Open'
                    img.src = '../icon/close.png'
                    img.onclick = () => {
                      window.referenceFieldOpen(html, titleDom.innerHTML)
                    }
                  }
                  items[i + 3].appendChild(img)
                }
              }
            } else {
              ele.style.color = '#676a6c'
              items[i + 1].style.color = '#676a6c'
              if (items[i + 2]) {
                items[i + 2].style.color = '#676a6c'
              }
              if (items[i + 3]) {
                items[i + 3].style.color = '#676a6c'
                items[i + 3].innerHTML = ''

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
                img.title = 'Associated with its'
                img.src = '../icon/self.png'
                img.onclick = () => {
                  window.setReferenceTable(html, this.canvasNodeKey[titleDom.innerHTML])
                }
                items[i + 3].appendChild(img)

                let add = document.createElement('img')
                add.title = 'New'
                add.style.width = '18px'
                add.style.height = '18px'
                add.style.cursor = 'pointer'
                add.style.marginLeft = '5px'
                add.style.marginBottom = '3px'
                add.src = '../icon/add.png'
                add.onmouseover = () => {
                  add.style.width = '21px'
                  add.style.height = '21px'
                }
                add.onmouseout = () => {
                  add.style.width = '18px'
                  add.style.height = '18px'
                }
                add.onclick = () => {
                  window.addReferenceTable(html, this.canvasNodeKey[titleDom.innerHTML])
                }
                items[i + 3].appendChild(add)
              }
            }
          }
        }
      }
    },

    closeChildenNode(id) {
      for (let i = 0; i < this.modelData.edges.length;) {
        let e = this.modelData.edges[i]
        if (e.sourceNode == id) {
          this.modelData.edges.splice(i, 1)
        } else {
          i++
        }
      }
      if (!this.referenceMap[id]) {
        return
      }
      for (let field in this.referenceMap[id]) {
        let f = this.referenceMap[id][field]
        if (f) {
          f.forEach((r, index) => {
            let id = r.dsCode + '.' + r.dbName + '.' + r.refTableName
            if (this.referenceMap[id]) {
              this.delNodeById(id)
              this.$nextTick(() => {
                this.setStyle()
                this.setStageFieldStyle()
              })
              this.closeChildenNode(id)
            } else {
              this.delNodeById(id)
              this.$nextTick(() => {
                this.setStyle()
                this.setStageFieldStyle()
              })
            }
          })
        }
      }
    },
    referenceFieldClose(field, title) {
      this.referenceFieldOpenState[this.canvasNodeKey[title] + '.' + field] = false
      for (let i = 0; i < this.modelData.edges.length;) {
        let e = this.modelData.edges[i]
        if (e.sourceNode == this.canvasNodeKey[title] && e.source == field) {
          this.modelData.edges.splice(i, 1)
        } else {
          i++
        }
      }
      for (let i = 0; i < this.referenceMap[title][field].length; i++) {
        let r = this.referenceMap[title][field][i]
        if (r.tableName == r.refTableName) {
          continue
        }
        let id = r.dsCode + '.' + r.dbName + '.' + r.refTableName

        let isUse = false
        this.modelData.edges.forEach(item => {
          if (item.targetNode == id) {
            isUse = true
          }
        })
        if (!isUse) {
          this.closeChildenNode(id)
          this.delNodeById(id)
        }
        this.$nextTick(() => {
          this.setStyle()
          this.setStageFieldStyle()
        })
      }
    },
    referenceFieldOpen(field, title) {
      this.referenceFieldClose(field, title)
      this.referenceFieldOpenState[this.canvasNodeKey[title] + '.' + field] = true

      let nodes = document.getElementsByClassName('node table-node')
      let maxTop = 0
      let maxLeft = 0
      if (nodes.length > 0) {
        for (let i = 0; i < nodes.length; i++) {
          let nodeTitle = nodes[i].getElementsByClassName('title-text')[0].innerHTML

          let fields = nodes[i].getElementsByClassName('field')
          let attrs = nodes[i].getAttribute('style').split(';')
          let top = attrs[0].split(':')[1].replace('px', '')
          let left = attrs[1].split(':')[1].replace('px', '')

          if (nodeTitle == title) {
            maxLeft = parseInt(left) + 450
          }
          if (parseInt(left) >= maxLeft && parseInt(top) > maxTop) {
            if (maxTop == 0) {
              maxTop = 50
            } else {
              maxTop = parseInt(top) + ((fields.length + 1) * 30)
            }
          }
        }
      }

      for (let i = 0; i < this.referenceMap[title][field].length; i++) {
        let r = this.referenceMap[title][field][i]
        if (r.tableName == r.refTableName) {
          continue
        }
        postAction('/editor/load-canvas', {
          key: r.dsCode + '.' + r.dbName + '.' + r.refTableName,
          title: '',
          from: ''
        }).then(res => {
          if (res.success == true) {
            let result = res.result
            let nodeData = result.nodeData
            let nodeTableName = r.refTableName

            const target = this.modelData.nodes.filter(item => item.id == nodeData.id)
            if (target && target.length > 0) {
              // nodeData.title = nodeData.title+"_"+target.length
              // nodeData.id = nodeData.id+"_"+target.length
              // nodeTableName = nodeTableName +"_"+target.length
              //
              // this.stageField[nodeData.title] = []
              // this.editField[nodeData.title] = []
              // this.referenceField[nodeData.title] = []
              // this.referenceField[nodeData.id] = []
              // this.pkField[nodeData.title] = result.pkField
              // this.pkField[nodeData.id] = result.pkField
              // this.referenceMap[nodeData.title] = []
              // this.referenceMap[nodeData.id] = []
              // this.canvasNodeKey[nodeData.title] = result.key+"_"+target.length
              // this.tableState[nodeData.title] = result.tableState
              //
              // nodeData.fields.forEach(item=>{
              //   if(item.fieldType == 'reference'){
              //     item.fieldTypeFull = 'reference'
              //   }
              // })

            } else {
              this.stageField[nodeData.title] = result.stageField
              this.editField[nodeData.title] = result.editField
              this.referenceField[nodeData.title] = result.referenceField
              this.referenceField[nodeData.id] = result.referenceField
              this.pkField[nodeData.title] = result.pkField
              this.pkField[nodeData.id] = result.pkField
              this.referenceMap[nodeData.title] = result.referenceMap
              this.referenceMap[nodeData.id] = result.referenceMap
              this.canvasNodeKey[nodeData.title] = result.key
              this.tableState[nodeData.title] = result.tableState
            }

            nodeData.top = parseInt(maxTop) + 50
            nodeData.left = parseInt(maxLeft)

            this.modelData.nodes.push(nodeData)
            maxTop = parseInt(maxTop) + ((nodeData.fields.length + 1) * 30)
            this.$nextTick(() => {
              let edge = {
                'id': r.dsCode + '.' + r.dbName + '.' + r.tableName + '.' + r.fieldName + '_to_' + r.dsCode + '.' + r.dbName + '.' + nodeTableName + '.' + r.refFieldName,
                'sourceNode': r.dsCode + '.' + r.dbName + '.' + r.tableName,
                'targetNode': r.dsCode + '.' + r.dbName + '.' + nodeTableName,
                'source': r.fieldName,
                'target': r.refFieldName
              }
              let filterElement = this.modelData.edges.filter(item => item.id == edge.id)[0]
              if (!filterElement) {
                this.modelData.edges.push(edge)
              }

              this.setStyle()
              this.setStageFieldStyle()
            })
          } else {
            if (res.message) {
              this.$message.error(res.message)
            } else {
              this.$message.error('Table For failure!')
            }
            console.error(res)
          }
        })
      }
    },

    ondragend(info) {
      let treeNode = info.node._props.dataRef
      if (treeNode['is-leaf']) {
        return
      }
      let w = document.body.clientWidth
      let f = w * 0.785
      if (info.event.x < f) {
        this.loadCanvas(treeNode.key, treeNode.title, treeNode.from)
      }
    },
    loadCanvas(key, title, from) {
      postAction('/editor/load-canvas', {
        key: key,
        title: title,
        from: from
      }).then(res => {
        if (res.success == true) {
          let result = res.result
          let nodeData = result.nodeData
          let leftLine = result.leftLine
          let rightLine = result.rightLine

          const target = this.modelData.nodes.filter(item => item.id == nodeData.id)
          if (target.length > 0) {
            // nodeData.title = nodeData.title+"_"+target.length
            // nodeData.id = nodeData.id+"_"+target.length
            // nodeData.key = nodeData.key+"_"+target.length
            //
            // this.stageField[nodeData.title] = []
            // this.editField[nodeData.title] = []
            // this.referenceField[nodeData.title] = []
            // this.referenceField[nodeData.id] = []
            // this.pkField[nodeData.title] = result.pkField
            // this.pkField[nodeData.id] = result.pkField
            // this.referenceMap[nodeData.title] = []
            // this.referenceMap[nodeData.id] = []
            // this.canvasNodeKey[nodeData.title] = result.key
            // this.tableState[nodeData.title] = result.tableState
            //
            // nodeData.fields.forEach(item=>{
            //   if(item.fieldType == 'reference'){
            //     item.fieldTypeFull = 'reference'
            //   }
            // })
          } else {
            this.stageField[nodeData.title] = result.stageField
            this.editField[nodeData.title] = result.editField
            this.referenceField[nodeData.title] = result.referenceField
            this.referenceField[nodeData.id] = result.referenceField
            this.pkField[nodeData.title] = result.pkField
            this.pkField[nodeData.id] = result.pkField
            this.referenceMap[nodeData.title] = result.referenceMap
            this.referenceMap[nodeData.id] = result.referenceMap
            this.canvasNodeKey[nodeData.title] = result.key
            this.tableState[nodeData.title] = result.tableState
          }

          let nodes = document.getElementsByClassName('node table-node')
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
            }
            nodeData.top = parseInt(maxTop)
            nodeData.left = parseInt(maxLeft)
          }
          this.modelData.nodes.push(nodeData)

          this.$nextTick(() => {
            leftLine.forEach(line => {
              let sn = line.dsCode + '.' + line.dbName + '.' + line.tableName
              let tn = line.dsCode + '.' + line.dbName + '.' + line.refTableName
              let exist = false
              this.modelData.edges.forEach(e => {
                if (e.sourceNode == sn && e.targetNode == tn && e.source == line.fieldName && e.target == line.refFieldName) {
                  exist = true
                }
              })
              if (!exist && sn != tn) {
                this.modelData.edges.push({
                  id: sn + '.' + line.fieldName + '_to_' + tn + '.' + line.refFieldName,
                  source: line.fieldName,
                  sourceNode: sn,
                  target: line.refFieldName,
                  targetNode: tn
                })
              }
            })

            // rightLine.forEach(line=>{
            //   let sn = line.dsCode+"."+line.dbName+"."+line.tableName
            //   let tn = line.dsCode+"."+line.dbName+"."+line.refTableName
            //   let exist = false
            //   this.modelData.edges.forEach(e=>{
            //     if (e.sourceNode == sn && e.targetNode == tn && e.source == line.fieldName && e.target == line.refFieldName) {
            //       exist = true
            //     }
            //   })
            //   if(!exist && sn!=tn){
            //     this.modelData.edges.push({
            //       id: sn + "." + line.fieldName + "_to_" + tn + "." + line.refFieldName,
            //       source: line.fieldName,
            //       sourceNode: sn,
            //       target: line.refFieldName,
            //       targetNode: tn
            //     })
            //   }
            // })
            this.setStyle()
            this.setStageFieldStyle()
          })
        } else {
          if (res.message) {
            this.$message.error(res.message)
          } else {
            this.$message.error('Table For failure!')
          }
          console.error(res)
        }
      })
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
    loadDicts(dictCodes) {
      if (dictCodes) {
        let codes = dictCodes.split(',')
        codes.forEach(c => {
          if (c) {
            //优先从缓存中读取字典配置
            if (getDictItemsFromCache(c)) {
              this.dictsMap[c] = getDictItemsFromCache(c)
            } else {
              //根据字典Code, 初始化字典数组
              ajaxGetDictItems(c, null).then((res) => {
                if (res.success) {
                  this.dictsMap[c] = res.result
                }
              })
            }
          }
        })
      }

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

    onSearchTable(value) {
      this.searchText = value
      this.loadedKeys = []
      this.loadTreeData()
    },
    onChange(e) {
      this.form.validateFields((err, values) => {
        if (!err) {
          this.searchText = e.target.value
          this.loadedKeys = []
          this.loadTreeData()
        }
      })
    },
    handleDbChange(selectedItem) {
      if (selectedItem) {
        this.dbCode = selectedItem
        this.loadedKeys = []
        this.loadTreeData()
        // this.loadFkTableNames(this.dsCode, this.dbCode)
      } else {
        this.dbCode = undefined
      }
    },

    loadTreeData() {
      postAction('/editor/load-tables', {
        dsCode: this.dsCode,
        dbCode: this.dbCode,
        searchText: this.searchText
      }).then(res => {
        if (res.success == true) {
          this.treeData = res.result
        } else {
          this.$message.error('Table For failure!')
          console.error(res)
        }
      })
    },
    loadFields(treeNode) {
      return new Promise(resolve => {
        let tableName = treeNode.dataRef.tableName
        postAction('/editor/load-fields', {
          dsCode: this.dsCode,
          dbCode: this.dbCode,
          tableName: tableName
        }).then(res => {
          if (res.success == true) {
            let fieldData = res.result
            treeNode.dataRef.children = fieldData
            this.treeData = [...this.treeData]
            resolve()
          } else {
            this.$message.error('Table For failure!')
            console.error(res)
          }
        })
      })
    },

    redo() {
      this.modelData.edges = []
      this.modelData.nodes = []
      this.currentNode = {}
      this.currentEdge = {}
      this.tableState = {}
      this.stageField = {}
      this.editField = {}
      this.referenceField = {}
      this.pkField = {}
      this.referenceMap = {}
      this.canvasNodeKey = {}
      this.$nextTick(() => {
        this.$message.success('The canvas is empty and you can continue！')
      })
    },
    remove() {
      if (this.currentNode.options) {
        let tmp = this.currentNode.options.id.split('.')
        postAction('/md/tables/remove', {
          dsCode: tmp[0], dbName: tmp[1], tableName: tmp[2]
        }).then(res => {
          if (res.success == true) {
            this.delNodeById(this.currentNode.options.id)
            this.loadTreeData()
            this.tableData = []
            this.tableColumns = []
            this.$message.success('Category  Delete the success！')
          } else {
            this.$message.error(res.message)
            console.error(res)
          }
        })
      } else {
        this.$message.warning('Select the Category you want to reset！')
      }
    },
    sync() {
      if (this.modelData.nodes.length == 0) {
        this.$message.warning('There is no data in the canvas!')
        return
      }
      if (this.currentNode.options) {
        let id = this.currentNode.options.id
        postAction('/md/tables/sync', {
          ids: id
        }).then(res => {
          if (res.success == true) {
            if (res.result.ndoeList) {
              res.result.ndoeList.forEach(ln => {
                let exist = false
                this.modelData.nodes.forEach(n => {
                  if (ln.id = n.id) {
                    exist = true
                  }
                })
                if (exist) {
                  this.updateTable(ln)
                }
              })
              this.$nextTick(() => {
                this.loadTreeData()
                this.$message.success('Category Synchronous success ！')
              })
            }
          } else {
            this.$message.error(res.message)
            console.error(res)
          }
        })
      } else {
        const that = this
        this.$confirm({
          title: 'Saving all tables on the mind map?',
          content: 'After a node is selected, only the structures related to the node are synchronized',
          onOk() {
            let ids = ''
            that.modelData.nodes.forEach(n => {
              ids += n.id + ','
            })
            let id = ids.substr(0, ids.length - 1)
            postAction('/md/tables/sync', {
              ids: id
            }).then(res => {
              if (res.success == true) {
                if (res.result.ndoeList) {
                  res.result.ndoeList.forEach(ln => {
                    let exist = false
                    that.modelData.nodes.forEach(n => {
                      if (ln.id = n.id) {
                        exist = true
                      }
                    })
                    if (exist) {
                      that.updateTable(ln)
                    }
                  })
                  that.$nextTick(() => {
                    that.loadTreeData()
                    that.$message.success('Category Synchronous success ！')
                  })
                }
              } else {
                that.$message.error(res.message)
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
    },

    addReferenceTable(field, nodeId) {
      this.isReferenceAdd = true
      this.primaryNodeId = nodeId
      this.primaryField = field

      let tmp = this.primaryNodeId.split('.')
      this.dataMd = []
      this.cacheDataMd = []
      this.editingKeyMd = ''
      this.$nextTick(() => {
        this.modelMd = { tableName: '', tableComment: '' }
      })
      this.addMd = true
      this.visible = true
      this.dsCode = tmp[0]
      this.dbCode = tmp[1]

      this.dataMd.push({
        fieldName: 'dpkid', fieldType: 'int', isPk: '1', fieldComment: 'dpkid', dataState: '1'
      })
      this.cacheDataMd.push({
        fieldName: 'dpkid', fieldType: 'int', isPk: '1', fieldComment: 'dpkid', dataState: '1'
      })
    },
    setReferenceTable(field, nodeId) {
      postAction('/md/tables/setReferenceTable', {
        nodeId: nodeId,
        field: field,
        pkField: this.pkField[nodeId][0]
      }).then(res => {
        if (res.success == true) {
          let result = res.result
          this.updateTable(result.node)
          this.$message.success('Category edit success！')
        } else {
          this.$message.error('Category edit failure！')
          console.error(res)
        }
      })
    },
    addTable() {
      this.form.validateFields((err, values) => {
        if (!err) {
          this.dataMd = []
          this.cacheDataMd = []
          this.dataMd.push({
            fieldName: 'dpkid', fieldType: 'int', isPk: '1', fieldComment: 'dpkid', dataState: '1'
          })
          this.cacheDataMd.push({
            fieldName: 'dpkid', fieldType: 'int', isPk: '1', fieldComment: 'dpkid', dataState: '1'
          })
          this.editingKeyMd = ''
          this.isReferenceAdd = false
          this.primaryNodeId = ''
          this.primaryField = ''

          this.$nextTick(() => {
            this.oldTableName = ''
            this.modelMd = { tableName: '', tableComment: '' }
          })
          this.addMd = true
          this.visible = true
        }
      })
    },
    saveTable(e) {
      this.$refs.form.validate((ok, err) => {
        if (ok) {
          if (this.modelMd.tableName != this.oldTableName) {
            let params = {
              tableName: 'pmkb_metadata_tables',
              fieldName: 'table_name',
              fieldVal: this.modelMd.tableName,
              dataId: ''
            }
            api.duplicateCheck(params).then(res => {
              if (res.success) {
                this.saveTableHandler()
              } else {
                this.$message.error('The table already exists！')
              }
            }).catch(err => {
              callback(err.message || err)
            })
          } else {
            this.saveTableHandler()
          }
        }
      })

    },
    saveTableHandler() {
      let formMdData = this.modelMd
      postAction('/md/tables/save', {
        table: {
          dsCode: this.dsCode,
          dbName: this.dbCode,
          tableName: formMdData.tableName,
          tableComment: formMdData.tableComment
        },
        fields: this.dataMd,
        isAdd: this.addMd,
        isReferenceAdd: this.isReferenceAdd,
        primaryNodeId: this.primaryNodeId,
        primaryField: this.primaryField,
        hasExt: true
      }).then(res => {
        if (res.success == true) {
          let result = res.result

          if (result.refNode) {
            this.updateTable(result.refNode)
            this.$nextTick(() => {
              this.referenceFieldClose(this.primaryField, result.refNode.nodeData.title)
              this.$nextTick(() => {
                setTimeout(() => {
                  this.referenceFieldOpen(this.primaryField, result.refNode.nodeData.title)
                }, 100)
              })

            })
          } else {
            this.updateTable(result.node)
          }
          this.$nextTick(() => {
            // this.modelData.edges = this.modelData.edges.filter(item => {
            //   return item.id.indexOf(result.edgeIdStart)==-1 || result.edgeIds.indexOf(item.id)>0
            // })

            // if(result.refEdge){
            //   let filterElement = this.modelData.edges.filter(item=>item.id == result.refEdge.id)[0]
            //   if(!filterElement){
            //     this.modelData.edges.push(result.refEdge)
            //   }
            // }

            this.$nextTick(() => {
              this.loadTreeData()
              this.visible = false
              this.$message.success('Category save success！')
            })
          })
        } else {
          this.$message.error('Category Save Failure!！')
          console.error(res)
        }
      })
    },
    updateTable(result) {
      let nodeData = result.nodeData
      this.stageField[nodeData.title] = result.stageField
      this.editField[nodeData.title] = result.editField
      this.referenceField[nodeData.title] = result.referenceField
      this.referenceField[nodeData.id] = result.referenceField
      this.pkField[nodeData.title] = result.pkField
      this.pkField[nodeData.id] = result.pkField
      this.referenceMap[nodeData.title] = result.referenceMap
      this.referenceMap[nodeData.id] = result.referenceMap
      this.canvasNodeKey[nodeData.title] = result.key
      this.tableState[nodeData.title] = result.tableState
      let target, targetIndex
      for (let i = 0; i < this.modelData.nodes.length; i++) {
        let item = this.modelData.nodes[i]
        if (nodeData.id === item.id) {
          target = item
          targetIndex = i
        }
      }
      let targetEdges = []
      if (!target) {
        let nodes = document.getElementsByClassName('node table-node')
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
          }
          nodeData.top = parseInt(maxTop)
          nodeData.left = parseInt(maxLeft)
        }
        this.modelData.nodes.push(nodeData)
      } else {
        let nodes = document.getElementsByClassName('node table-node')
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
          this.setStyle()
          this.setStageFieldStyle()
        }, 100)
      })
    },
    handleCancel(e) {
      this.visible = false
    },

    handleRowAdd() {
      if (this.editingKeyMd) {
        this.$message.warning('Please save the current modified line first！')
      } else {
        this.fieldAddNo = this.fieldAddNo + 1
        this.dataMd.unshift({ id: 'field-add-' + this.fieldAddNo })
        this.cacheDataMd.unshift({ id: 'field-add-' + this.fieldAddNo })
        this.editRowMd('field-add-' + this.fieldAddNo)
      }
    },
    loadFkTableNames(dsCode, dbName, tableName) {
      postAction('/editor/load-tables', {
        dsCode: dsCode,
        dbCode: dbName,
        searchText: ''
      }).then(res => {
        if (res.success == true) {
          this.fkTableNames = res.result
          // this.fkTableNames = res.result.filter(item => tableName != item.tableName);
        } else {
          this.$message.error('Table For failure!')
          console.error(res)
        }
      })
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
        /*if (column === 'fieldComment') {
          this.$message.warning('字段注释不能为空！')
          return
        }*/
      }
      if (value <= 0 && column === 'fieldLength' && row.fieldType === 'varchar') {
        this.$message.warning('The value is a string that cannot contain 0 characters！')
        return
      }
      const newData = [...this.dataMd]
      const target = newData.filter(item => id === item.id)[0]
      if (target) {
        if (column == 'isPk') {
          target[column] = value ? '1' : '0'
        } else {
          target[column] = value
          if (target.id.indexOf('field-add-') > -1) {
            target['dataState'] = '1'
          }
          if (target['dataState'] == '0') {
            target['dataState'] = '2'
          }
        }
        this.dataMd = newData
      }
    },
    editRowMd(id) {
      const newData = [...this.dataMd]
      const target = newData.filter(item => id === item.id)[0]
      this.editingKeyMd = id
      if (target) {
        target.editable = true
        this.dataMd = newData
      }
    },
    delRowMd(id) {
      this.dataMd.forEach((item, index) => {
        if (id === item.id) {
          this.dataMd.splice(index, 1)
          this.cacheDataMd.splice(index, 1)
        }
      })
    },
    saveRowMd(id) {
      const newData = [...this.dataMd]
      const newCacheData = [...this.cacheDataMd]
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
        this.dataMd = newData
        Object.assign(targetCache, target)
        this.cacheDataMd = newCacheData
      }
      this.editingKeyMd = ''
    },
    cancelRowMd(id) {
      const newData = [...this.dataMd]
      const target = newData.filter(item => id === item.id)[0]
      this.editingKeyMd = ''
      if (target) {
        let currentAddId = 'field-add-' + this.fieldAddNo
        if (target.id == currentAddId) {
          delete target.editable
          this.dataMd = newData
          this.dataMd.forEach((item, index) => {
            if (currentAddId === item.id) {
              this.dataMd.splice(index, 1)
              this.cacheDataMd.splice(index, 1)
            }
          })

        } else {
          Object.assign(target, this.cacheDataMd.filter(item => id === item.id)[0])
          delete target.editable
          this.dataMd = newData
        }
      }
    }
  }
}
</script>

<style scoped>
.left_div {
  width: 75%;
  height: 760px;
  float: left;
  background-color: white;
  padding: 5px;
}

.left_div .model_div {
  width: 100%;
  height: 760px;
}

.left_div .data_div {
  width: 100%;
  height: 300px;
}

.right_div {
  width: 24%;
  height: 760px;
  float: right;
  padding: 10px;
  margin-left: 5px;
  background-color: white;
}

.tree_div {
  margin-top: 20px;
  width: 100%;
  height: 480px;
  overflow: auto;
  background-color: white;
  border: grey 1px dashed;
}

.out {
  background-color: gray;
}

.over {
  background-color: red;
}

.down {
  background-color: yellow;
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

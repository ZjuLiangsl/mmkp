<template>
  <j-modal
    title="select dept"
    :width="modalWidth"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleSubmit"
    @cancel="handleCancel"
    @update:fullscreen="isFullscreen"
    wrapClassName="j-depart-select-modal"
    switchFullscreen
    cancelText="Close">
    <a-spin tip="Loading..." :spinning="false">
      <a-input-search style="margin-bottom: 1px" placeholder="Please enter the department name and press Enter to search" @search="onSearch" />
      <a-tree
        checkable
        :class="treeScreenClass"
        :treeData="treeData"
        :checkStrictly="checkStrictly"
        @check="onCheck"
        @select="onSelect"
        @expand="onExpand"
        :autoExpandParent="autoExpandParent"
        :expandedKeys="expandedKeys"
        :checkedKeys="checkedKeys">

        <template slot="title" slot-scope="{title}">
          <span v-if="title.indexOf(searchValue) > -1">
            {{title.substr(0, title.indexOf(searchValue))}}
            <span style="color: #f50">{{searchValue}}</span>
            {{title.substr(title.indexOf(searchValue) + searchValue.length)}}
          </span>
          <span v-else>{{title}}</span>
        </template>
      </a-tree>
    </a-spin>
    <template slot="footer" v-if="treeOpera && multi">
      <div class="drawer-bootom-button">
        <a-dropdown style="float: left" :trigger="['click']" placement="topCenter">
          <a-menu slot="overlay">
            <a-menu-item key="1" @click="switchCheckStrictly(1)">Father and son relation</a-menu-item>
            <a-menu-item key="2" @click="switchCheckStrictly(2)">Cancel the associated</a-menu-item>
          </a-menu>
          <a-button>
            Tree operations <a-icon type="up" />
          </a-button>
        </a-dropdown>
        <a-button @click="handleCancel" type="primary" style="margin-right: 0.8rem">colse</a-button>
        <a-button @click="handleSubmit" type="primary" >ok</a-button>
      </div>
    </template>
  </j-modal>
</template>

<script>
  import { queryDepartTreeList } from '@/api/api'
  export default {
    name: 'JSelectDepartModal',
    props:['modalWidth','multi','rootOpened','departId', 'store', 'text','treeOpera'],
    data(){
      return {
        visible:false,
        confirmLoading:false,
        treeData:[],
        autoExpandParent:true,
        expandedKeys:[],
        dataList:[],
        checkedKeys:[],
        checkedRows:[],
        searchValue:"",
        checkStrictly: true,
        fullscreen:false
      }
    },
    created(){
      this.loadDepart();
    },
    watch:{
      departId(){
        this.initDepartComponent()
      },
      visible: {
        handler() {
          this.initDepartComponent(true)
        }
      }
    },
    computed:{
      treeScreenClass() {
        return {
          'my-dept-select-tree': true,
          'fullscreen': this.fullscreen,
        }
      },
    },
    methods:{
      show(){
        this.visible=true
        this.checkedRows=[]
        this.checkedKeys=[]
      },
      loadDepart(){
        queryDepartTreeList().then(res=>{
          if(res.success){
            let arr = [...res.result]
            this.reWriterWithSlot(arr)
            this.treeData = arr
            this.initDepartComponent()
            if(this.rootOpened){
              this.initExpandedKeys(res.result)
            }
          }
        })
      },
      initDepartComponent(flag){
        let arr = []
        let fieldName = flag==true?'key':this.text
        if(this.departId){
          let arr2 = this.departId.split(',')
          for(let item of this.dataList){
            if(arr2.indexOf(item[this.store])>=0){
              arr.push(item[fieldName])
            }
          }
        }
        if(flag==true){
          this.checkedKeys = [...arr]
        }else{
          this.$emit("initComp", arr.join(','))
        }
      },
      reWriterWithSlot(arr){
        for(let item of arr){
          if(item.children && item.children.length>0){
            this.reWriterWithSlot(item.children)
            let temp = Object.assign({},item)
            temp.children = {}
            this.dataList.push(temp)
          }else{
            this.dataList.push(item)
            item.scopedSlots={ title: 'title' }
          }
        }
      },
      initExpandedKeys(arr){
        if(arr && arr.length>0){
          let keys = []
          for(let item of arr){
            if(item.children && item.children.length>0){
              keys.push(item.id)
            }
          }
          this.expandedKeys=[...keys]
          //this.allTreeKeys = [...keys]
        }else{
          this.expandedKeys=[]
          //this.allTreeKeys = []
        }
      },
      onCheck (checkedKeys,info) {
        if(!this.multi){
          let arr = checkedKeys.checked.filter(item => this.checkedKeys.indexOf(item) < 0)
          this.checkedKeys = [...arr]
          this.checkedRows = (this.checkedKeys.length === 0) ? [] : [info.node.dataRef]
        }else{
          if(this.checkStrictly){
            this.checkedKeys = checkedKeys.checked
          }else{
            this.checkedKeys = checkedKeys
          }
          this.checkedRows = this.getCheckedRows(this.checkedKeys)
        }
      },
      onSelect(selectedKeys,info) {
        if(this.checkStrictly){
          let keys = []
          keys.push(selectedKeys[0])
          if(!this.checkedKeys || this.checkedKeys.length===0 || !this.multi){
            this.checkedKeys = [...keys]
            this.checkedRows=[info.node.dataRef]
          }else{
            let currKey = info.node.dataRef.key
            if(this.checkedKeys.indexOf(currKey)>=0){
              this.checkedKeys = this.checkedKeys.filter(item=> item !==currKey)
            }else{
              this.checkedKeys.push(...keys)
            }
          }
          this.checkedRows = this.getCheckedRows(this.checkedKeys)
        }
      },
      onExpand (expandedKeys) {
        this.expandedKeys = expandedKeys
        this.autoExpandParent = false
      },
      handleSubmit(){
        if(!this.checkedKeys || this.checkedKeys.length==0){
          this.$emit("ok",'')
        }else{
          let checkRow = this.getCheckedRows(this.checkedKeys)
          let keyStr = this.checkedKeys.join(",")
          this.$emit("ok", checkRow, keyStr)
        }
        this.handleClear()
      },
      handleCancel(){
        this.handleClear()
      },
      handleClear(){
        this.visible=false
        this.checkedKeys=[]
      },
      getParentKey(currKey,treeData){
        let parentKey
        for (let i = 0; i < treeData.length; i++) {
          const node = treeData[i]
          if (node.children) {
            if (node.children.some(item => item.key === currKey)) {
              parentKey = node.key
            } else if (this.getParentKey(currKey, node.children)) {
              parentKey = this.getParentKey(currKey, node.children)
            }
          }
        }
        return parentKey
      },
      onSearch(value){
        const expandedKeys = this.dataList.map((item) => {
          if (item.title.indexOf(value) > -1) {
            return this.getParentKey(item.key,this.treeData)
          }
          return null
        }).filter((item, i, self) => item && self.indexOf(item) === i)

        Object.assign(this, {
          expandedKeys,
          searchValue: value,
          autoExpandParent: true,
        })


      },
      getCheckedRows(checkedKeys) {
        const forChildren = (list, key) => {
          for (let item of list) {
            if (item.id === key) {
              return item
            }
            if (item.children instanceof Array) {
              let value = forChildren(item.children, key)
              if (value != null) {
                return value
              }
            }
          }
          return null
        }

        let rows = []
        for (let key of checkedKeys) {
          let row = forChildren(this.treeData, key)
          if (row != null) {
            rows.push(row)
          }
        }
        return rows
      },
      switchCheckStrictly (v) {
        if(v==1){
          this.checkStrictly = false
        }else if(v==2){
          this.checkStrictly = true
        }
      },
      isFullscreen(val){
        this.fullscreen=val
      }
    }
  }

</script>

<style lang="less" scoped>
  .my-dept-select-tree{
    height:350px;

    &.fullscreen{
      height: calc(100vh - 250px);
    }
    overflow-y: scroll;
  }
  .drawer-bootom-button {
    position: absolute;
    bottom: 0;
    width: 100%;
    border-top: 1px solid #e8e8e8;
    padding: 10px 16px;
    text-align: right;
    left: 0;
    background: #fff;
    border-radius: 0 0 2px 2px;
  }
</style>
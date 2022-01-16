<template>
  <div style="margin: 10px">
    <a-tabs type="card" @change="tabsChange">
      <a-form :form="form1" layout="inline" slot="tabBarExtraContent">
        <a-form-item label="Source Name">
          <a-select
            style="width: 200px"
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
            style="width: 200px"
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
      </a-form>
      <a-tab-pane key="1" tab="Category Design">
        <!--      <m-editer ref="mEditorRef" :ds-list1="dsList" :ds-code1="dsCode" :db-list1="dbList" :db-code1="dbCode" :tree-data1="treeData"></m-editer>-->
        <m-editer ref="mEditorRef"></m-editer>
      </a-tab-pane>
      <a-tab-pane key="2" tab="Entity Input">
        <!--      <d-editer ref="dEditorRef" :ds-list1="dsList" :ds-code1="dsCode" :db-list1="dbList" :db-code1="dbCode" :table-list1="tableList"></d-editer>-->
        <d-editer ref="dEditorRef"></d-editer>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script>
import { getAction, postAction } from '@api/manage'
import { ajaxGetDictItems, getDictItemsFromCache } from '@api/api'
import MEditer from '@views/pmkb/modules/MEditor'
import DEditer from '@views/pmkb/modules/DEditor'

export default {
  name: 'mmkp',
  components: { DEditer, MEditer },
  data() {
    return {
      form1: this.$form.createForm(this, { name: 'dsb' }),
      dsList: [],
      dsCode: '',
      dbList: [],
      dbCode: '',
      treeData: [],
      tableList: [],
      mdata: {},
      ddata: {}
    }
  },
  mounted() {
    this.loadItems()
  },
  methods: {
    loadItems() {
      getAction('/sys/dataSource/getDsDict', {}).then(res => {
        if (res.success == true) {
          this.dsList = res.result
          // if (this.dsList && this.dsList.length > 0) {
          //   this.dsCode = this.dsList[0].value
          //   this.handleDsChange(this.dsCode)
          // }
        } else {
          this.$message.error('Data Source For failure!')
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
            // if (this.dbList && this.dbList.length > 0) {
            //   this.dbCode = this.dbList[0]
            // }
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

        if (this.$refs.mEditorRef) {
          this.$refs.mEditorRef.updateCode(this.dsCode, this.dbCode)
        }
        if (this.$refs.dEditorRef) {
          this.$refs.dEditorRef.updateCode(this.dsCode, this.dbCode)
        }
      } else {
        this.dbCode = undefined
      }
    },
    tabsChange(key) {
      console.log(this.dsCode)
      console.log(this.dbCode)
      this.$nextTick(() => {
        if (this.$refs.mEditorRef) {
          this.$refs.mEditorRef.updateCode(this.dsCode, this.dbCode)
        }
        if (this.$refs.dEditorRef) {
          this.$refs.dEditorRef.updateCode(this.dsCode, this.dbCode)
        }
      })

    }
  }
}
</script>

<style scoped>

</style>
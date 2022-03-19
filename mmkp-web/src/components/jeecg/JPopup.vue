<template>
  <div class="components-input-demo-presuffix" v-if="avalid">
    <!---->
    <a-input @click="openModal" :placeholder="placeholder" v-model="showText" readOnly :disabled="disabled">
      <a-icon slot="prefix" type="cluster" :title="title"/>
      <a-icon v-if="showText" slot="suffix" type="close-circle" @click="handleEmpty" title="reset"/>
    </a-input>

    <j-popup-onl-report
      ref="jPopupOnlReport"
      :code="code"
      :multi="multi"
      :sorter="sorter"
      :groupId="uniqGroupId"
      :param="param"
      @ok="callBack"
    />

  </div>
</template>

<script>
  import JPopupOnlReport from './modal/JPopupOnlReport'

  export default {
    name: 'JPopup',
    components: {
      JPopupOnlReport
    },
    props: {
      code: {
        type: String,
        default: '',
        required: false
      },
      field: {
        type: String,
        default: '',
        required: false
      },
      orgFields: {
        type: String,
        default: '',
        required: false
      },
      destFields: {
        type: String,
        default: '',
        required: false
      },
      sorter: {
        type: String,
        default: ''
      },
      width: {
        type: Number,
        default: 1200,
        required: false
      },
      placeholder: {
        type: String,
        default: 'select',
        required: false
      },
      value: {
        type: String,
        required: false
      },
      triggerChange: {
        type: Boolean,
        required: false,
        default: false
      },
      disabled: {
        type: Boolean,
        required: false,
        default: false
      },
      multi: {
        type: Boolean,
        required: false,
        default: false
      },
      param:{
        type: Object,
        required: false,
        default: ()=>{}
      },
      spliter:{
        type: String,
        required: false,
        default: ','
      },
      groupId: String

    },
    data() {
      return {
        showText: '',
        title: '',
        avalid: true
      }
    },
    computed: {
      uniqGroupId() {
        if (this.groupId) {
          let { groupId, code, field, orgFields, destFields } = this
          return `${groupId}_${code}_${field}_${orgFields}_${destFields}`
        }
      }
    },
    watch: {
      value: {
        immediate: true,
        handler: function(val) {
          if (!val) {
            this.showText = ''
          } else {
            this.showText = val.split(this.spliter).join(',')
          }
        }
      }
    },
    created() {
    },
    mounted() {
      if (!this.orgFields || !this.destFields || !this.code) {
        this.$message.error('The POPUP parameter is incorrectly configured!')
        this.avalid = false
      }
      if (this.destFields.split(',').length != this.orgFields.split(',').length) {
        this.$message.error('The popUP parameter is incorrectly configured. The original value and the target value are inconsistent!')
        this.avalid = false
      }
    },
    methods: {
      openModal() {
        if (this.disabled === false) {
          this.$refs.jPopupOnlReport.show()
        }
      },
      handleEmpty() {
        this.showText = ''
        let destFieldsArr = this.destFields.split(',')
        if (destFieldsArr.length === 0) {
          return
        }
        let res = {}
        for (let i = 0; i < destFieldsArr.length; i++) {
          res[destFieldsArr[i]] = ''
        }
        if (this.triggerChange) {
          this.$emit('callback', res)
        } else {
          this.$emit('input', '', res)
        }
      },
      callBack(rows) {
        let orgFieldsArr = this.orgFields.split(',')
        let destFieldsArr = this.destFields.split(',')
        let resetText = false
        if (this.field && this.field.length > 0) {
          this.showText = ''
          resetText = true
        }
        let res = {}
        if (orgFieldsArr.length > 0) {
          for (let i = 0; i < orgFieldsArr.length; i++) {
            let tempDestArr = []
            for(let rw of rows){
              let val = rw[orgFieldsArr[i]]
              if(typeof val=='undefined'|| val==null || val.toString()==""){
                val = ""
              }
              tempDestArr.push(val)
            }
            res[destFieldsArr[i]] = tempDestArr.join(",")
          }
          if (resetText === true) {
            let tempText = []
            for(let rw of rows){
              let val = rw[orgFieldsArr[destFieldsArr.indexOf(this.field)]]
              if(!val){
                val = ""
              }
              tempText.push(val)
            }
            this.showText = tempText.join(",")
          }
        }
        if (this.triggerChange) {
          this.$emit('callback', res)
        } else {
          // this.$emit("input",row[orgFieldsArr[destFieldsArr.indexOf(this.field)]])
          let str = ''
          if(this.showText){
            str = this.showText.split(',').join(this.spliter)
          }
          this.$emit('input', str, res)
        }
      }
    }
  }
</script>
<style scoped>
  .components-input-demo-presuffix .anticon-close-circle {
    cursor: pointer;
    color: #ccc;
    transition: color 0.3s;
    font-size: 12px;
  }

  .components-input-demo-presuffix .anticon-close-circle:hover {
    color: #f5222d;
  }

  .components-input-demo-presuffix .anticon-close-circle:active {
    color: #666;
  }
</style>
<template>
  <a-time-picker
    :disabled="disabled || readOnly"
    :placeholder="placeholder"
    :value="momVal"
    :format="dateFormat"
    :getCalendarContainer="getCalendarContainer"
    @change="handleTimeChange"/>
</template>

<script>
  import moment from 'moment'
  export default {
    name: 'JTime',
    props: {
      placeholder:{
        type: String,
        default: '',
        required: false
      },
      value:{
        type: String,
        required: false
      },
      dateFormat:{
        type: String,
        default: 'HH:mm:ss',
        required: false
      },
      readOnly:{
        type: Boolean,
        required: false,
        default: false
      },
      disabled:{
        type: Boolean,
        required: false,
        default: false
      },
      getCalendarContainer: {
        type: Function,
        default: (node) => node.parentNode
      }
    },
    data () {
      let timeStr = this.value;
      return {
        decorator:"",
        momVal:!timeStr?null:moment(timeStr,this.dateFormat)
      }
    },
    watch: {
      value (val) {
        if(!val){
          this.momVal = null
        }else{
          this.momVal = moment(val,this.dateFormat)
        }
      }
    },
    methods: {
      moment,
      handleTimeChange(mom,timeStr){
        this.$emit('change', timeStr);
      }
    },
    model: {
      prop: 'value',
      event: 'change'
    }
  }
</script>

<style scoped>

</style>
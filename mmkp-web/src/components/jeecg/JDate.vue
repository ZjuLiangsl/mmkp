<template>
  <a-date-picker
    dropdownClassName="j-date-picker"
    :disabled="disabled || readOnly"
    :placeholder="placeholder"
    @change="handleDateChange"
    :value="momVal"
    :showTime="showTime"
    :format="dateFormat"
    :getCalendarContainer="getCalendarContainer"
    v-bind="$attrs"/>
</template>
<script>
import moment from 'moment'

export default {
  name: 'JDate',
  props: {
    placeholder: {
      type: String,
      default: '',
      required: false
    },
    value: {
      type: String,
      required: false
    },
    dateFormat: {
      type: String,
      default: 'YYYY-MM-DD',
      required: false
    },
    triggerChange: {
      type: Boolean,
      required: false,
      default: false
    },
    readOnly: {
      type: Boolean,
      required: false,
      default: false
    },
    disabled: {
      type: Boolean,
      required: false,
      default: false
    },
    showTime: {
      type: Boolean,
      required: false,
      default: false
    },
    getCalendarContainer: {
      type: Function,
      default: (node) => node.parentNode
    }
  },
  data() {
    let dateStr = this.value
    return {
      decorator: '',
      momVal: !dateStr ? null : moment(dateStr, this.dateFormat)
    }
  },
  watch: {
    value(val) {
      if (!val) {
        this.momVal = null
      } else {
        this.momVal = moment(val, this.dateFormat)
      }
    }
  },
  methods: {
    moment,
    handleDateChange(mom, dateStr) {
      this.$emit('change', dateStr)
    }
  },
  model: {
    prop: 'value',
    event: 'change'
  }
}
</script>

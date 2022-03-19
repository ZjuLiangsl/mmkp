<template>
  <div class="config-list">
    <a-radio-group v-model="type">
      <div class="item">
        <a-radio value="TYPE_NOT_SET" class="choice" :disabled="disableChoice">Don't set</a-radio>
        <span class="tip-info">You can set either day or week</span>
      </div>
      <div class="item">
        <a-radio value="TYPE_RANGE" class="choice" :disabled="disableChoice">interval</a-radio>
        from
        <a-select v-model="valueRange.start" class="w80" :disabled="type!==TYPE_RANGE || disableChoice">
          <template v-for="(v, k) of WEEK_MAP">
            <a-select-option :value="v">{{ k }}</a-select-option>
          </template>
        </a-select>
        to
        <a-select v-model="valueRange.end" class="w80" :disabled="type!==TYPE_RANGE || disableChoice">
          <template v-for="(v, k) of WEEK_MAP">
            <a-select-option :value="v">{{ k }}</a-select-option>
          </template>
        </a-select>
      </div>
      <div class="item">
        <a-radio value="TYPE_LOOP" class="choice" :disabled="disableChoice">cycle</a-radio>
        from
        <a-select v-model="valueLoop.start" class="w80" :disabled="type!==TYPE_LOOP || disableChoice">
          <template v-for="(v, k) of WEEK_MAP">
            <a-select-option :value="v">{{ k }}</a-select-option>
          </template>
        </a-select>
        begin，interval
        <a-input-number :disabled="type!==TYPE_LOOP || disableChoice" :max="maxValue" :min="minValue" :precision="0"
                        class="w60" v-model="valueLoop.interval"/>
        day
      </div>
      <div class="item">
        <a-radio value="TYPE_SPECIFY" class="choice" :disabled="disableChoice">The specified</a-radio>
        <div class="list">
          <a-checkbox-group v-model="valueList">
            <template v-for="i in specifyRange">
              <a-checkbox class="list-check-item" :key="`key-${i}`" :value="i"
                          :disabled="type!==TYPE_SPECIFY || disabled">{{ i }}
              </a-checkbox>
            </template>
          </a-checkbox-group>
        </div>
      </div>
    </a-radio-group>
  </div>
</template>

<script>
import mixin from './mixin'
import { replaceWeekName, WEEK_MAP_EN } from './const.js'

const WEEK_MAP = {
  'Monday': 1,
  'Tuesday': 2,
  'Wednesday': 3,
  'Thursday': 4,
  'Friday': 5,
  'Saturday': 6,
  'Sunday': 7
}

export default {
  name: 'week',
  mixins: [mixin],
  props: {
    day: {
      type: String,
      default: '*'
    }
  },
  data() {
    return {
      WEEK_MAP,
      WEEK_MAP_EN
    }
  },
  computed: {
    disableChoice() {
      return (this.day && this.day !== '?') || this.disabled
    }
  },
  watch: {
    value_c(newVal, oldVal) {
      this.updateValue()
    },
    day(newVal) {
      // console.info('new day: ' + newVal)
      this.updateValue()
    }
  },
  methods: {
    updateValue() {
      this.$emit('change', this.disableChoice ? '?' : this.value_c)
    },
    preProcessProp(c) {
      return replaceWeekName(c)
    }
  },
  created() {
    this.DEFAULT_VALUE = '*'
    this.minValue = 1
    this.maxValue = 7
    this.valueRange.start = 1
    this.valueRange.end = 7
    this.valueLoop.start = 2
    this.valueLoop.interval = 1
    this.parseProp(this.prop)
  }
}
</script>

<style lang="less" scoped>
@import "mixin.less";
</style>

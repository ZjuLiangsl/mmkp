<template>
  <div class="config-list">
    <a-radio-group v-model="type">
      <div class="item">
        <a-radio value="TYPE_NOT_SET" class="choice" :disabled="disableChoice">Don't set</a-radio>
        <span class="tip-info">You can set either day or week</span>
      </div>
      <div class="item">
        <a-radio value="TYPE_EVERY" class="choice" :disabled="disableChoice">daily</a-radio>
      </div>
      <div class="item">
        <a-radio value="TYPE_RANGE" class="choice" :disabled="disableChoice">interval</a-radio>
        from
        <a-input-number :disabled="type!==TYPE_RANGE || disableChoice" :max="maxValue" :min="minValue" :precision="0"
                        class="w60" v-model="valueRange.start"/>
        day
        to
        <a-input-number :disabled="type!==TYPE_RANGE || disableChoice" :max="maxValue" :min="minValue" :precision="0"
                        class="w60" v-model="valueRange.end"/>
        day
      </div>
      <div class="item">
        <a-radio value="TYPE_LOOP" class="choice" :disabled="disableChoice">cycle</a-radio>
        from
        <a-input-number :disabled="type!==TYPE_LOOP || disableChoice" :max="maxValue" :min="minValue" :precision="0"
                        class="w60" v-model="valueLoop.start"/>
        day begin,interval
        <a-input-number :disabled="type!==TYPE_LOOP || disableChoice" :max="maxValue" :min="minValue" :precision="0"
                        class="w60" v-model="valueLoop.interval"/>
        day
      </div>
      <div class="item">
        <a-radio value="TYPE_WORK" class="choice" :disabled="disableChoice">Working days</a-radio>
        This month
        <a-input-number :disabled="type!==TYPE_WORK || disableChoice" :max="maxValue" :min="minValue" :precision="0"
                        class="w60" v-model="valueWork"/>
        Day, the nearest working day
      </div>
      <div class="item">
        <a-radio value="TYPE_LAST" class="choice" :disabled="disableChoice">The last day</a-radio>
      </div>
      <div class="item">
        <a-radio value="TYPE_SPECIFY" class="choice" :disabled="disableChoice">The specified</a-radio>
        <div class="list">
          <a-checkbox-group v-model="valueList">
            <template v-for="i of specifyRange">
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

export default {
  name: 'day',
  mixins: [mixin],
  props: {
    week: {
      type: String,
      default: '?'
    }
  },
  data() {
    return {}
  },
  computed: {
    disableChoice() {
      return (this.week && this.week !== '?') || this.disabled
    }
  },
  watch: {
    value_c(newVal, oldVal) {
      this.updateValue()
    },
    week(newVal, oldVal) {
      // console.info('new week: ' + newVal)
      this.updateValue()
    }
  },
  methods: {
    updateValue() {
      this.$emit('change', this.disableChoice ? '?' : this.value_c)
    }
  },
  created() {
    this.DEFAULT_VALUE = '*'
    this.minValue = 1
    this.maxValue = 31
    this.valueRange.start = 1
    this.valueRange.end = 31
    this.valueLoop.start = 1
    this.valueLoop.interval = 1
    this.parseProp(this.prop)
  }
}
</script>

<style lang="less" scoped>
@import "mixin.less";
</style>

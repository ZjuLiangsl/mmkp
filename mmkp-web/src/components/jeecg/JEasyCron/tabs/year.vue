<template>
  <div class="config-list">
    <a-radio-group v-model="type">
      <div class="item">
        <a-radio value="TYPE_EVERY" class="choice" :disabled="disabled">Every year</a-radio>
      </div>
      <div class="item">
        <a-radio value="TYPE_RANGE" class="choice" :disabled="disabled">interval</a-radio>
        from
        <a-input-number :disabled="type!==TYPE_RANGE || disabled" :min="0" :precision="0" class="w60"
                        v-model="valueRange.start"/>
        year
        to
        <a-input-number :disabled="type!==TYPE_RANGE || disabled" :min="1" :precision="0" class="w60"
                        v-model="valueRange.end"/>
        year
      </div>
      <div class="item">
        <a-radio value="TYPE_LOOP" class="choice" :disabled="disabled">cycle</a-radio>
        from
        <a-input-number :disabled="type!==TYPE_LOOP || disabled" :min="0" :precision="0" class="w60"
                        v-model="valueLoop.start"/>
        year begin,
        <a-input-number :disabled="type!==TYPE_LOOP || disabled" :min="1" :precision="0" class="w60"
                        v-model="valueLoop.interval"/>
        year
      </div>
    </a-radio-group>
  </div>
</template>

<script>
import mixin from './mixin'

export default {
  name: 'year',
  mixins: [mixin],
  data() {
    return {}
  },
  watch: {
    value_c(newVal, oldVal) {
      // console.info('change:' + newVal)
      this.$emit('change', newVal)
    }
  },
  created() {
    const nowYear = (new Date()).getFullYear()
    this.DEFAULT_VALUE = '*'
    this.minValue = 0
    this.maxValue = 0
    this.valueRange.start = nowYear
    this.valueRange.end = nowYear + 100
    this.valueLoop.start = nowYear
    this.valueLoop.interval = 1
    // console.info('created')
    this.parseProp(this.prop)
  }
}
</script>

<style lang="less" scoped>
@import "mixin.less";
</style>

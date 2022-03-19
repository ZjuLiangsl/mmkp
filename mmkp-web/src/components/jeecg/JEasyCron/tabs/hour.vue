<template>
  <div class="config-list">
    <a-radio-group v-model="type">
      <div class="item">
        <a-radio value="TYPE_EVERY" class="choice" :disabled="disabled">All the time</a-radio>
      </div>
      <div class="item">
        <a-radio value="TYPE_RANGE" class="choice" :disabled="disabled">interval</a-radio>
        from
        <a-input-number :disabled="type!==TYPE_RANGE || disabled" :max="maxValue" :min="minValue" :precision="0"
                        class="w60" v-model="valueRange.start"/>
        time
        to
        <a-input-number :disabled="type!==TYPE_RANGE || disabled" :max="maxValue" :min="minValue" :precision="0"
                        class="w60" v-model="valueRange.end"/>
        time
      </div>
      <div class="item">
        <a-radio value="TYPE_LOOP" class="choice" :disabled="disabled">cycle</a-radio>
        from
        <a-input-number :disabled="type!==TYPE_LOOP || disabled" :max="maxValue" :min="minValue" :precision="0"
                        class="w60" v-model="valueLoop.start"/>
        time begin，interval
        <a-input-number :disabled="type!==TYPE_LOOP || disabled" :max="maxValue" :min="minValue" :precision="0"
                        class="w60" v-model="valueLoop.interval"/>
        time
      </div>
      <div class="item">
        <a-radio value="TYPE_SPECIFY" class="choice" :disabled="disabled">The specified</a-radio>
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

export default {
  name: 'minute',
  mixins: [mixin],
  data() {
    return {}
  },
  watch: {
    value_c(newVal, oldVal) {
      this.$emit('change', newVal)
    }
  },
  created() {
    this.DEFAULT_VALUE = '*'
    this.minValue = 0
    this.maxValue = 23
    this.valueRange.start = 0
    this.valueRange.end = 23
    this.valueLoop.start = 0
    this.valueLoop.interval = 1
    this.parseProp(this.prop)
  }
}
</script>

<style lang="less" scoped>
@import "mixin.less";
</style>

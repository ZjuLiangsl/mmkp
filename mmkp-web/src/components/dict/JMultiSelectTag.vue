<template>
  <a-checkbox-group v-if="tagType=='checkbox'" @change="onChange" :value="arrayValue" :disabled="disabled">
    <a-checkbox v-for="(item, key) in dictOptions" :key="key" :value="item.value">{{
        item.text || item.label
      }}
    </a-checkbox>
  </a-checkbox-group>

  <a-select
    v-else-if="tagType=='select'"
    :value="arrayValue"
    @change="onChange"
    :disabled="disabled"
    mode="multiple"
    :placeholder="placeholder"
    :getPopupContainer="getParentContainer"
    optionFilterProp="children"
    :filterOption="filterOption"
    allowClear>
    <a-select-option
      v-for="(item,index) in dictOptions"
      :key="index"
      :value="item.value">
      <span style="display: inline-block;width: 100%" :title=" item.text || item.label ">
        {{ item.text || item.label }}
      </span>
    </a-select-option>
  </a-select>

</template>

<script>
import { ajaxGetDictItems, getDictItemsFromCache } from '@/api/api'

export default {
  name: 'JMultiSelectTag',
  props: {
    dictCode: String,
    placeholder: String,
    disabled: Boolean,
    value: String,
    type: String,
    options: Array,
    spliter: {
      type: String,
      required: false,
      default: ','
    },
    popContainer: {
      type: String,
      default: '',
      required: false
    }
  },
  data() {
    return {
      dictOptions: [],
      tagType: '',
      arrayValue: !this.value ? [] : this.value.split(this.spliter)
    }
  },
  created() {
    if (!this.type || this.type === 'list_multi') {
      this.tagType = 'select'
    } else {
      this.tagType = this.type
    }
    //this.initDictData();
  },
  watch: {
    options: function(val) {
      this.setCurrentDictOptions(val)
    },
    dictCode: {
      immediate: true,
      handler() {
        this.initDictData()
      }
    },
    value(val) {
      if (!val) {
        this.arrayValue = []
      } else {
        this.arrayValue = this.value.split(this.spliter)
      }
    }
  },
  methods: {
    initDictData() {
      if (this.options && this.options.length > 0) {
        this.dictOptions = [...this.options]
      } else {
        let cacheOption = getDictItemsFromCache(this.dictCode)
        if (cacheOption && cacheOption.length > 0) {
          this.dictOptions = cacheOption
          return
        }
        ajaxGetDictItems(this.dictCode, null).then((res) => {
          if (res.success) {
            this.dictOptions = res.result
          }
        })
      }

    },
    onChange(selectedValue) {
      this.$emit('change', selectedValue.join(this.spliter))
    },
    setCurrentDictOptions(dictOptions) {
      this.dictOptions = dictOptions
    },
    getCurrentDictOptions() {
      return this.dictOptions
    },
    getParentContainer(node) {
      if (!this.popContainer) {
        return node.parentNode
      } else {
        return document.querySelector(this.popContainer)
      }
    },
    filterOption(input, option) {
      return option.componentOptions.children[0].children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
    }
  },
  model: {
    prop: 'value',
    event: 'change'
  }
}
</script>

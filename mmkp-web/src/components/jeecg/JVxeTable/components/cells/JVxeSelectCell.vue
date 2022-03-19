<template>
  <a-select
    ref="select"
    :value="innerValue"
    allowClear
    :filterOption="handleSelectFilterOption"
    v-bind="selectProps"
    style="width: 100%;"
    @blur="handleBlur"
    @change="handleChange"
    @search="handleSearchSelect"
  >

    <div v-if="loading" slot="notFoundContent">
      <a-icon type="loading"  />
      <span>&nbsp;loading…</span>
    </div>

    <template v-for="option of selectOptions">
      <a-select-option :key="option.value" :value="option.value" :disabled="option.disabled">
        <span>{{option.text || option.label || option.title|| option.value}}</span>
      </a-select-option>
    </template>

  </a-select>
</template>

<script>
  import JVxeCellMixins, { dispatchEvent } from '@/components/jeecg/JVxeTable/mixins/JVxeCellMixins'
  import { JVXETypes } from '@comp/jeecg/JVxeTable/index'
  import { filterDictText } from '@comp/dict/JDictSelectUtil'

  export default {
    name: 'JVxeSelectCell',
    mixins: [JVxeCellMixins],
    data(){
      return {
        loading: false,
        asyncOptions: null,
      }
    },
    computed: {
      selectProps() {
        let props = {...this.cellProps}
        let {allowSearch, allowInput} = this.originColumn
        if (allowInput === true || allowSearch === true) {
          props['showSearch'] = true
        }
        return props
      },
      selectOptions() {
        if (this.asyncOptions) {
          return this.asyncOptions
        }
        let {linkage} = this.renderOptions
        if (linkage) {
          let {getLinkageOptionsSibling, config} = linkage
          let res = getLinkageOptionsSibling(this.row, this.originColumn, config, true)
          if (res instanceof Promise) {
            this.loading = true
            res.then(opt => {
              this.asyncOptions = opt
              this.loading = false
            }).catch(e => {
              console.error(e)
              this.loading = false
            })
          } else {
            this.asyncOptions = null
            return res
          }
        }
        return this.originColumn.options
      },
    },
    created() {
      let multiple = [JVXETypes.selectMultiple, JVXETypes.list_multi]
      let search = [JVXETypes.selectSearch, JVXETypes.sel_search]
      if (multiple.includes(this.$type)) {
        let props = this.originColumn.props || {}
        props['mode'] = 'multiple'
        props['maxTagCount'] = 1
        this.$set(this.originColumn, 'props', props)
      } else if (search.includes(this.$type)) {
        this.$set(this.originColumn, 'allowSearch', true)
      }
    },
    methods: {

      handleChange(value) {
        debugger
        let linkage = this.renderOptions.linkage
        if (linkage) {
          linkage.linkageSelectChange(this.row, this.originColumn, linkage.config, value)
        }
        this.handleChangeCommon(value)
      },

      handleBlur(value) {
        let {allowInput, options} = this.originColumn

        if (allowInput === true) {
          if (typeof value === 'string') {
            let indexes = []
            options.forEach((option, index) => {
              if (option.value.toLocaleString() === value.toLocaleString()) {
                delete option.searchAdd
              } else if (option.searchAdd === true) {
                indexes.push(index)
              }
            })
            for (let index of indexes.reverse()) {
              options.splice(index, 1)
            }
          }
        }

        this.handleBlurCommon(value)
      },

      handleSelectFilterOption(input, option) {
        let {allowSearch, allowInput} = this.originColumn
        if (allowSearch === true || allowInput === true) {
          return option.componentOptions.children[0].children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
        }
        return true
      },

      handleSearchSelect(value) {
        let {allowSearch, allowInput, options} = this.originColumn

        if (allowSearch !== true && allowInput === true) {
          let flag = false
          for (let option of options) {
            if (option.value.toLocaleString() === value.toLocaleString()) {
              flag = true
              break
            }
          }
          if (!flag && !!value) {
            options.push({title: value, value: value, searchAdd: true})
          }

        }
      },

    },
    enhanced: {
      aopEvents: {
        editActived(event) {
          dispatchEvent.call(this, event, 'ant-select')
        },
      },
      translate: {
        enabled: true,
        async handler(value,) {
          let options
          let {linkage} = this.renderOptions
          if (linkage) {
            let {getLinkageOptionsSibling, config} = linkage
            options = getLinkageOptionsSibling(this.row, this.originColumn, config, true)
            if (options instanceof Promise) {
              return new Promise(resolve => {
                options.then(opt => {
                  resolve(filterDictText(opt, value))
                })
              })
            }
          } else {
            options = this.column.own.options
          }
          return filterDictText(options, value)
        },
      },
      getValue(value) {
        if (Array.isArray(value)) {
          return value.join(',')
        } else {
          return value
        }
      },
      setValue(value) {
        let {column: {own: col}, params: {$table}} = this
        if ((col.props || {})['mode'] === 'multiple') {
          $table.$set(col.props, 'maxTagCount', 1)
        }
        if (value != null && value !== '') {
          if (typeof value === 'string') {
            return value === '' ? [] : value.split(',')
          }
          return value
        } else {
          return undefined
        }
      }
    }
  }
</script>

<style scoped>

</style>
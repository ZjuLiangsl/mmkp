<template>
  <div>
    <template v-if="hasFile" v-for="(file, fileKey) of [innerFile || {}]">
      <div :key="fileKey" style="position: relative;">
        <template v-if="!file || !(file['url'] || file['path'] || file['message'])">
          <a-tooltip :title="'Waiting: ' + JSON.stringify (file) + ((file['url'] || file['path'] || file['message']))">
            <a-icon type="loading"/>
          </a-tooltip>
        </template>
        <template v-else-if="file['path']">
          <img class="j-editable-image" :src="imgSrc" alt="none" @click="handleMoreOperation"/>
        </template>
        <a-tooltip v-else :title="file.message||'uplaod fail'" @click="handleClickShowImageError">
          <a-icon type="exclamation-circle" style="color:red;"/>
        </a-tooltip>

        <template style="width: 30px">
          <a-dropdown :trigger="['click']" placement="bottomRight" style="margin-left: 10px;">
            <a-tooltip title="operation">
              <a-icon
                v-if="file.status!=='uploading'"
                type="setting"
                style="cursor: pointer;"/>
            </a-tooltip>

            <a-menu slot="overlay">
              <a-menu-item v-if="originColumn.allowDownload !== false" @click="handleClickDownloadFile">
                <span><a-icon type="download"/>&nbsp;download</span>
              </a-menu-item>
              <a-menu-item v-if="originColumn.allowRemove !== false" @click="handleClickDeleteFile">
                <span><a-icon type="delete"/>&nbsp; Delete </span>
              </a-menu-item>
              <a-menu-item @click="handleMoreOperation(originColumn)">
                <span><a-icon type="bars"/> More</span>
              </a-menu-item>
            </a-menu>
          </a-dropdown>
        </template>

      </div>
    </template>
    <a-upload
      v-show="!hasFile"
      name="file"
      :data="{'isup': 1}"
      :multiple="false"
      :action="uploadAction"
      :headers="uploadHeaders"
      :showUploadList="false"
      v-bind="cellProps"
      @change="handleChangeUpload"
    >
      <a-button icon="upload">{{originColumn.btnText || 'upload img'}}</a-button>
    </a-upload>
    <j-file-pop ref="filePop" @ok="handleFileSuccess" :number="number"/>
  </div>
</template>

<script>
  import { getFileAccessHttpUrl } from '@api/manage'
  import JVxeCellMixins from '@/components/jeecg/JVxeTable/mixins/JVxeCellMixins'
  import { ACCESS_TOKEN } from '@/store/mutation-types'
  import JFilePop from '@/components/jeecg/minipop/JFilePop'

  import JVxeUploadCell from '@/components/jeecg/JVxeTable/components/cells/JVxeUploadCell'

  export default {
    name: 'JVxeImageCell',
    mixins: [JVxeCellMixins],
    components: {JFilePop},
    props: {},
    data() {
      return {
        innerFile: null,
        number:0
      }
    },
    computed: {
      /** upload headers */
      uploadHeaders() {
        let {originColumn: col} = this
        let headers = {}
        if (col.token === true) {
          headers['X-Access-Token'] = this.$ls.get(ACCESS_TOKEN)
        }
        return headers
      },

      uploadAction() {
        if (!this.originColumn.action) {
          return window._CONFIG['domianURL'] + '/sys/common/upload'
        } else {
          return this.originColumn.action
        }
      },

      hasFile() {
        return this.innerFile != null
      },

      imgSrc() {
        if (this.innerFile) {
          if (this.innerFile['url']) {
            return this.innerFile['url']
          } else if (this.innerFile['path']) {
            let path = this.innerFile['path'].split(',')[0]
            return getFileAccessHttpUrl(path)
          }
        }
        return ''
      },

      responseName() {
        if (this.originColumn.responseName) {
          return this.originColumn.responseName
        } else {
          return 'message'
        }
      },

    },
    watch: {
      innerValue: {
        immediate: true,
        handler() {
          if (this.innerValue) {
            this.innerFile = this.innerValue
          } else {
            this.innerFile = null
          }
        },
      },
    },
    methods: {

      handleMoreOperation(originColumn) {
        if (originColumn.number) {
          this.number = originColumn.number
        } else {
          this.number = 0
        }
        if(originColumn && originColumn.fieldExtendJson){
          let json = JSON.parse(originColumn.fieldExtendJson);
          this.number = json.uploadnum?json.uploadnum:0;
        }
        let path = ''
        if (this.innerFile) {
          path = this.innerFile.path
        }
        this.$refs.filePop.show('', path, 'img')
      },

      handleFileSuccess(file) {
        if (file) {
          this.innerFile.path = file.path
          this.handleChangeCommon(this.innerFile)
        }
      },

      handleClickShowImageError() {
        let file = this.innerFile || null
        if (file && file['message']) {
          this.$error({title: 'upload error', content: 'error msg：' + file['message'], maskClosable: true})
        }
      },

      handleChangeUpload(info) {
        let {originColumn: col} = this
        let {file} = info
        let value = {
          name: file.name,
          type: file.type,
          size: file.size,
          status: file.status,
          percent: file.percent
        }
        if (file.response) {
          value['responseName'] = file.response[this.responseName]
        }
        if (file.status === 'done') {
          if (typeof file.response.success === 'boolean') {
            if (file.response.success) {
              value['path'] = file.response[this.responseName]
              this.handleChangeCommon(value)
            } else {
              value['status'] = 'error'
              value['message'] = file.response.message || 'error'
            }
          } else {
            value['path'] = file.response[this.responseName]
            this.handleChangeCommon(value)
          }
        } else if (file.status === 'error') {
          value['message'] = file.response.message || 'error'
        }
        this.innerFile = value
      },

      handleClickDownloadFile() {
        if (this.imgSrc) {
          window.open(this.imgSrc)
        }
      },

      handleClickDeleteFile() {
        this.handleChangeCommon(null)
      },

    },
    enhanced: {
      switches: {visible: true},
      getValue: value => JVxeUploadCell.enhanced.getValue(value),
      setValue: value => JVxeUploadCell.enhanced.setValue(value),
    }
  }
</script>

<style scoped lang="less">
  .j-editable-image {
    height: 32px;
    max-width: 100px !important;
    cursor: pointer;

    &:hover {
      opacity: 0.8;
    }

    &:active {
      opacity: 0.6;
    }

  }
</style>

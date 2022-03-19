<template>
  <div>
    <div class="j-markdown-editor" :id="id"/>
    <div v-if="isShow">
      <j-modal
        title="upload img"
        :visible.sync="dialogVisible"
        width="30%"
        :before-close="handleClose"
        @ok="handleOk">
        <a-tabs default-active-key="1" @change="handleChange">
          <a-tab-pane tab="Local image upload" key="1" :forceRender="true">
            <j-upload v-model="fileList" :number="1"></j-upload>
            <div style="margin-top: 20px">
              <a-input v-model="remark" placeholder="remarks"></a-input>
            </div>
          </a-tab-pane>
          <a-tab-pane tab="Network picture address" key="2" :forceRender="true">
            <a-input v-model="networkPic" placeholder="address"></a-input>
            <a-input style="margin-top: 20px" v-model="remark" placeholder="remarks"></a-input>
          </a-tab-pane>
        </a-tabs>
      </j-modal>
    </div>
  </div>
</template>

<script>
import 'codemirror/lib/codemirror.css'
import '@toast-ui/editor/dist/toastui-editor.css';
import '@toast-ui/editor/dist/i18n/zh-cn';

import Editor from '@toast-ui/editor';
import defaultOptions from './default-options'
import JUpload from '@/components/jeecg/JUpload'
import { getFileAccessHttpUrl } from '@/api/manage'

export default {
  name: 'JMarkdownEditor',
  components: {
    JUpload,
  },
  props: {
    value: {
      type: String,
      default: ''
    },
    id: {
      type: String,
      required: false,
      default() {
        return 'markdown-editor-' + +new Date() + ((Math.random() * 1000).toFixed(0) + '')
      }
    },
    options: {
      type: Object,
      default() {
        return defaultOptions
      }
    },
    mode: {
      type: String,
      default: 'markdown'
    },
    height: {
      type: String,
      required: false,
      default: '300px'
    },
    language: {
      type: String,
      required: false,
      default: 'zh-CN'
    }
  },
  data() {
    return {
      editor: null,
      isShow:false,
      activeIndex:"1",
      dialogVisible:false,
      index:"1",
      fileList:[],
      remark:"",
      imageName:"",
      imageUrl:"",
      networkPic:""
    }
  },
  computed: {
    editorOptions() {
      const options = Object.assign({}, defaultOptions, this.options)
      options.initialEditType = this.mode
      options.height = this.height
      options.language = this.language
      return options
    }
  },
  watch: {
    value(newValue, preValue) {
      if (newValue !== preValue && newValue !== this.editor.getMarkdown()) {
        this.editor.setMarkdown(newValue)
      }
    },
    language(val) {
      this.destroyEditor()
      this.initEditor()
    },
    height(newValue) {
      this.editor.height(newValue)
    },
    mode(newValue) {
      this.editor.changeMode(newValue)
    }
  },
  mounted() {
    this.initEditor()
  },
  destroyed() {
    this.destroyEditor()
  },
  methods: {
    initEditor() {
      this.editor = new Editor({
        el: document.getElementById(this.id),
        ...this.editorOptions
      })
      if (this.value) {
        this.editor.setMarkdown(this.value)
      }
      this.editor.on('change', () => {
        this.$emit('change', this.editor.getMarkdown())
      })
      /*
       */
      let toolbar = this.editor.getUI().getToolbar();
      let fileDom = this.$refs.files;
      this.editor.eventManager.addEventType('isShowClickEvent');
      this.editor.eventManager.listen('isShowClickEvent', () => {
        this.isShow = true
        this.dialogVisible = true
      });
      this.editor.eventManager.removeEventHandler('addImageBlobHook')
      this.editor.eventManager.listen('addImageBlobHook', (blob, callback) => {
        this.upload(blob, url => {
          callback(url)
        })
      })
      toolbar.insertItem(15,{
        type: 'button',
        options:{
          name: 'customize',
          className: 'tui-image tui-toolbar-icons',
          event: 'isShowClickEvent',
          tooltip: 'upload image',
        }
        //
      });
    },
    destroyEditor() {
      if (!this.editor) return
      this.editor.off('change')
      this.editor.remove()
    },
    setMarkdown(value) {
      this.editor.setMarkdown(value)
    },
    getMarkdown() {
      return this.editor.getMarkdown()
    },
    setHtml(value) {
      this.editor.setHtml(value)
    },
    getHtml() {
      return this.editor.getHtml()
    },
    handleOk(){
      if(this.index=='1'){
        this.imageUrl = getFileAccessHttpUrl(this.fileList)
        if(this.remark){
          this.addImgToMd(this.imageUrl,this.remark)
        }else{
          this.addImgToMd(this.imageUrl,"")
        }
      }else{
        if(this.remark){
          this.addImgToMd(this.networkPic,this.remark)
        }else{
          this.addImgToMd(this.networkPic,"")
        }
      }
      this.index="1"
      this.fileList=[]
      this.imageName="";
      this.imageUrl="";
      this.remark=""
      this.networkPic=""
      this.dialogVisible=false
      this.isShow=false;
    },
    handleClose(done) {
      done();
    },
    handleChange(val){
      this.fileList=[]
      this.remark=""
      this.imageName=""
      this.imageUrl=""
      this.networkPic=""
      this.index=val
    },
    addImgToMd(data,name) {
      let editor = this.editor.getCodeMirror();
      let editorHtml = this.editor.getCurrentModeEditor();
      let isMarkdownMode = this.editor.isMarkdownMode();
      if (isMarkdownMode) {
        editor.replaceSelection(`![${name}](${data})`);
      } else {
        let range = editorHtml.getRange();
        let img = document.createElement('img');
        img.src = `${data}`;
        img.alt = name;
        range.insertNode(img);
      }
    },
  },
  model: {
    prop: 'value',
    event: 'change'
  }
}
</script>
<style scoped lang="less">

  .j-markdown-editor {
    /deep/ .tui-editor-defaultUI {
      .te-mode-switch,
      .tui-scrollsync
      {
        line-height: 1.5;
      }
    }
  }

</style>
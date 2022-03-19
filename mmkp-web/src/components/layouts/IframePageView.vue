<template>

    <iframe  :id="id" :src="url" frameborder="0" width="100%" height="800px" scrolling="auto"></iframe>

</template>

<script>
  import Vue from 'vue'
  import { ACCESS_TOKEN } from "@/store/mutation-types"
  import PageLayout from '../page/PageLayout'
  import RouteView from './RouteView'

  export default {
    name: "IframePageContent",
    inject:['closeCurrent'],
    data () {
      return {
        url: "",
        id:""
      }
    },
    created () {
      this.goUrl()
    },
    updated () {
      this.goUrl()
    },
    watch: {
      $route(to, from) {
        this.goUrl();
      }
    },
    methods: {
      goUrl () {
        let url = this.$route.meta.url
        let id = this.$route.path
        this.id = id
        //url = "http://www.baidu.com"
        console.log("------url------"+url)
        if (url !== null && url !== undefined) {
          //-----------------------------------------------------------------------------------------
          let tokenStr = "${token}";
          if(url.indexOf(tokenStr)!=-1) {
            let token = Vue.ls.get(ACCESS_TOKEN);
            this.url = url.replace(tokenStr, token);
          } else {
            this.url = url
          }
          //-----------------------------------------------------------------------------------------

          if(this.$route.meta.internalOrExternal != undefined && this.$route.meta.internalOrExternal==true){
            this.closeCurrent();
            window.open(this.url);
          }

        }
      }
    }
  }
</script>

<style>
</style>
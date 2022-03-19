<template>
  <div class="user-wrapper" :class="theme">
    <span class="action" @click="showClick">
      <a-icon type="search"></a-icon>
    </span>
    <component :is="searchMenuComp" v-show="searchMenuVisible || isMobile()" class="borders" :visible="searchMenuVisible" title="Search menu" :footer="null" @cancel="searchMenuVisible=false">
      <a-select
        class="search-input"
        showSearch
        :showArrow="false"
        placeholder="Search menu"
        optionFilterProp="children"
        :filterOption="filterOption"
        :open="isMobile()?true:null"
        :getPopupContainer="(node) => node.parentNode"
        :style="isMobile()?{width: '100%',marginBottom:'50px'}:{}"
        @change="searchMethods"
        @blur="hiddenClick"
      >
        <a-select-option v-for="(site,index) in searchMenuOptions" :key="index" :value="site.id">{{site.meta.title}}</a-select-option>
      </a-select>
    </component>

    <a-dropdown>
      <span class="action action-full ant-dropdown-link user-dropdown-menu">
        <span v-if="isDesktop()">welcome，{{ nickname() }}</span>
      </span>
      <a-menu slot="overlay" class="user-dropdown-menu-wrapper">

        <a-menu-item key="3"  @click="systemSetting">
           <a-icon type="tool"/>
           <span>Settings</span>
        </a-menu-item>
        <a-menu-item key="4" @click="updatePassword">
          <a-icon type="setting"/>
          <span>Password</span>
        </a-menu-item>
        <!--<a-menu-item key="5" @click="updateCurrentDepart">
          <a-icon type="cluster"/>
        </a-menu-item>-->
        <a-menu-item key="6" @click="clearCache">
          <a-icon type="sync"/>
          <span>Clear Cache</span>
        </a-menu-item>
       <!-- <a-menu-item key="2" disabled>
          <a-icon type="setting"/>
        </a-menu-item>
        <a-menu-divider/>
        <a-menu-item key="3">
          <a href="javascript:;" @click="handleLogout">
            <a-icon type="logout"/>
          </a>
        </a-menu-item>-->
      </a-menu>
    </a-dropdown>
    <span class="action">
      <a class="logout_title" href="javascript:;" @click="handleLogout">
        <a-icon type="logout"/>
        <span v-if="isDesktop()">&nbsp;Log out</span>
      </a>
    </span>
    <user-password ref="userPassword"></user-password>
    <depart-select ref="departSelect" :closable="true" title="Dept switch"></depart-select>
    <setting-drawer ref="settingDrawer" :closable="true" title="setting"></setting-drawer>
  </div>
</template>

<script>
  import UserPassword from './UserPassword'
  import SettingDrawer from "@/components/setting/SettingDrawer";
  import DepartSelect from './DepartSelect'
  import { mapActions, mapGetters,mapState } from 'vuex'
  import { mixinDevice } from '@/utils/mixin.js'
  import { getFileAccessHttpUrl,getAction } from "@/api/manage"
  import Vue from 'vue'
  import { UI_CACHE_DB_DICT_DATA } from "@/store/mutation-types"

  export default {
    name: "UserMenu",
    mixins: [mixinDevice],
    data(){
      return{
        searchMenuOptions:[],
        searchMenuComp: 'span',
        searchMenuVisible: true,
      }
    },
    components: {
      UserPassword,
      DepartSelect,
      SettingDrawer
    },
    props: {
      theme: {
        type: String,
        required: false,
        default: 'dark'
      }
    },
    created() {
      let lists = []
      this.searchMenus(lists,this.permissionMenuList)
      this.searchMenuOptions=[...lists]
    },
    mounted() {

    },
    computed: {
      ...mapState({
        permissionMenuList: state => state.user.permissionList

      })
    },
    watch: {
      device: {
        immediate: true,
        handler() {
          this.searchMenuVisible = false
          this.searchMenuComp = this.isMobile() ? 'a-modal' : 'span'
        },
      },
    },
    methods: {
      showClick() {
        this.searchMenuVisible = true
      },
      hiddenClick(){
        this.shows = false
      },
      ...mapActions(["Logout"]),
      ...mapGetters(["nickname", "avatar","userInfo"]),
      getAvatar(){
        return getFileAccessHttpUrl(this.avatar())
      },
      handleLogout() {
        const that = this

        this.$confirm({
          title: 'Info',
          content: 'Are you sure you want to log out ?',
          onOk() {
            return that.Logout({}).then(() => {
              that.$router.push({ path: '/user/login' });
              window.location.reload()
            }).catch(err => {
              that.$message.error({
                title: 'Error',
                description: err.message
              })
            })
          },
          onCancel() {
          },
        });
      },
      updatePassword(){
        let username = this.userInfo().username
        this.$refs.userPassword.show(username)
      },
      updateCurrentDepart(){
        this.$refs.departSelect.show()
      },
      systemSetting(){
        this.$refs.settingDrawer.showDrawer()
      },
      searchMenus(arr,menus){
        for(let i of menus){
          if(!i.hidden && "layouts/RouteView"!==i.component){
           arr.push(i)
          }
          if(i.children&& i.children.length>0){
            this.searchMenus(arr,i.children)
          }
        }
      },
      filterOption(input, option) {
        return option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
      },
      searchMethods(value) {
        let route = this.searchMenuOptions.filter(item => item.id === value)[0]
        if (route.meta.internalOrExternal === true) {
          window.open(route.meta.url, '_blank')
        } else {
          if(route.component.includes('layouts/IframePageView')){
            this.$router.push(route)
          }else{
            this.$router.push({ path: route.path })
          }
        }
        this.searchMenuVisible = false
      },
      clearCache(){
        getAction("sys/dict/refleshCache").then((res) => {
          if (res.success) {
            //重新加载缓存
            getAction("sys/dict/queryAllDictItems").then((res) => {
              if (res.success) {
                Vue.ls.remove(UI_CACHE_DB_DICT_DATA)
                Vue.ls.set(UI_CACHE_DB_DICT_DATA, res.result, 7 * 24 * 60 * 60 * 1000)
              }
            })
            this.$message.success("Cache refresh complete！");
          }
        }).catch(e=>{
          this.$message.warn("Cache refresh failed！");
          console.log("Refresh the failure",e)
        })
      }
    }
  }
</script>

<style lang="less" scoped>
  .user-wrapper .search-input {
    width: 180px;
    color: inherit;

    /deep/ .ant-select-selection {
      background-color: inherit;
      border: 0;
      border-bottom: 1px solid white;
      &__placeholder, &__field__placeholder {
        color: inherit;
      }
    }
  }
</style>

<style scoped>
  .logout_title {
    color: inherit;
    text-decoration: none;
  }
</style>
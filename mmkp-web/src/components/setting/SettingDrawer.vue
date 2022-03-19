<template>
  <div class="setting-drawer">
    <a-drawer
      width="300"
      placement="right"
      :closable="false"
      @close="onClose"
      :visible="visible"
      style="height: 100%;overflow: auto;"
    >
      <div class="setting-drawer-index-content">

        <div :style="{ marginBottom: '24px' }">
          <h3 class="setting-drawer-index-title">Overall style setting</h3>

          <div class="setting-drawer-index-blockChecbox">
            <a-tooltip>
              <template slot="title">
                Dark menu style
              </template>
              <div class="setting-drawer-index-item" @click="handleMenuTheme('dark')">
                <img src="https://gw.alipayobjects.com/zos/rmsportal/LCkqqYNmvBEbokSDscrm.svg" alt="dark">
                <div class="setting-drawer-index-selectIcon" v-if="navTheme === 'dark'">
                  <a-icon type="check"/>
                </div>
              </div>
            </a-tooltip>

            <a-tooltip>
              <template slot="title">
                Bright menu style
              </template>
              <div class="setting-drawer-index-item" @click="handleMenuTheme('light')">
                <img src="https://gw.alipayobjects.com/zos/rmsportal/jpRkZQMyYRryryPNtyIC.svg" alt="light">
                <div class="setting-drawer-index-selectIcon" v-if="navTheme !== 'dark'">
                  <a-icon type="check"/>
                </div>
              </div>
            </a-tooltip>
          </div>
        </div>

        <div :style="{ marginBottom: '24px' }">
          <h3 class="setting-drawer-index-title">The theme color</h3>

          <div style="height: 20px">
            <a-tooltip class="setting-drawer-theme-color-colorBlock" v-for="(item, index) in colorList" :key="index">
              <template slot="title">
                {{ item.key }}
              </template>
              <a-tag :color="item.color" @click="changeColor(item.color)">
                <a-icon type="check" v-if="item.color === primaryColor"></a-icon>
              </a-tag>
            </a-tooltip>

          </div>
        </div>
        <a-divider />

        <div :style="{ marginBottom: '24px' }">
          <h3 class="setting-drawer-index-title">Navigation mode</h3>

          <div class="setting-drawer-index-blockChecbox">
            <a-tooltip>
              <template slot="title">
                Sidebar navigation
              </template>
              <div class="setting-drawer-index-item" @click="handleLayout('sidemenu')">
                <img src="https://gw.alipayobjects.com/zos/rmsportal/JopDzEhOqwOjeNTXkoje.svg" alt="sidemenu">
                <div class="setting-drawer-index-selectIcon" v-if="layoutMode === 'sidemenu'">
                  <a-icon type="check"/>
                </div>
              </div>
            </a-tooltip>

            <a-tooltip>
              <template slot="title">
                Top bar navigation
              </template>
              <div class="setting-drawer-index-item" @click="handleLayout('topmenu')">
                <img src="https://gw.alipayobjects.com/zos/rmsportal/KDNDBbriJhLwuqMoxcAr.svg" alt="topmenu">
                <div class="setting-drawer-index-selectIcon" v-if="layoutMode !== 'sidemenu'">
                  <a-icon type="check"/>
                </div>
              </div>
            </a-tooltip>
          </div>
          <div :style="{ marginTop: '24px' }">
            <a-list :split="false">
              <a-list-item>
                <a-tooltip slot="actions">
                  <template slot="title">
                    This setting is valid only for top bar navigation
                  </template>
                  <a-select size="small" style="width: 80px;" :defaultValue="contentWidth" @change="handleContentWidthChange">
                    <a-select-option value="Fixed">fixed</a-select-option>
                    <a-select-option value="Fluid" v-if="layoutMode !== 'sidemenu'">streaming</a-select-option>
                  </a-select>
                </a-tooltip>
                <a-list-item-meta>
                  <div slot="title">Content area width</div>
                </a-list-item-meta>
              </a-list-item>
              <a-list-item>
                <a-switch slot="actions" size="small" :defaultChecked="fixedHeader" @change="handleFixedHeader" />
                <a-list-item-meta>
                  <div slot="title"> Header</div>
                </a-list-item-meta>
              </a-list-item>
              <a-list-item>
                <a-switch slot="actions" size="small" :disabled="!fixedHeader" :defaultChecked="autoHideHeader" @change="handleFixedHeaderHidden" />
                <a-list-item-meta>
                  <div slot="title" :style="{ textDecoration: !fixedHeader ? 'line-through' : 'unset' }">hide Header</div>
                </a-list-item-meta>
              </a-list-item>
              <a-list-item>
                <a-switch slot="actions" size="small" :disabled="(layoutMode === 'topmenu')" :checked="dataFixSiderbar" @change="handleFixSiderbar" />
                <a-list-item-meta>
                  <div slot="title" :style="{ textDecoration: layoutMode === 'topmenu' ? 'line-through' : 'unset' }">Fixed side menu</div>
                </a-list-item-meta>
              </a-list-item>
            </a-list>
          </div>
        </div>
        <a-divider />

        <div :style="{ marginBottom: '24px' }">
          <h3 class="setting-drawer-index-title">Other Settings</h3>
          <div>
            <a-list :split="false">
              <a-list-item>
                <a-switch slot="actions" size="small" :defaultChecked="colorWeak" @change="onColorWeak" />
                <a-list-item-meta>
                  <div slot="title">Color blindness mode</div>
                </a-list-item-meta>
              </a-list-item>
              <a-list-item>
                <a-switch slot="actions" size="small" :defaultChecked="multipage" @change="onMultipageWeak" />
                <a-list-item-meta>
                  <div slot="title">Multi-tab mode</div>
                </a-list-item-meta>
              </a-list-item>
            </a-list>
          </div>
        </div>
        <a-divider />
        <div :style="{ marginBottom: '24px' }">
          <a-alert type="warning">
            <span slot="message">
              The configuration bar is used for preview only in the development environment and is not displayed in the production environment. Therefore, manually modify the configuration file
              <a href="https://github.com/sendya/ant-design-pro-vue/blob/master/src/defaultSettings.js" target="_blank">src/defaultSettings.js</a>
            </span>
          </a-alert>
        </div>
      </div>
      <div class="setting-drawer-index-handle" @click="toggle" v-if="visible">
<!--        <a-icon type="setting" v-if="!visible"/>-->
<!--        <a-icon type="close" v-else/>-->
        <a-icon type="close" />
      </div>
    </a-drawer>
  </div>
</template>

<script>
  import DetailList from '@/components/tools/DetailList'
  import SettingItem from '@/components/setting/SettingItem'
  import config from '@/defaultSettings'
  import { updateTheme, updateColorWeak, colorList } from '@/components/tools/setting'
  import { mixin, mixinDevice } from '@/utils/mixin.js'
  import { triggerWindowResizeEvent } from '@/utils/util'

  export default {
    components: {
      DetailList,
      SettingItem
    },
    mixins: [mixin, mixinDevice],
    data() {
      return {
        visible: false,
        colorList,
        dataFixSiderbar: false
    }
    },
    mounted () {
      if (this.primaryColor !== config.primaryColor) {
        updateTheme(this.primaryColor)
      }
      if (this.colorWeak !== config.colorWeak) {
        updateColorWeak(this.colorWeak)
      }
      if (this.multipage !== config.multipage) {
        this.$store.dispatch('ToggleMultipage', this.multipage)
      }
    },
    methods: {
      showDrawer() {
        this.visible = true
      },
      onClose() {
        this.visible = false
      },
      toggle() {
        this.visible = !this.visible
      },
      onColorWeak (checked) {
        this.$store.dispatch('ToggleWeak', checked)
        updateColorWeak(checked)
      },
      onMultipageWeak (checked) {
        this.$store.dispatch('ToggleMultipage', checked)
      },
      handleMenuTheme (theme) {
        this.$store.dispatch('ToggleTheme', theme)
      },
      handleLayout (mode) {
        this.$store.dispatch('ToggleLayoutMode', mode)
        this.handleFixSiderbar(false)
        triggerWindowResizeEvent()
      },
      handleContentWidthChange (type) {
        this.$store.dispatch('ToggleContentWidth', type)
      },
      changeColor (color) {
        if (this.primaryColor !== color) {
          this.$store.dispatch('ToggleColor', color)
          updateTheme(color)
        }
      },
      handleFixedHeader (fixed) {
        this.$store.dispatch('ToggleFixedHeader', fixed)
      },
      handleFixedHeaderHidden (autoHidden) {
        this.$store.dispatch('ToggleFixedHeaderHidden', autoHidden)
      },
      handleFixSiderbar (fixed) {
        if (this.layoutMode === 'topmenu') {
          fixed = false
        }
        this.dataFixSiderbar = fixed
        this.$store.dispatch('ToggleFixSiderbar', fixed)
      }
    },
  }
</script>

<style lang="less" scoped>

  .setting-drawer-index-content {

    .setting-drawer-index-blockChecbox {
      display: flex;

      .setting-drawer-index-item {
        margin-right: 16px;
        position: relative;
        border-radius: 4px;
        cursor: pointer;

        img {
          width: 48px;
        }

        .setting-drawer-index-selectIcon {
          position: absolute;
          top: 0;
          right: 0;
          width: 100%;
          padding-top: 15px;
          padding-left: 24px;
          height: 100%;
          color: #1890ff;
          font-size: 14px;
          font-weight: 700;
        }
      }
    }
    .setting-drawer-theme-color-colorBlock {
      width: 20px;
      height: 20px;
      border-radius: 2px;
      float: left;
      cursor: pointer;
      margin-right: 8px;
      padding-left: 0px;
      padding-right: 0px;
      text-align: center;
      color: #fff;
      font-weight: 700;

      i {
        font-size: 14px;
      }
    }
  }

  .setting-drawer-index-handle {
    position: absolute;
    top: 240px;
    background: #1890ff;
    width: 48px;
    height: 48px;
    right: 300px;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    pointer-events: auto;
    z-index: 1001;
    text-align: center;
    font-size: 16px;
    border-radius: 4px 0 0 4px;

    i {
      color: rgb(255, 255, 255);
      font-size: 20px;
    }
  }
</style>

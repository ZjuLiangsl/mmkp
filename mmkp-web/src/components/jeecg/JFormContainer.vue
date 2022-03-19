<template>
  <div :class="disabled?'jeecg-form-container-disabled':''">
    <fieldset :disabled="disabled">
      <slot name="detail"></slot>
    </fieldset>
    <slot name="edit"></slot>
    <fieldset disabled>
      <slot></slot>
    </fieldset>
  </div>
</template>

<script>

  export default {
    name: 'JFormContainer',
    props:{
      disabled:{
        type:Boolean,
        default:false,
        required:false
      }
    },
    mounted(){
      console.log("I'm a form disabling component, but I don't support disabling iframe content in forms")
    }
  }
</script>
<style>
  .jeecg-form-container-disabled{
    cursor: not-allowed;
  }
  .jeecg-form-container-disabled fieldset[disabled] {
    -ms-pointer-events: none;
    pointer-events: none;
  }
  .jeecg-form-container-disabled .ant-select{
    -ms-pointer-events: none;
    pointer-events: none;
  }

  .jeecg-form-container-disabled .ant-upload-select{display:none}
  .jeecg-form-container-disabled .ant-upload-list{cursor:grabbing}
  .jeecg-form-container-disabled fieldset[disabled] .ant-upload-list{
    -ms-pointer-events: auto !important;
    pointer-events: auto !important;
  }

  .jeecg-form-container-disabled .ant-upload-list-item-actions .anticon-delete,
  .jeecg-form-container-disabled .ant-upload-list-item .anticon-close{
    display: none;
  }
</style>
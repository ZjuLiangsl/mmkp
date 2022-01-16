/** init domain config */
import Vue from 'vue'
//设置全局API_BASE_URL
Vue.prototype.API_BASE_URL = process.env.VUE_APP_API_BASE_URL
window._CONFIG['domianURL'] = Vue.prototype.API_BASE_URL
window._CONFIG['staticDomainURL'] = Vue.prototype.API_BASE_URL + '/sys/common/static'
window._CONFIG['pdfDomainURL'] = Vue.prototype.API_BASE_URL + '/sys/common/pdf/pdfPreviewIframe'
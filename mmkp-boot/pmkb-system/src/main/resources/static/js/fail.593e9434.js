(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["fail"],{"0673":function(t,e,s){"use strict";s.r(e);var c=function(){var t=this,e=t.$createElement,s=t._self._c||e;return s("div",{staticClass:"exception"},[s("div",{staticClass:"img"},[s("img",{attrs:{src:t.config[t.type].img}})]),s("div",{staticClass:"content"},[s("h1",[t._v(t._s(t.config[t.type].title))]),s("div",{staticClass:"desc"},[t._v(t._s(t.config[t.type].desc))]),s("div",{staticClass:"action"},[s("a-button",{attrs:{type:"primary"},on:{click:t.handleToHome}},[t._v("To Home")])],1)])])},a=[],i={403:{img:"https://gw.alipayobjects.com/zos/rmsportal/wZcnGqRDyhPOEYFcZDnb.svg",title:"403",desc:"抱歉，你无权访问该页面"},404:{img:"https://gw.alipayobjects.com/zos/rmsportal/KpnpchXsobRgLElEozzI.svg",title:"404",desc:"抱歉，你访问的页面不存在或无权访问"},500:{img:"https://gw.alipayobjects.com/zos/rmsportal/RVRUAYdCGeYNBWoKiIwB.svg",title:"500",desc:"抱歉，服务器出错了"}},o=i,n={name:"Exception",props:{type:{type:String,default:"404"}},data:function(){return{config:o}},methods:{handleToHome:function(){this.$router.push({name:"dashboard"})}}},l=n,r=(s("d92e"),s("cba8")),p=Object(r["a"])(l,c,a,!1,null,"564b67a3",null);e["default"]=p.exports},"56db":function(t,e,s){},cc89:function(t,e,s){"use strict";s.r(e);var c=function(){var t=this,e=t.$createElement,s=t._self._c||e;return s("exception-page",{attrs:{type:"404"}})},a=[],i=s("0673"),o={components:{ExceptionPage:i["default"]}},n=o,l=s("cba8"),r=Object(l["a"])(n,c,a,!1,null,"0d99f44e",null);e["default"]=r.exports},d92e:function(t,e,s){"use strict";s("56db")}}]);
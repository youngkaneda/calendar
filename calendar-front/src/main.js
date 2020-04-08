import Vue from "vue"
import App from "./App.vue"
import VueMask from "v-mask"
import VueToast from "vue-toast-notification"
import "vue-toast-notification/dist/theme-sugar.css"

Vue.use(VueMask)
Vue.use(VueToast, {
	position: "top-right",	
})

Vue.config.productionTip = false

new Vue({
	render: h => h(App),
}).$mount("#app")

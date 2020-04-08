<template>
	<div id="app">
		<div class="calendar-controls">
			<transition name="fade">
				<div v-if="message" class="notification is-success">{{ message }}</div>
			</transition>
			<div class="box">
				<div class="field">
					<label class="label">Title</label>
					<div class="control">
						<input v-model="newItemTitle" class="input" type="text" />
					</div>
				</div>

				<div class="field">
					<label class="label">Start date</label>
					<div class="control">
						<input v-model="newItemStartDate" v-mask="'##/##/#### ##:##:##'" class="input" placeholder="dd/mm/yyyy hh:mm:ss" />
					</div>
				</div>

				<div class="field">
					<label class="label">End date</label>
					<div class="control">
						<input v-model="newItemEndDate" v-mask="'##/##/#### ##:##:##'" class="input" placeholder="dd/mm/yyyy hh:mm:ss" />
					</div>
				</div>

				<button class="button is-info" @click="clickTestAddItem">
					Add Item
				</button>
			</div>
		</div>
		<div class="calendar-parent">
			<calendar-view
				:events="items"
				:show-date="showDate"
				:time-format-options="{ hour: 'numeric', minute: '2-digit', hour12: false }"
				:enable-drag-drop="true"
				:disable-past="disablePast"
				:disable-future="disableFuture"
				:show-event-times="showEventTimes"
				:display-period-uom="displayPeriodUom"
				:display-period-count="displayPeriodCount"
				:starting-day-of-week="startingDayOfWeek"
				:class="themeClasses"
				:period-changed-callback="periodChanged"
				:current-period-label="useTodayIcons ? 'icons' : ''"
				@drop-on-date="onDrop"
				@click-date="onClickDay"
				@click-event="onClickItem"
			>
				<calendar-view-header
					slot="header"
					slot-scope="{ headerProps }"
					:header-props="headerProps"
					@input="setShowDate"
				/>
			</calendar-view>
		</div>
		<div class="modal is-active" id="timeZoneModal">
			<div class="modal-background"/>
			<div class="modal-card">
				<section class="modal-card-body">
					<div class="content">
						<h3>Set your timezone please.</h3>
						<div class="field">
							<div class="control">
								<input v-model="timeZone" class="input is-primary" type="text" placeholder="eg.: America/Fortaleza"/>
							</div>
						</div>
					</div>
				</section>
				<footer class="modal-card-foot">
					<button class="button is-success" @click="selectTimeZoneAndConnectToWS">Confirm</button>
				</footer>
			</div>
		</div>
	</div>
</template>
<script>
require("vue-simple-calendar/static/css/default.css")
require("vue-simple-calendar/static/css/holidays-us.css")

import {
	CalendarView,
	CalendarViewHeader,
	CalendarMathMixin,
} from "vue-simple-calendar"
import moment from "moment-timezone"

export default {
	name: "App",
	components: {
		CalendarView,
		CalendarViewHeader,
	},
	mixins: [CalendarMathMixin],
	data() {
		return {
			showDate: this.thisMonth(1),
			message: "",
			startingDayOfWeek: 0,
			disablePast: false,
			disableFuture: false,
			displayPeriodUom: "month",
			displayPeriodCount: 1,
			showEventTimes: true,
			newItemTitle: "",
			newItemStartDate: "",
			newItemEndDate: "",
			useDefaultTheme: true,
			useHolidayTheme: true,
			useTodayIcons: false,
			items: [],
			timeZone: "",
			socket: null,
		}
	},
	computed: {
		userLocale() {
			return this.getDefaultBrowserLocale
		},
		dayNames() {
			return this.getFormattedWeekdayNames(this.userLocale, "long", 0)
		},
		themeClasses() {
			return {
				"theme-default": this.useDefaultTheme,
				"holiday-us-traditional": this.useHolidayTheme,
				"holiday-us-official": this.useHolidayTheme,
			}
		},
	},
	mounted() {
		this.newItemStartDate = moment(this.today()).format("DD/MM/YYYY")
		this.newItemEndDate = moment(this.today()).format("DD/MM/YYYY")
	},

	methods: {
		selectTimeZoneAndConnectToWS() {
			if (moment.tz.names().filter(e => e === this.timeZone).length > 0) {
				window.localStorage.setItem("timeZone", this.timeZone)
				var div = document.getElementById("timeZoneModal")
				div.classList.remove("is-active")
				this.createWebSocketConnection()
			} else {
				alert("Invalid time zone.")
			}
		},
		createWebSocketConnection() {
			this.socket = new WebSocket("ws://localhost:8080/calendar-service/event?timezone=" + window.localStorage.getItem("timeZone"))
			this.socket.onmessage = (message) => {
				var msg = JSON.parse(message.data)
				if (msg.op) {
					this.$toast.success(msg.reminder)
				} else {
					this.items = msg.events.map(e => {
						return {
							title: e.title,
							startDate: moment(e.beginAt, "DD/MM/YYYY HH:mm:ss"),
							endDate: moment(e.endAt, "DD/MM/YYYY HH:mm:ss"),
							id: e.id,
						}
					})
				}
			}
		},
		periodChanged() {
		},
		thisMonth(d, h, m) {
			const t = new Date()
			return new Date(t.getFullYear(), t.getMonth(), d, h || 0, m || 0)
		},
		onClickDay(d) {
			this.message = `You clicked: ${moment(d).format("DD/MM/YYYY")}`
		},
		onClickItem(e) {
			this.message = `You clicked: ${e.title}`
		},
		setShowDate(d) {
			this.message = `Changing calendar view to ${moment(d).format("DD/MM/YYYY")}`
			this.showDate = d
		},
		onDrop(item, date) {
			this.message = `You dropped ${item.id} on ${moment(d).format("DD/MM/YYYY")}`
			const eLength = this.dayDiff(item.startDate, date)
			item.originalEvent.startDate = this.addDays(item.startDate, eLength)
			item.originalEvent.endDate = this.addDays(item.endDate, eLength)
		},
		clickTestAddItem() {
			var item = {
				startDate: moment(this.newItemStartDate, "DD/MM/YYYY HH:mm:ss"),
				endDate: moment(this.newItemEndDate, "DD/MM/YYYY HH:mm:ss"),
				title: this.newItemTitle,
				id:
					"e" +
					Math.random()
						.toString(36)
						.substr(2, 10),
			}
			this.items.push(item)
			this.message = "You added a calendar item!"
			if (this.socket.OPEN) {
				var msg = {
					title: item.title,
					beginAt: item.startDate.format("DD/MM/YYYY HH:mm:ss"),
					endAt: item.endDate.format("DD/MM/YYYY HH:mm:ss"),
					timeZone: window.localStorage.getItem("timeZone"),
				}
				this.socket.send(JSON.stringify(msg))
			}
		},
	},
}
</script>

<style>
html,
body {
	height: 100%;
	margin: 0;
	background-color: #f7fcff;
}

#app {
	display: flex;
	flex-direction: row;
	font-family: Calibri, sans-serif;
	width: 95vw;
	min-width: 30rem;
	max-width: 100rem;
	min-height: 40rem;
	margin-left: auto;
	margin-right: auto;
}

.calendar-controls {
	margin-right: 1rem;
	min-width: 14rem;
	max-width: 14rem;
}

.calendar-parent {
	display: flex;
	flex-direction: column;
	flex-grow: 1;
	overflow-x: hidden;
	overflow-y: hidden;
	max-height: 80vh;
	background-color: white;
}

/* For long calendars, ensure each week gets sufficient height. The body of the calendar will scroll if needed */
.cv-wrapper.period-month.periodCount-2 .cv-week,
.cv-wrapper.period-month.periodCount-3 .cv-week,
.cv-wrapper.period-year .cv-week {
	min-height: 6rem;
}

/* These styles are optional, to illustrate the flexbility of styling the calendar purely with CSS. */

/* Add some styling for items tagged with the "birthday" class */
.theme-default .cv-event.birthday {
	background-color: #e0f0e0;
	border-color: #d7e7d7;
}

.theme-default .cv-event.birthday::before {
	content: "\1F382"; /* Birthday cake */
	margin-right: 0.5em;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity .5s;
}
.fade-enter, .fade-leave-to /* .fade-leave-active em versões anteriores a 2.1.8 */ {
  opacity: 0;
}
</style>

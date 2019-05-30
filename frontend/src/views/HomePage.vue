<template>
  <div :class="[multipage === true ? 'multi-page':'single-page', 'not-menu-page', 'home-page']">
    <a-row :gutter="8" class="head-info">
      <a-card class="head-info-card">
        <a-col :span="12">
          <div class="head-info-avatar">
            <img alt="头像" :src="avatar">
          </div>
          <div class="head-info-count">
            <div class="head-info-welcome">
              {{welcomeMessage}}
            </div>
            <div class="head-info-desc">
              <p>{{user.deptName ? user.deptName : '暂无部门'}} | {{user.roleName ? user.roleName : '暂无角色'}}</p>
            </div>
            <div class="head-info-time">上次登录时间：{{user ? user.lastLoginTime : '第一次访问系统'}}</div>
          </div>
        </a-col>
        <a-col :span="12">
          <div>
            <a-row class="more-info">
              <a-col :span="4"></a-col>
              <a-col :span="4"></a-col>
              <a-col :span="4"></a-col>
              <a-col :span="4">
                <head-info title="今日IP" :content="todayIp" :center="false" :bordered="false"/>
              </a-col>
              <a-col :span="4">
                <head-info title="今日访问" :content="todayVisitCount" :center="false" :bordered="false"/>
              </a-col>
              <a-col :span="4">
                <head-info title="总访问量" :content="totalVisitCount" :center="false" />
              </a-col>
            </a-row>
          </div>
        </a-col>
      </a-card>
    </a-row>
    <a-row :gutter="8" class="count-info">
      <a-col :span="12" class="visit-count-wrapper">
        <a-card class="visit-count">
          <apexchart ref="count" type=bar height=300 :options="chartOptions" :series="series" />
        </a-card>
      </a-col>
      <a-col :span="12" class="project-wrapper">
        <a-card title="今日天气" class="project-card">
          <div class="global-search-main">
            <div class="global-search-wrapper">
              <a-auto-complete
                :dataSource="dataSource"
                class="global-search"
                @select="onSelect"
                @search="handleSearch"
                placeholder="   请输入城市名">
                <a-input>
                  <a-button slot="suffix" class="search-btn" type="primary" @click.stop="searchWeather">
                    <a-icon type="search"/>
                  </a-button>
                  <a-button slot="suffix" class="search-btn switch_btn" type="primary" @click.stop="switchchart">
                    <a-icon type="swap"/>
                  </a-button>
                </a-input>
              </a-auto-complete>
            </div>
          </div>
          <div class="weather-area" v-show="loading">
            <div class="weather-chart-info" v-show="switch_tag">
              <apexchart ref="seven" height="257.5" type=line :options="seven.chartOptions" :series="seven.series" />
            </div>
            <div class="weather-chart-info" v-show="!switch_tag">
              <apexchart ref="future" height="257.5" type=area :options="future.chartOptions" :series="future.series" />
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>
<script>
import HeadInfo from '@/views/common/HeadInfo'
import {mapState} from 'vuex'
import moment from 'moment'
import axios from 'axios'
moment.locale('zh-cn')

export default {
  name: 'HomePage',
  components: {HeadInfo},
  data () {
    return {
      loading: true,
      switch_tag: true,
      seven: {
        series: [],
        chartOptions: {
          chart: {
            shadow: {
              enabled: true,
              color: '#000',
              top: 18,
              left: 7,
              blur: 10,
              opacity: 1
            },
            toolbar: {
              show: false
            }
          },
          colors: ['#f5564e', '#35d0ba'],
          dataLabels: {
            enabled: true
          },
          stroke: {
            curve: 'smooth'
          },
          markers: {
            size: 4
          },
          xaxis: {},
          yaxis: {}
        }
      },
      future: {
        series: [],
        chartOptions: {
          chart: {
            toolbar: {
              show: false
            }
          },
          dataLabels: {
            enabled: false
          },
          stroke: {
            curve: 'smooth'
          },
          xaxis: {}
        }
      },
      series: [],
      chartOptions: {
        chart: {
          toolbar: {
            show: false
          }
        },
        plotOptions: {
          bar: {
            horizontal: false,
            columnWidth: '35%'
          }
        },
        dataLabels: {
          enabled: false
        },
        stroke: {
          show: true,
          width: 2,
          colors: ['transparent']
        },
        xaxis: {
          categories: []
        },
        fill: {
          opacity: 1

        }
      },
      todayIp: '',
      todayVisitCount: '',
      totalVisitCount: '',
      userRole: '',
      userDept: '',
      lastLoginTime: '',
      welcomeMessage: '',
      dataSource: [],
      storage: [],
      citys: [],
      areaId: '101210101',
      weather: {
        provinceName: '',
        countyName: '',
        weathers: [],
        day_c: [],
        night_c: [],
        hours_c: [],
        dateArr: [],
        timeArr: [],
        publishTime: '',
        alarms: [],
        realtime: {},
        indexes: []
      }
    }
  },
  computed: {
    ...mapState({
      multipage: state => state.setting.multipage,
      user: state => state.account.user
    }),
    avatar () {
      return `static/avatar/${this.user.avatar}`
    }
  },
  methods: {
    welcome () {
      const date = new Date()
      const hour = date.getHours()
      let time = hour < 6 ? '早上好' : (hour <= 11 ? '上午好' : (hour <= 13 ? '中午好' : (hour <= 18 ? '下午好' : '晚上好')))
      let welcomeArr = [
        '喝杯咖啡休息下吧☕',
        '几天没见又更好看了呢😍',
        '记得要劳逸结合哦',
        '今天要完成什么工作呢',
        '今天吃了什么好吃的呢',
        '今天您微笑了吗😊',
        '今天帮助别人解决问题了吗',
        '准备吃些什么呢',
        '周末要不要去看电影？'
      ]
      let index = Math.floor((Math.random() * welcomeArr.length))
      return `${time}，${this.user.username}，${welcomeArr[index]}`
    },
    handleSearch (value) {
      this.dataSource = []
      this.storage = []
      this.areaId = ''
      if (!value) {
        return
      }
      for (let i = 0; i < this.citys.length; i++) {
        let currentCity = this.citys[i]
        if (currentCity.countyname.indexOf(value) !== -1) {
          this.dataSource.push(currentCity.countyname)
          this.storage.push(currentCity.areaid)
        }
      }
    },
    onSelect (value) {
      let index = this.dataSource.indexOf(value)
      this.areaId = this.storage[index]
    },
    switchchart () {
      this.loading = true
      this.switch_tag = !this.switch_tag
      console.log(this.areaId)
    },
    searchWeather () {
      if (!this.areaId) {
        this.$message.warning('请选择城市')
      } else {
        this.$get('weather?areaId=' + this.areaId).then((r) => {
          let data = JSON.parse(r.data.data)
          if (data.code === '200') {
            this.weather = {
              countyName: '',
              weathers: [],
              day_c: [],
              night_c: [],
              hours_c: [],
              dateArr: [],
              timeArr: [],
              publishTime: ''
            }
            this.loading = true
            this.weather.provinceName = data.value[0].provinceName
            this.weather.countyName = data.value[0].city
            this.weather.weathers = data.value[0].weathers
            this.weather.alarms = data.value[0].alarms
            this.weather.realtime = data.value[0].realtime
            this.weather.indexes = data.value[0].indexes
            let weathers = this.weather.weathers
            let min = 0
            let max = 0
            for (let i = 0; i < weathers.length; i++) {
              if (i === weathers.length - 1) {
                this.weather.day_c.unshift(parseFloat(weathers[i].temp_day_c))
                this.weather.night_c.unshift(parseFloat(weathers[i].temp_night_c))
                this.weather.dateArr.unshift(weathers[i].date.split('-')[1] + '-' + weathers[i].date.split('-')[2])
              } else {
                this.weather.day_c.push(parseFloat(weathers[i].temp_day_c))
                this.weather.night_c.push(parseFloat(weathers[i].temp_night_c))
                this.weather.dateArr.push(weathers[i].date.split('-')[1] + '-' + weathers[i].date.split('-')[2])
              }
              if (i === 0) {
                max = this.weather.day_c[0]
                min = this.weather.night_c[0]
              } else {
                if (this.weather.day_c[i] > max) {
                  max = this.weather.day_c[i]
                }
                if (this.weather.night_c[i] < min) {
                  min = this.weather.night_c[i]
                }
              }
            }
            let weather3HoursDetailsInfos = data.value[0].weatherDetailsInfo.weather3HoursDetailsInfos
            this.weather.publishTime = data.value[0].weatherDetailsInfo.publishTime
            for (let i = 0; i < weather3HoursDetailsInfos.length; i++) {
              let time = weather3HoursDetailsInfos[i].endTime.split(' ')[1].split(':')
              this.weather.hours_c.push(parseFloat(weather3HoursDetailsInfos[i].highestTemperature))
              this.weather.timeArr.push(time[0] + ':' + time[1])
            }
            this.$refs.seven.updateSeries([
              {
                name: '最高温',
                data: this.weather.day_c
              },
              {
                name: '最低温',
                data: this.weather.night_c
              }
            ], true)
            this.$refs.future.updateSeries([
              {
                name: '未来气温',
                data: this.weather.hours_c
              }
            ])
            this.$refs.seven.updateOptions({
              xaxis: {
                categories: this.weather.dateArr
              },
              yaxis: {
                min: min - 5,
                max: max + 5
              },
              title: {
                text: `${this.weather.provinceName} - ${this.weather.countyName}未来七日气温`,
                align: 'center'
              }
            }, true, true)
            this.$refs.future.updateOptions({
              xaxis: {
                categories: this.weather.timeArr
              },
              title: {
                text: `${this.weather.provinceName} - ${this.weather.countyName}未来气温细节`,
                align: 'center'
              }
            }, true, true)
          }
        }).catch((r) => {
          // console.error(this.switch_tag)
          // this.$message.error('天气查询失败')
        })
      }
    }
  },
  mounted () {
    this.welcomeMessage = this.welcome()
    this.$get(`index/${this.user.username}`).then((r) => {
      let data = r.data.data
      this.todayIp = data.todayIp
      this.todayVisitCount = data.todayVisitCount
      this.totalVisitCount = data.totalVisitCount
      let sevenVisitCount = []
      let dateArr = []
      for (let i = 6; i >= 0; i--) {
        let time = moment().subtract(i, 'days').format('MM-DD')
        let contain = false
        for (let o of data.lastSevenVisitCount) {
          if (o.days === time) {
            contain = true
            sevenVisitCount.push(o.count)
          }
        }
        if (!contain) {
          sevenVisitCount.push(0)
        }
        dateArr.push(time)
      }
      let sevenUserVistCount = []
      for (let i = 6; i >= 0; i--) {
        let time = moment().subtract(i, 'days').format('MM-DD')
        let contain = false
        for (let o of data.lastSevenUserVisitCount) {
          if (o.days === time) {
            contain = true
            sevenUserVistCount.push(o.count)
          }
        }
        if (!contain) {
          sevenUserVistCount.push(0)
        }
      }
      this.$refs.count.updateSeries([
        {
          name: '您',
          data: sevenUserVistCount
        },
        {
          name: '总数',
          data: sevenVisitCount
        }
      ], true)
      this.$refs.count.updateOptions({
        xaxis: {
          categories: dateArr
        },
        title: {
          text: '近七日系统访问记录',
          align: 'left'
        }
      }, true, true)
    }).catch((r) => {
      // console.error(r)
      this.$message.error('获取首页信息失败')
    })
    axios.get('../../../static/file/city.json').then((r) => {
      this.citys = r.data
    })
    this.searchWeather()
  }
}
</script>
<style lang="less">
  .home-page {
    .head-info {
      margin-bottom: .5rem;
      .head-info-card {
        padding: .5rem;
        border-color: #f1f1f1;
        .head-info-avatar {
          display: inline-block;
          float: left;
          margin-right: 1rem;
          img {
            width: 5rem;
            border-radius: 2px;
          }
        }
        .head-info-count {
          display: inline-block;
          float: left;
          .head-info-welcome {
            font-size: 1.05rem;
            margin-bottom: .1rem;
          }
          .head-info-desc {
            color: rgba(0, 0, 0, 0.45);
            font-size: .8rem;
            padding: .2rem 0;
            p {
              margin-bottom: 0;
            }
          }
          .head-info-time {
            color: rgba(0, 0, 0, 0.45);
            font-size: .8rem;
            padding: .2rem 0;
          }
        }
      }
    }
    .count-info {
      .visit-count-wrapper {
        padding-left: 0 !important;
        .visit-count {
          padding: .5rem;
          border-color: #f1f1f1;
          .ant-card-body {
            padding: .5rem 1rem !important;
          }
        }
      }
      .project-wrapper {
        padding-right: 0 !important;
        .project-card {
          border: none !important;
          .ant-card-head {
            border-left: 1px solid #f1f1f1 !important;
            border-top: 1px solid #f1f1f1 !important;
            border-right: 1px solid #f1f1f1 !important;
          }
          .ant-card-body {
            padding: 0 !important;
            table {
              width: 100%;
              td {
                width: 50%;
                border: 1px solid #f1f1f1;
                padding: .6rem;
                .project-avatar-wrapper {
                  display:inline-block;
                  float:left;
                  margin-right:.7rem;
                  .project-avatar {
                    color: #42b983;
                    background-color: #d6f8b8;
                  }
                }
              }
            }
          }
          .project-detail {
            display:inline-block;
            float:left;
            text-align:left;
            width: 78%;
            .project-name {
              font-size:.9rem;
              margin-top:-2px;
              font-weight:600;
            }
            .project-desc {
              color:rgba(0, 0, 0, 0.45);
              p {
                margin-bottom:0;
                font-size:.6rem;
                white-space:normal;
              }
            }
          }
        }
      }
    }
  }
  .global-search-main {
    /*margin-bottom: 2.5rem;*/
    .global-search-wrapper {
      width: 300px;
      margin:0 auto;
      .global-search {
        width: 100%;
      }
    }
  }
  .weather-area {
    display: inline;
    .weather-chart-info {
      width: 100%;
      display:inline-block;
    }
    .weather-info {
      margin: .5rem 0;
      width: 100%;
      display: inline-block;
      p {
        margin-bottom: .4rem !important;
      }
    }
  }
  .global-search.ant-select-auto-complete {
    .ant-select-selection--single {
      margin-right: -46px;
    }
    .ant-input-affix-wrapper {
      .ant-input:not(:last-child) {
        padding-right: 62px;
      }
      .ant-input-suffix {
        right: 10px;
      }
      .ant-input-suffix button {
        border-top-left-radius: 0;
        border-bottom-left-radius: 0;
      }
      /*.ant-input-prefix {*/
      /*  left: -47.25px;*/
      /*}*/
      /*.ant-input-prefix button {*/
      /*  border-top-right-radius: 0;*/
      /*  border-bottom-right-radius: 0;*/
      /*}*/
      .switch_btn {
        right: -10px;
      }
    }
  }
</style>

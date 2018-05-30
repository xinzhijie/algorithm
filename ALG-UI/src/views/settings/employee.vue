<template>
  <div class="index" v-loading.fullscreen.lock="fullscreenLoading" element-loading-text="拼命加载中...">
    <div>
    <input type="file" @change="importFile(this)" id="imFile" style="display: none"
           accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"/>
    <a id="downlink"></a>
    <el-button class="button" @click="uploadFile('train')">训练数据集导入</el-button>
    <el-button class="button" @click="uploadFile('test')">测试数据集导入</el-button>
    <!--<el-button class="button" @click="uploadFile('jpg')">测井图像导入</el-button>-->
    <el-button type="primary" @click="run()">确定</el-button>
    </div>
    <!--错误信息提示-->
    <el-dialog title="提示" v-model="errorDialog" size="tiny">
      <span>{{errorMsg}}</span>
      <span slot="footer" class="dialog-footer">
    <el-button type="primary" @click="errorDialog=false">确认</el-button>
  </span>
    </el-dialog>
    <div style="float: left;  width: 40%">
      <el-card class="box-card">
        <div  class="text item">
          训练数据集导入:{{ trainingData.length>0?' 完成':' 未完成' }}&nbsp;&nbsp;
          <el-button v-if="trainingDataShow" class="button"  @click="showTrain(trainingData)">展示</el-button>
        </div>
        <div  class="text item">
          测试数据集导入:{{ testData.length>0?' 完成':' 未完成' }}&nbsp;&nbsp;
          <el-button v-if="testDataShow" class="button" @click="showTrain(testData)">展示</el-button>
        </div>
        <!--<div  class="text item">-->
          <!--&nbsp;&nbsp;&nbsp;&nbsp;测井图像导入:{{ loggingImage>0?' 完成':' 未完成' }}-->
        <!--</div>-->
        <div  class="text item">
          &nbsp;&nbsp;&nbsp;&nbsp;测试结果:
          <el-button class="button" v-if="resultShow" @click="downloadFile(resultData)">导出
          </el-button>
          <el-button v-if="resultShow" class="button" @click="showTrain(resultData)">展示</el-button>
        </div>
      </el-card>
    </div>
    <div style="float: right; width: 50%">
      <el-table
        height="250"
        :data="tableData"
        style="width: 100%">
        <div v-for="item in columns">
        <el-table-column
          :prop="item"
          :label="item"
          width="180">
        </el-table-column>
        </div>
      </el-table>
    </div>

  </div>
</template>

<script>
  import Vue from 'vue'
  // 引入xlsx
  var XLSX = require('xlsx')
  export default {
    components: {
    },
    name: 'Index',
    data () {
      return {
        trainingDataShow: false,
        testDataShow: false,
        tableData: [],
        columns: [],
        resultShow: false,
        result: [],
        resultData: [],
        trainingData: [],
        testData: [],
        loggingImage: '',
        fullscreenLoading: false, // 加载中
        imFile: '', // 导入文件el
        outFile: '',  // 导出文件el
        errorDialog: false, // 错误信息弹窗
        errorMsg: '', // 错误信息内容
        butt: ''
      }
    },
    mounted () {
      this.imFile = document.getElementById('imFile')
      this.outFile = document.getElementById('downlink')
    },
    methods: {
      showTrain (temp) {
        this.columns = []
        for (var key in temp[0]) {
          this.columns.push(key)
        }
        this.tableData = temp
      },
      run () {
        if (this.trainingData.length > 0 && this.testData.length > 0) {
          const postCfg = {
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
          }
          var params = {
            'trainingData': this.trainingData,
            'testData': this.testData
          }
          this.$http.post('/api/alg/run/knn', JSON.stringify(params), postCfg).then(res => {
            this.resultData = JSON.parse(JSON.stringify(this.testData))
            const result = res.body
            for (let i = 0; i < result.length; i++) {
              this.resultData[i]['z-alg-result'] = result[i]
            }
            this.resultShow = true
          })
        } else {
          this.$notify.error({
            title: '错误',
            message: '请导入训练集'
          })
        }

      },
      uploadFile: function (res) { // 点击导入按钮
        this.butt = res
        this.imFile.click()
      },
      downloadFile: function (rs) { // 点击导出按钮
        let data = [{}]
        for (let k in rs[0]) {
          data[0][k] = k
        }
        data = data.concat(rs)
        this.downloadExl(data, 'result')
      },
      importFile () { // 导入excel
        this.fullscreenLoading = true
        let obj = this.imFile
        if (!obj.files) {
          this.fullscreenLoading = false
          return
        }
        var f = obj.files[0]
        var reader = new FileReader()
        let $t = this
        reader.onload = function (e) {
          var data = e.target.result
          if ($t.rABS) {
            $t.wb = XLSX.read(btoa(this.fixdata(data)), {  // 手动转化
              type: 'base64'
            })
          } else {
            $t.wb = XLSX.read(data, {
              type: 'binary'
            })
          }
          let json = XLSX.utils.sheet_to_json($t.wb.Sheets[$t.wb.SheetNames[0]])
          $t.dealFile($t.analyzeData(json)) // analyzeData: 解析导入数据
        }
        if (this.rABS) {
          reader.readAsArrayBuffer(f)
        } else {
          reader.readAsBinaryString(f)
        }
      },
      downloadExl: function (json, downName, type) {  // 导出到excel
        let keyMap = [] // 获取键
        for (let k in json[0]) {
          keyMap.push(k)
        }
        let tmpdata = [] // 用来保存转换好的json
        json.map((v, i) => keyMap.map((k, j) => Object.assign({}, {
          v: v[k],
          position: (j > 25 ? this.getCharCol(j) : String.fromCharCode(65 + j)) + (i + 1)
        }))).reduce((prev, next) => prev.concat(next)).forEach(function (v) {
          tmpdata[v.position] = {
            v: v.v
          }
        })
        let outputPos = Object.keys(tmpdata)  // 设置区域,比如表格从A1到D10
        let tmpWB = {
          SheetNames: ['mySheet'], // 保存的表标题
          Sheets: {
            'mySheet': Object.assign({},
              tmpdata, // 内容
              {
                '!ref': outputPos[0] + ':' + outputPos[outputPos.length - 1] // 设置填充区域
              })
          }
        }
        let tmpDown = new Blob([this.s2ab(XLSX.write(tmpWB,
          {bookType: (type === undefined ? 'xlsx' : type), bookSST: false, type: 'binary'} // 这里的数据是用来定义导出的格式类型
        ))], {
          type: ''
        })  // 创建二进制对象写入转换好的字节流
        var href = URL.createObjectURL(tmpDown)  // 创建对象超链接
        this.outFile.download = downName + '.xlsx'  // 下载名称
        this.outFile.href = href  // 绑定a标签
        this.outFile.click()  // 模拟点击实现下载
        setTimeout(function () {  // 延时释放
          URL.revokeObjectURL(tmpDown) // 用URL.revokeObjectURL()来释放这个object URL
        }, 100)
      },
      analyzeData: function (data) {  // 此处可以解析导入数据
        return data
      },
      dealFile: function (data) {   // 处理导入的数据
        this.imFile.value = ''
        this.fullscreenLoading = false
        if (data.length <= 0) {
          this.errorDialog = true
          this.errorMsg = '请导入正确信息'
        } else {
          if (this.butt === 'train') {
            this.trainingData = data
            this.trainingDataShow = true
          } else if (this.butt === 'test') {
            this.testData = data
            this.testDataShow = true
          } else if (this.butt === 'jpg') {
            this.loggingImage = data
          }
          this.resultShow = false
        }
      },
      s2ab: function (s) { // 字符串转字符流
        var buf = new ArrayBuffer(s.length)
        var view = new Uint8Array(buf)
        for (var i = 0; i !== s.length; ++i) {
          view[i] = s.charCodeAt(i) & 0xFF
        }
        return buf
      },
      getCharCol: function (n) { // 将指定的自然数转换为26进制表示。映射关系：[0-25] -> [A-Z]。
        let s = ''
        let m = 0
        while (n > 0) {
          m = n % 26 + 1
          s = String.fromCharCode(m + 64) + s
          n = (n - m) / 26
        }
        return s
      },
      fixdata: function (data) {  // 文件流转BinaryString
        var o = ''
        var l = 0
        var w = 10240
        for (; l < data.byteLength / w; ++l) {
          o += String.fromCharCode.apply(null, new Uint8Array(data.slice(l * w, l * w + w)))
        }
        o += String.fromCharCode.apply(null, new Uint8Array(data.slice(l * w)))
        return o
      }
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style>
  .el-table th>.cell {
    text-align: center;
  }
  .button {
    margin-bottom: 20px;
  }
   .text {
     font-size: 14px;
   }

  .item {
    margin-bottom: 18px;
  }

  .clearfix:before,
  .clearfix:after {
    display: table;
    content: "";
  }
  .clearfix:after {
    clear: both
  }

  .box-card {
    width: 480px;
  }

</style>

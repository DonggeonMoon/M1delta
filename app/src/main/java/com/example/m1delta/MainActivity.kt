package com.example.m1delta

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import kotlin.concurrent.thread
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.sql.Timestamp
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate.parse
import java.time.LocalDateTime.parse
import java.util.*
import java.util.Date.parse
import java.util.logging.Level.parse

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var handler = object : Handler() {
            var result = mutableMapOf<String, String>()
            var date = ""
            var value = ""
            var resultList = mutableListOf<MutableList<String>>()
            var len: Int = 0
        }

        thread(start = true) {
            var url =
                URL("https://api.stlouisfed.org/fred/series/observations?series_id=WM1NS&api_key=c4fd1324fdd7f20089f6930d9d4c7da8&file_type=json")
            var conn = url.openConnection()
            var input = conn.getInputStream()
            var isr = InputStreamReader(input)
            var br = BufferedReader(isr)
            var str: String? = null
            var sb = StringBuffer()
            do {
                str = br.readLine()
                if (str != null) {
                    sb.append(str)
                }
            } while (str != null)
            var jsonObject = JSONObject(sb.toString())
            var jsonArray = jsonObject.getJSONArray("observations")
            var len = jsonArray.length()
            handler.len = len

            for (i in (len-200)..(len-1)) {
                var date = jsonArray.getJSONObject(i).getString("date")
                var value = jsonArray.getJSONObject(i).getString("value")
                handler.result[date] = value
                var list = mutableListOf<String>(date, value)
                handler.resultList.add(list)
            }
        }

        var lineChart:LineChart = chart
        var chartData = LineData()
        var entry_chart = arrayListOf<Entry>()
        println("시스템")
        for ((k, v) in handler.result) {
            entry_chart.add(Entry(1.0F, 1.0F))
        }
        var lineDataSet = LineDataSet(entry_chart, "M1")
        chartData.addDataSet(lineDataSet);
        lineChart.setData(chartData);
        lineChart.invalidate();

        btn1.setOnClickListener {
            var d: Double = 0.0000001
            var v: Double = 0.0000001
            for ((k, v) in handler.result) {
                var ratio: Double = (v.toDouble() - d) / d
                txtResult.append("$k $v $ratio \n")
                d = v.toDouble()
            }
            txtResult.append(handler.len.toString())
        }

        btn2.setOnClickListener {
            var lineChart:LineChart = chart
            var chartData = LineData()
            var entry_chart = arrayListOf<Entry>()

            var y_prev = 0.0F
            for (i in handler.resultList) {
                var x = handler.resultList.indexOf(i).toFloat()
                var date = i[0]
                var y = i[1].toFloat()
                if (x == 0.0F) {
                    y_prev =y
                    continue
                }
                var ratio = (y-y_prev)/y_prev *100
                entry_chart.add(Entry(x, ratio))
                println("$x, $y, $ratio")
                y_prev = y
            }

            var lineDataSet = LineDataSet(entry_chart, "M1")
            chartData.addDataSet(lineDataSet);
            lineChart.setData(chartData);
            lineChart.invalidate();
        }
    }
}
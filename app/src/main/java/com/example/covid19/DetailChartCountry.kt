
package com.example.covid19

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.example.covid19.model.CountriesItem
import com.example.covid19.model.InfoNegara
import com.example.covid19.network.InfoService
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.android.synthetic.main.activity_detail_chart_country.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class DetailChartCountry : AppCompatActivity() {

    companion object{
        const val extra_country = "Extra_country"
        lateinit var simpanDataNegaraAtas : String
        lateinit var simpanDataFlagAtas : String
    }

    //untuk membuat nama variable file penyimpanan data
    private val sharedPrefile = "kotlinSharedReference"
    //untuk memproses data
    private lateinit var sharedPreference : SharedPreferences
    private var daycases = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_chart_country)

        //membatasi activity mana yan bisa mengunakan SharedPrefference
        sharedPreference = this.getSharedPreferences(sharedPrefile,Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = sharedPreference.edit()

        //dapatkan data dari parcelized dari intent
        val data = intent.getParcelableExtra<CountriesItem>(extra_country)
        val formater = DecimalFormat("#,###")
        data?.let {
            //untuk mengget nama negara
            txt_countryName.text = data.country
                       //get data terupdate
            txt_totalCurrentConfirmed.text = formater.format(data.totalConfirmed?.toDouble())
                        //get total yg meninggal

            txt_newConfirmed.text = formater.format(data.newConfirmed?.toDouble())
                        //get data new deaths

            txt_newDeaths.text = formater.format(data.newDeaths?.toDouble())
                        //get data new confirm

            txt_totalCurrentDeaths.text=formater.format(data.totalDeaths?.toDouble())
                        //get data total confirm

            txt_totalCurrentRecovered.text = formater.format(data.totalRecovered?.toDouble())
                        //get total recover

            txt_newRecovered.text = formater.format(data.newRecovered?.toDouble())
                        //get total new recover


//            untuk menyimpan data
            editor.putString(data.country,data.country)
                .apply()
            editor.commit()

            val simpanNegara = sharedPreference.getString(data.country,data.country)
            val simpanflag = sharedPreference.getString(data.countryCode,data.countryCode)
            simpanDataNegaraAtas = simpanNegara.toString()
            simpanDataFlagAtas = simpanflag.toString() + "/flat/64.png"

            if (simpanflag !=null){
                Glide.with(this).load("https://countryflags.io/$simpanDataFlagAtas")
                    .into(img_countryFlag)
            }else{
                Toast.makeText(this, "Gambar tidak ketemu", Toast.LENGTH_SHORT).show()
            }

        }

        getChart()
    }

    private fun getChart(){
        val okhttp= OkHttpClient().newBuilder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .writeTimeout(12, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.covid19api.com/dayone/country/")
            .client(okhttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(InfoService::class.java)
        api.getInfoService(simpanDataNegaraAtas).enqueue(object:Callback<List<InfoNegara>>{
            override fun onFailure(call: Call<List<InfoNegara>>, t: Throwable) {
                Toast.makeText(this@DetailChartCountry,"Error",Toast.LENGTH_SHORT).show()
            }

            @SuppressLint("SimpleDateFormat")
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<List<InfoNegara>>,
                response: Response<List<InfoNegara>>){
                val getListDataCorona : List<InfoNegara> = response.body()!!
                if (response.isSuccessful){
                    val barEntries : ArrayList<BarEntry> = ArrayList()
                    val barEntries2 : ArrayList<BarEntry> = ArrayList()
                    val barEntries3 : ArrayList<BarEntry> = ArrayList()
                    val barEntries4 : ArrayList<BarEntry> = ArrayList()
                    var i = 0

                    while (i < getListDataCorona.size){

                        for (s in getListDataCorona) {
                            val barEntry = BarEntry(i.toFloat(), s.Comfirmed?.toFloat() ?: 0f)
                            val barEntry2 = BarEntry(i.toFloat(), s.Death?.toFloat() ?: 0f)
                            val barEntry3 = BarEntry(i.toFloat(), s.Recovered?.toFloat() ?: 0f)
                            val barEntry4 = BarEntry(i.toFloat(), s.Active?.toFloat() ?: 0f)

                            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS'Z'")
                            val outputFormat = SimpleDateFormat("dd-MM-yyyy")
                            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS") val date: Date = inputFormat.parse(s.Date!!)
                            val formattedDate: String = outputFormat.format(date!!)
                            daycases.add(formattedDate)

                            barEntries.add(barEntry)
                            barEntries2.add(barEntry2)
                            barEntries3.add(barEntry3)
                            barEntries4.add(barEntry4)
                            i++
                        }

                        val xAxis: XAxis = chart_data1.xAxis
                        xAxis.valueFormatter = IndexAxisValueFormatter(daycases)
                        chart_data1.axisLeft.axisMinimum = 0f
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.granularity = 1f
                        xAxis.setCenterAxisLabels(true)
                        xAxis.isGranularityEnabled = true

                        val barDataSet = BarDataSet(barEntries, "Confirmed")
                        val barDataSet2 = BarDataSet(barEntries2, "Deaths")
                        val barDataSet3 = BarDataSet(barEntries3, "Recovered")
                        val barDataSet4 = BarDataSet(barEntries4, "Active")

                        barDataSet.setColors(Color.parseColor("#F44336"))
                        barDataSet2.setColors(Color.parseColor("#FFEB3B"))
                        barDataSet3.setColors(Color.parseColor("#03DAC5"))
                        barDataSet4.setColors(Color.parseColor("#2196F3"))

                        val data = BarData(barDataSet, barDataSet2, barDataSet3, barDataSet4)
                        chart_data1.data = data

                        val barSpace = 0.02f
                        val groupSpace = 0.3f
                        val groupCount = 4f

                        data.barWidth = 0.15f
                        chart_data1.invalidate()
                        chart_data1.setNoDataTextColor(R.color.black)
                        chart_data1.setTouchEnabled(true)
                        chart_data1.description.isEnabled = false
                        chart_data1.xAxis.axisMinimum = 0f
                        chart_data1.setVisibleXRangeMaximum(
                            0f + chart_data1.barData.getGroupWidth(
                                groupSpace,
                                barSpace
                            ) * groupCount
                        )
                        chart_data1.groupBars(0f,groupSpace, barSpace)

                    }
                }
            }

        })
    }
}

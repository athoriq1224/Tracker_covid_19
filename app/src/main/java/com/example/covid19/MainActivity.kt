package com.example.covid19

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.covid19.adapter.Country
import com.example.covid19.model.CountriesItem
import com.example.covid19.model.ResponseCountry
import com.example.covid19.network.ApiService
import com.example.covid19.network.RetrofitBuilder.retrofit
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.rv_country
import kotlinx.android.synthetic.main.list_country.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat

class MainActivity : AppCompatActivity() {

    private var ascending = true

    companion object{
        lateinit var adapters:Country
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapters.filter.filter(newText)
                return false
            }

        })

        swipe_refresh.setOnRefreshListener {
            getNegara()
            swipe_refresh.isRefreshing = false
        }

        initialzedView()
        getNegara()

    }

    private fun initialzedView() {
        button.setOnClickListener {
            sequenzeWithnotInternet(ascending)
            ascending = !ascending
        }
    }

    private fun sequenzeWithnotInternet(ascending: Boolean) {
        rv_country.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            if (ascending){
                (layoutManager as LinearLayoutManager).reverseLayout = true
                (layoutManager as LinearLayoutManager).stackFromEnd = true
                Toast.makeText(this@MainActivity,"Z-A",Toast.LENGTH_SHORT).show()
            }else{
                (layoutManager as LinearLayoutManager).reverseLayout = false
                (layoutManager as LinearLayoutManager).stackFromEnd = false
                Toast.makeText(this@MainActivity,"A-Z",Toast.LENGTH_SHORT).show()
            }
            adapter = adapter
        }

    }


    private fun getNegara() {
        val api = retrofit.create(ApiService::class.java)
        api.getAllNegara().enqueue(object :Callback<ResponseCountry>{
            override fun onFailure(call: Call<ResponseCountry>, t: Throwable) {
                progresbar.visibility = View.GONE
            }


            override fun onResponse(call: Call<ResponseCountry>, response: Response<ResponseCountry>)
            {
                if (response.isSuccessful){
                    val getlistDataCorona = response.body()!!.global
                    val formatter : NumberFormat = DecimalFormat("#,###")
                    txt_confirmed.text =
                        formatter.format(getlistDataCorona?.totalConfirmed?.toDouble())
                    txt_confirmed_recovered.text =
                        formatter.format(getlistDataCorona?.totalRecovered?.toDouble())
                    txt_confirmed_deaths.text =
                        formatter.format(getlistDataCorona?.totalDeaths?.toDouble())
                    rv_country.apply {
                        setHasFixedSize(true)
                        layoutManager = LinearLayoutManager(this@MainActivity)
                        progresbar.visibility = View.GONE
                        adapters = Country(
                            response.body()!!.countries as ArrayList<CountriesItem>
                        ){negara -> itemClicked(negara)}
                        adapter = adapters
                    }

                }else{
                    progresbar?.visibility = View.GONE
                }
            }

        })


    }

    private fun itemClicked(negara: CountriesItem) {
        val movewithData = Intent(this@MainActivity,DetailChartCountry::class.java)
        movewithData.putExtra(DetailChartCountry.extra_country,negara)
        startActivity(movewithData)
    }
}
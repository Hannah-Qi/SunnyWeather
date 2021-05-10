package com.example.sunnyweather.logic

import androidx.lifecycle.liveData
import com.example.sunnyweather.logic.model.Place
import com.example.sunnyweather.logic.model.Weather
import com.example.sunnyweather.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.Exception
import java.lang.RuntimeException

/*
* 第五步 仓库层代码，
* 仓库层主要工作是判断调用方请求的数据应该是从本地数据源中获取还是从网络数据源中获取，
* 并将获得的数据返回掉用方。因此仓库层有点像数据获取与缓存的中间层
* */
object Repository {
    /*
    * 一般在仓库层中定义的方法，为了能将异步获取的数据以响应编程的方式通知给上一层，通常会返回一个LiveData对象
    * 这里使用了lifecycle-livedata-ktx库提供的一个非常强大且好用的功能，可以自动构建并返回一个LiveData对象
    * 代码块中提供了一个挂起函数的上下文，就可以在liveData函数的代码块中调用任意的挂起函数（SunnyWeatherNetwork.serachPlaces）
    * */
    fun serachPlaces(query: String) = liveData(Dispatchers.IO) {
        val result = try {
            val placeResponse = SunnyWeatherNetwork.serachPlaces(query) //SunnyWeatherNetwork统一的网络数据源访问入口
            if(placeResponse.status == "ok"){
                val places = placeResponse.places
                Result.success(places)
            }else{
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
        }catch (e: Exception){
            Result.failure<List<Place>>(e)
        }
        emit(result)//将包装的结果发射出去，类似于LivaData的setValue()方法来通知数据的变化，这里无法直接返回一个LivaData对象，所以emit是一个替代方法
    }

    fun refreshWeather(lng: String,lat: String) = liveData {
        val result = try {
            coroutineScope {
                val deferredRealtime = async {
                    SunnyWeatherNetwork.getRealtimeWeather(lng,lat)
                }
                val deferredDaily = async {
                    SunnyWeatherNetwork.getDailyWeather(lng, lat)
                }
                val realtimeResponse = deferredRealtime.await()
                val dailyResponse = deferredDaily.await()
                if(realtimeResponse.status == "ok" && dailyResponse.status == "ok"){
                    val weather = Weather(realtimeResponse.result.realtime,dailyResponse.result.daily)
                    Result.success(weather)
                }else{
                    Result.failure(
                        RuntimeException(
                            "realtime response status is ${realtimeResponse.status}"+
                                    "daily response status is ${dailyResponse.status}"
                        )
                    )
                }
            }
        }catch (e: Exception){
            Result.failure<Weather>(e)
        }
        emit(result)
    }
}
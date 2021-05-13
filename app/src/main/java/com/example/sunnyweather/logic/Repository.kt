package com.example.sunnyweather.logic

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.sunnyweather.logic.model.Place
import com.example.sunnyweather.logic.model.Weather
import com.example.sunnyweather.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.Exception
import java.lang.RuntimeException
import kotlin.coroutines.CoroutineContext

/*
* 第五步 仓库层代码，
* 仓库层主要工作是判断调用方请求的数据应该是从本地数据源中获取还是从网络数据源中获取，
* 并将获得的数据返回调用方。因此仓库层有点像数据获取与缓存的中间层
* */
object Repository {
    /*
    * 一般在仓库层中定义的方法，为了能将异步获取的数据以响应编程的方式通知给上一层，通常会返回一个LiveData对象
    * 这里使用了lifecycle-livedata-ktx库提供的一个非常强大且好用的功能，可以自动构建并返回一个LiveData对象
    * 代码块中提供了一个挂起函数的上下文，就可以在liveData函数的代码块中调用任意的挂起函数（SunnyWeatherNetwork.serachPlaces）
    *
    * */

    /*
    * 我们必须为每个网络请求都进行try catch处理，增加了仓库层代码实现的复杂度，可以在统一的入口函数中进行封装，使得只要进行一次try catch处理及可以了
    * */
    fun serachPlaces(query: String) = fire(Dispatchers.IO) {
        val placeResponse = SunnyWeatherNetwork.serachPlaces(query) //SunnyWeatherNetwork统一的网络数据源访问入口
        if(placeResponse.status == "ok"){
            val places = placeResponse.places
            Result.success(places)
        }else{
            Result.failure(RuntimeException("response status is ${placeResponse.status}"))
        }
//        val result = try {
//
//        }catch (e: Exception){
//            Result.failure<List<Place>>(e)
//        }
//        emit(result)//将包装的结果发射出去，类似于LivaData的setValue()方法来通知数据的变化，这里无法直接返回一个LivaData对象，所以emit是一个替代方法
    }

    /*
    * 获取实时天气信息和获取未来几天天气信息这两个请求是没有先后顺序的，因此需要并发执行提高程序运行效率
    * 使用协程的async函数可以在同时得到他们的响应结果后才能进一步执行程序
    * async函数可以创建一个协程并返回它的执行结果，且只能在协程作用域中才能调用
    * 调用async的await()方法时，如果代码块中的代码还没有执行完，那么await()方法会将当前协程阻塞住，直到获取到执行结果
    * coroutineScope函数创建了一个协程作用
    * */
    fun refreshWeather(lng: String,lat: String) = fire(Dispatchers.IO) {
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
//        val result = try {
//
//        }catch (e: Exception){
//            Result.failure<Weather>(e)
//        }
//        emit(result)
    }

    //按照liveData()函数的参数接收标准定义  高阶函数
    /*
    * 在liveData()函数的代码块中，我们是拥有挂起函数上下文的，可是当回调Lambda表达式中，代码就没有挂起函数上下文了，但实际上Lambda表达式中的代码一定也是在挂起函数中运行的
    * 声明一个suspend ()函数，表示所有传入的Lambda表达式中的代码也是拥有挂起函数上下文的
    * */
    private fun <T> fire(context: CoroutineContext,block: suspend () -> Result<T>) = liveData<Result<T>>(context){
        val result = try{
            block()//Lambda表达式  block: suspend () -> Result<T>  中的代码
        }catch (e: Exception){
            Result.failure<T>(e)
        }
        emit(result)
    }
}
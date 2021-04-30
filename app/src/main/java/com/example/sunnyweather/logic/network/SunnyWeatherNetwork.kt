package com.example.sunnyweather.logic.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.await
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/*
* 第四步 再定义一个统一的网络数据源访问入口，对所有网络请求的API进行封装
* */
object SunnyWeatherNetwork {//单例类
    private val placeService = ServiceCreator.create(PlaceService::class.java)//创建了一个PlaceService接口的动态代理对象

    /*
    * 定义了一个serachPlaces()函数，并在这里调用了刚刚在PlaceService接口中定义的serachPlaces()方法，以发起搜索城市数据请求
    * */

    /*
    * 当外部调用了serachPlaces()函数，Retrofit就会立刻发起网络请求，同时当前协程也会被阻塞住。
    * 直到服务器响应我们请求之后，await()函数将解析出来的数据模型对象取出并返回，同时恢复当前协程的执行，
    * serachPlaces()函数在得到await()函数的返回值会将该数据再返回到上一层
    * */
    suspend fun serachPlaces(query: String) = placeService.searchPlaces(query).await()//协程 await()+suspend

    private suspend fun <T> Call<T>.await(): T{
        return suspendCoroutine {
            continuation -> enqueue(object : Callback<T>{
            override fun onResponse(call: Call<T>, response: Response<T>) {
                val body = response.body()
                if(body != null)
                    continuation.resume(body)
                else
                    continuation.resumeWithException(RuntimeException("response body is null"))
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                continuation.resumeWithException(t)
            }
        })
        }
    }
}
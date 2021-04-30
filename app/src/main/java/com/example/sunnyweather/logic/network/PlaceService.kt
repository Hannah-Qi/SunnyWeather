package com.example.sunnyweather.logic.network

import com.example.sunnyweather.SunnyWeatherApplication
import com.example.sunnyweather.logic.model.PlaceResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/*
* 第二部编写网络层相关的代码（定义一个用于访问彩云天气城市搜索API的Retrofit接口）
* */
interface PlaceService {
    //https://api.caiyunapp.com/v2/place?query=北京&token={TOKEN}&lang=zh_CN
    /*
    * 在searchPlaces() 方法上面声明了一个@GET注解，当调用searchPlaces()方法时，Retrofit就会自动发起一条GET请求
    * 其中，只有query这个参数需要动态指定，另外两个参数不变，写在@GET注解里面就可以了
    * */
    @GET("v2/place?token=${SunnyWeatherApplication.TOKEN}&lang=zh_CN")
    fun searchPlaces(@Query("query") query: String): Call<PlaceResponse>
    //searchPlaces()方法返回值为Call<PlaceResponse>，这样Retrofit就会将服务器返回的JSOn数据自动解析成PlaceResponse对象
}
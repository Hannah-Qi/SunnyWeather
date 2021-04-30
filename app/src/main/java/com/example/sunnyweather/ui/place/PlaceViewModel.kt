package com.example.sunnyweather.ui.place

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.sunnyweather.logic.Repository
import com.example.sunnyweather.logic.model.Place

/*
* 第六步 逻辑层的最后一步，定义ViewModel层
*
* 定义了一个searchPlaces()方法，将传入的搜索参数赋值给了一个serachLiveData对象，
* 并使用switchMap()方法来观察这个对象，否则仓库层返回的LiveData对象将无法进行观察
* */
class PlaceViewModel:ViewModel() {
    private val serachLiveData = MutableLiveData<String>()

    /*
    * placeList集合，用于对界面上显示的城市数据进行缓存，因为原则上与界面相关的数据都应该放到ViewModel中
    * */
    val placeList = ArrayList<Place>()

    /*
    * 每当searchPlaces()被调用时，switchMap()方法所对应的转换方法就会执行。
    * 然后在转换函数中，我们只需要调用仓库层定义的searchPlaces（）方法就可以发起网络请求，
    * 同时将仓库层返回的LiveData对象转换成一个可供Activty观察的LiveData对象
    * */
    val placeLiveData = Transformations.switchMap(serachLiveData){
        query -> Repository.serachPlaces(query)
    }

    fun searchPlaces(query: String){
        serachLiveData.value = query
    }
}
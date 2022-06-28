package ru.wb.go.network.api.app

import okhttp3.Interceptor
import okhttp3.Response
import ru.wb.go.app.App
import ru.wb.go.utils.RebootApplication
import ru.wb.go.utils.RebootDialogManager
import ru.wb.go.utils.analytics.YandexMetricManager
import java.io.IOException

class AutentificatorIntercept(
    private val metric: YandexMetricManager
    ) : Interceptor {

    var nameOfMethod:String? = null

    fun initNameOfMethod(name:String):String?{
        this.nameOfMethod = name
        return nameOfMethod
    }


    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val response: Response = chain.proceed(original)

        if (response.code == 409) {
            RebootDialogManager.showRebootDialog()
        }
        doOnSubscribe(nameOfMethod)

        if (response.isSuccessful) {
            doOnComplete(nameOfMethod)
        }
        else{
            onError(nameOfMethod,response.message)
        }
        return response
    }


     fun doOnNext(method: String?) {
        metric.onTechNetworkLog(method?: "", "doOnNext")
    }

     private fun doOnComplete(method: String?) { //завершил
        metric.onTechNetworkLog(method?: "", "doOnComplete")
    }

     private fun doOnSubscribe(method: String?) {
        metric.onTechNetworkLog(method?: "", "doOnSubscribe")
    }

     private fun onError(method: String?, error: String) {
        metric.onTechNetworkLog(method?: "", error)
    }
}


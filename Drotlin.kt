package drotlin

import android.app.Activity
import android.app.Fragment
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.Fragment as FragmentV4
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Looper
import android.os.Handler
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.util.AndroidRuntimeException
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import java.io.File
import kotlin.reflect.KClass


const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT
const val EXACTLY = View.MeasureSpec.EXACTLY
const val AT_MOST = View.MeasureSpec.AT_MOST
const val UNSPECIFIED = View.MeasureSpec.UNSPECIFIED

fun initDrotlin(context: Context) {
    drotlin.applicationContext = context.applicationContext
}

//Context==================================================
lateinit var applicationContext: Context

fun Activity.alert(message: String) {
    AlertDialog.Builder(this)
            .setTitle("提示")
            .setMessage(message)
            .setPositiveButton("ok", { d, _ -> d.dismiss() })
            .show()
}

fun Fragment.alert(message: String) {
    activity?.alert(message)
}

fun FragmentV4.alert(message: String) {
    activity?.alert(message)
}


fun <T : Activity> Activity.startActivity(clazz: KClass<T>) {
    startActivity(Intent(this, clazz.java))
}

inline fun <T : Activity> Activity.startActivity(clazz: KClass<T>, handler: Intent.() -> Unit) {
    val intent = Intent(this, clazz.java)
    handler(intent)
    startActivity(intent)
}

inline fun <reified T : ViewModel> FragmentActivity.getViewModel(): T {
    return ViewModelProviders.of(this).get(T::class.java)
}


inline fun <reified T : ViewModel> FragmentV4.getViewModel(): T {
    return ViewModelProviders.of(this).get(T::class.java)
}

fun toast(message: String) {
    val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
    toast.setGravity(Gravity.CENTER, 0, 0)
    toast.show()
}


//Debug==================================================
val isDebug: Boolean by lazy { drotlin.applicationContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0 }

fun Any?.log(message: String?, tag: Any? = this) {
    if (isDebug) {
        val logTag: String = when (tag) {
            is String -> tag
            null -> "!null tag!"
            else -> tag::class.java.simpleName
        }
        logI(logTag, message ?: "!null message!")
    }
}

fun Any?.log(map: Map<String, String>?, tag: Any? = this) {
    if (isDebug) {
        val logTag: String = when (tag) {
            is String -> tag
            null -> "!null tag!"
            else -> tag::class.java.simpleName
        }
        logI(logTag, map?.toString() ?: "!null map!")
    }
}

private fun logI(tag: String, msg: String) {
    Log.d(tag, msg)
}

fun throwE(e: Exception) {
    if (isDebug) {
        throw AndroidRuntimeException(e)
    }
}

fun logException(tag: String, message: String = "Catch Exception", e: Exception) {
    if (isDebug) {
        Log.e(tag, message, e)
    }
}

inline fun <T> T.applyWhenTest(runnable: T.() -> Unit): T {
    if (isDebug) {
        runnable()
    }
    return this
}

//Thread==================================================
val mainHandler = Handler(Looper.getMainLooper())

fun <T> T.runOnUiThread(delay: Long = 0, runnable: T.() -> Unit) {
    mainHandler.postDelayed({ runnable() }, delay)
}

//Ui==================================================
fun View.inflate(layoutId: Int, viewGroup: ViewGroup? = parent as? ViewGroup, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutId, viewGroup, attachToRoot)
}

fun ViewGroup.inflate(layoutId: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutId, this, attachToRoot)
}

fun Context.inflate(layoutId: Int, viewGroup: ViewGroup? = null, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(this).inflate(layoutId, viewGroup, attachToRoot)
}

val Int.dimensionPixelSize: Int
    get() = applicationContext.resources.getDimensionPixelSize(this)

val Int.dip2Px: Int
    get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, toFloat(), applicationContext.resources.displayMetrics)
            .toInt()

val Int.colorResource: Int
    get() = if (Build.VERSION.SDK_INT >= 23) {
        applicationContext.resources.getColor(this, applicationContext.theme)
    } else {
        applicationContext.resources.getColor(this)
    }

val Int.stringResource: String
    get() = applicationContext.resources.getString(this)

fun Int.setAlpha(alpha: Int): Int {
    return (this and 0x00ffffff) or (alpha shl 24)
}

fun Int.toMeasureSpec(mode: Int): Int {
    return View.MeasureSpec.makeMeasureSpec(this, mode)
}

operator fun ViewGroup.plus(child: View): ViewGroup {
    this.addView(child)
    return this
}

operator fun ViewGroup.minus(child: View): ViewGroup {
    this.removeView(child)
    return this
}

operator fun ViewGroup.get(index: Int): View? {
    return getChildAt(index)
}

//File==================================================
val File?.exists: Boolean
    get() = this != null && exists()

val File?.isDirectory: Boolean
    get() = this != null && this.exists() && this.isDirectory

fun getPictureDir(): File {
    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
}

fun String.toFile(): File {
    return File(this)
}

val File.uri: Uri
    get() = Uri.fromFile(this)

//Other==================================================
val String.uri: Uri
    get() = Uri.parse(this)

fun Any?.stringValue(nullReplace: String = ""): String {
    return when {
        this == null -> nullReplace
        this is String -> this
        else -> return toString()
    }
}
这是一个MVVM基础架构，易拓展、轻量、低耦合。

其中状态回调、全局通用view均是已接口或回调形式交互，且未内置这些功能，只需要根据你的定义需求，对接这个接口和回调即可。

配合ARouter使用更nice。

### 1.简单的使用

#### 1.1 新建自己的BaseActivity

```kotlin
abstract class BaseActivity<T : ViewDataBinding, E : BaseViewModel>(modelClass: Class<E>) :
    EasyBaseActivity<T, E>(modelClass) {

    override fun initLoadingViewEnd() {
    }

    override fun defaultHideLoadingView() {
    }

    override fun showLoading() {
    }

    override fun hideLoading() {

    }

    override fun error(error: Throwable) {
    }

    override fun retry() {
    }

}
```

#### 1.2 创建布局

```xml
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:android="http://schemas.android.com/apk/res/android" xmlns:tools="http://schemas.android.com/tools">

    <data>

        <variable name="vm" type="com.tson.easy.MainViewModel" />

    </data>

    <LinearLayout android:layout_width="match_parent" android:layout_height="match_parent" android:gravity="center_horizontal"
        android:orientation="horizontal">

        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:padding="10dp" android:text="@{vm.des}"
            android:textColor="@android:color/holo_green_dark" tools:text="hello world" />


    </LinearLayout>

</layout>
```

#### 1.3 创建viewModel

```kotlin
class MainViewModel : BaseViewModel() {

    val des = ObservableField("hello MVVM！")

}
```

#### 1.4 新建MainActivity

```kotlin
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>(MainViewModel::class.java) {

//    当你指定这个viewModel的类型为public的时候，则这个view是全局的，生命周期感知的是application的，只有进程关闭的时候，才会回收这个viewModel。
//    如果没有指定，则这个viewModel生命周期感知的就是当前上下文的，比如这个activity关闭之后，则viewModel也会回收。
//    override fun viewModelType() = Limit.PUBLIC

    override fun initView() {

    }
}
```

如上已完成MVVM的简单使用了

### 2. 依赖说明

#### 2.1 arr方式

直接下载最新版本的AAR导入即可

#### 2.2 maven方式

- 根目录下的build.gradle添加如下代码

```groovy
allprojects {
    repositories {
        google()
        jcenter()
        //添加下面这个maven配置
        maven {
            //这是maven仓库地址
            url 'https://raw.githubusercontent.com/xintanggithub/maven/master'
        }
    }
}
```

GitHub的地址偶尔无法访问，可以配置host或翻墙使用

- 需要使用的module下build.gradle添加引用

```groovy
    implementation "com.easy.assembly.base:lib:1.0.11"
```

### 3. 全局viewModel和私有viewModel

- 全局viewModel

当你指定这个viewModel的类型为public的时候，则这个view是全局的，生命周期感知的是application的，只有进程关闭的时候，才会回收这个viewModel。

- 私有viewModel

如果没有指定，则这个viewModel生命周期感知的就是当前上下文的，比如这个activity关闭之后，则viewModel也会回收。

- Activity或Fragment的指定方式

```kotlin
    override fun viewModelType() = Limit.PUBLIC  // Limit.PUBLIC 全局  Limit.PROJECT 私有 
```

- 非Activity或Fragment创建viewModel指定方式

```kotlin
// Activity或fragment非绑定的viewModel创建方式
val viewModel = getViewModel(MainViewModel::class.java, componentUse = true) // componentUse = true 全局  false 生命周期感知当前上下文的

// 或者
val viewModel = EasyApplication.of().get(MainViewModel::class.java) // 获取全局viewModel
val viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(MainViewModel::class.java) // 获取生命周期感知当前上下文的

```

### 3. 自定义公共loadingView和errorView(推荐，但不是唯一方式)

```kotlin
这种如果是全局只有一个LoadingViewModel，且需要根据页面做状态隔离，建议根据activity做tag区分各个页面状态。
比如：A界面是loading中， 到B界面后，状态变为loading error ， 因为是全局ViewModel ， 所以会导致A 、 B都被更新为loading error，所以可以把A的loading状态单独保存，B的也单独保存，做状态隔离，即使B变更后，也不影响A的状态。


或者不使用LoadingViewModel的这种方式，因为loadingView和errorView的处理逻辑都在你的BaseActivity中实现的，所以怎么实现，怎么处理都是自定义的。


最快的方法：把viewModel当成普通对象使用，对如下代码进行修改：
// 获取ViewModel
loadingViewModel = getViewModel(LoadingViewModel::class.java)

改为
loadingViewModel = LoadingViewModel()

```

以下内容在上面的基础版之上添加即可

- 新增公共布局viewModel(LoadingViewModel)和xml(loading_layout.xml)

这里只是demo，内部逻辑可自定义

```kotlin
class LoadingViewModel : ViewModel() {

    var loadingMessage = ObservableField("")

    fun retry() {
        loadStatus.retry()
    }
}
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:android="http://schemas.android.com/apk/res/android">

    <data>

        <variable name="loading" type="com.tson.easydemo.model.LoadingViewModel" />

        <import type="android.view.View" />
    </data>

    <LinearLayout android:layout_width="match_parent" android:layout_height="match_parent" android:background="@color/cardview_shadow_start_color"
        android:gravity="center" android:orientation="vertical">

        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@{loading.loadingMessage}"
            android:textSize="22sp" />

        <Button android:layout_width="wrap_content" android:layout_height="wrap_content" android:onClick="@{()->loading.retry()}" android:text="重试"
            android:visibility="@{loading.loadingMessage.contains(`错误`)?View.VISIBLE:View.GONE}" />
    </LinearLayout>
</layout>
```

- BaseActivity处理公共布局的加载逻辑，注意注释

```kotlin
abstract class BaseActivity<T : ViewDataBinding, E : BaseViewModel>(modelClass: Class<E>) :
    EasyBaseActivity<T, E>(modelClass) {

    // loadingView布局binding
    lateinit var loadingBinding: LoadingLayoutBinding

    lateinit var loadingViewModel: LoadingViewModel

    // 设定loadingView布局，也可以在子类实现，如果在子类实现，以下binding和viewModel的操作也同步在子类处理
    override fun requestLoadingViewId(): Int = R.layout.loading_layout

    override fun defaultHideLoadingView() {
        super.defaultHideLoadingView()
        // 默认的公共布局处理，一般做loadingView默认显示或隐藏，触发时机在initView方法之前
        hideRoot()
    }

    private fun showRoot() {
        loadingBinding.root.visibility = View.VISIBLE
    }

    private fun hideRoot() {
        loadingBinding.root.visibility = View.GONE
    }

    /**
     * loadingView绑定到contentView成功后触发，触发时机在 [defaultHideLoadingView] 之前
     */
    override fun initLoadingViewEnd() {
        // dataBinding绑定view
        loadingBinding = DataBindingUtil.bind(loadingView)!!
        // 获取ViewModel
        loadingViewModel = getViewModel(LoadingViewModel::class.java)
        // 绑定ViewModel
        loadingBinding.loading = loadingViewModel
        // 关联页面业务ViewModel和公共loadingView和errorView的核心代码，由接口实现
        viewModel.loadStatus = this
        loadingViewModel.loadStatus = this
    }

    override fun error(error: Throwable) {
        // 显示错误信息
        showRoot()
        loadingViewModel.loadingMessage.set("❌错误信息：${error.message}")
    }

    override fun hideLoading() {
        // 隐藏loading 或 error
        loadingViewModel.loadingMessage.set("")
        hideRoot()
    }

    override fun retry() {
        //  重试
        hideRoot()
        Toast.makeText(this, "handle  retry ", Toast.LENGTH_SHORT).show()
    }

    override fun showLoading() {
        // 显示loading
        showRoot()
        loadingViewModel.loadingMessage.set("")
        loadingViewModel.loadingMessage.set("现在正在加载中...")
    }

}
```

以上便完成了公共view的实现，下面看看在activity的业务viewModel里面怎么使用的

```kotlin
class MainViewModel : BaseViewModel() {

    var content = ObservableField("hello world！")

    fun userLoadingViewMethod() {
        loadStatus.showLoading() // 显示
        loadStatus.hideLoading() // 隐藏
        loadStatus.error(Exception("error message")) //错误
        loadStatus.retry() //重试
    }
}
```

BaseFragment同理，只是更改一下继承为EasyBaseFragment即可，所以baseActivity和BaseFragment中的公共loadingView也可以抽出来，给baseActivity和BaseFragment共用。
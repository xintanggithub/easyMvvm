package com.tson.easy.test

import androidx.databinding.ViewDataBinding
import com.tson.easy.fragment.EasyBaseFragment
import com.tson.easy.model.BaseViewModel

/**
 * 基类activity
 *
 * @Author:         Tson
 *
 * @CreateDate:     2022/3/16 12:47
 */
abstract class BaseFragment<T : ViewDataBinding, E : BaseViewModel>(modelClass: Class<E>) :
    EasyBaseFragment<T, E>(modelClass) {

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
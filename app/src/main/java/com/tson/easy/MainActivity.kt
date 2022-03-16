package com.tson.easy

import com.tson.easy.databinding.ActivityMainBinding
import com.tson.easy.test.BaseActivity

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>(MainViewModel::class.java) {

    override fun initView() {}

}
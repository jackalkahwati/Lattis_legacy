package io.lattis.operator.presentation.base.fragment

import io.lattis.operator.presentation.base.BaseView


abstract class BaseTabFragment<Presenter : FragmentPresenter<V>,V:BaseView> :BaseFragment<Presenter,V>(),BaseView{

    abstract fun getTitle():String
}

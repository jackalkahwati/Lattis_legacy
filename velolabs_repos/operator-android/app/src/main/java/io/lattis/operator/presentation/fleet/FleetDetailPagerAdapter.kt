package io.lattis.operator.presentation.fleet

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import io.lattis.operator.presentation.base.fragment.BaseTabFragment

class FleetDetailPagerAdapter(
    val fm: FragmentManager,
    val fragments:List<BaseTabFragment<*,*>>
) : FragmentStatePagerAdapter(fm,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int  = fragments.size

    override fun getItem(i: Int): Fragment {
        return fragments.get(i)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return fragments.get(position).getTitle()
    }


}


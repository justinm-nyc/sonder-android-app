package Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter: FragmentPagerAdapter {
    private var fragments: ArrayList<Fragment>
    private var titles: ArrayList<String>

    constructor(
        fm: FragmentManager
    ) : super(fm) {
        this.fragments = ArrayList()
        this.titles = ArrayList()
    }


    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }

    fun addFragment(fragment: Fragment, title: String) {
        fragments.add(fragment)
        titles.add(title)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }

}
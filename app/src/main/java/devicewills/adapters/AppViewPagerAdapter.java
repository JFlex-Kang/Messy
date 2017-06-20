package devicewills.adapters;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;

import devicewills.activities.AppFragments;

/**
 *
 */
public class AppViewPagerAdapter extends FragmentPagerAdapter {

	private ArrayList<AppFragments> fragments = new ArrayList<>();
	private AppFragments currentFragment;

	public AppViewPagerAdapter(FragmentManager fm) {
		super(fm);

		fragments.clear();
		fragments.add(AppFragments.newInstance(0));
		fragments.add(AppFragments.newInstance(1));
	}

	@Override
	public AppFragments getItem(int position) {
		return fragments.get(position);
	}

	@Override
	public int getCount() {
		return fragments.size();
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		if (getCurrentFragment() != object) {
			currentFragment = ((AppFragments) object);
		}
		super.setPrimaryItem(container, position, object);
	}

	/**
	 * Get the current fragment
	 */
	public AppFragments getCurrentFragment() {
		return currentFragment;
	}
}
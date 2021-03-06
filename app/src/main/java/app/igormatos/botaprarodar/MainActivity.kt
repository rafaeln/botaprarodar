package app.igormatos.botaprarodar

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import app.igormatos.botaprarodar.local.Preferences
import app.igormatos.botaprarodar.usecases.DashboardFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_choose_user.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.toolbar as toolbar1


class MainActivity : AppCompatActivity() {


    private val activitiesFragment: Fragment = TripsFragment()
    private val usersFragment: Fragment = UsersFragment()
    private val bicycleFragment: Fragment = BicyclesFragment()
    private val dashboardFragment: Fragment = DashboardFragment()
    val fm = supportFragmentManager
    var active = activitiesFragment

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                fm.beginTransaction().hide(active).show(activitiesFragment).commit()
                active = activitiesFragment
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_users -> {
                fm.beginTransaction().hide(active).show(usersFragment).commit()
                active = usersFragment
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_bicycles -> {
                fm.beginTransaction().hide(active).show(bicycleFragment).commit()
                    active = bicycleFragment
                    return@OnNavigationItemSelectedListener true
        }

            R.id.navigation_dashboard -> {
                fm.beginTransaction().hide(active).show(dashboardFragment).commit()
                active = dashboardFragment
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.itemIconTintList = ColorStateList.valueOf(resources.getColor(R.color.tintColor))

        if (fm.fragments.isEmpty()) {
            fm.beginTransaction().add(R.id.main_container, dashboardFragment, "4").hide(dashboardFragment).commit()
            fm.beginTransaction().add(R.id.main_container, bicycleFragment, "3").hide(bicycleFragment).commit()
            fm.beginTransaction().add(R.id.main_container, usersFragment, "2").hide(usersFragment).commit()
            fm.beginTransaction().add(R.id.main_container, activitiesFragment, "1").commit()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId) {
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                Preferences.clear(this)

                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}

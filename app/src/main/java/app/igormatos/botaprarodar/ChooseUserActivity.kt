package app.igormatos.botaprarodar

import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.widget.ActionMenuView
import android.widget.SearchView
import androidx.appcompat.view.menu.ActionMenuItemView
import app.igormatos.botaprarodar.local.model.Withdraw
import kotlinx.android.synthetic.main.activity_choose_user.*
import org.parceler.Parcels

class ChooseUserActivity : AppCompatActivity() {

    var withdrawalParcelable: Parcelable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_user)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        withdrawalParcelable =
            if (intent.hasExtra(WITHDRAWAL_EXTRA)) intent.getParcelableExtra(WITHDRAWAL_EXTRA) as Parcelable else null

        val withdrawal = Parcels.unwrap(withdrawalParcelable) as Withdraw
        val fragment = UsersFragment.newInstance(withdrawal)

        supportFragmentManager.beginTransaction().add(R.id.chooseUserFragment, fragment, "1").commit()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        return when (id) {
            R.id.action_search -> true
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

}

package de.smartsquare.kickdroid.base

import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotterknife.KotterKnife

/**
 * @author Ruben Gees
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onDestroy() {
        KotterKnife.reset(this)

        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }

        return super.onOptionsItemSelected(item)
    }
}

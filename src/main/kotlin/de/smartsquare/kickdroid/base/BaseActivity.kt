package de.smartsquare.kickdroid.base

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
}

package de.smartsquare.kickdroid

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import kotterknife.KotterKnife

/**
 * @author Ruben Gees
 */
@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {

    override fun onDestroy() {
        KotterKnife.reset(this)

        super.onDestroy()
    }
}

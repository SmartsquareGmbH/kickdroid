package de.smartsquare.kickdroid.view

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.View
import androidx.core.view.ViewCompat
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.view.IconicsImageView
import de.smartsquare.kickdroid.R

/**
 * @author Ruben Gees
 */
class LoadingIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : IconicsImageView(context, attrs, defStyleAttr) {

    private companion object {
        private const val ROTATION_FACTOR = 1.5f
        private const val ANIMATION_DURATION_FACTOR = 3L
    }

    init {
        this.icon = IconicsDrawable(context)
            .icon(CommunityMaterial.Icon2.cmd_soccer)
            .colorRes(R.color.colorPrimary)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        animateOrCancel()
    }

    override fun onDetachedFromWindow() {
        animateOrCancel()

        super.onDetachedFromWindow()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)

        animateOrCancel()
    }

    private fun animateOrCancel() {
        val parentView = parent as View

        if (ViewCompat.isAttachedToWindow(this) && visibility == View.VISIBLE) {
            if (ViewCompat.isLaidOut(parentView)) {
                doAnimate()
            } else {
                parentView.post { animateOrCancel() }
            }
        } else {
            ViewCompat.animate(this).cancel()
        }
    }

    private fun doAnimate() {
        val parentView = parent as View
        val targetX = parentView.width
        val targetRotation = targetX / ROTATION_FACTOR
        val targetDuration = targetX * ANIMATION_DURATION_FACTOR

        this.x = (-width).toFloat()

        ViewCompat.animate(this)
            .x(Resources.getSystem().displayMetrics.widthPixels.toFloat())
            .rotationBy(targetRotation)
            .setDuration(targetDuration)
            .withEndAction { animateOrCancel() }
            .start()
    }
}

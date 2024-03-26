package dev.sudhanshu.videoplayer


import android.content.Context
import android.graphics.Rect
import android.media.MediaPlayer
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.VideoView

interface VideoPlayListener {
    fun onVideoStarted()
}

interface VideoNotVisibleListener {
    fun onVideoNotVisible()
}

class HomeFeedVideoView(context: Context, attrs: AttributeSet?) : VideoView(context, attrs) {

    private var isVisibleToUser = false
    var isMuted = false
    private var mediaPlayer: MediaPlayer? = null
    private var videoPlayListener: VideoPlayListener? = null
    private var videoNotVisibleListener: VideoNotVisibleListener? = null

    fun setVideoPlayListener(listener: VideoPlayListener) {
        videoPlayListener = listener
    }

    fun setVideoNotVisibleListener(listener: VideoNotVisibleListener) {
        videoNotVisibleListener = listener
    }

    init {
        setOnPreparedListener {
            mediaPlayer = it
            start()
            it.isLooping = true
            applyMuteState()
            videoPlayListener?.onVideoStarted()
        }

        viewTreeObserver.addOnPreDrawListener {
            val previousVisible = isVisibleToUser
            isVisibleToUser = isViewVisible(this@HomeFeedVideoView)
            if (previousVisible && !isVisibleToUser) {
                videoNotVisibleListener?.onVideoNotVisible()
            }
            if (!isVisibleToUser) {
                pause()
            } else {
                start()
            }
            true
        }
    }

    private fun isViewVisibleToUser(view: View): Boolean {
        val rect = Rect()
        val isVisible = view.getGlobalVisibleRect(rect) && rect.height() == view.height && rect.width() == view.width
        return isVisible
    }

    private fun isViewVisibleToUserWithAccessibility(view: View): Boolean {
        val nodeInfo = AccessibilityNodeInfo.obtain()
        view.onInitializeAccessibilityNodeInfo(nodeInfo)
        val isVisible = nodeInfo.isVisibleToUser
        nodeInfo.recycle()
        return isVisible
    }

    private fun isViewVisible(view: View): Boolean {
        return isViewVisibleToUser(view) && isViewVisibleToUserWithAccessibility(view)
    }

    fun setVideoUrl(url: String) {
        setVideoURI(Uri.parse(url))
    }

    fun toggleMute() {
        if (isMuted) {
            unmute()
        } else {
            mute()
        }
    }

    private fun mute() {
        isMuted = true
        mediaPlayer?.setVolume(0f, 0f)
    }

    private fun unmute() {
        isMuted = false
        mediaPlayer?.setVolume(1f, 1f)
    }

    private fun applyMuteState() {
        if (isMuted) {
            mute()
        } else {
            unmute()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (isPlaying) {
                pause()
            } else {
                start()
            }
            return true
        }
        return super.onTouchEvent(event)
    }
}









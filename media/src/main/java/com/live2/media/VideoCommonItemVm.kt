package com.live2.media

import androidx.databinding.ObservableBoolean
import com.live2.media.utils.NonNullObservableField
import com.live2.media.core.exoplayer.PlayerHelper
import com.live2.media.internal.model.PostModel

abstract class VideoCommonItemVm(
    val item: PostModel.Video,
    playerHelper: PlayerHelper
) {
    val video = NonNullObservableField(item)
    val isVideoPlaying = ObservableBoolean(false)
    private var previousProgress: Long = Long.MIN_VALUE

    var playerLayout: PlayerWatchBodyView? = null
    val videoItemSeekbarVm = VideoItemSeekbarVm(playerHelper)
    var productPlacement: ProductPlacementView? = null

    val shouldShowFeedbackToast = ObservableBoolean(false)

    fun initSeekbar() = videoItemSeekbarVm.initSeekbar {}
    fun pauseSeekbar() = videoItemSeekbarVm.stopSeekbar()


}

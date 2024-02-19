package com.live2.media

interface VideoItemClickCallbacks {
    fun onPlayPauseClicked(videoItemVM: VideoItemVM)
    fun onShareClicked(videoItemVM: VideoItemVM)
    fun onMuteUnMuteClicked(videoItemVM: VideoItemVM)
    fun onCloseClicked(videoItemVM: VideoItemVM)
    fun onCampaignClicked(videoItemVM: VideoItemVM)
    fun onCampaignCTAClicked(videoItemVM: VideoItemVM)
    fun onPipClicked(videoItemVM: VideoItemVM)
    fun onVideoTapForward(position: Int, videoItemVM: VideoItemVM)
    fun onVideoTapBackward(position: Int, videoItemVM: VideoItemVM)
}
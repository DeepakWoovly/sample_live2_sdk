package com.live2.media.ui.campaigns

interface CampaignsClickCallback {
    fun onMCQSubmitClicked(
        publicCampaignId: String,
        optionIds: List<String>,
        optionTextList: List<String>
    )

    fun onPollSubmitClicked(
        publicCampaignId: String,
        optionId: String,
        optionText: String
    )

    fun onQuestionSubmitClicked(
        publicCampaignId: String,
        answerText: String
    )
}
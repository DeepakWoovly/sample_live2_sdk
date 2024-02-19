package com.live2.media.ui.campaigns

import com.live2.media.internal.model.PostModel

interface CampaignListener {
    fun onSubmitClicked()
    fun setupOptions(optionsList: List<PostModel.Option>)
}
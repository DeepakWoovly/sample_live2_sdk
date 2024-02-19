package com.example.videosdk.feature.campaigns

import com.example.videosdk.network.model.PostModel

interface CampaignListener {
    fun onSubmitClicked()
    fun setupOptions(optionsList: List<PostModel.Option>)
}
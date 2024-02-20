package com.live2.media.client

enum class SiteSectionType {
    StoryView,
    StoryWindowView,
    Carousel
}

sealed class ViewType(val value: String) {
    data object StoryView : ViewType("StoryView")
    data object StoryWindowView : ViewType("StoryWindowView")
    data object Carousel : ViewType("Carousel")

    companion object {
        fun fromValue(value: String): ViewType? {
            return when (value) {
                StoryView.value -> StoryView
                StoryWindowView.value -> StoryWindowView
                Carousel.value -> Carousel
                else -> null
            }
        }
    }
}
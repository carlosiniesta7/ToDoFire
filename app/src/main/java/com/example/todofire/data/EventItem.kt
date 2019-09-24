package com.example.todofire.data

class EventItem(var objectId: String? = null,
                var eventText: String? = null,
                var eventDate: String? = null) {

    companion object Factory {
        fun create(): EventItem = EventItem()
    }
}
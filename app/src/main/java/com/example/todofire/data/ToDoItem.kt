package com.example.todofire.data

class ToDoItem (var objectId: String? = null,
                var itemText: String? = null,
                var done: Boolean? = false) {

    companion object Factory {
        fun create(): ToDoItem = ToDoItem()
    }

}
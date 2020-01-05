package com.crskdev.jccp.ui.app.books.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.crskdev.jccp.domain.model.Book
import com.crskdev.jccp.domain.model.BookFilter
import com.crskdev.jccp.domain.util.coroutines.AbstractDispatchers
import com.crskdev.jccp.system.repository.WorkingRepository
import com.crskdev.jccp.system.sub.CompositeSubscription
import com.crskdev.jccp.system.sub.Subscription

/**
 * Created by Cristian Pela on 11.11.2019.
 */
class ListViewModel(
    private val dispatchers: AbstractDispatchers,
    private val repository: WorkingRepository,
    private val filterMapper: UIOrderByFilterMapper) : ViewModel(){

    val booksLiveData: LiveData<List<Book>> = MutableLiveData()
    val menuLiveData: LiveData<Menu> = MutableLiveData()

    private val subscription: CompositeSubscription = CompositeSubscription()

    init {
        require(booksLiveData is MutableLiveData && menuLiveData is MutableLiveData)
        subscription += repository.subscribe {
            booksLiveData.value = it.books
            if (it.filter is BookFilter.OrderBy) {
                filterMapper.currentFilter = it.filter as BookFilter.OrderBy
            }
        }
        subscription += filterMapper.subscribe {
            menuLiveData.value = it
        }
    }

    fun remove(book: Book) {
        repository.remove(book)
    }

    fun toBookFilter(menuItem: MenuItem) =
        filterMapper.toBookFilter(menuItem)?.apply {
            repository.filter = this
        }

    override fun onCleared() {
        subscription.clear()
    }
}

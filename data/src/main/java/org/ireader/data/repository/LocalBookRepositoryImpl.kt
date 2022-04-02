package org.ireader.data.repository

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.ireader.data.local.AppDatabase
import org.ireader.data.local.dao.LibraryBookDao
import org.ireader.data.local.dao.LibraryChapterDao
import org.ireader.data.local.dao.RemoteKeysDao
import org.ireader.domain.models.SortType
import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.LocalBookRepository
import timber.log.Timber

class LocalBookRepositoryImpl(
    private val bookDao: LibraryBookDao,
    private val libraryChapterDao: LibraryChapterDao,
    private val appDatabase: AppDatabase,
    private val remoteKeysDao: RemoteKeysDao,
) : LocalBookRepository {

    override fun subscribeBookById(id: Long): Flow<Book?> = flow {
        Timber.d("Timber: GetExploreBookByIdUseCase was Called")
        bookDao.subscribeBookById(bookId = id)
            .first { book ->
                if (book != null) {
                    emit(book)
                    true
                } else {
                    emit(null)
                    true
                }
            }
        Timber.d("Timber: GetExploreBookByIdUseCase was Finished Successfully")

    }

    override suspend fun findBookById(id: Long): Book? {
        return bookDao.findBookById(id)
    }


    override suspend fun findUnreadBooks(): List<Book> {
        return bookDao.findUnreadBooks()
    }

    override suspend fun findCompletedBooks(): List<Book> {
        return bookDao.findCompletedBooks()
    }

    override suspend fun findDownloadedBooks(): List<Book> {
        return bookDao.findDownloadedBooks()
    }

    override fun subscribeAllInLibrary(
        sortByAbs: Boolean,
        sortByDateAdded: Boolean,
        sortByLastRead: Boolean,
        dateFetched: Boolean,
        sortByTotalChapters: Boolean,
        dateAdded: Boolean,
        latestChapter: Boolean,
        lastChecked: Boolean,
        desc: Boolean,
    ): Flow<List<Book>> {
        return when {
            sortByLastRead -> bookDao.subscribeLatestRead(desc)
            sortByTotalChapters -> bookDao.subscribeTotalChapter(desc)
            latestChapter -> bookDao.subscribeLatestChapter(desc)
            else -> {
                bookDao.subscribeAllInLibraryBooks(
                    sortByAbs = sortByAbs,
                    sortByDateAdded = sortByDateAdded,
                    sortByLastRead = sortByLastRead,
                    desc = desc,
                    dateFetched = dateFetched,
                    dateAdded = dateAdded,
                    sortByTotalChapter = sortByTotalChapters,
                    lastChecked = lastChecked
                )
            }

        }
    }


    override suspend fun findAllInLibraryBooks(
        sortType: SortType,
        isAsc: Boolean,
        unreadFilter: Boolean,
    ): List<Book> {
        return bookDao.findAllInLibraryBooks()
    }

    override fun getBooksByQueryByPagingSource(query: String):
            PagingSource<Int, Book> {
        return getBooksByQueryPagingSource(query)
    }

    override fun getBooksByQueryPagingSource(query: String): PagingSource<Int, Book> {
        return bookDao.searchBook(query)
    }

    /*******************GET **************************************/
    override suspend fun deleteNotInLibraryChapters() {
        libraryChapterDao.deleteNotInLibraryChapters()
    }

    override suspend fun deleteAllExploreBook() {
        return remoteKeysDao.deleteAllExploredBook()
    }


    override suspend fun deleteBookById(id: Long) {
        return bookDao.deleteBook(bookId = id)
    }

    override suspend fun deleteAllBooks() {
        return bookDao.deleteAllBook()
    }


    override fun getAllExploreBookPagingSource(): PagingSource<Int, Book> {
        return remoteKeysDao.getAllExploreBookByPaging()
    }

    override suspend fun findBookByKey(key: String): Book? {
        return bookDao.findBookByKey(key = key)
    }

    override suspend fun findBooksByKey(key: String): List<Book> {
        return bookDao.findBooksByKey(key = key)
    }


    override suspend fun insertBooks(book: List<Book>): List<Long> {
        return remoteKeysDao.insertAllExploredBook(book)
    }

    override suspend fun findFavoriteSourceIds(): List<Long> {
        return bookDao.findFavoriteSourceIds()
    }

    override suspend fun deleteAllExploredBook() {
        return bookDao.deleteExploredBooks()
    }

    override suspend fun convertExploredTOLibraryBooks() {
        return bookDao.convertExploredTOLibraryBooks()
    }

    override suspend fun insertBook(book: Book): Long {
        return bookDao.insertBook(book)
    }
}
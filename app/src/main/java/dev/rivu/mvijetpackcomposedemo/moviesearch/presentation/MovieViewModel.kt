package dev.rivu.mvijetpackcomposedemo.moviesearch.presentation

import androidx.hilt.lifecycle.ViewModelInject
import dev.rivu.mvijetpackcomposedemo.base.presentation.BaseViewModel
import dev.rivu.mvijetpackcomposedemo.base.presentation.ISchedulerProvider
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.FlowableTransformer
import io.reactivex.rxjava3.functions.BiFunction

class MovieViewModel @ViewModelInject constructor(
    override val actionProcessor: MovieProcessor
) :
    BaseViewModel<MovieIntent, MoviesState, MovieAction, MovieResult>() {

    override fun intentFilter(): FlowableTransformer<MovieIntent, MovieIntent> {
        return FlowableTransformer { intents ->
            intents.publish { shared ->
                Flowable.merge<MovieIntent>(
                    shared.ofType(MovieIntent.InitialIntent::class.java).take(1),
                    shared.filter {
                        it !is MovieIntent.InitialIntent
                    }
                )
            }
        }
    }

    override fun actionFromIntent(intent: MovieIntent): MovieAction {
        return when (intent) {
            is MovieIntent.InitialIntent -> MovieAction.InitAction
            is MovieIntent.SearchIntent -> MovieAction.SearchAction(intent.query)
            is MovieIntent.ClickIntent -> MovieAction.DetailAction(intent.imdbId)
            is MovieIntent.ClearClickIntent -> MovieAction.ClearDetailAction
            is MovieIntent.SaveSearchHistory -> MovieAction.SaveSearchHistory(intent.searchHistory)
        }
    }

    override fun reducer(): BiFunction<MoviesState, MovieResult, MoviesState> =
        BiFunction { previousState, result ->
            when (result) {
                is MovieResult.InitResult -> previousState.copy(searchHistory = result.searchHistory)
                is MovieResult.SearchResult.InProgress -> previousState.copy(
                    query = result.query,
                    isLoading = true
                )
                is MovieResult.SearchResult.Success -> previousState.copy(
                    query = result.query,
                    movies = result.movies,
                    error = null,
                    isLoading = false,
                    detail = null
                )
                is MovieResult.SearchResult.Failure -> previousState.copy(
                    query = result.query,
                    error = result.error,
                    isLoading = false
                )
                is MovieResult.LoadDetailResult.InProgress -> previousState.copy(
                    isLoading = true
                )
                is MovieResult.LoadDetailResult.Success -> previousState.copy(
                    error = null,
                    isLoading = false,
                    detail = result.movieDetail
                )
                is MovieResult.LoadDetailResult.Failure -> previousState.copy(
                    error = result.error,
                    isLoading = false
                )
                is MovieResult.ClearDetailResult -> previousState.copy(
                    detail = null
                )
                is MovieResult.SaveSearchResult -> previousState
            }
        }

    override fun initialState(): MoviesState = MoviesState.initialState()
}
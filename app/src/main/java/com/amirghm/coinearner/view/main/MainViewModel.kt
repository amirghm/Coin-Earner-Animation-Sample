package com.amirghm.coinearner.view.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amirghm.coinearner.view.main.MainFragment.Companion.MAX_EARNING_COINS
import com.amirghm.coinearner.view.main.MainFragment.Companion.MIN_EARNING_COINS
import kotlin.random.Random

/**
 * Created by Amir Hossein Ghasemi since 27/12/20
 *
 * Usage: This class represents the main viewModel view of the app
 *
 */
class MainViewModel : ViewModel() {

    // The user coins stored here
    val coins = MutableLiveData(Random(System.currentTimeMillis()).nextInt(0, 100))

    // When the earn coin button clicked, this liveData will be filled by view model and the view
    // will be able to animate requested coins
    val randomCoinCountLiveData = MutableLiveData<Int>()

    // When a coin animation completed, this liveData will be notified
    val coinAnimationCompleted = MutableLiveData<Int>()

    /**
     * This function add the requested count coin to the coinBox
     * @param count The number of requested coins to be added to the coinBox
     */
    fun addCoin(count: Int) {
        coins.value = (coins.value ?: 0) + count
    }

    /**
     * This function represents number of random generated coins to animate and added to coinBox
     */
    fun getSomeCoins() {
        randomCoinCountLiveData.value =
            Random(System.currentTimeMillis()).nextInt(MIN_EARNING_COINS, MAX_EARNING_COINS)
    }
}
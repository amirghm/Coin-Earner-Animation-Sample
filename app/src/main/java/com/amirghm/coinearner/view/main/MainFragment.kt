package com.amirghm.coinearner.view.main

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Path
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.addListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.amirghm.coinearner.R
import com.amirghm.coinearner.databinding.FragmentMainBinding
import com.amirghm.coinearner.utils.dp
import com.amirghm.coinearner.utils.extentions.absX
import com.amirghm.coinearner.utils.extentions.absY
import kotlin.random.Random

/**
 * Created by Amir Hossein Ghasemi since 27/12/20
 *
 * Usage: This class represents the main fragment view of the app
 * In this fragment you can earn some coins
 *
 */
class MainFragment : Fragment() {

    companion object {
        const val ANIMATION_DURATION = 1200f
        const val MIN_EARNING_COINS = 1
        const val MAX_EARNING_COINS = 50
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentMainBinding
    private lateinit var mediaPlayer: MediaPlayer
    private val coinDiceHandler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_main,
            container,
            false
        )
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        configureView()
        configureMediaPlayer()
        configureViewModel()
    }

    /**
     * Configure anything about view, such as listeners and initial some view settings
     */
    private fun configureView() {
        binding.getCoinButton.setOnClickListener {
            getSomeCoins()
        }
    }

    /**
     * This function initialize the [mediaPlayer] For playing coin sound sound
     */
    private fun configureMediaPlayer() {
        mediaPlayer = MediaPlayer.create(activity, R.raw.toss_coin)
    }

    /**
     * This function handles all live data observers and it is interface between view and view model class
     */
    private fun configureViewModel() {
        viewModel.coins.observe(viewLifecycleOwner, {
            binding.coinTextView.text = getString(R.string.app_x_coins, it.toString())
        })

        viewModel.randomCoinCountLiveData.observe(viewLifecycleOwner, {
            playCoinAnimation(count = it, duration = ANIMATION_DURATION)
        })

        viewModel.coinAnimationCompleted.observe(viewLifecycleOwner, {
            addCoin()
        })
    }

    /**
     * By calling this function we create a random value then notifies to view to start showing earning animation
     */
    private fun getSomeCoins() {
        viewModel.getSomeCoins()
    }

    /**
     * By calling this function the coin animation played
     * @param index the index of played coin
     * @param count the total coins to be earned
     * @param duration : the duration of earning coin animation
     *
     * We use a handler for creating a loop for playing animations. with small delay this function called again
     * and after that another coin animation played, this processed continued until the last coin earned and this
     * handler not called again.
     */
    @SuppressLint("InflateParams")
    private fun playCoinAnimation(index: Int = 0, count: Int, duration: Float) {
        coinDiceHandler.postDelayed({

            // create and add [coinImageView] to root layout
            val coinImageView = layoutInflater.inflate(R.layout.coin, null)
            addCoinToParentView(coinImageView)

            // get suitable path for animating the coins
            val translationPath = getTranslationPath()
            val scalePath = getScalePath()

            // Animate the get coin button on consuming coins
            animateCoinButtonScale(scalePath,duration)

            // Animate the coin with the [translationPath] data
            handleCoinTranslationAnimation(coinImageView,translationPath,scalePath,index,count,duration)

            // Handle Alpha animation for the coins
            handleCoinAlphaAnimation(coinImageView,duration)

            // Handle Loop Animation [Handler]
            handleAnimationLoop(index,count,duration)

        }, Random(System.currentTimeMillis()).nextLong(0, 150))
    }

    /**
     * Handle Animation Loop
     * @param index the current index of animated coin
     * @param count the total number of requested adding coin
     * @param duration the total time of playing animation for the coins
     */
    private fun handleAnimationLoop(index: Int,count: Int,duration: Float) {
        if (index < count)
            playCoinAnimation(index + 1, count, duration)
    }

    /**
     * Handle coin alpha translation animation
     * @param view the coinView which animation is completed
     * @param duration the total time of playing animation for the coins
     */
    private fun handleCoinAlphaAnimation(view: View, duration: Float) {
        ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f, 1f, 1f, 1f, 0.5f)
            .setDuration(duration.toLong())
            .apply {
                interpolator = PathInterpolator(0.255F, 0.935F, 0.785F, 0.170F)
            }.start()
    }

    /**
     * Play coin translation animation, after animation ended, handle all needed process for completing the animation
     * @param view the coinView which animation is completed
     * @param translationPath the path used to handle the coin translation animation
     * @param scalePath the path used to handle the scale animation
     * @param index the current index of animated coin
     * @param count the total number of requested adding coin
     * @param duration the total time of playing animation for the coins
     */
    private fun handleCoinTranslationAnimation(
        view: View,
        translationPath: Path,
        scalePath: Path,
        index: Int,
        count: Int,
        duration: Float
    ) {
        ObjectAnimator.ofFloat(view, View.X, View.Y, translationPath)
            .setDuration(duration.toLong())
            .apply {
                // we use a custom interpolator to make the animation smooth
                interpolator = PathInterpolator(0.255F, 0.935F, 0.785F, 0.170F)
                addListener(onEnd = { handleCoinAnimationEnding(view,scalePath,index,count,duration) })
            }.start()
    }

    /**
     * Animates the get coin action button while a coin consumed
     * @param scalePath the path used to handle the scale animation
     * @param duration the total time of playing animation for the coins
     */
    private fun animateCoinButtonScale(scalePath: Path, duration: Float) {
        ObjectAnimator.ofFloat(binding.getCoinButton, View.SCALE_X, View.SCALE_Y, scalePath)
            .setDuration((0.36 * duration).toLong()).start()
    }

    /**
     * This function handles the ending of played animation for a coin.
     * After processing the animation a LiveData called for notifying the animation is completed
     * (and a coin added to coinBox)
     * @param view the coinView which animation is completed
     * @param index the current index of animated coin
     * @param count the total number of requested adding coin
     * @param duration the total time of playing animation for the coins
     */
    private fun handleCoinAnimationEnding(
        view: View,
        scalePath:Path,
        index: Int,
        count: Int,
        duration: Float
    ) {
        removeAnimatedCoin(view)
        handleCoinSound()
        if (index >= count) {
            // Handle CoinBox animation on last coins earned
            val scalePathEnd = getCoinBoxScalePath()
            ObjectAnimator.ofFloat(
                binding.coinPanelRelativeLayout,
                View.SCALE_X,
                View.SCALE_Y,
                scalePathEnd
            ).setDuration((0.18 * duration).toLong()).start()

            // Handle the ending of playing sound
            handleCoinSoundEnding()
        } else {
            // Handle the coinBox item animation
            ObjectAnimator
                .ofFloat(
                    binding.coinImageView,
                    View.SCALE_X,
                    View.SCALE_Y,
                    scalePath
                )
                .setDuration((0.18 * duration).toLong()).start()
        }
        notifyCoinAnimationCompleted(index)
    }

    /**
     * This function notifies coin animation completed
     * @param index the index of completed coin
     */
    private fun notifyCoinAnimationCompleted(index: Int) {
        viewModel.coinAnimationCompleted.value = index
    }

    /**
     * This function handles the ending of played sound
     */
    private fun handleCoinSoundEnding() {
        mediaPlayer.seekTo(4800)
    }

    /**
     * This function returns a path for coinBox scale animation while a coin added into that
     * @return this function returns a suitable path for coinBox scale animation
     */
    private fun getCoinBoxScalePath(): Path {
        val coinBoxScalePath = Path()
        val scaleValueEnd =
            Random(System.currentTimeMillis()).nextDouble(1.0, 1.1).toFloat()
        coinBoxScalePath.moveTo(1f, 1f)
        coinBoxScalePath.lineTo(scaleValueEnd, scaleValueEnd)
        coinBoxScalePath.lineTo(1f, 1f)

        return coinBoxScalePath
    }

    /**
     * This function handles the coin sound. we use a long sound to prevent high cpu usage on playing
     * multiple same sound. if the sound is playing, no new sound played. and if not, we play the sound
     * with a random seek time
     */
    private fun handleCoinSound() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.seekTo(
                Random(System.currentTimeMillis()).nextInt(
                    500,
                    2000
                )
            )
            mediaPlayer.start()
        }
    }

    /**
     * Remove Animated coin from the view
     * @param view the requested view to remove from the root ViewGroup
     */
    private fun removeAnimatedCoin(view: View) {
        (binding.root as ViewGroup).removeView(view)
    }

    /**
     * This function returns a path for scale animation while a coin added into that
     * @return this function returns a suitable path for scale animation
     */
    private fun getScalePath(): Path {
        val scalePath = Path()
        val scaleValue = Random(System.currentTimeMillis()).nextDouble(1.0, 1.3).toFloat()
        scalePath.moveTo(1f, 1f)
        scalePath.lineTo(scaleValue, scaleValue)
        scalePath.lineTo(1f, 1f)

        return scalePath
    }

    /**
     * We use path for generating translation animation for a coin, until reach to the coin box
     * This function gets a suitable translation animation for requested coin
     * @return transaction information for requested coin
     */
    private fun getTranslationPath(): Path {
        val translationPath = Path()
        translationPath.moveTo(
            binding.getCoinButton.absX().toFloat() + binding.getCoinButton.width / 2,
            binding.getCoinButton.absY().toFloat() + binding.getCoinButton.height / 2
        )
        translationPath.lineTo(
            binding.root.width * Random(System.currentTimeMillis()).nextDouble(0.45, 0.55)
                .toFloat(),
            binding.root.height * Random(System.currentTimeMillis()).nextDouble(0.25, 0.35)
                .toFloat()
        )
        translationPath.lineTo(
            binding.root.width * Random(System.currentTimeMillis()).nextDouble(0.35, 0.65)
                .toFloat(),
            binding.root.height * Random(System.currentTimeMillis()).nextDouble(0.35, 0.4).toFloat()
        )
        translationPath.lineTo(
            binding.root.width * Random(System.currentTimeMillis()).nextDouble(0.35, 0.55)
                .toFloat(),
            binding.root.height * Random(System.currentTimeMillis()).nextDouble(0.3, 0.4).toFloat()
        )
        translationPath.lineTo(
            binding.root.width * Random(System.currentTimeMillis()).nextDouble(0.4, 0.6)
                .toFloat(), binding.root.height * Random(
                System.currentTimeMillis()
            ).nextDouble(0.3, 0.42).toFloat()
        )
        translationPath.lineTo(
            binding.root.width * Random(System.currentTimeMillis()).nextDouble(0.3, 0.7)
                .toFloat(), binding.root.height * (0.279).toFloat()
        )
        translationPath.lineTo(
            binding.coinImageView.absX().toFloat(),
            binding.coinImageView.absY().toFloat()
        )
        return translationPath
    }

    /**
     * This function adds the coin View to the root ViewGroup
     * @param view the ImageView that we want to add to root layout
     */
    private fun addCoinToParentView(view: View) {
        (binding.root as ViewGroup).addView(view)
        (view.layoutParams as ConstraintLayout.LayoutParams).width = 24.dp(context)
        (view.layoutParams as ConstraintLayout.LayoutParams).height = 24.dp(context)
    }

    private fun addCoin(count: Int = 1) {
        viewModel.addCoin(count)
    }
}

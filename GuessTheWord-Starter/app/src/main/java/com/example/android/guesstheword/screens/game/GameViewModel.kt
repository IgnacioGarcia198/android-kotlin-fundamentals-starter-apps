package com.example.android.guesstheword.screens.game

import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.*

class GameViewModel:ViewModel() {
    // The current word
    private val _word = MutableLiveData<String>()
    val word : LiveData<String>
    get() = _word

    private val _score = MutableLiveData<Int>()
    val score : LiveData<Int>
    get() = _score

    private val _eventGameFinish = MutableLiveData<Boolean>()
    val eventGameFinish : LiveData<Boolean>
    get() = _eventGameFinish

    private val _currentTime = MutableLiveData<Long>()
    val currentTime:LiveData<Long>
    get() = _currentTime
    private val timer:CountDownTimer

    val currentTimeString:LiveData<String> = Transformations.map(_currentTime){millis-> DateUtils.formatElapsedTime(millis)}
    val wordLength = Transformations.map(_word){text->text.length}

    private fun letterAtPos(word:String) : Pair<Char,Int> {

        val pos = Random().nextInt(word.length)
        val c = word[pos]
        return Pair(c,pos)
    }
    private val letterAndPos = Transformations.map(_word){text->letterAtPos(text)}
    val letter = Transformations.map(letterAndPos){pair->pair.first.toUpperCase()}
    var pos = Transformations.map(letterAndPos){pair->pair.second}

    /*val wordHint = Transformations.map(_word){
        text -> val randpos =(text.indices).random()
        "Current word has ${text.length} letters\n"+
                "The letter at position $randpos is ${text[randpos].toUpperCase()}"
    }*/

    // The list of words - the front of the list is the next word to guess
    lateinit var wordList: MutableList<String>

    init {
        Log.i("GameViewModel","GameViewModel created!")

        timer = object:CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {
            override fun onTick(millisUntilFinished: Long) {
                _currentTime.value = millisUntilFinished/ ONE_SECOND
            }

            override fun onFinish() {
                _currentTime.value = DONE
                onGameFinish()
            }
        }
        timer.start()

        _word.value = ""
        _score.value = 0
        resetList()
        nextWord()
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("GameViewModel","GameViewModel destroyed!")
        timer.cancel()
    }

    /**
     * Resets the list of words and randomizes the order
     */
    private fun resetList() {
        wordList = mutableListOf(
                "queen",
                "hospital",
                "basketball",
                "cat",
                "change",
                "snail",
                "soup",
                "calendar",
                "sad",
                "desk",
                "guitar",
                "home",
                "railway",
                "zebra",
                "jelly",
                "car",
                "crow",
                "trade",
                "bag",
                "roll",
                "bubble"
        )
        wordList.shuffle()
    }

    /**
     * Moves to the next word in the list
     */
    private fun nextWord() {
        if (!wordList.isEmpty()) {
            //Select and remove a word from the list
            _word.value = wordList.removeAt(0)
        }
        else {
            resetList()
        }
    }

    fun onSkip() {
        if (!wordList.isEmpty()) {
            _score.value = (score.value)?.minus(1)
        }
        nextWord()
    }

    fun onCorrect() {
        if (!wordList.isEmpty()) {
            _score.value = (score.value)?.plus(1)
        }
        nextWord()
    }

    fun onGameFinish() {
        _eventGameFinish.value = true
    }

    fun onGameFinishComplete() {
        _eventGameFinish.value = false
    }

    // constants
    companion object {
        // Time when the game is over
        private const val DONE = 0L

        // Countdown time interval
        private const val ONE_SECOND = 1000L

        // Total time for the game
        private const val COUNTDOWN_TIME = 60000L
    }
}
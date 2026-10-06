package com.example.p1.media

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Reproductor de efectos de sonido ligado a un ciclo de vida.
 *
 * Garantías:
 *  - Nunca hay más de un [MediaPlayer] vivo: cada `play()` libera el anterior.
 *  - Se libera al terminar la reproducción, ante cualquier error y en `onStop`
 *    (app en segundo plano / cambio de pestaña), por lo que no hay fugas de recursos nativos.
 *  - Se desregistra solo en `onDestroy`.
 *
 * Uso: `viewLifecycleOwner.lifecycle.addObserver(SoundEffectPlayer(requireContext()))`.
 */
class SoundEffectPlayer(context: Context) : DefaultLifecycleObserver {

    // applicationContext evita retener la Activity/Fragment.
    private val appContext = context.applicationContext

    // `var` estrictamente necesario: el recurso nativo se crea y libera dinámicamente.
    private var mediaPlayer: MediaPlayer? = null

    fun play(@RawRes soundRes: Int) {
        releasePlayer()
        // MediaPlayer.create() devuelve null si el recurso no se puede preparar.
        mediaPlayer = MediaPlayer.create(appContext, soundRes)?.apply {
            setOnCompletionListener { player -> releaseIfCurrent(player) }
            setOnErrorListener { player, _, _ ->
                releaseIfCurrent(player)
                true // error gestionado: evita que se invoque OnCompletion
            }
            start()
        }
    }

    override fun onStop(owner: LifecycleOwner) = releasePlayer()

    override fun onDestroy(owner: LifecycleOwner) {
        releasePlayer()
        owner.lifecycle.removeObserver(this)
    }

    private fun releaseIfCurrent(player: MediaPlayer) {
        if (player === mediaPlayer) releasePlayer() else player.release()
    }

    private fun releasePlayer() {
        mediaPlayer?.run {
            setOnCompletionListener(null)
            setOnErrorListener(null)
            release()
        }
        mediaPlayer = null
    }
}


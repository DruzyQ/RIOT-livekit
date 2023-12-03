package com.zeerak.riotlivekit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.ajalt.timberkt.Timber
import io.livekit.android.ConnectOptions
import io.livekit.android.LiveKit
import io.livekit.android.room.Room
import io.livekit.android.room.RoomListener
import io.livekit.android.room.participant.Participant
import io.livekit.android.room.participant.RemoteParticipant
import kotlinx.coroutines.launch

class CallViewModel(
    val url: String,
    val token: String,
    application: Application
) : AndroidViewModel(application), RoomListener {
    private val mutableRoom = MutableLiveData<Room>()
    val room: LiveData<Room> = mutableRoom
    private val mutableRemoteParticipants = MutableLiveData<List<RemoteParticipant>>()
    val remoteParticipants: LiveData<List<RemoteParticipant>> = mutableRemoteParticipants

    init {
        viewModelScope.launch {
          /*  val room = LiveKit.connect(
                application,
                url,
                token,
                ConnectOptions(),
                this@CallViewModel
            )*/

            val liveKitConnect = LiveKit.create(application)
            liveKitConnect.connect(url, token)

            val localParticipant = liveKitConnect.localParticipant
           /* val audioTrack = localParticipant.createAudioTrack()
            localParticipant.publishAudioTrack(audioTrack)
            val videoTrack = localParticipant.createVideoTrack()
            localParticipant.publishVideoTrack(videoTrack)
            videoTrack.startCapture()*/

            localParticipant.setCameraEnabled(false)
            localParticipant.setMicrophoneEnabled(false)


            updateParticipants(liveKitConnect)
            mutableRoom.value = liveKitConnect
        }
    }

    private fun updateParticipants(room: Room) {
        mutableRemoteParticipants.postValue(
            room.remoteParticipants
                .keys
                .sortedBy { it }
                .mapNotNull { room.remoteParticipants[it] }
        )
    }

    override fun onCleared() {
        super.onCleared()
        mutableRoom.value?.disconnect()
    }

    override fun onDisconnect(room: Room, error: Exception?) {
    }

    override fun onParticipantConnected(
        room: Room,
        participant: RemoteParticipant
    ) {
        updateParticipants(room)
    }

    override fun onParticipantDisconnected(
        room: Room,
        participant: RemoteParticipant
    ) {
        updateParticipants(room)
    }

    override fun onFailedToConnect(room: Room, error: Throwable) {
    }

    override fun onActiveSpeakersChanged(speakers: List<Participant>, room: Room) {
        Timber.i { "active speakers changed ${speakers.count()}" }
    }

    override fun onMetadataChanged(participant: Participant, prevMetadata: String?, room: Room) {
        Timber.i { "Participant metadata changed: ${participant.identity}" }
    }
}

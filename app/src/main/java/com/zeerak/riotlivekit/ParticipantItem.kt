package com.zeerak.riotlivekit

import android.view.View
import com.github.ajalt.timberkt.Timber
import com.xwray.groupie.viewbinding.BindableItem
import com.xwray.groupie.viewbinding.GroupieViewHolder
import com.zeerak.riotlivekit.databinding.ParticipantItemBinding
import io.livekit.android.room.Room
import io.livekit.android.room.participant.ParticipantListener
import io.livekit.android.room.participant.RemoteParticipant
import io.livekit.android.room.track.*

class ParticipantItem(
    val room: Room,
    val remoteParticipant: RemoteParticipant
) :
    BindableItem<ParticipantItemBinding>() {

    private var videoBound = false

    override fun initializeViewBinding(view: View): ParticipantItemBinding {
        val binding = ParticipantItemBinding.bind(view)
        room.initVideoRenderer(binding.renderer)
        return binding
    }

    override fun bind(viewBinding: ParticipantItemBinding, position: Int) {
        viewBinding.run {

            remoteParticipant.listener = object : ParticipantListener {
                override fun onTrackSubscribed(
                    track: Track,
                    publication: RemoteTrackPublication,
                    participant: RemoteParticipant
                ) {
                    if (track is VideoTrack) {
                        setupVideoIfNeeded(track, viewBinding)
                    }
                }

                override fun onTrackUnpublished(
                    publication: RemoteTrackPublication,
                    participant: RemoteParticipant
                ) {
                    super.onTrackUnpublished(publication, participant)
                    Timber.e { "Track unpublished" }
                }
            }
            val existingTrack = getVideoTrack()
            if (existingTrack != null) {
                setupVideoIfNeeded(existingTrack, viewBinding)
            }
        }
    }

    private fun getVideoTrack(): VideoTrack? {
        return remoteParticipant
            .videoTracks
            .firstOrNull()?.first?.track as? VideoTrack
    }

    internal fun setupVideoIfNeeded(videoTrack: VideoTrack, viewBinding: ParticipantItemBinding) {
        if (videoBound) {
            return
        }

        videoBound = true
        Timber.v { "adding renderer to $videoTrack" }
        videoTrack.addRenderer(viewBinding.renderer)
    }

    override fun unbind(viewHolder: GroupieViewHolder<ParticipantItemBinding>) {
        super.unbind(viewHolder)
        videoBound = false
    }

    override fun getLayout(): Int = R.layout.participant_item
}
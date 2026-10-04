package com.rogbandroid.rogermote.tv.samsung

import com.rogbandroid.rogermote.tv.RemoteCommand

internal object SamsungCommandMapper {
    fun toSamsungKey(command: RemoteCommand): SamsungKey = when (command) {
        RemoteCommand.PowerOff -> SamsungKey.PowerOff
        RemoteCommand.VolumeUp -> SamsungKey.VolumeUp
        RemoteCommand.VolumeDown -> SamsungKey.VolumeDown
        RemoteCommand.Mute -> SamsungKey.Mute
        RemoteCommand.Up -> SamsungKey.Up
        RemoteCommand.Down -> SamsungKey.Down
        RemoteCommand.Left -> SamsungKey.Left
        RemoteCommand.Right -> SamsungKey.Right
        RemoteCommand.Enter -> SamsungKey.Enter
        RemoteCommand.Back -> SamsungKey.Back
        RemoteCommand.Home -> SamsungKey.Home
        RemoteCommand.Source -> SamsungKey.Source
        RemoteCommand.Menu -> SamsungKey.Menu
        RemoteCommand.Exit -> SamsungKey.Exit
        RemoteCommand.Guide -> SamsungKey.Guide
        RemoteCommand.ChannelUp -> SamsungKey.ChannelUp
        RemoteCommand.ChannelDown -> SamsungKey.ChannelDown
        RemoteCommand.PreviousChannel -> SamsungKey.PreviousChannel
        RemoteCommand.Number0 -> SamsungKey.Number0
        RemoteCommand.Number1 -> SamsungKey.Number1
        RemoteCommand.Number2 -> SamsungKey.Number2
        RemoteCommand.Number3 -> SamsungKey.Number3
        RemoteCommand.Number4 -> SamsungKey.Number4
        RemoteCommand.Number5 -> SamsungKey.Number5
        RemoteCommand.Number6 -> SamsungKey.Number6
        RemoteCommand.Number7 -> SamsungKey.Number7
        RemoteCommand.Number8 -> SamsungKey.Number8
        RemoteCommand.Number9 -> SamsungKey.Number9
        RemoteCommand.Play -> SamsungKey.Play
        RemoteCommand.Pause -> SamsungKey.Pause
        RemoteCommand.Stop -> SamsungKey.Stop
        RemoteCommand.Rewind -> SamsungKey.Rewind
        RemoteCommand.FastForward -> SamsungKey.FastForward
        RemoteCommand.Info -> SamsungKey.Info
        RemoteCommand.Subtitles -> SamsungKey.Subtitles
        RemoteCommand.Red -> SamsungKey.Red
        RemoteCommand.Green -> SamsungKey.Green
        RemoteCommand.Yellow -> SamsungKey.Yellow
        RemoteCommand.Blue -> SamsungKey.Blue
    }
}
